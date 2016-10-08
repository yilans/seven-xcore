package com.seven.x.core.id.seq;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.support.SQLErrorCodes;
import org.springframework.jdbc.support.SQLErrorCodesFactory;
import org.springframework.util.StringUtils;

import com.seven.x.core.util.DateTime;


/**
 * 缓存序列生成器
 * <p>
 * 启动是从数据库中加载缓存的序列为当前序列，缓存序列为数据库中序列+缓存大小,并且更新缓存序列至数据库
 * <p>
 * 获取序列时，当前序列原子加step，同步执行条件判断：<br>
 * 当切日时更新当前序列为0，缓存序列为缓存大小，并且更新缓存序列至数据库<br>
 * 否则如当前序列大于或等于缓存序列，将缓存序列增加缓存大小，并且更新缓存序列至数据库<br>
 * 
 * @author zhang.jun
 * @version 0.1
 */
public class CachedTableSequenceFactory extends AbstractSequenceFactory {

	private final static Logger logger = LoggerFactory.getLogger(CachedTableSequenceFactory.class);

	private volatile AtomicLong currentIndex = null;

	private volatile long cacheIndex;

	private long cacheSize = 5000;

	private int step = 1;

	boolean dateCutoff;

	AbstractLockedSequenceTable table;

	boolean autoCommit = false;

	private int retryCount = 20;

	private long accountingDate = 0;

	private SQLErrorCodes sqlErrorCodes;


	/**
	 * 日切重置为cacheSize
	 * <p>
	 * 大于缓存大小原先是直接更新数据库为当前大小加上缓存大小，<br>
	 * 为了解决两台机器同用一个数据库的问题，先从数据库查询当前的大小再加上缓存大小后才更新进数据库。
	 * <p>
	 */
	@Override
	protected long[] internalGenerate(int num) {

		synchronized (this) {
			long index = 0L;

			//如果日期不相等，则需要日切
			long now = this.getCurrentAccountingDate();
			if (dateCutoff && accountingDate != 0 && DateTime.compareDateWithoutTime(new Date(accountingDate), new Date(now)) != 0) {
				accountingDate = now;
				reset();
			}
			
			// 如果未初始化,则先进行初始化
			if (currentIndex == null) {
				try {
					init();
				} catch (SQLException e) {
					throw new IllegalStateException(e);
				}
			}

			index = currentIndex.addAndGet(step);
			if (index > cacheIndex) {
				logger.info("ID Service:{} get seq from db.", this.type);
				// 更新数据库中Sequence为cacheIndex
				update();
				index = currentIndex.addAndGet(step);
			}

			logger.debug("Type {} Generate id {}, accounting date is {}", new Object[] { this.type, index, accountingDate });
			// 用会计日期来格式化日期,而不是系统日期
			return new long[] { index, accountingDate };
		}

	}

	/**
	 * 初次启动时加载
	 * 
	 * @throws SQLException
	 */
	private void init() throws SQLException {
		Connection connection = this.table.getConnection();
		boolean bool = connection.getAutoCommit();
		boolean retryAfterDuplicateKey = false;

		try {
			if (!this.autoCommit)
				connection.setAutoCommit(false);

			// 获取初始的会计日期
			accountingDate = this.getCurrentAccountingDate();

			long[] reslut = table.doSelect(connection, this.type);
			if (reslut[0] == -1L) {
				this.table.doInsert(connection, this.type, cacheSize, accountingDate);
				reslut[0] = 0; // 初始化序号开始均为0
				cacheIndex = reslut[0] + cacheSize;// 从数据库中查询出的大小加上缓存大小
				currentIndex = new AtomicLong(reslut[0]);
			} else {
				//如果当前时间大于记录的时间则需要重置
				reslut[0] = DateTime.compareDateWithoutTime(new Date(accountingDate), new Date(reslut[1])) > 0  ? 0 : reslut[0];
				
				cacheIndex = reslut[0] + cacheSize;// 从数据库中查询出的大小加上缓存大小
				currentIndex = new AtomicLong(reslut[0]);

				table.doUpdate(connection, this.type, cacheIndex, accountingDate);
			}

			if (!this.autoCommit)
				connection.commit();
		} catch (SQLException e) {
			if (!this.autoCommit)
				connection.rollback();

			if (this.isDuplicateKeyException(e)) {
				retryAfterDuplicateKey = true;
				logger.warn("ID type " + this.type + "Can not insert id record. cause by duplicate key. now retry init....", e);
			} else {
				throw e;
			}
		} finally {
			if (!this.autoCommit)
				connection.setAutoCommit(bool);
			connection.close();

			if (retryAfterDuplicateKey) {
				init();
			}
		}
	}

	private void update() {
		try {
			Connection connection = this.table.getConnection();
			boolean bool = connection.getAutoCommit();
			try {
				if (!this.autoCommit)
					connection.setAutoCommit(false);
				long[] reslut = this.table.doSelect(connection, this.type);
				cacheIndex = reslut[0] + cacheSize;
				currentIndex = new AtomicLong(reslut[0]);
				forRetryUpdate(cacheIndex, connection);
				if (!this.autoCommit)
					connection.commit();
			} catch (SQLException e) {
				if (!this.autoCommit)
					connection.rollback();
				throw e;
			} finally {
				if (!this.autoCommit)
					connection.setAutoCommit(bool);
				connection.close();
			}
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * 重置ID
	 */
	private void reset() {
		try {
			Connection connection = this.table.getConnection();
			boolean bool = connection.getAutoCommit();
			try {
				if (!this.autoCommit)
					connection.setAutoCommit(false);

				// 如果记录中最后会计日期与当前会计日期不一致，则需要日切 modified by yan.jsh 20150901
				long[] reslut = table.doSelect(connection, this.type);
				long lastAccountingDate = reslut[1];
				if (lastAccountingDate != -1L && DateTime.compareDateWithoutTime(new Date(lastAccountingDate), new Date(accountingDate)) != 0) {
					logger.info("ID Service:{} reset seq to 0 on swithing accounting date.", this.type);
					// 更新cacheIndex为
					cacheIndex = cacheSize;
					currentIndex = new AtomicLong();

					// 更新数据库中Sequence为cacheIndex
					forRetryUpdate(cacheIndex, connection);
				} else {
					currentIndex = null;
					logger.info("ID Service:{} do not need to reset seq to 0 on swithing accounting date,because no recored or has reseted by other server.", this.type);
				}

				if (!this.autoCommit)
					connection.commit();
			} catch (SQLException e) {
				if (!this.autoCommit)
					connection.rollback();
				throw e;
			} finally {
				if (!this.autoCommit)
					connection.setAutoCommit(bool);
				connection.close();
			}
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		}
	}

	private void forRetryUpdate(long index, Connection connection) throws SQLException {
		int i = 0;
		for (int j = 0; (j < this.retryCount) && (i != 1); j++) {
			try {
				i = this.table.doUpdate(connection, this.type, index, accountingDate);
			} catch (SQLException e) {
				this.log.warn("{} get id retry: {} , cause: {}", new Object[] { this.type, j, e });
				if (j == this.retryCount - 1) {
					throw e;
				}
			}
		}
		if (i != 1) {
			// throw new IllegalStateException("cannot get id"); //当序号未被使用时,
			// 不会有记录, 不能报错.
			this.log.warn("{} cannot get id, it is not used as all.", this.type);
		}
	}

	/**
	 * 判断当前异常是否为重KEY异常
	 * 
	 * @param e
	 *            SQL异常
	 * @return true - 是, false - 否
	 */
	private boolean isDuplicateKeyException(SQLException e) {
		if (e == null) {
			return false;
		}

		if (sqlErrorCodes == null) {
			sqlErrorCodes = SQLErrorCodesFactory.getInstance().getErrorCodes(this.table.dataSource);
		}

		String errorCode = null;
		if (this.sqlErrorCodes.isUseSqlStateForTranslation()) {
			errorCode = e.getSQLState();
		} else {
			SQLException current = e;
			while (current.getErrorCode() == 0 && current.getCause() instanceof SQLException) {
				current = (SQLException) current.getCause();
			}
			errorCode = Integer.toString(current.getErrorCode());
		}

		if (StringUtils.hasText(errorCode) && Arrays.binarySearch(sqlErrorCodes.getDuplicateKeyCodes(), errorCode) >= 0) {
			return true;
		}

		return false;
	}

	public void setTable(AbstractLockedSequenceTable table) {
		this.table = table;
	}

	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}

	public void setDateCutoff(boolean dateCutoff) {
		this.dateCutoff = dateCutoff;
	}

	public void setCacheSize(int cacheSize) {
		this.cacheSize = cacheSize;
	}

	public void setStep(int step) {
		this.step = step;
	}

	/**
	 * 获取当前日期时间
	 * 
	 * @return
	 */
	private long getCurrentAccountingDate() {
		return this.table.getTimeSerivce().currentTimeMillis();
	}
}

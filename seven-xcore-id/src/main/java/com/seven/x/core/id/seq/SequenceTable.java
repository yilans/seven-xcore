package com.seven.x.core.id.seq;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 序号表
 * <p>负责生成序号
 * <p>waring：当并发取号时，会导致序号重复
 * @author yan.jsh
 * 2011-7-10
 *
 */
public class SequenceTable {
	
	private static final Logger log = LoggerFactory.getLogger(SequenceTable.class);
	
	private DataSource dataSource;
	
	private String tableName = "seq_no";
	
	private String idColume = "id";
	
	private String typeColume = "type";
	
	private String insertSql;
	
	private String updateByTypeAndIdSql;
	
	private String selectSql;
	
	private String resetSql;
	
	private String updateByTypeSql;
	
	private TimeService timeService = new DefaultTimeService();

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public void setIdColume(String idColume) {
		this.idColume = idColume;
	}

	public void setTypeColume(String typeColume) {
		this.typeColume = typeColume;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void setTimeService(TimeService timeService) {
		this.timeService = timeService;
	}

	protected TimeService getTimeSerivce() {
		return this.timeService;
	}

	/**
	 * 在初始化时创建序号表
	 */
	public void init() {
		Connection connection = null;
		Statement statement = null;
		
		try {
			connection = this.dataSource.getConnection();
			statement = connection.createStatement();
			String sql = "create table " + this.tableName + "(" + this.typeColume + " char(2) not null, " + this.idColume + " bigint, datetime TIMESTAMP, primary key(" + this.typeColume + "))";
			statement.execute(sql);
			statement.close();
		
		} catch (SQLException e) {
			log.warn("cannot create sequence table, " + e.getMessage());
			try {
				connection.close();
			} catch (Exception exception1) {
			}
		
		} finally {
			try {
				connection.close();
			} catch (Exception exception2) {
			}
		}
	}
	
	/**
	 * 重置序号
	 */
	public void reset() {
		try {
			Connection connection = this.dataSource.getConnection();
			Statement statement = null;
			boolean isAutoCommit = connection.getAutoCommit();
			try {
				connection.setAutoCommit(false);
				statement = connection.createStatement();
				
				String sql = "update " + this.tableName + " set " + this.idColume + "=0, datetime='" + new Timestamp(this.timeService.currentTimeMillis()) + "'";
				
				statement.execute(sql);
				
				connection.commit();
			
			} catch (SQLException e) {
				connection.rollback();
				log.warn("cannot reset sequence table, " + e);
			} finally {
				connection.setAutoCommit(isAutoCommit);

				connection.close();
			}
		} catch (SQLException e) {
			log.warn("cannot reset sequence table, " + e);
		}
	} 

	protected Connection getConnection() throws SQLException {
		return this.dataSource.getConnection();
	}

	/**
	 * 获取序号
	 * @param connection
	 * @param type 类型
	 * @param isDateCutoff 是否日切
	 * @return
	 * @throws SQLException
	 */
	protected long[] doSelect(Connection connection, String type, boolean isDateCutoff) throws SQLException {
		
		long currentTimeMillis = this.timeService.currentTimeMillis();
		
		long[] result = { -1L, -1L };
		
		PreparedStatement statement = connection.prepareStatement(getSelectSql());
		
		ResultSet resultSet = null;
		
		try {
			statement.setString(1, type);
			resultSet = statement.executeQuery();
			
			try {
				if (resultSet.next()) {
					result[0] = resultSet.getLong(1);
					
					Timestamp timestamp = resultSet.getTimestamp(2);
					
					if ((timestamp != null) && (timestamp.getTime() > currentTimeMillis)) {
						result[1] = timestamp.getTime();
					} else {
						result[1] = currentTimeMillis;
						
						//如果满足日切条件，则日切
						if ((isDateCutoff) && (this.timeService.isCutoff(5, currentTimeMillis, timestamp.getTime()))) {
							int k = doReset(connection, type, currentTimeMillis, result[0]);
							if (k == 1) {
								result[0] = 0L;
							} else
								return doSelect(connection, type, isDateCutoff);
						}
					}
				} else {
					result[1] = currentTimeMillis;
					log.warn("cannot get a id due to no initialized.");
				}
			} finally {
			}
		} catch (SQLException e) {

			throw e;
		} finally {
			statement.close();
			if (null != resultSet) {
				resultSet.close();
			}
		}
		statement.close();

		return result;
	}

	/**
	 * 新增序号记录
	 * @param connection
	 * @param type 类型
	 * @param id 序号
	 * @param timestamp 时间戳
	 * @throws SQLException
	 */
	protected void doInsert(Connection connection, String type, long id, long timestamp) throws SQLException {
																									
		PreparedStatement preparedstatement = connection.prepareStatement(getInsertSql());
		
		try {
			preparedstatement.setString(1, type);
			preparedstatement.setLong(2, id);
			preparedstatement.setTimestamp(3, new Timestamp(timestamp));
			int k = preparedstatement.executeUpdate();
		
			if (k != 1)
				throw new SQLException("sequence table insert fail.");
		} catch (SQLException e) {
			throw e;
		} finally {
			preparedstatement.close();

		}
	}
	
	/**
	 * 根据类型查找序号
	 * @param connection
	 * @param type 类型
	 * @return
	 * @throws SQLException
	 */
	protected long[] doSelect(Connection connection,String type) throws SQLException{
		PreparedStatement statement = connection.prepareStatement(getSelectSql());
		ResultSet resultSet = null;
		long[] result = { -1L, -1L };
		try {
			statement.setString(1, type);
			resultSet = statement.executeQuery();
			
			if (resultSet.next()) {
				result[0] = resultSet.getLong(1);
				Timestamp timestamp = resultSet.getTimestamp(2);
				result[1] = timestamp.getTime();
			}
			
			return result;
		} catch (SQLException e) {
			log.warn("could not reset type " + type + ", " + e);
			throw e;
		} finally {
			statement.close();
		}
	}

	/**
	 * 重置序号
	 * @param connection
	 * @param type 类型
	 * @param timestamp 时间戳
	 * @param id 序号
	 * @return
	 * @throws SQLException
	 */
	protected int doReset(Connection connection, String type, long timestamp, long id) throws SQLException {
		PreparedStatement statement = connection.prepareStatement(getResetSql());
		
		try {
			statement.setTimestamp(1, new Timestamp(timestamp));
			statement.setString(2, type);
			statement.setLong(3, id);
			int k = statement.executeUpdate();
			return k;
			
		} catch (SQLException e) {
			log.warn("could not reset type " + type + ", " + e);
			throw e;
		} finally {
			statement.close();
		}
	}

	/**
	 * 更新序号
	 * @param connection
	 * @param type 类型
	 * @param newId 新序号
	 * @param timestamp 时间戳
	 * @param id 原序号
	 * @return
	 * @throws SQLException
	 */
	protected int doUpdate(Connection connection, String type, int newId, long timestamp, long id) throws SQLException {
		PreparedStatement statement = connection.prepareStatement(getUpdateByTypeAndIdSql());
		try {
			statement.setLong(1, newId);
			statement.setTimestamp(2, new Timestamp(timestamp));
			statement.setString(3, type);
			statement.setLong(4, id);
			int k = statement.executeUpdate();
			
			return k;
		
		} catch (SQLException e) {
			throw e;
		} finally {
			statement.close();
		}
	}
	
	/**
	 * 根据类型更新序号
	 * @param connection
	 * @param type 类型
	 * @param id 新序号
	 * @param timestamp 时间戳
	 * @return
	 * @throws SQLException
	 */
	protected int doUpdate(Connection connection, String type, long id, long timestamp) throws SQLException {
		PreparedStatement statement = connection.prepareStatement(getUpdateByTypeSql());
		
		try {
			statement.setLong(1, id);
			statement.setTimestamp(2, new Timestamp(timestamp));
			statement.setString(3, type);
			int k = statement.executeUpdate();
			return k;
			
		} catch (SQLException e) {
			throw e;
		} finally {
			statement.close();
		}
	}

	private String getSelectSql() {
		if (this.selectSql == null)
			this.selectSql = ("select " + this.idColume + ", datetime from " + this.tableName + " where " + this.typeColume + "=?");
		return this.selectSql;
	}

	private String getResetSql() {
		if (this.resetSql == null)
			this.resetSql = ("update " + this.tableName + " set " + this.idColume + "=0, datetime=? where " + this.typeColume + "=? and " + this.idColume + "=?");
		return this.resetSql;
	}

	private String getUpdateByTypeAndIdSql() {
		if (this.updateByTypeAndIdSql == null)
			this.updateByTypeAndIdSql = ("update " + this.tableName + " set " + this.idColume + "=" + this.idColume + "+?, datetime=? where " + this.typeColume + "=? and " + this.idColume + "=?");
		return this.updateByTypeAndIdSql;
	}

	private String getInsertSql() {
		if (this.insertSql == null)
			this.insertSql = ("insert into " + this.tableName + " (" + this.typeColume + "," + this.idColume + ", datetime) values (?,?,?)");
		return this.insertSql;
	}
	
	private String getUpdateByTypeSql() {
		if (this.updateByTypeSql == null)
			this.updateByTypeSql = ("update " + this.tableName + " set " + this.idColume + " =?, datetime=? where " + this.typeColume + "=?");
		return this.updateByTypeSql;
	}
}

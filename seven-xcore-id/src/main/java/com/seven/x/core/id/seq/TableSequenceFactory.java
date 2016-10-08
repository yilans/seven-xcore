package com.seven.x.core.id.seq;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 表序号生成工厂
 * <p>通过表字段生成序号
 * @author yan.jsh
 * 2011-7-10
 *
 */
public class TableSequenceFactory extends AbstractSequenceFactory {
	
	private int retryCount = 20;
	
	SequenceTable table;
	
	boolean isDateCutoff;
	
	boolean isAutoCommit = true;

	public void setTable(SequenceTable table) {
		this.table = table;
	}

	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}

	public void setAutoCommit(boolean isAutoCommit) {
		this.isAutoCommit = isAutoCommit;
	}

	public void setDateCutoff(boolean isDateCutoff) {
		this.isDateCutoff = isDateCutoff;
	}

	protected long[] internalGenerate(int newId) {
		try {
			Connection connection = this.table.getConnection();
			boolean bool = connection.getAutoCommit();
			try {
				if (!this.isAutoCommit)
					connection.setAutoCommit(false);
				
				int flag = 0;
				long[] result = (long[]) null;
				
				for (int j = 0; (j < this.retryCount) && (flag != 1); j++) {
					try {
						result = this.table.doSelect(connection, this.type, this.isDateCutoff);
						if (result[0] == -1L) {
							this.table.doInsert(connection, this.type, 0L, result[1]);
							result[0] = 0L;
						}
					
						flag = this.table.doUpdate(connection, this.type, newId, result[1], result[0]);
					
					} catch (SQLException e) {
						this.log.warn("get id retry: " + j + ", cause:" + e);
						if (j == this.retryCount - 1) {
							throw e;
						}
					}
				}
				
				if (flag != 1) {
					throw new IllegalStateException("cannot get id");
				}

				if (!this.isAutoCommit)
					connection.commit();
				return result;
			
			} catch (SQLException e) {
				if (!this.isAutoCommit)
					connection.rollback();
				throw e;
			} finally {
				if (!this.isAutoCommit)
					connection.setAutoCommit(bool);
				connection.close();
			}
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		}
	}
}

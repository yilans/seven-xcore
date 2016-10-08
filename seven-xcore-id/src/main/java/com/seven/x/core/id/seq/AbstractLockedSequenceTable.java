package com.seven.x.core.id.seq;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 扩展Sequence Table，使其支持加锁，
 * 否则会出现ID重复的问题
 * @author yan.jsh 
 * 2015年6月17日
 */
public abstract class AbstractLockedSequenceTable extends SequenceTable {
	
	private static final Logger log = LoggerFactory.getLogger(AbstractLockedSequenceTable.class);
	
	protected String tableName = "seq_no";
	
	protected String idColume = "id";
	
	protected String typeColume = "type";
	
	protected String updateByTypeSql;
	
	protected DataSource dataSource;
	
	public void setTableName(String tableName) {
		super.setTableName(tableName);
		this.tableName = tableName;
	}

	public void setIdColume(String idColume) {
		super.setIdColume(idColume);
		this.idColume = idColume;
	}

	public void setTypeColume(String typeColume) {
		super.setTypeColume(typeColume);
		this.typeColume = typeColume;
	}
	
	
	@Override
	public void setDataSource(DataSource dataSource) {
		super.setDataSource(dataSource);
		this.dataSource = dataSource;
	}

	/**
	 * 根据类型查找序号
	 * @param connection
	 * @param type 类型
	 * @return
	 * @throws SQLException
	 */
	public long[] doSelect(Connection connection,String type) throws SQLException{
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
	
	@Override
	public void doInsert(Connection arg0, String arg1, long arg2, long arg3) throws SQLException {
		super.doInsert(arg0, arg1, arg2, arg3);
	}

//	@Override
//	public int doReset(Connection arg0, String arg1, long arg2, long arg3) throws SQLException {
//		return super.doReset(arg0, arg1, arg2, arg3);
//	}


	@Override
	public Connection getConnection() throws SQLException {
		return super.getConnection();
	}

	@Override
	public TimeService getTimeSerivce() {
		return super.getTimeSerivce();
	}

	protected String getUpdateByTypeSql() {
		if (this.updateByTypeSql == null)
			this.updateByTypeSql = ("update " + this.tableName + " set " + this.idColume + " =?, datetime=? where " + this.typeColume + "=?");
		return this.updateByTypeSql;
	}
	
	public abstract String getSelectSql();
}

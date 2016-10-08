package com.seven.x.core.id.seq;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import javax.sql.DataSource;

public class SqlTimeService extends DefaultTimeService {
	
	private String timestampSql = "values(current_timestamp)";
	
	private DataSource dataSource;

	public void setTimestampSql(String timestampSql) {
		this.timestampSql = timestampSql;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	protected long remoteTimestamp() throws Throwable {
		Connection connection = null;
		try {
			connection = this.dataSource.getConnection();
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(this.timestampSql);
			
			long currentTimeMillis = System.currentTimeMillis();
			
			try {
				
				if (((ResultSet) resultSet).next()) {
					Timestamp timestamp = ((ResultSet) resultSet).getTimestamp(1);
					currentTimeMillis = timestamp.getTime();
			
				} else {
					throw new IllegalStateException("cannot get timestamp from database.");
				}
			} finally {
				((ResultSet) resultSet).close();
			}
			((ResultSet) resultSet).close();

			statement.close();
			return currentTimeMillis;
			
		} catch (SQLException e) {
			IllegalStateException cause = new IllegalStateException("cannot get timestamp from database.");
			cause.initCause(e);
			throw cause;
			
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
			}
		}
	}
}

package com.seven.x.core.orm.mybatis.support;

import java.sql.SQLException;
import javax.sql.DataSource;
import org.apache.ibatis.exceptions.PersistenceException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;

public class MyBatisExceptionTranslator implements PersistenceExceptionTranslator {
	
	private final DataSource dataSource;
	
	private SQLExceptionTranslator translator;

	public MyBatisExceptionTranslator(DataSource dataSource, boolean flag) {
		this.dataSource = dataSource;

		if (!flag)
			initTranslator();
	}

	public DataAccessException translateExceptionIfPossible(RuntimeException runtimeException) {
		if ((runtimeException instanceof PersistenceException)) {
			if ((runtimeException.getCause() instanceof PersistenceException)) {
				runtimeException = (PersistenceException) runtimeException.getCause();
			}
			if ((runtimeException.getCause() instanceof SQLException)) {
				initTranslator();
				return this.translator.translate(runtimeException.getMessage() + "\n", null, (SQLException) runtimeException.getCause());
			}
			return new MyBatisSystemException(runtimeException);
		}
		return null;
	}

	private synchronized void initTranslator() {
		if (this.translator == null)
			this.translator = new SQLErrorCodeSQLExceptionTranslator(this.dataSource);
	}
}

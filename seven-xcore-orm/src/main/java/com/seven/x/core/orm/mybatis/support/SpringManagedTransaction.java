package com.seven.x.core.orm.mybatis.support;

import java.lang.reflect.InvocationHandler;

import java.lang.reflect.Proxy;

import java.sql.Connection;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.ibatis.logging.Log;

import org.apache.ibatis.logging.LogFactory;

import org.apache.ibatis.logging.jdbc.ConnectionLogger;

import org.apache.ibatis.transaction.Transaction;

import org.springframework.jdbc.datasource.DataSourceUtils;

import org.springframework.util.Assert;

public class SpringManagedTransaction implements Transaction {
	
	private final Log log = LogFactory.getLog(getClass());
	
	private final Connection connection;
	
	private final Connection loggerConnection;
	
	private final DataSource dataSource;
	
	private final boolean isTransactional;

	public SpringManagedTransaction(Connection connection, DataSource dataSource) {
		
		Assert.notNull(connection, "No Connection specified");
		
		Assert.notNull(dataSource, "No DataSource specified");

		this.connection = connection;
		this.dataSource = dataSource;
		this.loggerConnection = getLoggerConnection(connection);
		this.isTransactional = DataSourceUtils.isConnectionTransactional(this.loggerConnection, dataSource);

		if (this.log.isDebugEnabled())
			this.log.debug("JDBC Connection [" + this.connection + "] will" + (this.isTransactional ? " " : " not ") + "be managed by Spring");
	}

	public Connection getConnection() {
		return this.connection;
	}

	public void commit() throws SQLException {
		if (!this.isTransactional) {
			if (this.log.isDebugEnabled()) {
				this.log.debug("Committing JDBC Connection [" + this.connection + "]");
			}
			this.connection.commit();
		}
	}

	public void rollback() throws SQLException {
		if (!this.isTransactional) {
			if (this.log.isDebugEnabled()) {
				this.log.debug("Rolling back JDBC Connection [" + this.connection + "]");
			}
			this.connection.rollback();
		}
	}

	public void close() throws SQLException {
		DataSourceUtils.releaseConnection(this.loggerConnection, this.dataSource);
	}

	private Connection getLoggerConnection(Connection connection) {
		if (Proxy.isProxyClass(connection.getClass())) {
			
			InvocationHandler handler = Proxy.getInvocationHandler(connection);
			
			if ((handler instanceof ConnectionLogger)) {
				return ((ConnectionLogger) handler).getConnection();
			}
		}
		return connection;
	}

}

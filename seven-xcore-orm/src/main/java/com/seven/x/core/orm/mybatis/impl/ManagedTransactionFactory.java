package com.seven.x.core.orm.mybatis.impl;

import java.sql.Connection;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.managed.ManagedTransaction;

/**
 * @author hjs 
 * 2012-12-21
 * @see {@link org.apache.ibatis.transaction.managed.ManagedTransactionFactory
 *      ManagedTransactionFactory}
 */
public class ManagedTransactionFactory implements TransactionFactory {

	private boolean closeConnection = true;

	@Override
	public void setProperties(Properties props) {
		if (props != null) {
			String closeConnectionProperty = props.getProperty("closeConnection");
			if (closeConnectionProperty != null) {
				closeConnection = Boolean.valueOf(closeConnectionProperty);
			}
		}
	}

	@Override
	public Transaction newTransaction(Connection conn) {
		return new ManagedTransaction(conn, closeConnection);
	}

	@Override
	public Transaction newTransaction(DataSource ds, TransactionIsolationLevel level, boolean autoCommit) {
		// Silently ignores autocommit and isolation level, as managed
		// transactions are entirely
		// controlled by an external manager. It's silently ignored so that
		// code remains portable between managed and unmanaged configurations.
		return new ManagedTransaction(ds, level, closeConnection);
	}
}

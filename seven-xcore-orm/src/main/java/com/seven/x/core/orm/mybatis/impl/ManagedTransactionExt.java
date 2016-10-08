package com.seven.x.core.orm.mybatis.impl;

import java.sql.Connection;
import java.sql.SQLException;
import org.apache.ibatis.transaction.Transaction;

public class ManagedTransactionExt implements Transaction {
	private Connection connection;

	public ManagedTransactionExt(Connection conn) {
		this.connection = conn;
	}

	public Connection getConnection() {
		return this.connection;
	}

	public void commit() throws SQLException {
	}

	public void rollback() throws SQLException {
	}

	public void close() throws SQLException {
	}
}

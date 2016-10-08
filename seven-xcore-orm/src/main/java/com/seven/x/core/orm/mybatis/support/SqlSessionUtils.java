package com.seven.x.core.orm.mybatis.support;

import java.sql.Connection;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.ibatis.exceptions.PersistenceException;

import org.apache.ibatis.logging.Log;

import org.apache.ibatis.logging.LogFactory;

import org.apache.ibatis.session.ExecutorType;

import org.apache.ibatis.session.SqlSession;

import org.apache.ibatis.session.SqlSessionFactory;

import org.springframework.dao.TransientDataAccessResourceException;

import org.springframework.dao.support.PersistenceExceptionTranslator;

import org.springframework.jdbc.CannotGetJdbcConnectionException;

import org.springframework.jdbc.datasource.DataSourceUtils;

import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;

import org.springframework.transaction.support.TransactionSynchronizationAdapter;

import org.springframework.transaction.support.TransactionSynchronizationManager;

import org.springframework.util.Assert;

public final class SqlSessionUtils {
	
	private static final Log log = LogFactory.getLog(SqlSessionUtils.class);

	public static SqlSession getSqlSession(SqlSessionFactory sqlSessionFactory) {
		ExecutorType type = sqlSessionFactory.getConfiguration().getDefaultExecutorType();
		return getSqlSession(sqlSessionFactory, type, null);
	}

	public static SqlSession getSqlSession(SqlSessionFactory sqlSessionFactory, ExecutorType executorType, PersistenceExceptionTranslator translator) {
		
		Assert.notNull(sqlSessionFactory, "No SqlSessionFactory specified");
		Assert.notNull(executorType, "No ExecutorType specified");

		SqlSessionHolder sqlSessionHolder = (SqlSessionHolder) TransactionSynchronizationManager.getResource(sqlSessionFactory);

		if ((sqlSessionHolder != null) && (sqlSessionHolder.isSynchronizedWithTransaction())) {
			if (sqlSessionHolder.getExecutorType() != executorType) {
				throw new TransientDataAccessResourceException("Cannot change the ExecutorType when there is an existing transaction");
			}

			sqlSessionHolder.requested();

			if (log.isDebugEnabled()) {
				log.debug("Fetched SqlSession [" + sqlSessionHolder.getSqlSession() + "] from current transaction");
			}

			return sqlSessionHolder.getSqlSession();
		}

		DataSource dataSource = sqlSessionFactory.getConfiguration().getEnvironment().getDataSource();

		boolean isProxy = dataSource instanceof TransactionAwareDataSourceProxy;
		Connection connection;
		
		try {
			connection = isProxy ? dataSource.getConnection() : DataSourceUtils.getConnection(dataSource);
		} catch (SQLException e) {
			throw new CannotGetJdbcConnectionException("Could not get JDBC Connection for SqlSession", e);
		}

		if (log.isDebugEnabled()) {
			log.debug("Creating SqlSession with JDBC Connection [" + connection + "]");
		}

		SqlSession sqlSession = sqlSessionFactory.openSession(executorType, connection);

		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			
			if ((!(sqlSessionFactory.getConfiguration().getEnvironment().getTransactionFactory() instanceof SpringManagedTransactionFactory)) && (DataSourceUtils.isConnectionTransactional(connection, dataSource))) {
				throw new TransientDataAccessResourceException("SqlSessionFactory must be using a SpringManagedTransactionFactory in order to use Spring transaction synchronization");
			}

			if (log.isDebugEnabled()) {
				log.debug("Registering transaction synchronization for SqlSession [" + sqlSession + "]");
			}
			
			sqlSessionHolder = new SqlSessionHolder(sqlSession, executorType, translator);
			TransactionSynchronizationManager.bindResource(sqlSessionFactory, sqlSessionHolder);
			TransactionSynchronizationManager.registerSynchronization(new ExTransactionSynchronizationAdapter(sqlSessionHolder, sqlSessionFactory));
			sqlSessionHolder.setSynchronizedWithTransaction(true);
			sqlSessionHolder.requested();
		
		} else if (log.isDebugEnabled()) {
			log.debug("SqlSession [" + sqlSession + "] was not registered for synchronization because synchronization is not active");
		}

		return sqlSession;
	}

	public static void closeSqlSession(SqlSession sqlSession, SqlSessionFactory sqlSessionFactory) {
		Assert.notNull(sqlSession, "No SqlSession specified");
		Assert.notNull(sqlSessionFactory, "No SqlSessionFactory specified");

		SqlSessionHolder sqlSessionHolder = (SqlSessionHolder) TransactionSynchronizationManager.getResource(sqlSessionFactory);
		
		if ((sqlSessionHolder != null) && (sqlSessionHolder.getSqlSession() == sqlSession)) {
			if (log.isDebugEnabled()) {
				log.debug("Releasing transactional SqlSession [" + sqlSession + "]");
			}
			sqlSessionHolder.released();
		} else {
			if (log.isDebugEnabled()) {
				log.debug("Closing no transactional SqlSession [" + sqlSession + "]");
			}
			sqlSession.close();
		}
	}

	public static boolean isSqlSessionTransactional(SqlSession sqlSession, SqlSessionFactory sqlSessionFactory) {
		Assert.notNull(sqlSession, "No SqlSession specified");
		Assert.notNull(sqlSessionFactory, "No SqlSessionFactory specified");

		SqlSessionHolder sqlSessionHolder = (SqlSessionHolder) TransactionSynchronizationManager.getResource(sqlSessionFactory);

		return (sqlSessionHolder != null) && (sqlSessionHolder.getSqlSession() == sqlSession);
	}

	private static final class ExTransactionSynchronizationAdapter extends TransactionSynchronizationAdapter {
		private final SqlSessionHolder sqlSessionHolder;
		private final SqlSessionFactory sqlSessionFactory;

		public ExTransactionSynchronizationAdapter(SqlSessionHolder holder, SqlSessionFactory sqlSessionFactory) {
			Assert.notNull(holder, "Parameter 'holder' must be not null");
			Assert.notNull(sqlSessionFactory, "Parameter 'sessionFactory' must be not null");

			this.sqlSessionHolder = holder;
			this.sqlSessionFactory = sqlSessionFactory;
		}

		public int getOrder() {
			return 999;
		}

		public void suspend() {
			TransactionSynchronizationManager.unbindResource(this.sqlSessionFactory);
		}

		public void resume() {
			TransactionSynchronizationManager.bindResource(this.sqlSessionFactory, this.sqlSessionHolder);
		}

		public void beforeCommit(boolean flag) {
			if (TransactionSynchronizationManager.isActualTransactionActive())
				try {
					if (SqlSessionUtils.log.isDebugEnabled()) {
						SqlSessionUtils.log.debug("Transaction synchronization committing SqlSession [" + this.sqlSessionHolder.getSqlSession() + "]");
					}
					this.sqlSessionHolder.getSqlSession().commit();
				} catch (PersistenceException e) {
					if (this.sqlSessionHolder.getPersistenceExceptionTranslator() != null) {
						throw this.sqlSessionHolder.getPersistenceExceptionTranslator().translateExceptionIfPossible(e);
					}
					throw e;
				}
		}

		public void afterCompletion(int num) {
			if (!this.sqlSessionHolder.isOpen()) {
				TransactionSynchronizationManager.unbindResource(this.sqlSessionFactory);
				try {
					if (SqlSessionUtils.log.isDebugEnabled()) {
						SqlSessionUtils.log.debug("Transaction synchronization closing SqlSession [" + this.sqlSessionHolder.getSqlSession() + "]");
					}
					this.sqlSessionHolder.getSqlSession().close();
				} finally {
					this.sqlSessionHolder.reset();
				}
			}
		}

	}

}

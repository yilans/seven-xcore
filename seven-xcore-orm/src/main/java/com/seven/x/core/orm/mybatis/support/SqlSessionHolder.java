package com.seven.x.core.orm.mybatis.support;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.transaction.support.ResourceHolderSupport;
import org.springframework.util.Assert;

public final class SqlSessionHolder extends ResourceHolderSupport {
	
	private final SqlSession sqlSession;
	
	private final ExecutorType executorType;
	
	private final PersistenceExceptionTranslator persistenceExceptionTranslator;

	public SqlSessionHolder(SqlSession sqlSession, ExecutorType executorType, PersistenceExceptionTranslator translator) {
		Assert.notNull(sqlSession, "SqlSession must not be null");
		Assert.notNull(executorType, "ExecutorType must not be null");

		this.sqlSession = sqlSession;
		this.executorType = executorType;
		this.persistenceExceptionTranslator = translator;
	}

	public SqlSession getSqlSession() {
		return this.sqlSession;
	}

	public ExecutorType getExecutorType() {
		return this.executorType;
	}

	public PersistenceExceptionTranslator getPersistenceExceptionTranslator() {
		return this.persistenceExceptionTranslator;
	}
}

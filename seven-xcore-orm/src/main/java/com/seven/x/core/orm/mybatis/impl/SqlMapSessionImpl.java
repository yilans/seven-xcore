package com.seven.x.core.orm.mybatis.impl;

import com.seven.x.core.orm.mybatis.SqlMapSession;
import com.seven.x.core.orm.mybatis.support.SqlSessionUtils;

import java.util.List;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.reflection.ExceptionUtil;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.jdbc.JdbcUpdateAffectedIncorrectNumberOfRowsException;
import org.springframework.util.Assert;

public class SqlMapSessionImpl implements SqlMapSession {
	
	static final Logger log = LoggerFactory.getLogger(SqlMapSessionImpl.class);
	
	private SqlSession sqlSession;
	
	private PersistenceExceptionTranslator persistenceExceptionTranslator;
	
	private SqlSessionFactory sqlSessionFactory;

	SqlMapSessionImpl(SqlSession sqlSession, SqlSessionFactory sqlSessionFactory, PersistenceExceptionTranslator persistenceExceptionTranslator) {
		this.sqlSession = sqlSession;
		this.persistenceExceptionTranslator = persistenceExceptionTranslator;
		this.sqlSessionFactory = sqlSessionFactory;
	}

	private <T> T callBack(Transaction<T> transaction) throws DataAccessException {
		Assert.notNull(transaction, "Callback object must not be null");
		try {
			return (T)transaction.transact(this.sqlSession);
		} catch (Throwable cause) {
			
			Object exception = ExceptionUtil.unwrapThrowable(cause);
			if ((this.persistenceExceptionTranslator != null) && ((exception instanceof PersistenceException))) {
				exception = this.persistenceExceptionTranslator.translateExceptionIfPossible((PersistenceException) exception);
			}
			
			throw ((DataAccessException) exception);
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T queryForObject(String name) throws DataAccessException {
		return (T)queryForObject(name, null);
	}

	@SuppressWarnings("unchecked")
	public <T> T queryForObject(final String name, final Object param) throws DataAccessException {
		return (T) callBack(new Transaction<Object>() {
			public Object transact(SqlSession sqlSession) {
				return sqlSession.selectOne(name, param);
			}

		});
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> queryForList(String name) throws DataAccessException {
		return (List<T>)queryForList(name, null);
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> queryForList(final String name, final Object param) throws DataAccessException {
		return (List<T>) callBack(new Transaction<Object>() {
			public List<T> transact(SqlSession sqlSession) {
				return sqlSession.selectList(name, param);
			}

		});
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> queryForList(String name, int offset, int limit) throws DataAccessException {
		return (List<T>)queryForList(name, null, offset, limit);
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> queryForList(final String name, final Object param, final int offset, final int limit) throws DataAccessException {
		return (List<T>) callBack(new Transaction<Object>() {
			public List<T> transact(SqlSession sqlSession) {
				return sqlSession.selectList(name, param, new RowBounds(offset, limit));
			}

		});
	}

	public void queryWithRowHandler(final String name, final Object param, final ResultHandler resultHandler) throws DataAccessException {
		callBack(new Transaction<Object>() {
			public Object transact(SqlSession sqlSession) {
				sqlSession.select(name, param, resultHandler);
				return null;
			}

		});
	}

	public void queryWithRowHandler(final String name, final Object param, final int offset, final int limit, final ResultHandler resultHandler) throws DataAccessException {
		callBack(new Transaction<Object>() {
			public Object transact(SqlSession sqlSession) {
				sqlSession.select(name, param, new RowBounds(offset, limit), resultHandler);
				return null;
			}

		});
	}

	public int insert(String name) throws DataAccessException {
		return insert(name, null);
	}

	public int insert(final String name, final Object param) throws DataAccessException {
		return ((Integer) callBack(new Transaction<Object>() {
			public Integer transact(SqlSession sqlSession) {
				return Integer.valueOf(sqlSession.insert(name, param));
			}

		})).intValue();
	}

	public int update(String name) throws DataAccessException {
		return update(name, null);
	}

	public int update(final String name, final Object param) throws DataAccessException {
		return ((Integer) callBack(new Transaction<Object>() {
			public Integer transact(SqlSession sqlSession) {
				return Integer.valueOf(sqlSession.update(name, param));
			}

		})).intValue();
	}

	public void update(String name, Object param, int expected) throws DataAccessException {
		int actual = update(name, param);
		if (actual != expected)
			throw new JdbcUpdateAffectedIncorrectNumberOfRowsException(name, expected, actual);
	}

	public int delete(String name) throws DataAccessException {
		return delete(name, null);
	}

	public int delete(final String name, final Object param) throws DataAccessException {
		return ((Integer) callBack(new Transaction<Object>() {
			public Integer transact(SqlSession sqlSession) {
				return Integer.valueOf(sqlSession.delete(name, param));
			}

		})).intValue();
	}

	public void delete(String name, Object param, int expected) throws DataAccessException {
		int actual = delete(name, param);
		if (actual != expected)
			throw new JdbcUpdateAffectedIncorrectNumberOfRowsException(name, expected, actual);
	}

	public void commit() throws DataAccessException {
		callBack(new Transaction<Object>() {
			public Object transact(SqlSession sqlSession) {
				sqlSession.commit();
				return null;
			}

		});
	}

	public void rollback() throws DataAccessException {
		callBack(new Transaction<Object>() {
			public Object transact(SqlSession sqlSession) {
				sqlSession.rollback();
				return null;
			}

		});
	}

	public void close() throws DataAccessException {
		SqlSessionUtils.closeSqlSession(this.sqlSession, this.sqlSessionFactory);
	}

	public <T> T getMapper(Class<T> type) {
		return (T)this.sqlSession.getMapper(type);
	}

	static abstract interface Transaction<T> {
		public abstract T transact(SqlSession sqlSession);

	}

}

package com.seven.x.core.orm.mybatis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.DaoSupport;
import org.springframework.util.Assert;

public abstract class SqlMapDaoSupport extends DaoSupport {
	private SqlMap sqlMap;

	@Autowired(required = false)
	public final void setSqlMap(SqlMap sqlMap) {
		this.sqlMap = sqlMap;
	}

	public final SqlMap getSqlMap() {
		return this.sqlMap;
	}

	protected void checkDaoConfig() {
		Assert.notNull(this.sqlMap, "Property 'sqlMap' are required");
	}
}

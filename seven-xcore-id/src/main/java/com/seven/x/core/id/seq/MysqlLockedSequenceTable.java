package com.seven.x.core.id.seq;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mysql对应的加锁查询ID语句以及初始化语句
 * @author yan.jsh
 * 2015年6月17日
 */
public class MysqlLockedSequenceTable extends AbstractLockedSequenceTable{

	private static final Logger log = LoggerFactory.getLogger(AbstractLockedSequenceTable.class);
	
	protected String selectSql;
	
	public String getSelectSql() {
		if (this.selectSql == null)
			this.selectSql = ("select " + this.idColume + ", datetime from " + this.tableName + " where " + this.typeColume + "=? for update");
		return this.selectSql;
	}

	@Override
	public void init() {
		Connection localConnection = null;
		Statement statement = null;
		try {
			localConnection = this.dataSource.getConnection();
			statement = localConnection.createStatement();
			String str = "CREATE TABLE " + this.tableName + "("
					+ this.typeColume + " CHAR(2) NOT NULL, " + this.idColume
					+ " DECIMAL(15), DATETIME DATE,  CONSTRAINT PK_SEQ_NO PRIMARY KEY("
					+ this.typeColume + "))";
			statement.execute(str);
			statement.close();
			
		} catch (SQLException e) {
			log.warn("cannot create sequence table, " + e.getMessage());
			try {
				localConnection.close();
			} catch (Exception e1) {
				log.error("", e1);
			}
		} finally {
			try {
				localConnection.close();
			} catch (Exception e1) {
				log.error("", e1);
			}
		}
	}
	
	
}

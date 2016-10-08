package com.seven.x.core.orm.mybatis.support;

import org.springframework.dao.UncategorizedDataAccessException;

public class MyBatisSystemException extends UncategorizedDataAccessException {
	
	private static final long serialVersionUID = -5284728621670758939L;

	public MyBatisSystemException(Throwable cause) {
		super(null, cause);
	}
}

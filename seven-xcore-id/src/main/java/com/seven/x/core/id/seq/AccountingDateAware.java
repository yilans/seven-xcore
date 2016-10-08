package com.seven.x.core.id.seq;

import java.util.Date;

/**
 * 会计日期初始化通知接口
 * @author yan.jsh
 *  2015-06-17
 */ 
public interface AccountingDateAware {

	/**
	 * 设置会计日期
	 * @param accountingDate
	 */
	public void setAccountingDate(Date accountingDate);
}

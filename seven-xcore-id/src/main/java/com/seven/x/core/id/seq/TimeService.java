package com.seven.x.core.id.seq;

/**
 * 时间服务接口类
 * @author yan.jsh
 * 2011-7-5
 *
 */
public interface TimeService {
	
	public static final int FIRST_DAY_OF_WEEK = 2;
	public static final int MINUTE = 12;
	public static final int HOUR = 11;
	public static final int DAY = 5;
	public static final int WEEK = 3;
	public static final int MONTH = 2;
	public static final int YEAR = 1;
	
	/**
	 * 根据不同的时间域判定是否切换日期
	 * @param field 时间域
	 * @param timeMills 基准时间
	 * @return
	 */
	public boolean isCutoff(int field, long timeMills);

	/**
	 * 根据不同的时间域判定是否切换日期
	 * @param field 时间域
	 * @param currentTimeMills 当前时间
	 * @param timeMills 基准时间
	 * @return
	 */
	public boolean isCutoff(int field, long currentTimeMills, long timeMills);

	/**
	 * 获取当前时间
	 * @return
	 */
	public long currentTimeMillis();
}

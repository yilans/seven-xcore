package com.seven.x.core.id.seq;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;


/**
 * 时间服务默认实现类
 * 
 * @author yan.jsh
 * 2011-7-5
 *
 */
class DefaultTimeService implements TimeService {
	
	private long remoteTimeMillis;
	
	private long localTimeMillis;
	
	/**
	 * 时间间隔
	 */
	private int sychronizeInterval = 60000;
	
	private TimeZone timeZone;
	
	private Locale locale;
	
	/**
	 * 周的第一天
	 */
	private int firstDayOfWeek = TimeService.FIRST_DAY_OF_WEEK;

	public void setSychronizeInterval(int sychronizeInterval) {
		this.sychronizeInterval = sychronizeInterval;
	}

	public void setTimeZone(TimeZone timeZone) {
		this.timeZone = timeZone;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public void setFirstDayOfWeek(int firstDayOfWeek) {
		this.firstDayOfWeek = firstDayOfWeek;
	}

	public final void reset() {
		this.remoteTimeMillis = remoteTimeMillis();
		this.localTimeMillis = localTimeMillis();
	}

	protected long localTimeMillis() {
		return System.currentTimeMillis();
	}

	private long getInterval() {
		return System.currentTimeMillis() - this.localTimeMillis;
	}

	protected long remoteTimeMillis() {
		return System.currentTimeMillis();
	}

	@Override
	public final synchronized long currentTimeMillis() {
		long interval = getInterval();
		if (interval > this.sychronizeInterval)
			reset();
		return this.remoteTimeMillis + getInterval();
	}

	@Override
	public final boolean isCutoff(int field, long timeMills) {
		long currentTimeMillis = currentTimeMillis();
		return isCutoff(field, currentTimeMillis, timeMills);
	}

	@Override
	public final boolean isCutoff(int field, long currentTimeMills, long timeMills) {

		//1.0.1版本修改内容：修复日切异常的BUG
		//modified by yan.jsh 20120705
		//if (get(field, currentTimeMills) - get(field, timeMills) > 0)
		//		return true;
		
		if (get(TimeService.YEAR, currentTimeMills) - get(TimeService.YEAR, timeMills) > 0)
			return true;
		if (get(TimeService.YEAR, currentTimeMills) - get(TimeService.YEAR, timeMills) == 0) {
			if (get(TimeService.MONTH, currentTimeMills) - get(TimeService.MONTH, timeMills) > 0)
				return true;
			if (get(TimeService.MONTH, currentTimeMills) - get(TimeService.MONTH, timeMills) == 0) {
				if (get(field, currentTimeMills) - get(field, timeMills) > 0) {
					return true;
				}
				return false;
			}
			return false;
		}
		//end
		
		return false;
	}

	private Calendar getInstance() {
		Calendar calendar = null;
		if ((this.timeZone != null) && (this.locale != null))
			calendar = Calendar.getInstance(this.timeZone, this.locale);
		else if (this.timeZone != null)
			calendar = Calendar.getInstance(this.timeZone);
		else if (this.locale != null)
			calendar = Calendar.getInstance(this.locale);
		else {
			calendar = Calendar.getInstance();
		}
		calendar.setFirstDayOfWeek(this.firstDayOfWeek);
		return calendar;
	}

	/**
	 * 根据时间域获取日历上的值
	 * @param field
	 * @param timeMills
	 * @return
	 */
	private int get(int field, long timeMills) {
		Calendar calendar = getInstance();
		calendar.setTimeInMillis(timeMills);
		return calendar.get(field);
	}
}

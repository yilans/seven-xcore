package com.seven.x.core.util;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 日志时间工具类
 * @author hjs
 * @since 2011-1-21
 */
public class DateTime {
	
	private final static Logger logger = LoggerFactory.getLogger(DateTime.class);
	
	/**
	 * 默认日期格式，yyyy-MM-dd，从配置文件读取
	 */
//	public static String DEFAULT_DATE_PATTERN = Config.getString("pattern.date.default"); marked by hjs 2013-3-12  暂时不支持Config
	public static String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";
	
	/**
	 * 默认时间格式，HH:mm:ss，从配置文件读取
	 */
//	public static String DEFAULT_TIME_PATTERN = Config.getString("pattern.time.default"); marked by hjs 2013-3-12  暂时不支持Config
	public static String DEFAULT_TIME_PATTERN = "HH:mm:ss";
	
	/**
	 * 默认日期时间格式，yyyy-MM-dd HH:mm:ss，从配置文件读取
	 */
//	public static String DEFAULT_DATETIME_PATTERN = Config.getString("pattern.datetime.default"); marked by hjs 2013-3-12  暂时不支持Config
	public static String DEFAULT_DATETIME_PATTERN = DEFAULT_DATE_PATTERN + " " + DEFAULT_TIME_PATTERN;
	
	public static String DEFAULT_YEAR_PATTERN = "yyyy";
	
	private DateTime(){
	}
	
	static {
//		marked by hjs 2013-3-12  暂时不支持Config
//		DEFAULT_DATE_PATTERN = Config.getString("pattern.date.default");
//		DEFAULT_TIME_PATTERN = Config.getString("pattern.time.default");
//		DEFAULT_DATETIME_PATTERN = Config.getString("pattern.datetime.default");
	}
	
	/**
	 * getCurrentDataTime 以{@link DEFAULT_DATETIME_PATTERN} 格式获得当前时间。
	 */
	public static final String getCurrentDataTime() {
		java.util.Date date = new java.util.Date();
		try {
			SimpleDateFormat simpledateformat = new SimpleDateFormat();
			simpledateformat.applyPattern(DEFAULT_DATETIME_PATTERN);
			return simpledateformat.format(date);
		} catch (Exception e) {
			logger.error("", e);
			return null;
		}
	}
	
	/**
	 * getDate 以 yyyymmdd 的格式获得当前时间。
	 */
	public static String getDate(int day) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, day);
		int iYear = cal.get(Calendar.YEAR);
		int iMonth = cal.get(Calendar.MONTH) + 1;
		int iDay = cal.get(Calendar.DAY_OF_MONTH);
		return "" + iYear + (iMonth < 10 ? "0" + iMonth : "" + iMonth) + (iDay < 10 ? "0" + iDay : "" + iDay);
	}
	
	/**
	 * getDate 以 yyyy-mm-dd 的格式获得当前时间，其中－为可定义的分隔符。
	 */
	public static String getDate(int day, String delimiter) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, day);
		int iYear = cal.get(Calendar.YEAR);
		int iMonth = cal.get(Calendar.MONTH) + 1;
		int iDay = cal.get(Calendar.DAY_OF_MONTH);
		return "" + iYear + delimiter + (iMonth < 10 ? "0" + iMonth : "" + iMonth) + delimiter + (iDay < 10 ? "0" + iDay : "" + iDay);
	}

	public static final String millis2DateTime(long millis) {
		java.util.Date date = new java.util.Date(millis);
		try {
			SimpleDateFormat simpledateformat = new SimpleDateFormat();
			simpledateformat.applyPattern(DEFAULT_DATETIME_PATTERN);
			return simpledateformat.format(date);
		} catch (Exception e) {
			logger.error("", e);
			return null;
		}
	}

	/**
	 * 返回两个Date对象的之间的分钟数
	 * 
	 * @param date1 作为被减数的Date对象
	 * @param date2 作为减数的Date对象
	 * @return int 两个Date对象的之间的分钟数
	 */
	public static int getMinutesTween(Date date1, Date date2) {
		long mill1 = date1.getTime();
		long mill2 = date2.getTime();
		return (int) ((mill1 - mill2) / (1000 * 60));
	}

	/**
	 * 返回两个Date对象的之间的小时数
	 * 
	 * @param date1 作为被减数的Date对象
	 * @param date2 作为减数的Date对象
	 * @return int 两个Date对象的之间的小时数
	 */
	public static int getHoursTween(Date date1, Date date2) {
		long mill1 = date1.getTime();
		long mill2 = date2.getTime();
		return (int) ((mill1 - mill2) / (1000 * 60 * 60));
	}
	
	/**
	 * 返回两个string(格式是：yyyy-MM-dd HH:mm:ss)的之间的小时数
	 * 
	 * @param date1 作为被减数的String对象
	 * @param date2 作为减数的String对象
	 * @return int 两个Date对象的之间的小时数
	 */
	public static int getHoursTween(String date1, String date2) {
		Calendar cal1 = getCalendarFrom(formatDate(date1, DEFAULT_DATETIME_PATTERN));
		Calendar cal2 = getCalendarFrom(formatDate(date2, DEFAULT_DATETIME_PATTERN));
		Date d1 = cal1.getTime();
		Date d2 = cal2.getTime();
		return getHoursTween(d1, d2);
	}
	
	/**
	 * 返回两个Date对象的之间的天数
	 * 
	 * @param date1 作为被减数的Date对象
	 * @param date2 作为减数的Date对象
	 * @return int 两个Date对象的之间的天数
	 */
	public static int getDaysTween(Date date1, Date date2) {
		// return toJulian(date1) - toJulian(date2);
		long mill1 = date1.getTime();
		long mill2 = date2.getTime();
		return (int) ((mill1 - mill2) / (1000 * 60 * 60 * 24));
	}

	/**
	 * 返回两个String(格式是：yyyy-MM-dd HH:mm:ss)对象的之间的天数
	 * 
	 * @param date1 作为被减数的String对象
	 * @param date2 作为减数的String对象
	 * @return int 两个String对象的之间的天数
	 */
	public static int getDaysTween(String date1, String date2) {
		Calendar cal1 = getCalendarFrom(formatDate(date1, DEFAULT_DATETIME_PATTERN));
		Calendar cal2 = getCalendarFrom(formatDate(date2, DEFAULT_DATETIME_PATTERN));
		Date d1 = cal1.getTime();
		Date d2 = cal2.getTime();
		return getDaysTween(d1, d2);
	}

	
	public static int getMonthsTween(Date startDate, Date endDate) {
		Calendar startCal = Calendar.getInstance();
		Calendar endCal = Calendar.getInstance();

		startCal.setTime(startDate);
		endCal.setTime(endDate);
		
		int years = endCal.get(Calendar.YEAR) - startCal.get(Calendar.YEAR);
		int months = 0;
		if(endCal.get(Calendar.MONTH) >= startCal.get(Calendar.MONTH)){
			months = endCal.get(Calendar.MONTH) - startCal.get(Calendar.MONTH);
		} else {
			// Calendar.get(Calendar.MONTH)锟斤拷锟截碉拷锟斤拷锟斤拷为0~11始
			months = 11 - startCal.get(Calendar.MONTH);
			months += endCal.get(Calendar.MONTH) + 1;
			years--;
		}
		return years * 12 + months;
	}
	
	/**
	 * 将Date数据类型转换为特定的格式, 如格式为null, 则使用缺省格式yyyy-MM-dd.
	 * 
	 * @param day 日期
	 * @param toPattern 要转换成的日期格式
	 * @return String 返回日期字符串
	 */
	public static String formatDate(Date day, String toPattern) {
		String date = null;
		if (day != null) {
			try {
				SimpleDateFormat formatter = null;
				if (toPattern != null) {
					formatter = new SimpleDateFormat(toPattern);
				} else {
					formatter = new SimpleDateFormat(DEFAULT_DATE_PATTERN);
				}
				date = formatter.format(day);
			} catch (Exception e) {
				logger.error("", e);
				return null;
			}
			return date;
		} else {
			return null;
		}
	}
	/**
	 * 将原有的日期格式的字符串转换为特定的格式, 如(原有和转换)格式为null, 则使用缺省格式yyyy-MM-dd.
	 * 
	 * @param dateStr 日期格式的字符串
	 * @param fromPattern 原有的日期格式
	 * @param toPattern 转换成的日期格式
	 * @return String 返回日期字符串
	 */
	public static String formatDate(String dateStr, String fromPattern, String toPattern) {
		String date = null;
		if (toPattern == null) {
			toPattern = DEFAULT_DATE_PATTERN;
		}
		if (dateStr != null) {
			try {
				SimpleDateFormat formatter = null;
				if (fromPattern != null) {
					formatter = new SimpleDateFormat(fromPattern);
				} else {
					formatter = new SimpleDateFormat(DEFAULT_DATE_PATTERN);
				}
				Date day = formatter.parse(dateStr);
				formatter.applyPattern(toPattern);
				date = formatter.format(day);
			} catch (Exception e) {
				logger.error("Error formatting Date", e);
				return dateStr;
			}
			return date;
		} else {
			return null;
		}
	}
	/**
	 * 将原有的日期格式的字符串转换为特定的格式, 原有格式为yyyy-MM-dd.
	 * 
	 * @param dateStr 日期格式的字符串
	 * @param toPattern 转换成的日期格式
	 * @return String 返回日期字符串
	 */
	public static String formatDate(String dateStr, String toPattern) {
		return formatDate(dateStr, null, toPattern);
	}

	/**
	 * 返回输入的字符串代表的Calendar对象.
	 * 
	 * @param dateStr 输入的字符串,格式根据配置文件中的pattern.date.default参数决定
	 * @return Calendar 返回代表输入字符串的Calendar对象.
	 */
	public static Calendar getCalendarFrom(String dateStr) {
		return getCalendarFrom(dateStr, DEFAULT_DATE_PATTERN);
	}

	/**
	 * 返回输入的字符串代表的Calendar对象.
	 * 
	 * @param dateStr 输入的字符串
	 * @param pattern 输入的字符串日期格式, null则表示使用默认格式，格式根据配置文件中的pattern.date.default参数决定
	 * @return Calendar 返回代表输入字符串的Calendar对象
	 */
	public static Calendar getCalendarFrom(String dateStr, String pattern) {
		Date date = getDateFrom(dateStr, pattern);

		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal;
	}

	/**
	 * 返回输入的字符串代表的Date对象.
	 * 
	 * @param dateStr 输入的字符串
	 * @param pattern 输入的字符串日期格式, null则表示使用默认格式，格式根据配置文件中的pattern.datetime.default参数决定
	 * @return Date 返回代表输入字符串的Date对象
	 */
	public static Date getDateFrom(String dateStr, String pattern) {
		if (pattern == null) {
			pattern = DEFAULT_DATETIME_PATTERN;
		}
		try {
			DateFormat myformat = new SimpleDateFormat(pattern);
			Date date = myformat.parse(dateStr);
			return date;
		} catch (Exception e) {
			logger.error("Error get date from string[" + dateStr + "], pattern[" + pattern + "]", e);
			return null;
		}
	}

	/**
	 * 返回输入的字符串代表的Date对象.
	 * 
	 * @param dateStr 日期字符串，格式根据配置文件中的pattern.date.default参数决定
	 * @return Date 返回代表输入字符串的Date对象
	 */
	public static Date getDateFrom(String dateStr) {
		return getDateFrom(dateStr, DEFAULT_DATE_PATTERN);
	}

	/**
	 * 返回输入的字符串代表的Date对象.
	 * 
	 * @param dateStr 输入的字符串
	 * @param isBeginning true表示日初，false表示日末
	 * @return Date 返回代表输入字符串的Date对象
	 */
	public static Date getDateFrom(String dateStr, boolean isBeginning) {
		Date date = getDateFrom(dateStr);
		return resetTime(date, isBeginning);
	}

	/**
	 * 返回当前年月日期，pattern为null则格式根据配置文件中的pattern.datetime.default参数决定
	 * @param pattern 日期格式
	 * @return String 年月日期
	 */
	public static String getCurrentDateTime(String pattern) {
		if(pattern == null){
			pattern = DEFAULT_DATETIME_PATTERN;
		}
		
		String date = null;
		try {
			DateFormat myformat = new SimpleDateFormat(pattern);
			date = myformat.format(new Date());
		} catch (Exception e) {
			logger.error("Error get current date from patten[" + pattern + "]", e);
		}
		return date;
	}

	/**
	 * 返回当前年月日期，格式根据配置文件中的pattern.datetime.default参数决定
	 * 
	 * @return String 年月日期
	 */
	public static String getCurrentDateTime() {
		return getCurrentDateTime(DEFAULT_DATETIME_PATTERN);
	}

	/**
	 * 返回当前时间，格式根据配置文件中的pattern.time.default参数决定
	 * 
	 * @return String 当前时间
	 */
	public static String getCurrentTime() {
		return getCurrentDateTime(DEFAULT_TIME_PATTERN);
	}

	/**
	 * 返回当前年月日期，格式根据配置文件中的pattern.date.default参数决定
	 * 
	 * @return String 年月日期
	 */
	public static String getCurrentDate() {
		String date = null;
		try {
			DateFormat myformat = new SimpleDateFormat(DEFAULT_DATE_PATTERN);
			date = myformat.format(new Date());
		} catch (Exception e) {
			logger.error("Error get current date", e);
		}
		return date;
	}

	/**
	 * 忽略时间精度，只比较两个java.util.Date日期部分的大小
	 * @author hjs
	 * @since 2011-1-21
	 * @param d1
	 * @param d2
	 * @return
	 */
	public static int compareDateWithoutTime(Date d1, Date d2) {
		return (new Integer(formatDate(d1, "yyyyMMdd"))).compareTo(new Integer(formatDate(d2, "yyyyMMdd")));
	}

	/**
	 * 比较两个java.util.Date的大小，比较部分由pattern参数决定，不设定pattern则比价日期+时间（精确到秒，不包括毫秒）
	 * @author hjs
	 * @since 2011-1-21
	 * @param d1
	 * @param d2
	 * @param pattern
	 * @return
	 */
	public static int compareDate(Date d1, Date d2, String pattern) {
		if (pattern == null || pattern == "")
			pattern = "yyyyMMddHHmmss";
		return (new Long(formatDate(d1, pattern))).compareTo(new Long(formatDate(d2, pattern)));
	}
	
	/**
	 * 比较两个Date的月份是否相等
	 * @author Dzxing
	 * @date 2015年6月26日 上午9:25:10
	 * @param d1
	 * @param d2
	 * @return
	 */
	public static int compareMonth(Date d1, Date d2){
		Calendar cal = Calendar.getInstance();
		cal.setTime(d1);
		int m1 = cal.get(Calendar.MONTH);
	    cal.setTime(d2);
	    int m2 = cal.get(Calendar.MONTH);
	    return m1-m2;
	}
	
	/**
	 * 比较两个java.sql.Date的大小，比较部分由pattern参数决定，不设定pattern则比价日期+时间（精确到秒，不包括毫秒）
	 * @author kyle and zhang.lu
	 * @since 2015-4-16
	 * @param d1
	 * @param d2
	 * @param pattern
	 * @return
	 */
	public static int compareSqlDate(java.sql.Date d1, java.sql.Date d2, String pattern) {
		if (pattern == null || pattern == "")
			pattern = "yyyyMMdd";
		return (new Long(formatDate(d1, pattern))).compareTo(new Long(formatDate(d2, pattern)));
	}

	/**
	 * 获取本月最后一天的日期
	 * @author hjs
	 * @since 2011-1-24
	 * @return 本月最后一天的日期
	 */
	public static Date getLastDateOfCurrMonth(){
		return getLastDateOfMonth(null);
	}
	
	/**
	 * 获取指定日期当月最后一天的日期
	 * @author hjs
	 * @since 2011-1-24
	 * @param date 如果为null值，则默认为当月
	 * @return 指定日期当月最后一天的日期
	 */
	public static Date getLastDateOfMonth(Date date){
		Calendar cal = Calendar.getInstance();
		if(date != null) {
			cal.setTime(date);
		}
		cal.add(Calendar.MONDAY, 1);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.add(Calendar.DAY_OF_MONTH, -1);
		return cal.getTime();
	}

	/**
	 * 获取当月第一天的日期
	 * @author hjs
	 * 2012-7-5
	 * @return 当月第一天的日期
	 */
	public static Date getFirstDateOfCurrMonth(){
		return getFirstDateOfMonth(null);
	}

	/**
	 * 获取指定日期当月第一天的日期
	 * @author hjs
	 * 2012-7-5
	 * @param date 如果为null值，则默认为当月
	 * @return 指定日期当月第一天的日期
	 */
	public static Date getFirstDateOfMonth(Date date){
		Calendar cal = Calendar.getInstance();
		if(date != null) {
			cal.setTime(date);
		}
		cal.set(Calendar.DAY_OF_MONTH, 1);
		return cal.getTime();
	}

	/**
	 * 获取本周的第一天(星期天)的日期
	 * @author hjs
	 * 2012-7-5
	 * @return 本周的第一天(星期天)的日期
	 */
	public static Date getFirstDateOfCurrWeek(){
		return getFirstDateOfWeek(null);
	}
	
	/**
	 * 获取指定日期当前周的第一天(星期天)的日期
	 * @author hjs
	 * 2012-7-5
	 * @param date 如果为null值，则默认为本周
	 * @return 指定日期当前周的第一天(星期天)的日期
	 */
	public static Date getFirstDateOfWeek(Date date){
		Calendar cal = Calendar.getInstance();
		if(date != null) {
			cal.setTime(date);
		}
		cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		return cal.getTime();
	}
	
	/**
	 * 获取本周的最后一天(星期六)的日期
	 * @author hjs
	 * 2012-7-5
	 * @return 本周的最后一天(星期六)的日期
	 */
	public static Date getLastDateOfCurrWeek(){
		return getLastDateOfWeek(null);
	}
	
	/**
	 * 获取指定日期当前周的最后一天(星期六)的日期
	 * @author hjs
	 * 2012-7-5
	 * @param date 如果为null值，则默认为本周
	 * @return 指定日期当前周的最后一天(星期六)的日期
	 */
	public static Date getLastDateOfWeek(Date date){
		Calendar cal = Calendar.getInstance();
		if(date != null) {
			cal.setTime(date);
		}
		cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
		return cal.getTime();
	}
	
	/**
	 * 获取指定日期当前年的最后一天的日期
	 * @author Dzxing
	 * @date 2015-3-30 下午5:01:01
	 * @param date
	 * @return 指定日期当前年的最后一天的日期
	 */
	public static Date getLastDateofYear(Date date){
		Calendar cal = Calendar.getInstance();
		if(date != null) {
			cal.setTime(date);
		}
		cal.add(Calendar.YEAR, 1);
		cal.set(Calendar.DAY_OF_YEAR, 1);
		cal.add(Calendar.DAY_OF_YEAR, -1);
		return cal.getTime();
	}
	
	/**
	 * 
	 * @author hjs
	 * 2012-9-16
	 * @param date
	 * @param field Calendar.field，例如Calendar.SECOND, Calendar.MONTH
	 * @param step
	 * @return
	 */
	public static Date addDateField(Date date, int field, int step){
		Calendar cal = Calendar.getInstance();
		if(date != null){
			cal.setTime(date);
		}
		cal.add(field, step);
		return cal.getTime();
	}
	
	public static Date addSeconds(Date date, int seconds){
		return addDateField(date, Calendar.SECOND, seconds);
	}
	
	public static Date addMinutes(Date date, int minutes){
		return addDateField(date, Calendar.MINUTE, minutes);
	}
	
	public static Date addHours(Date date, int hours){
		return addDateField(date, Calendar.HOUR, hours);
	}

	/**
	 * 调整日期的天数
	 * @author hjs
	 * 2012-3-1
	 * @param date 为null则默认为当前日期
	 * @param days 步进
	 * @return
	 */
	public static Date addDays(Date date, int days){
		return addDateField(date, Calendar.DATE, days);
	}

	/**
	 * 调整日期的月份
	 * @author hjs
	 * 2012-3-1
	 * @param date 为null则默认为当前日期
	 * @param months 步进
	 * @return
	 */
	public static Date addMonths(Date date, int months){
		return addDateField(date, Calendar.MONTH, months);
	}
	
	/**
	 * 调整日期的年份
	 * @author hjs
	 * 2012-3-1
	 * @param date 为null则默认为当前日期
	 * @param years 步进
	 * @return
	 */
	public static Date addYears(Date date, int years){
		return addDateField(date, Calendar.YEAR, years);
	}
	
	/**
	 * 将{@link java.util.Date}转换成{@link javax.xml.datatype.XMLGregorianCalendar}
	 * @author hjs 2011-6-28
	 * @param date
	 * @return
	 */
	public static XMLGregorianCalendar convertToXMLGregorianCalendar(Date date) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(date);
		XMLGregorianCalendar gc = null;
		try {
			gc = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
		} catch (Exception e) {
			logger.error("Error converting Date[" + date + "] to XMLGregorianCalendar", e);
		}
		return gc;
	}
	
	/**
	 * 将{@link java.util.Date}转换成{@link javax.xml.datatype.XMLGregorianCalendar}
	 * @author hjs 2013-10-24
	 * @param date
	 * @return
	 */
	public static XMLGregorianCalendar convertToXMLGregorianCalendar(String date) {
		XMLGregorianCalendar gc = null;
		try {
			gc = DatatypeFactory.newInstance().newXMLGregorianCalendar(date);
		} catch (Exception e) {
			logger.error("Error converting Date[" + date + "] to XMLGregorianCalendar", e);
		}
		return gc;
	}

	/**
	 * 将{@link javax.xml.datatype.XMLGregorianCalendar}转换成{@link java.util.Date}
	 * 
	 * @author hjs 2011-6-28
	 * @param cal
	 * @return
	 */
	public static Date convertToDate(XMLGregorianCalendar cal) {
		GregorianCalendar ca = cal.toGregorianCalendar();
		return ca.getTime();
	}
	
	/**
	 * 根据isBeginning参数，将java.util.Date设定为日初（0:0:0）或日末（23:59:59）
	 * @author hjs
	 * @since 2011-1-21
	 * @param date
	 * @param isBeginning true表示日初，false表示日末
	 * @return
	 */
	public static Date resetTime(Date date, boolean isBeginning) {
		return isBeginning ? asBeginningOfDate(date) : asEndOfDate(date);
	}

	
	/**
	 * 根据isBeginning参数，将Timestamp设定为日初（0:0:0）或日末（23:59:59）
	 * @author hjs
	 * @since 2011-1-21
	 * @param date
	 * @param isBeginning true表示日初，false表示日末
	 * @return
	 */
	public static Timestamp resetTimeOfTimestamp(Date date, boolean isBeginning) {
		Date _date = resetTime(date, isBeginning);
		return new Timestamp(_date.getTime());
	}
	
	/**
	 * 格式化成日初时间，如：2012-1-1 0:0:0
	 * @author hjs
	 * 2012-1-18
	 * @param date
	 * @return
	 */
	public static Date asBeginningOfDate(Date date){
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
	
	/**
	 * 格式化成日末时间，如：2012-1-1 23::59:59
	 * @author hjs
	 * 2012-1-18
	 * @param date
	 * @return
	 */
	public static Date asEndOfDate(Date date){
		date = addDays(date, 1);
		date = asBeginningOfDate(date);

		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.SECOND, -1);
		return cal.getTime();
	}
	
	/**
	 * 修改java.sql.Time的日期部分
	 * @author hjs
	 * 2012-5-21
	 * @param time
	 * @param date
	 * @return
	 */
	public static Time changeDateOfTime(Time time, Date date){
		Calendar timeCal = Calendar.getInstance();
		timeCal.setTime(time);

		Calendar dateCal = Calendar.getInstance();
		dateCal.setTime(date);
		
		timeCal.set(Calendar.YEAR, dateCal.get(Calendar.YEAR));
		timeCal.set(Calendar.MONTH, dateCal.get(Calendar.MONTH));
		timeCal.set(Calendar.DATE, dateCal.get(Calendar.DATE));
		
		return new Time(timeCal.getTime().getTime());
	}
	
//	/**
//	 * sql日期计算
//	 * @param date 日期
//	 * @param term 期数
//	 * @param calUnit 期数单位
//	 *                本金宽限期单位 0-天 1-月 2-年
//	 * @author chen.shp
//	 * @since 2015-5-7
//	 * @return
//	 */
//	public static java.sql.Date calculateSqlDate(java.sql.Date date,int term,String calUnit){
//		java.util.Date uDate = new java.util.Date(date.getTime());
//		Calendar sCalendar = Calendar.getInstance();  
//        sCalendar.setTime(uDate); 
//        int calendarType = 0;
//		if("0".equals(calUnit)){
//			calendarType = Calendar.DATE;
//		}else if("1".equals(calUnit)){
//			calendarType = Calendar.MONTH;
//		}else if("2".equals(calUnit)){
//			calendarType = Calendar.YEAR;
//		}
//		sCalendar.add(calendarType, term);
//		return new java.sql.Date(sCalendar.getTime().getTime());
//	}
	/**
	 * 获取当前年天数
	 * @return
	 */
	public static int getYearDays(){
		int result = 0;
		Calendar cal = Calendar.getInstance();      
        DateFormat format = new SimpleDateFormat (DEFAULT_YEAR_PATTERN);      
        try {      
            cal.setTime(format.parse(format.format(new Date())));      
        } catch (ParseException e) {  
        	logger.error("Error getYearDays:" + e);
        }      
        result = cal.getActualMaximum(Calendar.DAY_OF_YEAR);   
        return result;
	}
	
	/**
	 * sqlDate和Time拼装utilDate
	 * @author Dzxing
	 * @date 2015年7月29日 下午6:16:05
	 * @param date
	 * @param time
	 * @return
	 */
	public static Date formatDate(java.sql.Date date, Time time){
		SimpleDateFormat timeFormat = new SimpleDateFormat("HHmmss");
		String dateStr = formatDate(date, "yyyyMMdd");
		String timeStr = timeFormat.format(time);
		return getDateFrom(dateStr+timeStr, "yyyyMMddHHmmss");
	}
	
}

package com.seven.x.core.util;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.sql.Date;

import org.springframework.util.StringUtils;

/**
 * 扩展的日期时间转换类，供Jackson转换使用
 * @author yan.jsh
 * 2015年1月15日
 */
public class ExSimpleDateFormat extends SimpleDateFormat {
	
	private static final long serialVersionUID = 727686277241170700L;

	private static final String DEFAULT_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
	
	private static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";
	
	private static final String DEFAULT_TIME_PATTERN = "HH:mm:ss";
	
	/**
	 * 将日期时间类型转换成默认格式的字符串
	 */
	@Override
	public StringBuffer format(java.util.Date date, StringBuffer toAppendTo, FieldPosition pos) {
		if(date == null){
			return toAppendTo;
		}
		
		String pattern = "";
		
		if(date instanceof Date){
			pattern = DEFAULT_DATE_PATTERN;
			
		} else if(date instanceof Time){
			pattern = DEFAULT_TIME_PATTERN;
			
		} else if(date instanceof java.util.Date){
			pattern = DEFAULT_DATETIME_PATTERN;
			
		} else if(date instanceof Timestamp){
			pattern = DEFAULT_DATETIME_PATTERN;
		}
		
		if(StringUtils.hasText(pattern)){
			String _date = DateTime.formatDate(date, pattern);
			return toAppendTo.append(_date);
		}
		
		return toAppendTo;
	}

	@Override
	public Date parse(String text, ParsePosition pos) {
		return null;
	}
	
	/**
	 * 递归将所有日期时间类型转换成默认的字符串
	 * @param data 原始数据
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<String, Object> formatAllDate(Map<String, Object> data){
		if(null == data){
			return data;
		}
		Map<String, Object> result = new HashMap<String, Object>();
		
		for(Entry<String, Object> entry : data.entrySet()){
			Object value = entry.getValue();
			String key = entry.getKey();
			if(value instanceof Map){
				result.put(key, formatAllDate((Map<String, Object>) value));
			}else if(value instanceof List){
				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
				for(Object o : (List)value){
					if(o instanceof Map){
						list.add(formatAllDate((Map<String, Object>) o));
					}
				}
				result.put(key, list);
			}else if(value instanceof java.util.Date){
				result.put(key, this.format((java.util.Date)value));
			} else{
				result.put(key,value);
			}
		}
		
		return result;
		
	}
}

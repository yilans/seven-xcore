package com.seven.x.core.id.seq;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.seven.x.core.id.IdCreator;

/**
 * 序号格式化类
 * @author yan.jsh
 * 2011-7-10
 *
 */
class SequenceFormat {
	private String pattern;
//	private String b;
	private AbstractDateFormat[] formats;

	public static SequenceFormat newInstance(String pattern) {
		return new SequenceFormat(pattern);
	}

	private SequenceFormat(String pattern) {
		this.pattern = pattern;
		init();
	}

	public String format(IdCreator<?> idCreator, long number, Date date) {
		StringBuffer sb = new StringBuffer(32);
		
		for (int i = 0; i < this.formats.length; i++) {
			AbstractDateFormat dateFormat = this.formats[i];
			if ((dateFormat instanceof IdCreatorDateFormat))
				dateFormat.simpleFormat(idCreator, sb);
			else if ((dateFormat instanceof NumberDataFormat))
				dateFormat.simpleFormat(new Long(number), sb);
			else if ((dateFormat instanceof SequenceFormat))
				dateFormat.simpleFormat(date, sb);
			else {
				dateFormat.simpleFormat(date, sb);
			}
		}
		return sb.toString();
	}

	protected void init() {
		
		char[] patternChars = this.pattern.toCharArray();
		int i = 0;
		StringBuffer sb = new StringBuffer();
		
		ArrayList<AbstractDateFormat> formats = new ArrayList<AbstractDateFormat>();
		
		for (int j = 0; j < patternChars.length; j++) {
			if ('{' == patternChars[j]) {
				if (i != 0)
					throw new IllegalArgumentException("invalid pattern: " + this.pattern);
				i = 1;
				if (sb.length() > 0) {
					formats.add(new DefaultDataFormat(sb.toString()));
					sb.setLength(0);
				}
			} else if ('}' == patternChars[j]) {
				if (i == 0)
					throw new IllegalArgumentException("invalid pattern: " + this.pattern);
				i = 0;
				if (sb.length() > 0) {
					formats.add(getInstance(sb.toString()));
					sb.setLength(0);
				}
			} else {
				sb.append(patternChars[j]);
			}
		}
		if (i != 0)
			throw new IllegalArgumentException("invalid pattern: " + this.pattern);
		if (sb.length() > 0) {
			formats.add(new DefaultDataFormat(sb.toString()));
			sb.setLength(0);
		}
		this.formats = ((AbstractDateFormat[]) formats.toArray(new AbstractDateFormat[formats.size()]));
	}

	protected AbstractDateFormat getInstance(String pattern) {
		int i = pattern.charAt(0);
		switch (i) {
		case 35:
			return new NumberDataFormat(pattern.length());
		case 36:
			return new IdCreatorDateFormat(pattern.substring(1));
		}
		
//		this.b = pattern;
		
		return new ThreadLocalDateFormat(pattern);
	}

	class ThreadLocalDateFormat implements AbstractDateFormat {
		
		private ThreadLocal<DateFormat> threadLocal = null;

		public void simpleFormat(Object date, StringBuffer sb) {
			
			if ((date != null) && (!(date instanceof Date)))
				throw new IllegalArgumentException("input is not a date: " + date);
			
			Date _date = (Date) date;
			
			if (_date == null)
				_date = new Date();
			
			sb.append(((DateFormat) this.threadLocal.get()).format(_date));
		}

		ThreadLocalDateFormat(final String pattern) {
			this.threadLocal = new ThreadLocal<DateFormat>() {
				protected DateFormat initialValue() {
					return new SimpleDateFormat(pattern);
				}
			};
		}
	}

	class IdCreatorDateFormat implements AbstractDateFormat {
		private String field;

		public void simpleFormat(Object idFactory, StringBuffer sb) {
			if ((idFactory == null) || (!(idFactory instanceof IdCreator)))
				throw new IllegalArgumentException("input is not a id factory: " + idFactory);
			
			Class<?> clazz = idFactory.getClass();
			
			try {
				Method method = clazz.getMethod("get" + this.field, null);
				Object result = method.invoke(idFactory, null);
				
				if (result == null)
					throw new IllegalArgumentException("field '" + this.field + "'s value is null: " + idFactory);
				
				sb.append(result);
			} catch (Exception e) {
				throw new IllegalArgumentException("cannot get field '" + this.field + "'s value: " + idFactory);
			}
		}

		IdCreatorDateFormat(String field) {
			this.field = (Character.toUpperCase(field.charAt(0)) + field.substring(1));
		}
	}

	static abstract interface AbstractDateFormat {
		public abstract void simpleFormat(Object obj, StringBuffer sb);
	}

	class NumberDataFormat implements AbstractDateFormat {
		private int number;
		private long pow;

		public void simpleFormat(Object number, StringBuffer sb) {
			if ((number == null) || (!(number instanceof Number))) {
				throw new IllegalArgumentException("input is not a number: " + number);
			}
			
			String str = new Long(this.pow + ((Number) number).longValue()).toString();
			
			sb.append(str.substring(str.length() - this.number));
		}

		NumberDataFormat(int number) {
			this.number = number;
			this.pow = (long) Math.pow(10.0D, number);
		}
	}

	class DefaultDataFormat implements AbstractDateFormat {
		private String date;

		public void simpleFormat(Object obj, StringBuffer sb) {
			if (this.date == null) {
				throw new IllegalArgumentException("input is null.");
			}
			sb.append(this.date);
		}

		DefaultDataFormat(String date) {
			this.date = date;
		}
	}
}

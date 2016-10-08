package com.seven.x.core.id.seq;

import com.seven.x.core.id.IdCreator;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 序号生成抽象类
 * <p>可生成1个或多个序号，并支持对序号进行格式化处理
 * @author yan.jsh
 * 2011-7-10
 *
 */
public abstract class AbstractSequenceFactory implements IdCreator<Object> {
	
	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	protected String type;
	
	private SequenceFormat format;

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return this.type;
	}

	public void setFormat(String pattern) {
		this.format = SequenceFormat.newInstance(pattern);
	}

	public Object create() {
		Object[] result = create(1);
		return result[0];
	}

	public Object[] create(int num) {
		if (this.type == null) {
			throw new RuntimeException("you must setType(String) in SequenceFactory.");
		}
		
		long[] result = internalGenerate(num);
		
		if (num == 1) {
			if (this.format == null) {
				return new Long[] { new Long(result[0]) };
			}
			return new String[] { format(result[0], result[1]) };
		}
		
		if (this.format == null) {
			Long[] ids = new Long[num];
			for (int i = 0; i < num; i++) {
				ids[i] = new Long(result[0] + i);
			}
			return ids;
		}
		
		String[] ids = new String[num];
		for (int i = 0; i < num; i++) {
			ids[i] = format(result[0] + i, result[1]);
		}
		
		return ids;
	}

	private String format(long id, long timestamp) {
		return this.format.format(this, id, timestamp < 0L ? new Date() : new Date(timestamp));
	}

	protected abstract long[] internalGenerate(int num);
}

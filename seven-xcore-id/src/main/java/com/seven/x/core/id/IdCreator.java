package com.seven.x.core.id;

/**
 * 序号生成器接口
 * @author yan.jsh
 * 2011-7-10
 *
 * @param <T>
 */
public interface IdCreator<T> {
	
	public T create();

	public T[] create(int num);
}

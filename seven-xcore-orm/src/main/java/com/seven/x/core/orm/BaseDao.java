package com.seven.x.core.orm;

import java.util.List;

/**
 * 数据库操作基类接口
 * @author yan.jsh
 * 2015年10月4日
 * @param <ID> 唯一主键类型
 * @param <DM> 对应实体类
 */
public interface BaseDao<ID,DM> {
	/**
	 * 新增
	 * @param persistable
	 * @return
	 */
	public int insert(DM persistable);
	
	/**
	 * 根据SeqNo更新记录
	 * @param persistable
	 * @return
	 */
	public int update(DM persistable);
	
	/**
	 * 删除
	 * @param seqNo
	 * @return
	 */
	public int delete(ID id);
	
	/**
	 * 根据SeqNo查询
	 * @param seqNo
	 * @return
	 */
	public DM getById(ID id);
	
	/**
	 * 根据业务主键查询记录
	 * @param persistable
	 * @return
	 */
	public List<DM> findByBizKeys(DM persistable);
	
	/**
	 * 列表查询，支持分页
	 * @param persistable
	 * @return
	 */
	public List<DM> find(DM persistable);
	
	/**
	 * 根据业务主键更新记录
	 * @param persistable
	 * @return
	 */
	public int updateByBizKeys(DM persistable);
}

package com.seven.x.core.orm.mybatis;

import java.util.List;
import org.apache.ibatis.session.ResultHandler;
import org.springframework.dao.DataAccessException;

/**
 * SqlMap接口类
 * @author yan.jsh
 * 2011-7-10
 *
 */
public interface SqlMap {
	
	/**
	 * 创建SqlMapSession实例
	 * @param isBatch 是否为批量类型
	 * @return
	 * @throws DataAccessException
	 */
	public SqlMapSession startSession(boolean isBatch) throws DataAccessException;

	/**
	 * 查找
	 * @param name SQL名称
	 * @return
	 * @throws DataAccessException
	 */
	public <T> T queryForObject(String name) throws DataAccessException;

	/**
	 * 查找
	 * @param name SQL名称
	 * @param param 参数
	 * @return
	 * @throws DataAccessException
	 */
	public <T> T queryForObject(String name, Object param) throws DataAccessException;

	/**
	 * 列表查询
	 * @param name SQL名称
	 * @return 列表
	 * @throws DataAccessException
	 */
	public <T> List<T> queryForList(String name) throws DataAccessException;

	/**
	 * 列表查询
	 * @param name SQL名称
	 * @param param 参数
	 * @return
	 * @throws DataAccessException
	 */
	public <T> List<T> queryForList(String name, Object param) throws DataAccessException;

	/**
	 * 列表查询（限制返回）
	 * @param name SQL名称
	 * @param param 参数
	 * @param offset 
	 * @param limit
	 * @return
	 * @throws DataAccessException
	 */
	public <T> List<T> queryForList(String name, Object param, int offset, int limit) throws DataAccessException;

	/**
	 * 查询
	 * @param name SQL名称
	 * @param param 参数
	 * @param handler 结果处理器
	 * @throws DataAccessException
	 */
	public void queryWithRowHandler(String name, Object param, ResultHandler handler) throws DataAccessException;

	/**
	 * 查询（限制返回）
	 * @param name SQL名称
	 * @param param 参数
	 * @param offset
	 * @param limit
	 * @param handler 结果处理器
	 * @throws DataAccessException
	 */
	public void queryWithRowHandler(String name, Object param, int offset, int limit, ResultHandler handler) throws DataAccessException;

	/**
	 * 新增
	 * @param name SQL名称
	 * @return
	 * @throws DataAccessException
	 */
	public int insert(String name) throws DataAccessException;

	/**
	 * 新增
	 * @param name SQL名称
	 * @param param 参数
	 * @return
	 * @throws DataAccessException
	 */
	public int insert(String name, Object param) throws DataAccessException;

	/**
	 * 更新
	 * @param name SQL名称
	 * @return
	 * @throws DataAccessException
	 */
	public int update(String name) throws DataAccessException;

	/**
	 * 更新
	 * @param name SQL名称
	 * @param param 参数
	 * @return
	 * @throws DataAccessException
	 */
	public int update(String name, Object param) throws DataAccessException;

	/**
	 * 更新
	 * @param name SQL名称
	 * @param param 参数
	 * @param expected 期待返回结果，如果结果与期待返回结果则抛出异常
	 * @throws DataAccessException
	 */
	public void update(String name, Object param, int expected) throws DataAccessException;

	/**
	 * 删除
	 * @param name SQL名称
	 * @return
	 * @throws DataAccessException
	 */
	public int delete(String name) throws DataAccessException;

	/**
	 * 删除
	 * @param name SQL名称
	 * @param param 参数
	 * @return
	 * @throws DataAccessException
	 */
	public int delete(String name, Object param) throws DataAccessException;

	/**
	 * 删除
	 * @param name SQL名称
	 * @param param 参数
	 * @param expected 期待返回结果，如果结果与期待返回结果则抛出异常
	 * @throws DataAccessException
	 */
	public void delete(String name, Object param, int expected) throws DataAccessException;
}

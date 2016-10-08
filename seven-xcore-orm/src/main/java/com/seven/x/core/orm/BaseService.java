package com.seven.x.core.orm;

import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 操作数据库公共基类组件
 * @author yan.jsh
 * 2015年10月5日
 * @param <T> 对应的实体类
 * @param <D> 对应的数据表Dao
 */
public abstract class BaseService<T, D extends BaseDao<String,T>> implements ApplicationContextAware {

	private ApplicationContext applicationContext;

	protected D baseDao = null;
	
	/**
	 * 实现ApplicationContextAware接口的context注入函数, 将其存入静态变量.
	 */
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext; // NOSONAR
	}

	/**
	 * 从静态变量ApplicationContext中取得Bean, 自动转型为所赋值对象的类型.
	 */
	@SuppressWarnings({ "unchecked", "hiding" })
	protected <T> T getBean(String name) {
		return (T) applicationContext.getBean(name);
	}

	/**
	 * 新增参数
	 * 
	 * @param persistable
	 *            新增参数
	 */
	public void add(T persistable) {
		if (null == persistable)
			return;
		
		this.getDao().insert(persistable);
	}

	/**
	 * 根据SeqNo修改参数
	 * <p>
	 * 默认会添加更新人、更新时间、更新分行等信息
	 * 
	 * @param persistable
	 *            修改后的参数
	 */
	public void modify(T persistable) {
		if (null == persistable)
			return;

		this.getDao().update(persistable);

	}

	/**
	 * 根据业务主键修改参数
	 * <p>
	 * 默认会添加更新人、更新时间、更新分行等信息
	 * 
	 * @param persistable
	 *            修改后的参数
	 */
	public void modifyByBizKeys(T persistable) {
		if (null == persistable)
			return;

		this.getDao().updateByBizKeys(persistable);
	}

	/**
	 * 删除参数
	 * <p>
	 * 物理删除，只能删除失效或停用的记录
	 * 
	 * @param id
	 *            主键
	 * @return
	 */
	public void delete(String id) {
		if (null == id)
			return;

		this.getDao().delete(id);
	}

	/**
	 * 查询明细
	 * 
	 * @param id
	 *            主键
	 */
	public T view(String id) {
		return this.getDao().getById(id);
	}

	/**
	 * 条件查询，支持分页
	 * 
	 * @param persistable
	 * @return
	 */
	public List<T> query(T persistable) {
		return this.getDao().find(persistable);
	}

	/**
	 * 根据业务主键查询多条记录
	 * 
	 * @param persistable
	 * @return 多条记录
	 */
	public List<T> queryByBizKeys(T persistable) {
		return this.getDao().findByBizKeys(persistable);
	}

	/**
	 * 根据业务主键查询唯一记录
	 * <p>
	 * 如果返回多条记录则报错;如果没有匹配记录则返回null
	 * 
	 * @param persistable
	 * @return 唯一记录
	 */
	public T getByBizKeys(T persistable) {
		return getByBizKeysFromDataBase(persistable);
	}


	/**
	 * 获取domain的简单类名,首字母小写
	 * 
	 * @return
	 */
	private String getSimpleDomainName() {
		Class<T> domainClazz = getDomainClass();
		String domainName = domainClazz.getSimpleName();
		StringBuffer sb = new StringBuffer();
		sb.append(domainName.substring(0, 1).toLowerCase());
		sb.append(domainName.substring(1));
		return sb.toString();
	}

	/**
	 * 根据业务主键从数据库中查询
	 * 
	 * @param persistable
	 * @return
	 */
	private T getByBizKeysFromDataBase(T persistable) {
		List<T> results = this.queryByBizKeys(persistable);
		if (results == null || results.size() == 0) {
			return null;
		}
		return results.get(0);
	}

	/**
	 * 获取对应的持久化Dao
	 * <p>
	 * 默认取Domain名称，首字母小写，加上“Dao”，作为Dao的名称
	 * <p>
	 * 比如Subject，则对为"subjectDao"
	 * 
	 * @return
	 */
	protected D getDao() {
		if(null == baseDao){
			String simpleDomainName = getSimpleDomainName();

			StringBuffer sb = new StringBuffer();
			sb.append(simpleDomainName);
			sb.append("Dao");

			baseDao = this.getBean(sb.toString());
		}
		
		return baseDao;
	}


	/**
	 * 获取对应domain的class类型
	 * 
	 * @return
	 */
	protected abstract Class<T> getDomainClass();


}

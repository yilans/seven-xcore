package com.seven.x.core.orm.mybatis;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.seven.x.core.orm.BaseDao;

/**
 * 针对Mybatis实现的数据库操作基类
 * @author yan.jsh
 * 2015年10月4日
 * @param <ID> 唯一主键类型
 * @param <DM> 对应实体类
 */
public abstract class MybatisBaseDao<ID,DM> implements BaseDao<ID,DM>{
	
	protected final static String INSERT_SQL_ID = "insert";
	
	protected final static String UPDATE_SQL_ID = "update";
	
	protected final static String DELETE_SQL_ID = "delete";
	
	protected final static String GET_BY_ID_SQL_ID = "getById";
	
	protected final static String FIND_BY_BIZ_KEYS_SQL_ID = "findByBizKeys";
	
	protected final static String FIND_PAGABLE_SQL_ID = "findPagable";
	
	protected final static String UPDATE_BY_BIZ_KEYS_SQL_ID = "updateByBizKeys";

	protected Map<String, String> sqlIds = new HashMap<String, String>();
	
	
	@Autowired
	protected SqlMap sqlMap;
	
	/**
	 * 新增
	 * @param persistable
	 * @return
	 */
	public int insert(DM persistable){
		return this.save(this.getFullSqlId(INSERT_SQL_ID), persistable);
	}
	
	/**
	 * 根据SeqNo更新记录
	 * @param persistable
	 * @return
	 */
	public int update(DM persistable){
		return this.update(this.getFullSqlId(UPDATE_SQL_ID), persistable);
	}
	
	/**
	 * 删除
	 * @param seqNo
	 * @return
	 */
	public int delete(ID id){
		return this.delete(this.getFullSqlId(DELETE_SQL_ID), id);
	}
	
	/**
	 * 根据SeqNo查询
	 * @param seqNo
	 * @return
	 */
	public DM getById(ID id){
		return this.get(this.getFullSqlId(GET_BY_ID_SQL_ID), id);
	}
	
	/**
	 * 根据业务主键查询记录
	 * @param persistable
	 * @return
	 */
	public List<DM> findByBizKeys(DM persistable){
		return this.find(this.getFullSqlId(FIND_BY_BIZ_KEYS_SQL_ID), persistable);
	}
	
	/**
	 * 列表查询，支持分页
	 * @param persistable
	 * @return
	 */
	public List<DM> find(DM persistable){
		return this.find(this.getFullSqlId(FIND_PAGABLE_SQL_ID), persistable);
	}
	
	/**
	 * 根据业务主键更新记录
	 * @param persistable
	 * @return
	 */
	public int updateByBizKeys(DM persistable){
		return this.update(this.getFullSqlId(UPDATE_BY_BIZ_KEYS_SQL_ID), persistable);
	}


	/**
	 * 基准增加方法，执行insert操作
	 * 2013-3-14 
	 * @param sqlId
	 * @param object
	 */
	protected int save(String sqlId,Object object){
		return sqlMap.insert(sqlId, object);
	}
	
	/**
	 * 基准修改方法，执行update操作
	 * 2013-3-14 
	 * @param sqlId
	 * @param object
	 */
	protected int update(String sqlId,Object object){
		return sqlMap.update(sqlId, object);
	}
	
	/**
	 * 基准删除方法，执行delete操作
	 * 2013-3-14 
	 * @param sqlId
	 * @param object
	 */
	protected int delete(String sqlId,Object object){
		return sqlMap.delete(sqlId, object);
	}
	
	/**
	 * 基准查询方法，返回List集合
	 * 2013-3-14 下午5:07:45
	 * @param sqlId
	 * @param object
	 * @return
	 */
	protected List<DM> find(String sqlId,Object object){
		return sqlMap.queryForList(sqlId, object);
	}
	
	/**
	 * 基准查询方法，返回单个对象
	 * 2013-3-14 
	 * @param sqlId
	 * @param object
	 * @return
	 */
	protected DM get(String sqlId,Object object){
		return sqlMap.queryForObject(sqlId, object);
	}
	
	/**
	 * 获取Mapping文件中的命名空间
	 * <p>默认为Domain类的类名，首字母小写
	 * @return
	 */
	protected String getFullSqlId(String sqlId){
		String fullSqlId = this.sqlIds.get(sqlId);
		
		if(null == fullSqlId){
			String className = this.getDomainClass().getSimpleName();
			
			StringBuffer sb = new StringBuffer();
			sb.append(className.substring(0, 1).toLowerCase());
			sb.append(className.substring(1));
			sb.append(".");
			sb.append(sqlId);
			fullSqlId = sb.toString() ;
			this.sqlIds.put(sqlId, fullSqlId);
		}
		
		return fullSqlId;
	}
	
	/**
	 * 获取对应domain的class类型
	 * @return
	 */
	public abstract Class<DM> getDomainClass();

}
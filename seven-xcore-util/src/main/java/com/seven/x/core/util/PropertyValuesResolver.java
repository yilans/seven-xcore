package com.seven.x.core.util;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorUtils;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.core.GenericCollectionTypeResolver;
import org.springframework.validation.DataBinder;

/**
 * 数据绑定中的支持类,利用spring 提供的已实现的数据绑定方式将一个Map结构的数据绑定在
 * 一个对象上,此resolver是数据绑定前进行预处理,得到spring {@link DataBinder}可识别的
 * {@link PropertyValues}.
 * 
 * @version 1.0.0,Jul 26, 2012
 * @since 1.0.0
 */
public class PropertyValuesResolver {

	/** Cacher caching java bean's {@link PropertyDescriptor}.*/
	private Map<String,PropertyDescriptor>  propertyDescCache=new ConcurrentHashMap<String, PropertyDescriptor>();
	
	/**
	 * later for binding properties to target type instance.
	 */
	public PropertyValues resolve(Object data,Class<?>targetType){
		MutablePropertyValues values=new MutablePropertyValues();
		if(null==data){
			return values;
		}
		if(data instanceof Map<?, ?>){
			Map<?,?> dataMap=(Map<?, ?>) data;
			
			for(Map.Entry<?, ?> entry:dataMap.entrySet()){
				String key=entry.getKey().toString();
				Object value=entry.getValue();
				PropertyDescriptor pd=findPropertyDesc(targetType, key);
				if(null==pd){
				  continue;	
				}
				//sub type.
				Class<?> propertyType=pd.getPropertyType();
				Method readMethod=pd.getReadMethod();
				if(propertyType==null||readMethod==null){
					continue;
				}
				values.addPropertyValues(resolve(key, value, propertyType,readMethod,1));
			}
		}
		return values;
	}
	
	//-----------------------------------------------------Scaffold Methods.
	

	/*
	 * Find property descriptor from specified type with given property name.
	 */
	private PropertyDescriptor findPropertyDesc(Class<?> type,String propertyName){
		String key=(type.getName()+PropertyAccessor.NESTED_PROPERTY_SEPARATOR+propertyName);
		if(propertyDescCache.containsKey(key)){
			return propertyDescCache.get(key);
		}
	    return doFindPropertyDesc(type.getName(), type, propertyName);
	}
	
	
	private PropertyDescriptor doFindPropertyDesc(String key,Class<?> type,String propertyName){
		
		if(PropertyAccessorUtils.isNestedOrIndexedProperty(propertyName)){
			int index=PropertyAccessorUtils.getFirstNestedPropertySeparatorIndex(propertyName);
			String currentProperty=propertyName.substring(0,index);
			PropertyDescriptor pd=BeanUtils.getPropertyDescriptor(type, currentProperty);
			if(pd!=null){
			    Class<?> propertyType=pd.getPropertyType();
			    if(null==propertyType) return null;
			    Method writeMethod=pd.getWriteMethod();
			    if(null==writeMethod) return null;
			    key+=(PropertyAccessor.NESTED_PROPERTY_SEPARATOR+currentProperty);
			    String subsequenceProperty=propertyName.substring(index+1);
			    return doFindPropertyDesc(key,propertyType,subsequenceProperty);
			}else{
				return null;
			}
		}else{
			
			key=key+PropertyAccessor.NESTED_PROPERTY_SEPARATOR+propertyName;
			PropertyDescriptor pd=BeanUtils.getPropertyDescriptor(type, propertyName);
			if(pd!=null){
			propertyDescCache.put(key,pd);
			}
			return pd;
		}
	}
	
	/*
	 * Resolve java bean property.
	 */
	private PropertyValues  resolve(String path,Object data,Class<?> type,Method method,int level){

		if(null==data||null==type){
			return null;
		}
		MutablePropertyValues values=new MutablePropertyValues();
		if(BeanUtils.isSimpleProperty(type)){
			values.add(path, data);
			return values;
		}else if(List.class.isAssignableFrom(type)){
			handleListType(path,values,data,method,level);
		}else if(Map.class.isAssignableFrom(type)){
			handleMapType(path, values, data, method, level);
		}else{
			PropertyValues pvs=resolve(data,type);
			if(null!=pvs){
				PropertyValue[] pvArray=pvs.getPropertyValues();
				if(null!=pvArray){
				 for(PropertyValue pv:pvArray){
					 values.add(path+PropertyAccessor.NESTED_PROPERTY_SEPARATOR+pv.getName(), pv.getValue());
				 }
				}
			}
		}
		return values;
	}
	
	
	/*
	 * Resolve list type nested property.
	 */
	private PropertyValues  handleListType(String path,MutablePropertyValues values,Object data,Method method,int level){
		
		Class<?> eleType=GenericCollectionTypeResolver.getCollectionReturnType(method,level);
		if(null==eleType){
			return values;
		}
		if(data instanceof List<?>){
			List<?> dataList=(List<?>) data;
			for(int i=0;i<dataList.size();i++){
			 Object element=dataList.get(i);
			 String subPath=path+PropertyAccessor.PROPERTY_KEY_PREFIX+i+PropertyAccessor.PROPERTY_KEY_SUFFIX;
			 values.addPropertyValues(resolve(subPath,element,eleType,method,level+1));
			}
		}
		return values;
	}

	/*
	 * Resolve Map type nested property.
	 */
    private PropertyValues  handleMapType(String path,MutablePropertyValues values,Object data,Method method,int level){
		Class<?> eleType=GenericCollectionTypeResolver.getMapValueReturnType(method,level);	
		
		if(null==eleType){
			return values;
		}
		if(data instanceof Map<?, ?>){
			Map<?,?> dataMap=(Map<?, ?>) data;
			for(Map.Entry<?, ?> entry:dataMap.entrySet()){
				String key=entry.getKey().toString();
				Object value=entry.getValue();
				String subPath=path+PropertyAccessor.PROPERTY_KEY_PREFIX+key+PropertyAccessor.PROPERTY_KEY_SUFFIX; 
				values.addPropertyValues(resolve(subPath,value,eleType,method,level+1));
			}
		}
		return values;
	}
}

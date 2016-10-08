 /*@(#)BeanUtils.java   Aug 30, 2012
 *   
 * Copy Right 2012 Bank of Communications Co.Ltd. 
 * All Copyright Reserved
 */
package com.seven.x.core.util;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyValues;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;
//import org.springframework.validation.FixedDataBinder;
import org.springframework.validation.ObjectError;

/**
 * 常用的静态方法,包含一些 bean 的实例化,java bean 同 map 之间的转换,bean property 
 * 类型的检查等操作.
 * <p>
 *  Static convenience methods for JavaBeans: for converting java bean to {@link Map},
 *  convert {@link Map} to java bean,checking bean types ,instantiate bean,etc.
 *
 * <p>Mainly for use to convert Map to domain object.
 * 
 * @version 1.0.0,Aug 30, 2012
 * @since 1.0.0
 */
public final class BeanUtils {

	/** Logger.*/
	private static final Log logger=LogFactory.getLog(BeanUtils.class);
	
	/* Property values resolver.*/
	private static PropertyValuesResolver resolver=new PropertyValuesResolver();
	
	private static final String JAVA_CLASS_PROPERTY="class";
	
	/**
	 *  Convert a Object to a tree {@link Map} will erase class information.
	 */
	public static final Map<String,Object>  toMap(Object object) {
		return toMap(object,JAVA_CLASS_PROPERTY);
	}
	
	
	public static final Map<String,Object>  toMap(Object object,String ...ignoreProperties) {
		return toMap(object, new HashSet<Object>(),false,ignoreProperties);
	}
	
	/**
	 * Convert a java bean to flat map(JUMP data map structure) will erase class information.
	 */
	public static final Map<String,Object> toFlatMap(Object object){
		return toFlatMap(object,JAVA_CLASS_PROPERTY);
	}
	
	/**
	 * Convert a java bean to flat map(JUMP data map structure).
	 */
	public static final Map<String,Object> toFlatMap(Object object,String ... ignoreProperties){
		return toMap(object, new HashSet<Object>(),true,ignoreProperties);
	}
	
	
	
	/**
	 * Check if the given type represents a "simple" value type:
	 * a primitive, a String or other CharSequence, a Number, a Date,
	 * a URI, a URL, a Locale or a Class.
	 * @param clazz the type to check
	 * @return whether the given type represents a "simple" value type
	 */
	public static boolean isSimpleValueType(Class<?> clazz){
		
		return ClassUtils.isPrimitiveOrWrapper(clazz) || clazz.isEnum() ||
				CharSequence.class.isAssignableFrom(clazz) ||
				Number.class.isAssignableFrom(clazz) ||
				Date.class.isAssignableFrom(clazz) ||
				clazz.equals(URI.class) || clazz.equals(URL.class) ||
				clazz.equals(Locale.class) || clazz.equals(Class.class);
	}
	
	
	/**
	 * Copy the property values of the given source bean into the target bean.
	 * <p>Note: The source and target classes do not have to match or even be derived
	 * from each other, as long as the properties match. Any bean properties that the
	 * source bean exposes but the target bean does not will silently be ignored.
	 * <p>This is just a convenience method. For more complex transfer needs,
	 * consider using a full BeanWrapper.
	 * @param source the source bean
	 * @param target the target bean
	 * @throws BeansException if the copying failed
	 * @see BeanWrapper
	 */
	public static void copyProperties(Object source, Object target) throws BeansException {
		org.springframework.beans.BeanUtils.copyProperties(source, target);
	}

	/**
	 * Copy the property values of the given source bean into the given target bean,
	 * ignoring the given "ignoreProperties".
	 * <p>Note: The source and target classes do not have to match or even be derived
	 * from each other, as long as the properties match. Any bean properties that the
	 * source bean exposes but the target bean does not will silently be ignored.
	 * <p>This is just a convenience method. For more complex transfer needs,
	 * consider using a full BeanWrapper.
	 * @param source the source bean
	 * @param target the target bean
	 * @param ignoreProperties array of property names to ignore
	 * @throws BeansException if the copying failed
	 * @see BeanWrapper
	 */
	public static void copyProperties(Object source, Object target, String[] ignoreProperties)
			throws BeansException {
		org.springframework.beans.BeanUtils.copyProperties(source, target,ignoreProperties);
	}
	
	
	/**
	 * Instantiate a Create using it's no-arg constructor,then fill it's properties 
	 * using specified map.
	 */
	@SuppressWarnings("unchecked")
	public static final <T>T  toObject(Map<String,Object> data,Class<T> clazz){
		
		T object=instantiate(clazz);
		
		PropertyValues pvs=resolver.resolve(data, clazz);
		DataBinder binder=new DataBinder(object);
		binder.bind(pvs);
		BindingResult result=binder.getBindingResult();
		
		if(result.hasErrors()){
			List<ObjectError> errors=result.getAllErrors();
			for(ObjectError error:errors){
				logger.error(error);
			}
		}
			return (T) binder.getTarget();
	}
	
	
	
	/**
	 * <p> Convert a iterable {@link Map} elements to specified class list.
	 * 
	 * @see #toObject(Map, Class)
	 */
	public static final <T> Iterable<T> toObjects(Iterable<Map<String,Object>> data,Class<T> clazz){
		List<T> arrayList=new ArrayList<T>();
		for(Map<String,Object> element:data){
			T entity=toObject(element, clazz);
			if(null!=entity){
				arrayList.add(entity);
			}
		}
		return arrayList;
	}
	
	/**
	 * Retrieve the JavaBeans <code>PropertyDescriptors</code> for the given property.
	 * Delegate to spring's {@link org.springframework.beans.BeanUtils}.
	 * @param clazz the Class to retrieve the PropertyDescriptor for
	 * @param propertyName the name of the property
	 * @return the corresponding PropertyDescriptor, or <code>null</code> if none
	 * @throws BeansException if PropertyDescriptor lookup fails
	 */
	public static PropertyDescriptor getPropertyDescriptor(Class<?> clazz, String propertyName){
		return org.springframework.beans.BeanUtils.getPropertyDescriptor(clazz, propertyName);
	}

	
	/**
	 * Convenience method to fill a java bean property with specified map.
	 * @param source Source map.
	 * @param target Target java object.
	 * @param ignoreError True will throw {@link RuntimeException} if binding have error.
	 */
	public static final void  copyProperties(Map<String,Object> source,Object target,boolean ignoreError){
		
		PropertyValues pvs=resolver.resolve(source, target.getClass());
		
		DataBinder binder=new DataBinder(target);
		binder.bind(pvs);
		BindingResult result=binder.getBindingResult();
		if(result.hasErrors()){
			
			List<ObjectError> errors=result.getAllErrors();
			for(ObjectError error:errors){
				logger.error(error);
			}
			if(!ignoreError){
			throw new RuntimeException(result.getAllErrors().toString());
			}
		}
		
	}
	
	
	
	/**
	 * Convenience method to instantiate a class using its no-arg constructor.
	 * As this method doesn't try to load classes by name, it should avoid
	 * class-loading issues.
	 * @param clazz class to instantiate
	 * @return the new instance
	 * @throws BeanInstantiationException if the bean cannot be instantiated
	 */
	public static <T> T instantiate(Class<T> clazz) throws BeanInstantiationException {
		Assert.notNull(clazz, "Class must not be null");
		if (clazz.isInterface()) {
			throw new BeanInstantiationException(clazz, "Specified class is an interface");
		}
		try {
			return clazz.newInstance();
		}
		catch (InstantiationException ex) {
			throw new BeanInstantiationException(clazz, "Is it an abstract class?", ex);
		}
		catch (IllegalAccessException ex) {
			throw new BeanInstantiationException(clazz, "Is the constructor accessible?", ex);
		}
	}
	
	/**
	 * Convenience method set a java bean's property.
	 * @param source  The object to set.
	 * @param property  The property to set.
	 * @param value  The value to set.
	 * @throws BindException If bind error happen this exception will be thrown.
	 */
	public static  void setProperty(Object source,String property,Object value) throws BindException{
		if(source==null){
			return;
		}
		MutablePropertyValues  pvs=new MutablePropertyValues();
		pvs.add(property, value);
		DataBinder binder=new DataBinder(source);
		binder.bind(pvs);
		BindingResult result=binder.getBindingResult();
		if(result.hasErrors()){
			throw new BindException(result);
		}
	}
	
	//---------------------------------------------------Private Methods.
	
	
	private static final Map<String,Object>  toMap(Object object,Set<Object> convertContext,boolean flat,String [] ignoreProperties) {
		return toMap(null, object, convertContext, flat,ignoreProperties);
	}
	
	private static final Map<String,Object>  toMap(String key,Object object,Set<Object> convertContext,boolean flat,String [] ignoreProperties) {
		
		if(null==object){
			return null;
		}
		
		if(convertContext.contains(object)){
			return null;
		}
		Class<?> type=object.getClass();
		if(isSimpleValueType(type)){
			throw new RuntimeException("Simple type["+type.getName()+"] can not to map!");
		}
		convertContext.add(object);
		Map<String, Object> desc=new HashMap<String, Object>();
		PropertyDescriptor[] pds=org.springframework.beans.BeanUtils.getPropertyDescriptors(type);
		List<String> ignoreList=(ignoreProperties!=null)?Arrays.asList(ignoreProperties):null;
		
		for(PropertyDescriptor pd:pds){
			//ignore property.
			if(null!=ignoreProperties&&ignoreList.contains(pd.getName())){
				continue;
			}
			Method readMethod=pd.getReadMethod();
			if(readMethod!=null){
				//modify by zhangjun 20130515
				//在websphere上,会存在非public的方法，需要设置Accessible为true
				if(!readMethod.isAccessible()){
					readMethod.setAccessible(true);
				}
				desc.put(pd.getName(),ReflectionUtils.invokeMethod(readMethod, object));
			}
		}
		Map<String,Object> resultMap=new HashMap<String, Object>();
		
		for(String innerKey:desc.keySet()){
			Object value=desc.get(innerKey);
			if(flat){
				String subKey=(key==null)?innerKey:(key+PropertyAccessor.NESTED_PROPERTY_SEPARATOR+innerKey);
				Map<String,Object> flatMap=descFlat(subKey,value,convertContext,ignoreProperties);
				if(null!=flatMap){
					resultMap.putAll(flatMap);
				}
				
			}else{
				resultMap.put(innerKey,  desc(value, convertContext,ignoreProperties));	
			}
		}
		return resultMap;
	}
	
	
	
	private static Map<String,Object> descFlat(String key,Object object,Set<Object>  context,String [] ignoreProperties){
		Map<String,Object> result=new HashMap<String, Object>();
		if(null==object||context.contains(object)){
			result.put(key, null);
			return result;
		}
		Class<?> type=object.getClass();
		//if the target property type is simple value or array.
		if(isSimpleValueType(type)||type.isArray()){
			result.put(key, object);
			return result;
		}
		if(object instanceof Collection){
			@SuppressWarnings("unchecked")
			Collection<Object> coll=(Collection<Object>) object;
			List<Object> resultList=new ArrayList<Object>(coll.size());
			for(Object o:coll){
				Object value=descFlat(null,o,context,ignoreProperties);
				resultList.add(value);
			}
			result.put(key, resultList);
			return result;
			
		}else if (object instanceof Map){
			Map<?,?> mapValue=(Map<?, ?>) object;
			Map<String,Object> resultMap=new HashMap<String, Object>();
			for(Object innerKey:mapValue.keySet()){
				Object value=mapValue.get(innerKey);
				String nextKey=key+PropertyAccessor.NESTED_PROPERTY_SEPARATOR+innerKey;
				
				Map<String,Object> nestedDesc=descFlat(nextKey,value,context,ignoreProperties);
				resultMap.putAll(nestedDesc);
			}
			return resultMap;
		}//other type.
		else{
			return toMap(key,object,context,true,ignoreProperties);
		}
	}
	
	
	private static Object desc(Object object ,Set<Object> context,String [] ignoreProperties){
		if(null==object||context.contains(object)){
			return null;
		}
		Class<?> type=object.getClass();
		if(isSimpleValueType(type)||type.isArray()){
			return object;
		}
		
		if(object instanceof Collection){
			@SuppressWarnings("unchecked")
			Collection<Object> coll=(Collection<Object>) object;
			List<Object> resultList=new ArrayList<Object>(coll.size());
			for(Object o:coll){
				Object value=desc(o,context,ignoreProperties);
				resultList.add(value);
			}
			return resultList;
			
		}else if (object instanceof Map){
			Map<?,?> mapValue=(Map<?, ?>) object;
			Map<String,Object> resultMap=new HashMap<String, Object>();
			for(Object key:mapValue.keySet()){
				Object value=mapValue.get(key);
				resultMap.put(key.toString(),desc(value,context,ignoreProperties));
			}
			return resultMap;
		}//other type.
		else{
			return toMap(object,context,false,ignoreProperties);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String, Object> toFlatMap(Map<String, Object> map,Map<String, Object> keyValues){
		for (String key : map.keySet()) {
			if(map.get(key) instanceof Map<?, ?>){
				toFlatMap((Map<String, Object>) map.get(key), keyValues);
			}
			else{
				keyValues.put(key, map.get(key));
			}
		}
		return keyValues;
	}
}

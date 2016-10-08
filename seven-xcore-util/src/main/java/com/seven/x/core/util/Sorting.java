/**
 * @author hjs
 * 2007-3-13
 */
package com.seven.x.core.util;

import java.lang.reflect.Method;
import java.text.Collator;
import java.util.*;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 排序工具类
 * @author hjs
 * 2007-3-13
 */
public class Sorting {
	private static final Logger logger = LoggerFactory.getLogger(Sorting.class);
	
	/**
	 * 升序
	 */
	public static final int SORT_TYPE_ASC = 0; 
	
	/**
	 * 降序
	 */
	public static final int SORT_TYPE_DESC = 1; 
	
	//用于排序的field数组
//	private Object[] keyWordArray = null; 
	//用于全部field的排序类型
//	private int sortType = SORT_TYPE_ASC;
	//对应每个field的排序类型
//	private int[] sortTypeArray = null;
	
	private Sorting(){
	}
	
	/**
	 * 对成员为Map的List进行排序
	 * @param mapList：用于排序的map list
	 * @param keyWordArray：用于排序的key
	 * @param sortType：默认为ASC
	 * @throws Exception
	 */
	public static void sortMapList(List<Map<?, ?>> mapList, String[] keyWordArray, int sortType) throws Exception {
		if(keyWordArray!=null && keyWordArray.length>0){
			int[] sortTypeArray = new int[keyWordArray.length];
			Arrays.fill(sortTypeArray, sortType);
			sortMapList(mapList, keyWordArray, sortTypeArray);
		}
	}
	/**
	 * 对成员为Map的List进行排序
	 * @param mapList：用于排序的map list
	 * @param keyWordArray：用于排序的key
	 * @param sortTypeArray：默认为ASC
	 * @throws Exception
	 */
	public static void sortMapList(List<Map<?, ?>> mapList, String[] keyWordArray, int[] sortTypeArray) throws Exception {
		if(keyWordArray!=null && keyWordArray.length>0){
			if(sortTypeArray==null){//排序类型数组为空
				sortTypeArray = new int[keyWordArray.length];
				Arrays.fill(sortTypeArray, SORT_TYPE_ASC);
			} else if(sortTypeArray.length > keyWordArray.length){ //排序类型数组的个数超过field的个数
				//只截取需要的个数
				int[] tempArray = new int[keyWordArray.length];
				System.arraycopy(sortTypeArray, 0, tempArray, 0, tempArray.length);
				sortTypeArray = tempArray;
			} else if(sortTypeArray.length < keyWordArray.length){ //排序类型数组的个数小于field的个数
				//补充缺少的类型
				int[] tempArray = new int[keyWordArray.length];
				System.arraycopy(sortTypeArray, 0, tempArray, 0, sortTypeArray.length);
				Arrays.fill(tempArray, sortTypeArray.length, tempArray.length-1, SORT_TYPE_ASC);
			}
			Collections.sort(mapList, new Sorting().new MapComparator(keyWordArray, sortTypeArray));
		}
	}
	
	/**
	 * 对成员为Java Bean的List进行排序
	 * @param beanList：用于排序的bean list
	 * @param propertyArray：用于排序的property
	 * @param sortType：默认为ASC
	 * @throws Exception
	 */
	public static void sortBeanList(List<?> beanList, String[] propertyArray, int sortType) throws Exception {
		if(propertyArray!=null && propertyArray.length>0){
			int[] sortTypeArray = new int[propertyArray.length];
			Arrays.fill(sortTypeArray, sortType);
			sortBeanList(beanList, propertyArray, sortTypeArray);
		}
	}
	/**
	 * 对成员为Java Bean的List进行排序
	 * @param beanList：用于排序的bean list
	 * @param propertyArray：用于排序的property
	 * @param sortTypeArray：默认为ASC
	 * @throws Exception
	 */
	public static void sortBeanList(List<?> beanList, String[] propertyArray, int[] sortTypeArray) throws Exception {
		if(propertyArray!=null && propertyArray.length>0){
			if(sortTypeArray==null){//排序类型数组为空
				sortTypeArray = new int[propertyArray.length];
				Arrays.fill(sortTypeArray, SORT_TYPE_ASC);
			} else if(sortTypeArray.length > propertyArray.length){ //排序类型数组的个数超过field的个数
				//只截取需要的个数
				int[] tempArray = new int[propertyArray.length];
				System.arraycopy(sortTypeArray, 0, tempArray, 0, tempArray.length);
				sortTypeArray = tempArray;
			} else if(sortTypeArray.length < propertyArray.length){ //排序类型数组的个数小于field的个数
				//补充缺少的类型
				int[] tempArray = new int[propertyArray.length];
				System.arraycopy(sortTypeArray, 0, tempArray, 0, sortTypeArray.length);
				Arrays.fill(tempArray, sortTypeArray.length, tempArray.length-1, SORT_TYPE_ASC);
			}
			Collections.sort(beanList, new Sorting().new BeanComparator(propertyArray, sortTypeArray));
		}
	}
	
	private class MapComparator implements Comparator<Map<?, ?>> {
		//用于排序的field数组
		private Object[] keyWordArray = null; 
		//对应每个field的排序类型
		private int[] sortTypeArray = null;
		
		public MapComparator(Object[] keyWordArray, int[] sortTypeArray){
			this.keyWordArray = keyWordArray;
			this.sortTypeArray = sortTypeArray;
		}
		
		public int compare(Map<?, ?> row1, Map<?, ?> row2) {
//			Map row1= (Map) o1;
//			Map row2= (Map) o2;
			Object comparedFiled1 = null;
			Object comparedFiled2 = null;
			
			//取用于排序的key value
			for(int i=0; i<keyWordArray.length; i++) {
				//default result
				int result = 0;
				
				comparedFiled1 = row1.get(keyWordArray[i]);
				comparedFiled2 = row2.get(keyWordArray[i]);
				if (SORT_TYPE_DESC == sortTypeArray[i]) { //降序
					comparedFiled1 = row2.get(keyWordArray[i]);
					comparedFiled2 = row1.get(keyWordArray[i]);
				}
				
				//if compared object is null
				if(comparedFiled1==null && comparedFiled2==null){
					result = 0;
				} else if(comparedFiled1==null) {
					result = -1;
				} else if (comparedFiled2==null) {
					result = 1;
				} else {
					result = compareField(comparedFiled1, comparedFiled2);
				}
				if(result == 0){ //相等, 进入下一个key的比较
					continue;
				}
				//最终结果
				return result;
			}
			//默认结果
			return 0;
		}
	}
	
	private class BeanComparator implements Comparator<Object> {
		//用于排序的field数组
		private Object[] keyWordArray = null; 
		//对应每个field的排序类型
		private int[] sortTypeArray = null;
		
		public BeanComparator(Object[] keyWordArray, int[] sortTypeArray){
			this.keyWordArray = keyWordArray;
			this.sortTypeArray = sortTypeArray;
		}
		public int compare(Object o1, Object o2) {
			Object comparedFiled1 = null;
			Object comparedFiled2 = null;
			
			//取用于排序的key value
			for(int i=0; i<keyWordArray.length; i++) {
				//default result
				int result = 0;
				
				try {
					comparedFiled1 = BeanUtils.getProperty(o1, (String) keyWordArray[i]);
					comparedFiled2 = BeanUtils.getProperty(o2, (String) keyWordArray[i]);
					if (SORT_TYPE_DESC == sortTypeArray[i]) { //降序
						comparedFiled2 = BeanUtils.getProperty(o1, (String) keyWordArray[i]);
						comparedFiled1 = BeanUtils.getProperty(o2, (String) keyWordArray[i]);
					}
				} catch (Exception e) {
					logger.error("BeanComparator.compare() error", e);
				}
				
				//if compared object is null
				if(comparedFiled1==null && comparedFiled2==null){
					result = 0;
				} else if(comparedFiled1==null) {
					result = -1;
				} else if (comparedFiled2==null) {
					result = 1;
				} else {
					result = compareField(comparedFiled1, comparedFiled2);
				}
				if(result == 0){ //相等, 进入下一个key的比较
					continue;
				}
				//最终结果
				return result;
			}
			//默认结果
			return 0;
		}
	}
	
	private int compareField(Object o1, Object o2) {
		Integer result = new Integer(0);
		if(o1 instanceof String) {
			Comparator<Object> chineseComparator = Collator.getInstance(Locale.CHINESE);
			result = new Integer(chineseComparator.compare(o1, o2));
		} else {
			try {
				Method comparedMethod = o1.getClass().getMethod("compareTo", new Class[]{o1.getClass()});
				result = (Integer) comparedMethod.invoke(o1, new Object[]{o2});
			} catch (Exception e) {
				logger.error("Not supported filed type[" + o1.getClass().getName() + "]", e);
			}
		}
		return result.intValue();
	}

	/**
	 * test
	 * @param args
	 */
	public static void main(String[] args) {
		Calendar cal = Calendar.getInstance();
		Date d1 = cal.getTime();
		cal.add(Calendar.DATE, -1);
		Date d2 = cal.getTime();
		cal.add(Calendar.YEAR, -1);
		Date d3 = cal.getTime();
		List list1 = new ArrayList();
		
		Map row1 = new HashMap();
		row1.put("MATURITY_DATE", "060815");
		row1.put("CURRENCY_CODE", "003");
		row1.put("ACCOUNT_NO", "601428618101");
		row1.put("DATE", d1);
		row1.put("NAME", "张汕");
		list1.add(row1);
		
		Map row2 = new HashMap();
		row2.put("MATURITY_DATE", "070122");
		row2.put("CURRENCY_CODE", "004");
		row2.put("ACCOUNT_NO", "901829618102");
		row2.put("DATE", d2);
		row2.put("NAME", "张山");
		list1.add(row2);
		
		Map row3 = new HashMap();
		row3.put("MATURITY_DATE", "060815");
		row3.put("CURRENCY_CODE", "003");
		row3.put("ACCOUNT_NO", "601428618101");
		row3.put("DATE", d3);
		row3.put("NAME", "张山");
		list1.add(row3);
		
		try {
			System.out.println("Original:");
//			Utils.printMapList(list1, 0);
			
			System.out.println("\nSort by [NAME], [DATE], [ACCOUNT_NO]:");
			Sorting.sortMapList(list1,
					new String[]{"NAME", "DATE", "ACCOUNT_NO"}, Sorting.SORT_TYPE_ASC);
//			Utils.printMapList(list1, 0);

			System.out.println("\nSORT_TYPE_DESC:");
			Sorting.sortMapList(list1,
					new String[]{"NAME", "DATE", "ACCOUNT_NO"}, Sorting.SORT_TYPE_DESC);
//			Utils.printMapList(list1, 0);
			Comparator c = Collator.getInstance(Locale.CHINESE);
			System.out.println(c.compare("憋", "别"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

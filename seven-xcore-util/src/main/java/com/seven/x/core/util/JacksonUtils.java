package com.seven.x.core.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 扩展JUMP默认的JSON数据处理工具类
 * 使其支持将日期时间类型转换成默认的格式
 * @author yan.jsh
 * 2015-1-15
 *
 */
public class JacksonUtils {
	
	private static final Logger log = LoggerFactory.getLogger(JacksonUtils.class);
	
	private static final ObjectMapper objectMapper = new ObjectMapper();

	static {
		ExSimpleDateFormat df = new ExSimpleDateFormat();
		objectMapper.getSerializationConfig().setDateFormat(df); 
		objectMapper.getDeserializationConfig().set(DeserializationConfig.Feature.USE_BIG_DECIMAL_FOR_FLOATS, true);
	}

	/**
	 * 将对象转换成JSON格式报文
	 * @param obj 源对象
	 * @param encoding 编码
	 * @return
	 */
	public static byte[] jsonFromObject(Object obj, String encoding) {
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		JsonGenerator generator = null;
		
		try {
			generator = objectMapper.getJsonFactory().createJsonGenerator(baos, JsonEncoding.valueOf(encoding));
			generator.writeObject(obj);
			generator.flush();
		
		} catch (RuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			log.error("Unable to serialize to json: " + obj, ex);
			return null;
		} finally {
			if (generator != null)
				try {
					generator.close();
				} catch (IOException ex) {
				}
		}
		return baos.toByteArray();
	}

	/**
	 * 将对象转换成JSON格式报文
	 * @param obj 源对象
	 * @param encoding 编码
	 * @return
	 */
	public static String jsonFromObject(Object obj) {
		byte[] bytes = jsonFromObject(obj,"UTF8");
		try {
			return new String(bytes,"UTF8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 将JSON数据流转换成对象
	 * @param inputStream JSON数据流
	 * @param type 目标对象类型
	 * @return
	 */
	public static <T> T objectFromJson(InputStream inputStream, Class<T> type) {
		
		JsonParser parser = null;
		
		T t;
		
		try {
			
			parser = objectMapper.getJsonFactory().createJsonParser(inputStream);
			t = parser.readValueAs(type);
			
		} catch (RuntimeException ex) {
			log.error("Runtime exception during deserializing " + type.getSimpleName());
			throw ex;
			
		} catch (Exception ex) {
			log.error("Exception during deserializing " + type.getSimpleName());
			return null;
			
		} finally {
			if (parser != null)
				try {
					parser.close();
				} catch (IOException ex) {
				}
		}
		return t;
	}

	/**
	 * 将JSON字符串转换成对象
	 * @param json JSON字符串
	 * @param type 目标对象类型
	 * @return
	 */
	public static <T> T objectFromJson(String json, Class<T> type) {
		
		JsonParser parser = null;
		
		T t;
		
		try {
			parser = objectMapper.getJsonFactory().createJsonParser(json);
			t = parser.readValueAs(type);
			
		} catch (RuntimeException ex) {
			log.error("Runtime exception during deserializing " + type.getSimpleName() + " from " + substring(json, 80));
			throw ex;
			
		} catch (Exception ex) {
			log.error("Exception during deserializing " + type.getSimpleName() + " from " + substring(json, 80));
			return null;
			
		} finally {
			if (parser != null)
				try {
					parser.close();
				} catch (IOException ex) {
				}
		}
		return t;
	}

	static String substring(String json, int length) {
		if ((json == null) || (json.length() < length))
			return json;
		return json.substring(0, length) + " ...";
	}
	
	public static <T> T  toBean(String json, Class<T> type){
		try {
			return objectMapper.readValue(json, type);
		} catch (Exception e) {
			log.error("Exception during transform to bean. ", e);
		}
		
		return null;
	}
	
	public static <T> T  toListBean(String json, Class<T> type){
		try {
			JavaType javaType = objectMapper.getTypeFactory().constructParametricType(List.class, type); 
			return objectMapper.readValue(json, javaType);
		} catch (Exception e) {
			log.error("Exception during transform to list bean. ", e);
		}
		
		return null;
	}
	
	public static <T> T toListBean(String json, TypeReference<T> type){
		try {
			return objectMapper.readValue(json, type);
		} catch (Exception e) {
			log.error("Exception during transform to list bean. ", e);
		}
		return null;
	}
	
	public static void main(String[] args) {
		Map<String, Object> map = new HashMap<String,Object>();
		map.put("aa", "11");
		map.put("bb", "中文");
		map.put("cc", new Date());
		map.put("dd", new java.sql.Date(System.currentTimeMillis()));
		map.put("ee", new java.sql.Time(System.currentTimeMillis()));
		System.out.println(JacksonUtils.jsonFromObject(map));
	}

}

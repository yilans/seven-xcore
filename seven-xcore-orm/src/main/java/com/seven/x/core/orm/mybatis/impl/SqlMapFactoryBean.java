package com.seven.x.core.orm.mybatis.impl;

import com.seven.x.core.orm.mybatis.SqlMap;
import com.seven.x.core.orm.mybatis.support.MyBatisExceptionTranslator;
import com.seven.x.core.orm.mybatis.support.SpringManagedTransactionFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.NestedIOException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.util.ObjectUtils;

public class SqlMapFactoryBean implements FactoryBean<SqlMap>, InitializingBean {
	
	private DataSource dataSource;
	
	private Resource configLocation;
	
	private Resource[] mappingLocations;
	
	private Properties properties;
	
	private String environment = SqlMapFactoryBean.class.getName();

	private String encoding = "UTF-8";
	
	private SqlSessionFactory sqlSessionFactory;
	
	private TransactionFactory transactionFactory;
	
	private SqlMap sqlMap;
	
	private SqlSessionFactoryBuilder sqlSessionFactoryBuilder = new SqlSessionFactoryBuilder();
	
	private PersistenceExceptionTranslator persistenceExceptionTranslator;

	public void setConfigLocation(Resource resource) {
		this.configLocation = resource;
	}

	public void setMappingLocations(Resource[] resources) {
		this.mappingLocations = resources;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void afterPropertiesSet() throws Exception {
		this.sqlMap = buildSqlMap(this.configLocation, this.mappingLocations, this.properties, this.environment);
	}

//	@SuppressWarnings("deprecation")
	protected SqlMap buildSqlMap(Resource configLocation, Resource[] mappingLocations, Properties properties, String environment) throws IOException {
		if (configLocation == null)
			throw new IllegalArgumentException("'configLocation' is required");
		
		XMLConfigBuilder builder;
		Configuration configuration;
		
		try {
			InputStreamReader reader = new InputStreamReader(configLocation.getInputStream(), this.encoding);
			builder = new XMLConfigBuilder(reader, null, properties);
			
			configuration = builder.parse();
			
		} catch (Exception e) {
			throw new NestedIOException("Failed to parse config resource: " + configLocation, e);
		} finally {
			ErrorContext.instance().reset();
		}

		if (this.transactionFactory == null) {
//			this.transactionFactory = new SpringManagedTransactionFactory(this.dataSource); marked by hjs 2012-12-21
			this.transactionFactory = new SpringManagedTransactionFactory();
		}

		Environment _environment = new Environment(this.environment, this.transactionFactory, this.dataSource);

		configuration.setEnvironment(_environment);

		if (!ObjectUtils.isEmpty(mappingLocations)) {
			for (Resource resource : mappingLocations) {
				
				if (resource != null) {
					String path;
					if ((resource instanceof ClassPathResource))
						path = ((ClassPathResource) resource).getPath();
					else {
						path = resource.toString();
					}
					try {
						InputStreamReader reader = new InputStreamReader(resource.getInputStream(), this.encoding);

						XMLMapperBuilder mapperBuilder = new XMLMapperBuilder(reader, configuration, path, configuration.getSqlFragments());
						
						mapperBuilder.parse();
					
					} catch (Exception e) {
						throw new NestedIOException("Failed to parse mapping resource: '" + resource + "'", e);
					} finally {
						ErrorContext.instance().reset();
					}
				}
			}
		}

		this.sqlSessionFactory = this.sqlSessionFactoryBuilder.build(configuration);

		if (this.persistenceExceptionTranslator == null) {
			this.persistenceExceptionTranslator = new MyBatisExceptionTranslator(this.sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(), true);
		}

		return new SqlMapImpl(this.dataSource, this.sqlSessionFactory, this.persistenceExceptionTranslator);
	}

	public SqlMap getObject() {
		return this.sqlMap;
	}

	public Class<? extends SqlMap> getObjectType() {
		return this.sqlMap != null ? this.sqlMap.getClass() : SqlMap.class;
	}

	public boolean isSingleton() {
		return true;
	}

}

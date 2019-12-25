package com.dili.ss.datasource.aop;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.dili.ss.constant.SsConstants;
import com.dili.ss.datasource.*;
import com.dili.ss.datasource.selector.RoundRobinSelector;
import com.dili.ss.datasource.selector.WeightedRoundRobinSelector;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.util.text.BasicTextEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

//import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
//import org.springframework.boot.bind.RelaxedDataBinder;
//import org.springframework.boot.bind.RelaxedPropertyResolver;

/**
 * 动态数据源注册<br/>
 * 启动动态数据源请在启动类中（如SpringBootApplication）
 * 添加 @Import(DynamicDataSourceRegister.class)
 * Created by asiamaster on 2017/8/8 0008.
 */
public class DynamicRoutingDataSourceRegister implements ImportBeanDefinitionRegistrar, EnvironmentAware {

	private static final Logger logger = LoggerFactory.getLogger(DynamicRoutingDataSourceRegister.class);

	// 如配置文件中未指定数据源类型，使用该默认值
//	org.apache.tomcat.jdbc.pool.DataSource
	private static final Object DATASOURCE_TYPE_DEFAULT = "com.alibaba.druid.pool.DruidDataSource";

	// 数据源
	private DataSource defaultDataSource;
	private Map<String, DataSource> customDataSources = new HashMap<>();

	@Autowired
	PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer;

	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
		Map<Object, Object> targetDataSources = new HashMap<Object, Object>();
		// 将主数据源添加到更多数据源中
		targetDataSources.put("dataSource", defaultDataSource);
		DynamicRoutingDataSourceContextHolder.dataSourceIds.add(SwitchDataSource.DEFAULT_DATASOURCE);
		// 添加更多数据源
		targetDataSources.putAll(customDataSources);
		for (String key : customDataSources.keySet()) {
			DynamicRoutingDataSourceContextHolder.dataSourceIds.add(key);
		}
		// 创建DynamicRoutingDataSource
		GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
		if(DataSourceManager.switchMode.equals(SwitchMode.MULTI)) {
			beanDefinition.setBeanClass(DynamicRoutingDataSource.class);
		}else {
			beanDefinition.setBeanClass(DynamicSelectorDataSource.class);
		}
		beanDefinition.setSynthetic(true);
		MutablePropertyValues mpv = beanDefinition.getPropertyValues();
		mpv.addPropertyValue("defaultTargetDataSource", defaultDataSource);
		mpv.addPropertyValue("targetDataSources", targetDataSources);
		if(DataSourceManager.switchMode.equals(SwitchMode.MASTER_SLAVE)) {
			if(DataSourceManager.selectorMode.equals(SelectorMode.ROUND_ROBIN)) {
				mpv.addPropertyValue("dataSourceSelector", new RoundRobinSelector());
			}else{
				mpv.addPropertyValue("dataSourceSelector", new WeightedRoundRobinSelector());
			}
		}
		registry.registerBeanDefinition("dataSource", beanDefinition);
		logger.info("DynamicRoutingDataSource Registry");
	}

	/**
	 * 加载多数据源配置
	 */
	@Override
	public void setEnvironment(Environment env) {
		initDefaultDataSource(env);
		initCustomDataSources(env);
	}

	/**
	 * 初始化主数据源
	 */
	private void initDefaultDataSource(Environment env) {
		// 读取主数据源
//		RelaxedPropertyResolver propertyResolver = new RelaxedPropertyResolver(env, "spring.datasource.");
		Map<String, Object> dsMap = new HashMap<>();
		//初始化数据源切换模式，默认为1(多数据源)
		DataSourceManager.switchMode = SwitchMode.getSwitchModeByCode(env.getProperty("spring.datasource.switch-mode", "1"));
		if(SwitchMode.MASTER_SLAVE.equals(DataSourceManager.switchMode)) {
			//负载均衡模式，默认为轮询
			DataSourceManager.selectorMode = SelectorMode.getSelectorModeByCode(env.getProperty("spring.datasource.selector-mode", "1"));
		}
		dsMap.put("type", env.getProperty("spring.datasource.type"));
		dsMap.put("driver-class-name", env.getProperty("spring.datasource.driver-class-name"));
		dsMap.put("url", env.getProperty("spring.datasource.url"));
		dsMap.put("username", env.getProperty("spring.datasource.username"));
		dsMap.put("password", env.getProperty("spring.datasource.password"));
		defaultDataSource = buildDataSource(dsMap, env);
	}

	/**
	 * 初始化更多数据源
	 */
	private void initCustomDataSources(Environment env) {
		// 读取配置文件获取更多数据源，也可以通过defaultDataSource读取数据库获取更多数据源
//		RelaxedPropertyResolver propertyResolver = new RelaxedPropertyResolver(env, "spring.datasource.");
		String dsPrefixs = env.getProperty("spring.datasource.names");
		for (String dsPrefix : dsPrefixs.split(",")) {// 多个数据源
			Map<String, Object> dsMap = new HashMap<>();
			dsMap.put("driver-class-name", env.getProperty("spring.datasource."+dsPrefix+".driver-class-name"));
			dsMap.put("url", env.getProperty("spring.datasource."+dsPrefix+".url"));
			dsMap.put("username", env.getProperty("spring.datasource."+dsPrefix+".username"));
			dsMap.put("password", env.getProperty("spring.datasource."+dsPrefix+".password"));
			DataSource ds = buildDataSource(dsMap, env);
			customDataSources.put(dsPrefix, ds);
			//主从数据源需要初始化DataSourceManager的数据
			if(SwitchMode.MASTER_SLAVE.equals(DataSourceManager.switchMode)){
				DataSourceManager.slaves.add(dsPrefix);
				Object weightObj = dsMap.get("weight");
				String weightStr = weightObj == null ? "1" : weightObj.toString();
				DataSourceManager.weights.put(dsPrefix, Integer.parseInt(weightStr));
			}
		}
	}

	/**
	 * 创建DataSource
	 *
	 * @param dsMap(type, driverClassName, url, username, password) 数据源的单独配置
	 * @param env	统一配置从环境中取
	 * @return
	 */
	private DataSource buildDataSource(Map<String, Object> dsMap, Environment env) {
		try {
			Object type = dsMap.get("type");
			if (type == null) {
				type = DATASOURCE_TYPE_DEFAULT;// 默认DataSource
			}
			if(!type.equals(DATASOURCE_TYPE_DEFAULT)){
				throw new RuntimeException("暂不支持非DruidDataSource数据源!");
			}
			Class<? extends DataSource> dataSourceType;
			dataSourceType = (Class<? extends DataSource>) Class.forName((String) type);
			String driverClassName = dsMap.get("driver-class-name").toString();
			String url = dsMap.get("url").toString();
			String username = decrypt(dsMap.get("username").toString());
			String password = decrypt(dsMap.get("password").toString());
//			DataSourceBuilder factory = DataSourceBuilder.create().driverClassName(driverClassName).url(url)
//					.username(username).password(password).type(dataSourceType);
//			DataSource dataSource =  factory.build();
//			dataBinder(dataSource, env);
			Binder binder = Binder.get(env);
			Properties datasourceProp = binder.bind("spring.datasource", Bindable.of(Properties.class)).get();
			datasourceProp.putAll(dsMap);
			return DruidDataSourceFactory.createDataSource(datasourceProp);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 配置属性解密
	 * @param value
	 * @return
	 */
	private String decrypt(String value){
		if(StringUtils.isBlank(value)) {
			return value;
		}
		if(value.startsWith("ENC(") && value.endsWith(")")){
			BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
			textEncryptor.setPassword(SsConstants.ENCRYPT_PROPERTY_PASSWORD);
			return textEncryptor.decrypt(value.substring(4, value.length()-1));
		}else {
			return value;
		}
	}

	/**
	 * 为DataSource绑定更多数据
	 *
	 * @param dataSource
	 * @param env 统一配置从环境中取
	 */
	private void dataBinder(DataSource dataSource, Environment env){
		//记录spring.datasource.*除type,driver-class-name,url, username和password外的通用属性，用于DataSource属性绑定
//		PropertyValues dataSourcePropertyValues;

//		RelaxedDataBinder dataBinder = new RelaxedDataBinder(dataSource);
//		//dataBinder.setValidator(new LocalValidatorFactory().run(this.applicationContext));
//		dataBinder.setConversionService(conversionService);
//		dataBinder.setIgnoreNestedProperties(false);//false
//		dataBinder.setIgnoreInvalidFields(false);//false
//		dataBinder.setIgnoreUnknownFields(true);//true
//		Map<String, Object> rpr = new RelaxedPropertyResolver(env, "spring.datasource").getSubProperties(".");
//		Map<String, Object> values = new HashMap<>(rpr);
//		// 排除已经设置的属性
//		values.remove("type");
//		values.remove("driver-class-name");
//		values.remove("url");
//		values.remove("username");
//		values.remove("password");
//			dataSourcePropertyValues = new MutablePropertyValues(values);
//		dataBinder.bind(dataSourcePropertyValues);

//		Binder binder = Binder.get(env);
//		Properties datasourceProp = binder.bind("spring.datasource", Bindable.of(Properties.class)).get();
//		// 排除已经设置的属性
//		datasourceProp.remove("type");
//		datasourceProp.remove("driver-class-name");
//		datasourceProp.remove("url");
//		datasourceProp.remove("username");
//		datasourceProp.remove("password");
//		try {
//			DruidDataSourceFactory.createDataSource(datasourceProp);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		Class<? extends DataSource> dsClazz = dataSource.getClass();




	}




}
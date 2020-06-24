package com.dili.ss.mvc.boot;

import com.dili.ss.mvc.converter.JsonHttpMessageConverter;
import com.dili.ss.util.SpringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 配置csrfInterceptor.enable=true启用CSRF攻击拦截<br></>
 * 拦截路径配置:CSRFInterceptor.path和CSRFInterceptor.excludePaths
 * Created by asiamaster on 2017/6/19 0019.
 */
@Configuration
@ConditionalOnExpression("'${web.enable}'=='true'")
//@EnableWebMvc //不能使用@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

	@Value("${web.instanceResolver:false}")
	private Boolean instanceResolver;
	@Autowired
	public Environment env;

	@Bean
	public Converter<String, Date> addDateConvert() {
		return new Converter<String, Date>() {
			@Override
			public Date convert(String source) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date date = null;
				try {
					date = sdf.parse(source);
				} catch (ParseException e) {
					sdf = new SimpleDateFormat("yyyy-MM-dd");
					try {
						date = sdf.parse(source);
					} catch (ParseException e1) {
						e1.printStackTrace();
					}
				}
				return date;
			}
		};
	}

	@Bean
	public Converter<String, LocalDate> addLocalDateConvert() {
		return new Converter<String, LocalDate>() {
			@Override
			public LocalDate convert(String source) {
				return LocalDate.parse(source, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			}
		};
	}

	@Bean
	public Converter<String, LocalDateTime> addLocalDateTimeConvert() {
		return new Converter<String, LocalDateTime>() {
			@Override
			public LocalDateTime convert(String source) {
				return LocalDateTime.parse(source, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
			}
		};
	}

	@Bean
	public Converter<String, Instant> addInstantConvert() {
		return new Converter<String, Instant>() {
			@Override
			public Instant convert(String source) {
				try {
					//毫秒数转为Instant
					return Instant.ofEpochMilli(Long.parseLong(source));
				} catch (NumberFormatException e) {
					//转换失败直接抛运行时异常
					return Instant.from(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault()).parse(source));
				}
			}
		};
	}

//	如果加了@EnableWebMvc 注解，只能自己添加添加加载处理了,还要注意是否需要添加webJars，所以最好不要添加@EnableWebMvc注解
//	增加@EnableWebMvc注解以后WebMvcAutoConfiguration中配置就不会生效
//	@Override
//	public void addResourceHandlers(ResourceHandlerRegistry registry) {
//		registry.addResourceHandler("/**").addResourceLocations("/static/**");
//	}

	@Override
	public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
		Iterator it =  converters.iterator();
		int index = -1;
		int tmp = 0;
		while (it.hasNext()){
			Object obj = it.next();
			//去掉MappingJackson2HttpMessageConverter
			if(obj instanceof MappingJackson2HttpMessageConverter ){
				it.remove();
				index = tmp;
			}
			if(index == -1) {
				tmp++;
			}
		}
		JsonHttpMessageConverter fastJsonHttpMessageConverter = new JsonHttpMessageConverter();
		List<MediaType> supportedMediaTypes = new ArrayList<>();
		supportedMediaTypes.add(MediaType.APPLICATION_JSON);
		supportedMediaTypes.add(MediaType.APPLICATION_JSON_UTF8);
		supportedMediaTypes.add(MediaType.TEXT_PLAIN);
		fastJsonHttpMessageConverter.setSupportedMediaTypes(supportedMediaTypes);
//		String dateFormat = env.getProperty("spring.fastjson.date-format");
//		if(StringUtils.isNotBlank(dateFormat)){
//			fastJsonHttpMessageConverter.setDateFormat(dateFormat);
//		}
		//将FastJsonHttpMessageConverter加在MappingJackson2HttpMessageConverter的位置，比xml解析器靠前，以提升性能
		if(index != -1){
			converters.add(index, fastJsonHttpMessageConverter);
		}
		//如果没有MappingJackson2HttpMessageConverter，还是要在最后加上FastJsonHttpMessageConverter
		else {
			converters.add(fastJsonHttpMessageConverter);
		}
	}

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		if(instanceResolver){
			argumentResolvers.add(new DTOInstArgumentResolver());
		}else {
			argumentResolvers.add(new DTOArgumentResolver());
		}
	}

//	@Override
//	public void addInterceptors(InterceptorRegistry registry) {
//		registry.addInterceptor(tokenInterceptor).addPathPatterns("/idempotent/**");
//	}

	/**
	 * 自定义返回类型
	 * @return
	 */
//	@Bean
//	public ResponseBodyWrapFactoryBean getResponseBodyWrap() {
//		return new ResponseBodyWrapFactoryBean();
//	}


	/**
	 * 统一错误页面处理
	 * 配置:
	 * error.page.default=error/default (错误页面的controller返回地址)
	 * error.page.indexPage=http://crm.diligrp.com:8085/crm/index.html (返回首页地址)
	 *
	 */
	@Bean
	@ConditionalOnExpression("'${exceptionResolver.enable}'=='true'")
	public SimpleMappingExceptionResolver simpleMappingExceptionResolver(){
		SimpleMappingExceptionResolver simpleMappingExceptionResolver = new SimpleMappingExceptionResolver();
//		定义默认的异常处理页面
		simpleMappingExceptionResolver.setDefaultErrorView("error/default");
//		定义异常处理页面用来获取异常信息的变量名，如果不添加exceptionAttribute属性，则默认为exception
		simpleMappingExceptionResolver.setExceptionAttribute("exception");
//		定义需要特殊处理的异常，用类名或完全路径名作为key，异常页面名作为值
		Properties mappings = new Properties();
		mappings.put("java.lang.RuntimeException", SpringUtil.getProperty("error.page.default", "error/default"));
		mappings.put("java.lang.Exception", SpringUtil.getProperty("error.page.default", "error/default"));
		mappings.put("java.lang.Throwable", SpringUtil.getProperty("error.page.default", "error/default"));
		simpleMappingExceptionResolver.setExceptionMappings(mappings);
		return simpleMappingExceptionResolver;
	}

	/**
	 * Springboot的异步线程池
	 * @param configurer
	 */
	@Override
	public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		//核心线程数10：线程池创建时候初始化的线程数
		executor.setCorePoolSize(10);
		//最大线程数20：线程池最大的线程数，只有在缓冲队列满了之后才会申请超过核心线程数的线程
		executor.setMaxPoolSize(20);
		//缓冲队列200：用来缓冲执行任务的队列
		executor.setQueueCapacity(200);
		//允许线程的空闲时间60秒：当超过了核心线程出之外的线程在空闲时间到达之后会被销毁
		executor.setKeepAliveSeconds(60);
		//线程池名的前缀：设置好了之后可以方便我们定位处理任务所在的线程池
		executor.setThreadNamePrefix("taskExecutor-");
		//线程池对拒绝任务的处理策略：这里采用了CallerRunsPolicy策略，当线程池没有处理能力的时候，该策略会直接在 execute 方法的调用线程中运行被拒绝的任务；如果执行程序已关闭，则会丢弃该任务
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		//setWaitForTasksToCompleteOnShutdown（true）该方法就是这里的关键，用来设置线程池关闭的时候等待所有任务都完成再继续销毁其他的Bean，这样这些异步任务的销毁就会先于Redis线程池的销毁。
		executor.setWaitForTasksToCompleteOnShutdown(true);
		//同时，这里还设置了setAwaitTerminationSeconds(60)，该方法用来设置线程池中任务的等待时间，如果超过这个时候还没有销毁就强制销毁，以确保应用最后能够被关闭，而不是阻塞住。
		executor.setAwaitTerminationSeconds(60);
	}

//	@Override
//	public void addFormatters(FormatterRegistry registry) {
//		registry.addFormatterForFieldAnnotation(new Cent2YuanAnnotationFormatterFactory());
//	}

	public Boolean getInstanceResolver() {
		return instanceResolver;
	}

	public void setInstanceResolver(Boolean instanceResolver) {
		this.instanceResolver = instanceResolver;
	}
}

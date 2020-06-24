package com.dili.ss.util;

/**
 * Created by asiamastor on 2017/1/22.
 */

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.Map;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SpringUtil implements ApplicationContextAware, EnvironmentAware {

    private static ApplicationContext applicationContext = null;
    private static Environment environment;

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public ApplicationContext applicationContext() {
        return applicationContext;
    }

    public static Environment getEnvironment() {
        return environment;
    }

    //获取applicationContext

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (SpringUtil.applicationContext == null) {
            SpringUtil.applicationContext = applicationContext;
        }
        System.out.println("---------------SpringUtil------------------------------------------------------");
        System.out.println("ApplicationContext配置成功,在普通类可以通过调用SpringUtils.getAppContext()获取applicationContext对象,applicationContext=" + SpringUtil.applicationContext);
        System.out.println("--------------------------------------------------------------------------------");
    }


    //通过name获取 Bean.
    public static Object getBean(String name) {
        if(getApplicationContext() != null) {
            return getApplicationContext().getBean(name);
        }else{
            return null;
        }
    }

    /**
     * 获取某一类的所有的bean
     * includeNonSingletons和allowEagerInit默认为true
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> Map<String, T> getBeansOfType(Class<T> clazz){
        return getApplicationContext().getBeansOfType(clazz);
    }

    /**
     * 获取某一类的所有的bean
     * @param clazz
     * @param includeNonSingletons 第一代表是否也包含原型（Class祖先）bean或者或者只是singletons（包含FactoryBean生成的）
     * @param allowEagerInit 第二个表示是否立即实例化懒加载或者由FactoryBean生成的Bean以保证依赖关系
     * @param <T>
     * @return
     */
    public static <T> Map<String, T> getBeansOfType(Class<T> clazz, Boolean includeNonSingletons, boolean allowEagerInit){
        return getApplicationContext().getBeansOfType(clazz, includeNonSingletons , allowEagerInit);
    }

    //获取泛型Bean
    public static <T> T getGenericBean(String name) {
        return (T) getApplicationContext().getBean(name);
    }
    //通过class获取Bean.
    public static <T> T getBean(Class<T> clazz) {
        return getApplicationContext().getBean(clazz);
    }


    //通过name,以及Clazz返回指定的Bean
    public static <T> T getBean(String name, Class<T> clazz) {
        return getApplicationContext().getBean(name, clazz);
    }

    public static Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType) throws BeansException{
        return getApplicationContext().getBeansWithAnnotation(annotationType);
    }


    @Override
    public void setEnvironment(Environment environment) {
        if (SpringUtil.environment == null) {
            SpringUtil.environment = environment;
        }
    }

    public static String getProperty(String key){
        return getEnvironment().getProperty(key);
    }

    public static String getProperty(String key, String defaultValue){
        return getEnvironment().getProperty(key, defaultValue);
    }


}
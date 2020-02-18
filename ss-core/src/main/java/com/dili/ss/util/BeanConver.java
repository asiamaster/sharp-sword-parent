package com.dili.ss.util;


import com.dili.ss.domain.BasePage;
import com.dili.ss.domain.BaseQuery;
import com.dili.ss.dto.DTOUtils;
import com.dili.ss.exception.AppException;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.beans.BeanCopier;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by asiamastor on 2017/1/9.
 * 处理JavaBean和JavaBean转Map相关的逻辑
 */
public class BeanConver {
    private final static Logger LOG = LoggerFactory.getLogger(BeanConver.class);

    public static Map<String,BeanCopier> beanCopierMap = new HashMap<String, BeanCopier>();

    /**
     * 实例类转换
     * 支持DTO、DTOInstance和javaBean转为javaBean
     * 不支持转为DTO
     * @param source 源对象
     * @param target 目标对象
     * @param <T> 源对象
     * @param <K> 目标对象
     */
    public static<T,K> K copyBean(T source, Class<K> target){
        if (source == null) {
            return null;
        }
        String beanKey = generateKey(source.getClass(), target);
        BeanCopier copier = null;
        if (!beanCopierMap.containsKey(beanKey)) {
            copier = BeanCopier.create(source.getClass(), target, false);
            beanCopierMap.put(beanKey, copier);
        } else {
            copier = beanCopierMap.get(beanKey);
        }
        K result = null;
        try {
            result = (K)target.newInstance();
        } catch (Exception e) {
            LOG.error("实例转换出错");
        }
        copier.copy(source,result,null);
        return result;
    }

    /**
     * 拷贝Map到javaBean或DTOInstance
     * @param map
     * @param beanClass javaBean类或DTOInstance接口类
     * @param <T>
     * @return
     */
    public static <T> T copyMap(Map<String, Object> map, Class<T> beanClass){
        if (map == null) {
            return null;
        }
        Object obj = null;
        try {
            obj = beanClass.isInterface() ? DTOUtils.newInstance((Class)beanClass) : beanClass.newInstance();
            Field[] fields = obj.getClass().getDeclaredFields();
            for (Field field : fields) {
                int mod = field.getModifiers();
                if(Modifier.isStatic(mod) || Modifier.isFinal(mod)){
                    continue;
                }
                field.setAccessible(true);
                field.set(obj, map.get(field.getName()));
            }
        } catch (InstantiationException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        }
        return (T)obj;
    }

    /**
     * page实例类转换
     * @param <T> 源对象
     * @param <K> 目标对象
     * @param source 源对象
     * @param target 目标对象
     * @return
     */
    public static <T,K> List<K> copyList(List<T> source, Class<K> target){
        List<K> list = new ArrayList<K>();
        if(CollectionUtils.isEmpty(source)){
            return new ArrayList<>();
        }
        String beanKey = generateKey(source.get(0).getClass(), target);
        BeanCopier copier = null;
        if (!beanCopierMap.containsKey(beanKey)) {
            copier = BeanCopier.create(source.get(0).getClass(), target, false);
            beanCopierMap.put(beanKey, copier);
        } else {
            copier = beanCopierMap.get(beanKey);
        }
        for(T af : source){
            K af1 = null;
            try {
                af1 = (K)target.newInstance();
            } catch (Exception e) {
                LOG.error("实例转换出错");
            }
            copier.copy(af,af1,null);
            list.add(af1);
        }
        return list;
    }

    /**
     * 把javaBean、Instance或dto对象转换为Map键值对
     *
     * @param bean or dto
     * @param type 转换的类型
     * @param recursive 向上递归
     * @return
     * @throws Exception
     */
    public static Map<String, Object> transformObjectToMap(Object bean, Class<?> type, boolean recursive) throws Exception {
        if(bean instanceof Map){
            return (Map) bean;
        }
        if(DTOUtils.isProxy(bean)){
            try {
                return DTOUtils.go(bean, true);
            } catch (Throwable throwable) {
                throw new AppException(throwable.getMessage());
            }
        }
        Map<String, Object> returnMap = new HashMap<String, Object>();
        if(recursive){
            if(type.getSuperclass() != null && !type.getSuperclass().equals(Object.class)){
                returnMap.putAll(transformObjectToMap(bean, type.getSuperclass(), true));
            }
            if(type.getInterfaces() != null && type.getInterfaces().length > 0){
                for(Class<?> intf : type.getInterfaces()){
                    returnMap.putAll(transformObjectToMap(bean, intf, true));
                }
            }
        }
        BeanInfo beanInfo = Introspector.getBeanInfo(type);
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        for (int i = 0; i < propertyDescriptors.length; i++) {
            PropertyDescriptor descriptor = propertyDescriptors[i];
            String propertyName = descriptor.getName();
            if (!"class".equals(propertyName)) {
                Method readMethod = descriptor.getReadMethod();
                //可能该属性并没有getter方法
                if(readMethod == null){
                    continue;
                }
                Object result = null;
                try {
                    result = readMethod.invoke(bean, new Object[0]);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                }
                if (result != null) {
                    returnMap.put(propertyName, result);
                }
            }
        }
        return returnMap;
    }

    /**
     * 把javaBean或dto对象转换为Map键值对
     *
     * @param bean or dto
     * @param recursive 是否向上递归
     * @return
     * @throws Exception
     */
    public static Map<String, Object> transformObjectToMap(Object bean, boolean recursive) throws Exception {
        Class<?> clazz = DTOUtils.isProxy(bean) ? DTOUtils.getDTOClass(bean) : bean.getClass();
        return transformObjectToMap(bean, clazz, recursive);
    }

    /**
     * 把javaBean或dto对象转换为Map键值对, 不递归
     * @param bean or dto
     * @return
     * @throws Exception
     */
    public static Map<String, Object> transformObjectToMap(Object bean) throws Exception {
        Class<?> clazz = DTOUtils.isProxy(bean) ? DTOUtils.getDTOClass(bean) : bean.getClass();
        return transformObjectToMap(bean, clazz, false);
    }


    /**
     * 将com.github.pagehelper.Page对象转换为我们自已的BasePage对象
     * @param page
     * @param <T>
     * @return
     */
    @Deprecated
    public static <T> BasePage<T> convertPage(Page<T> page){
        BasePage<T> result = new BasePage<T>();
        result.setDatas(page.getResult());
        result.setPage(page.getPageNum());
        result.setRows(page.getPageSize());
        result.setTotalItem(Integer.parseInt(String.valueOf(page.getTotal())));
        result.setTotalPage(page.getPages());
        result.setStartIndex(page.getStartRow());
        return result;
    }


    /**
     * 拷贝继承BaseQuery的bean
     * @param source
     * @param target
     * @param <T>
     * @param <K>
     * @return
     */
    @Deprecated
    public static <T,K> K copyBaseQueryBean(T source,Class<K> target ){
        K k = copyBean(source, target);
        if(BaseQuery.class.isAssignableFrom(target)){
            BasePage p = (BasePage)k;
            try {
                if(hasMethod(source.getClass(),"getPage" )) {
                    Method getPageIndex = source.getClass().getMethod("getPage");
                    Object pageIndex = getPageIndex.invoke(source);
                    if (pageIndex != null && (pageIndex instanceof Integer || "int".equals(pageIndex.getClass().getName()))) {
                        p.setPage((Integer) pageIndex);
                    }
                }

                if(hasMethod(source.getClass(),"getRows" )) {
                    Method getPageSize = source.getClass().getMethod("getRows");
                    Object pageSize = getPageSize.invoke(source);
                    if (pageSize != null && (pageSize instanceof Integer || "int".equals(pageSize.getClass().getName()))) {
                        p.setRows((Integer) pageSize);
                    } else {
                        p.setRows(BasePage.DEFAULT_PAGE_SIZE);
                    }
                }

                if(hasMethod(source.getClass(),"getOrderFieldType" )) {
                    Method getOrderFieldType = source.getClass().getMethod("getOrderFieldType");
                    Object orderFieldType = getOrderFieldType.invoke(source);
                    if (orderFieldType != null && (orderFieldType instanceof String)) {
                        p.setOrderFieldType((String) orderFieldType);
                    }
                }

                if(hasMethod(source.getClass(),"getOrderField" )) {
                    Method getOrderField = source.getClass().getMethod("getOrderField");
                    Object orderField = getOrderField.invoke(source);
                    if (orderField != null && (orderField instanceof String)) {
                        p.setOrderField((String) orderField);
                    }
                }
            } catch (NoSuchMethodException e) {
                LOG.info(target.getName()+"类没有分页方法:getPageIndex或getPageSize");
            } catch (IllegalAccessException e) {
                LOG.info(source.getClass().getName()+"类的getPageIndex或getPageSize方法调用参数不对!");
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                LOG.info(source.getClass().getName()+"类的getPageIndex或getPageSize方法调用对象不对!");
                e.printStackTrace();
            }
        }
        return k;
    }

    /**
     * page实例类转换，将BasePage里面的data转换为target类型
     * @param source 源对象
     * @param target 目标对象
     * @param <T> 源对象
     * @param <K> 目标对象
     * @return
     */
    @Deprecated
    public static <T,K> BasePage<K> copyPage(BasePage<T> source, Class<K> target){
        List<K> list = new ArrayList<K>();
        BasePage<K> result = new BasePage<K>();
        List<T> sourceList = source.getDatas();
        for(T af : sourceList){
            String beanKey = generateKey(source.getClass(), target);
            BeanCopier copier = null;
            if (!beanCopierMap.containsKey(beanKey)) {
                copier = BeanCopier.create(source.getClass(), target, false);
                beanCopierMap.put(beanKey, copier);
            } else {
                copier = beanCopierMap.get(beanKey);
            }
            K af1 = null;
            try {
                af1 = (K)target.newInstance();
            } catch (Exception e) {
                LOG.error("实例转换出错");
            }
            copier.copy(af, af1,null);
            list.add(af1);
        }
        result.setDatas(list);
        result.setPage(source.getPage());
        result.setRows(source.getRows());
        result.setTotalItem(source.getTotalItem());
        return result;
    }

    /**
     * 将com.github.pagehelper.Page对象转换为我们自已的BasePage对象
     * @param page
     * @param <T>
     * @return
     */
    @Deprecated
    public static <T> BasePage<T> convertPage(PageInfo<T> page){
        BasePage<T> result = new BasePage<T>();
        result.setDatas(page.getList());
        result.setPage(page.getPageNum());
        result.setRows(page.getPageSize());
        result.setTotalItem(Integer.parseInt(String.valueOf(page.getTotal())));
        result.setTotalPage(page.getPages());
        result.setStartIndex(page.getStartRow());
        return result;
    }

    /**
     * 将org.springframework.data.domain.Page对象转换为我们自已的BasePage对象
     * 依赖spring-data-commons包
     * @param page
     * @param <T>
     * @return
     */
    public static <T> BasePage<T> convertPage(org.springframework.data.domain.Page<T> page){
        BasePage<T> result = new BasePage<T>();
        result.setDatas(page.getContent());
        result.setPage(page.getNumber()+1);
        result.setRows(page.getSize());
        result.setTotalItem(Integer.parseInt(String.valueOf(page.getTotalElements())));
        result.setTotalPage(page.getTotalPages());
        Integer startIndex = (result.getPage() - 1)*result.getRows()+1; //计算出页开始行数
        if(startIndex<1){
            startIndex = 1;
        }
        if(startIndex>result.getTotalItem()){
            startIndex = result.getTotalItem();
        }
        result.setStartIndex(startIndex);
        return result;
    }

    private static String generateKey(Class<?> class1, Class<?> class2) {
        return class1.toString() + class2.toString();
    }

    private static boolean hasMethod(Class clazz, String methodName, Class<?>... parameterTypes ){
        try {
            clazz.getMethod(methodName, parameterTypes);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}

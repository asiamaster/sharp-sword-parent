package com.dili.ss.util;

/**
 * Created by asiamastor on 2017/1/3.
 */

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.util.*;

/**
 * 反射工具类.
 *
 * 提供访问私有变量,获取泛型类型Class, 提取集合中元素的属性, 转换字符串到对象等Util函数.
 *
 */
public class ReflectionUtils {

    private static Logger logger = LoggerFactory.getLogger(ReflectionUtils.class);

    /**
     * 调用Getter方法.
     */
    public static Object invokeGetterMethod(Object obj, String propertyName) {
        String getterMethodName = "get" + StringUtils.capitalize(propertyName);
        return invokeMethod(obj, getterMethodName, new Class[] {}, new Object[] {});
    }

    /**
     * 调用Setter方法.使用value的Class来查找Setter方法.
     */
    public static void invokeSetterMethod(Object obj, String propertyName, Object value) {
        invokeSetterMethod(obj, propertyName, value, null);
    }

    /**
     * 调用Setter方法.
     *
     * @param propertyType 用于查找Setter方法,为空时使用value的Class替代.
     */
    public static void invokeSetterMethod(Object obj, String propertyName, Object value, Class<?> propertyType) {
        Class<?> type = propertyType != null ? propertyType : value.getClass();
        String setterMethodName = "set" + StringUtils.capitalize(propertyName);
        invokeMethod(obj, setterMethodName, new Class[] { type }, new Object[] { value });
    }

    /**
     * 直接读取对象属性值, 无视private/protected修饰符, 不经过getter函数.
     */
    public static Object getFieldValue(final Object obj, final String fieldName) {
        Field field = getAccessibleField(obj, fieldName);

        if (field == null) {
            throw new IllegalArgumentException("Could not find field [" + fieldName + "] on target [" + obj + "]");
        }

        Object result = null;
        try {
            result = field.get(obj);
        } catch (IllegalAccessException e) {
            logger.error("不可能抛出的异常{}", e.getMessage());
        }
        return result;
    }

    /**
     * 直接设置对象属性值, 无视private/protected修饰符, 不经过setter函数.
     */
    public static void setFieldValue(final Object obj, final String fieldName, final Object value) {
        Field field = getAccessibleField(obj, fieldName);

        if (field == null) {
            throw new IllegalArgumentException("Could not find field [" + fieldName + "] on target [" + obj + "]");
        }

        try {
            field.set(obj, value);
        } catch (IllegalAccessException e) {
            logger.error("不可能抛出的异常:{}", e.getMessage());
        }
    }

    /**
     * 循环向上转型, 获取对象的所有Field, 并强制设置为可访问.
     *
     * 如向上转型到Object仍无法找到, 返回null.
     */
    public static List<Field> getAccessibleFields(final Class clazz, boolean excludeStatic, boolean excludeFinal) {
        Assert.notNull(clazz, "clazz不能为空");
        List<Field> fields = Lists.newArrayList();
        for (Class<?> superClass = clazz; superClass != Object.class && superClass != null; superClass = superClass.getSuperclass()) {
            Field[] declaredFields = superClass.getDeclaredFields();
            for(Field field : declaredFields){
                if("serialVersionUID".equals(field.getName())){
                    continue;
                }
                //是否排除static字段
                if(excludeStatic && Modifier.isStatic(field.getModifiers())){
                    continue;
                }
                //是否排除Final字段
                if(excludeFinal && Modifier.isFinal(field.getModifiers())){
                    continue;
                }
                if(!fields.contains(field)){
                    field.setAccessible(true);
                    fields.add(field);
                }
            }
        }
        return fields;
    }

    /**
     * 循环向上转型, 获取对象的DeclaredField,   并强制设置为可访问.
     *
     * 如向上转型到Object仍无法找到, 返回null.
     */
    public static Field getAccessibleField(final Object obj, final String fieldName) {
        Assert.notNull(obj, "object不能为空");
        Assert.hasText(fieldName, "fieldName");
        return getAccessibleField(obj.getClass(), fieldName);
    }

    /**
     * 循环向上转型, 获取对象的DeclaredField,   并强制设置为可访问.
     *
     * 如向上转型到Object仍无法找到, 返回null.
     */
    public static Field getAccessibleField(final Class clazz, final String fieldName) {
        Assert.notNull(clazz, "clazz不能为空");
        Assert.hasText(fieldName, "fieldName");
        for (Class<?> superClass = clazz; superClass != Object.class && superClass != null; superClass = superClass.getSuperclass()) {
            try {
                Field field = superClass.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException e) {//NOSONAR
                // Field不在当前类定义,继续向上转型
            }
        }
        return null;
    }

    /**
     * 直接调用对象方法, 无视private/protected修饰符.
     * 用于一次性调用的情况.
     */
    public static Object invokeMethod(final Object obj, final String methodName, final Class<?>[] parameterTypes,
                                      final Object[] args) {
        Method method = getAccessibleMethod(obj, methodName, parameterTypes);
        if (method == null) {
            throw new IllegalArgumentException("Could not find method [" + methodName + "] on target [" + obj + "]");
        }

        try {
            return method.invoke(obj, args);
        } catch (Exception e) {
            throw convertReflectionExceptionToUnchecked(e);
        }
    }

    /**
     * 循环向上转型, 获取对象的DeclaredMethod,并强制设置为可访问.
     * 如向上转型到Object仍无法找到, 返回null.
     *
     * 用于方法需要被多次调用的情况. 先使用本函数先取得Method,然后调用Method.invoke(Object obj, Object... args)
     */
    public static Method getAccessibleMethod(final Object obj, final String methodName,
                                             final Class<?>... parameterTypes) {
        Assert.notNull(obj, "object不能为空");
        return getAccessibleMethod(obj.getClass(), methodName, parameterTypes);
    }

    /**
     * 循环向上转型, 获取对象的DeclaredMethod,并强制设置为可访问.
     * 如向上转型到Object仍无法找到, 返回null.
     *
     * 用于方法需要被多次调用的情况. 先使用本函数先取得Method,然后调用Method.invoke(Object obj, Object... args)
     */
    public static Method getAccessibleMethod(final Class clazz, final String methodName,
                                             final Class<?>... parameterTypes) {
        Assert.notNull(clazz, "clazz不能为空");
        for (Class<?> superClass = clazz; superClass != Object.class && superClass != null; superClass = superClass.getSuperclass()) {
            try {
                Method method = superClass.getDeclaredMethod(methodName, parameterTypes);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException e) {//NOSONAR
                // Method不在当前类定义,继续向上转型
            }
        }
        //如果当前clazz是接口，则需要搜索所有父接口
        if(clazz.isInterface()) {
            return getIntefacesAccessibleMethod(clazz, methodName, parameterTypes);
        }else{
            return null;
        }
    }

    /**
     * 获取接口及所有父接口中的所有方法,并强制设置为可访问
     * @param intfClasses
     * @return
     */
    public static List<Method> getAccessibleMethods(Class<?> intfClasses){
        List<Method> methods = new ArrayList<>();
        getAccessibleMethodsRecursive(methods, intfClasses);
        return methods;
    }

    /**
     * 递归获取接口及所有父接口中的所有方法,并强制设置为可访问，最后放入到methods对象中
     * @param intfClass
     */
    public static Method getIntefacesAccessibleMethod(final Class<?> intfClass,final String methodName, final Class<?>... parameterTypes){
        Assert.notNull(intfClass, "intfClasses不能为空");
        try {
            Method method = intfClass.getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            // Method不在当前类定义,继续向上转型
        }
        for (Class<?> clazz : intfClass.getInterfaces()) {
            return getIntefacesAccessibleMethod(clazz, methodName, parameterTypes);
        }
        return null;
    }

    /**
     * 通过反射, 获得Class定义中声明的父类的泛型参数的类型.
     * 如无法找到, 返回Object.class.
     * eg.
     * public UserDao extends HibernateDao<User>
     *
     * @param clazz The class to introspect
     * @return the first generic declaration, or Object.class if cannot be determined
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> Class<T> getSuperClassGenricType(final Class clazz) {
        return getSuperClassGenricType(clazz, 0);
    }

    /**
     * 通过反射, 获得Class定义中声明的父类的泛型参数的类型.
     * 如无法找到, 返回Object.class.
     *
     * 如public UserDao extends HibernateDao<User,Long>
     *
     * @param clazz clazz The class to introspect
     * @param index the Index of the generic ddeclaration,start from 0.
     * @return the index generic declaration, or Object.class if cannot be determined
     */
    @SuppressWarnings("rawtypes")
    public static Class getSuperClassGenricType(final Class clazz, final int index) {
        Type genType = clazz.getGenericSuperclass();
        if (!(genType instanceof ParameterizedType)) {
            logger.warn(clazz.getSimpleName() + "'s superclass not ParameterizedType");
            return Object.class;
        }

        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();

        if (index >= params.length || index < 0) {
            logger.warn("Index: " + index + ", Size of " + clazz.getSimpleName() + "'s Parameterized Type: "
                    + params.length);
            return Object.class;
        }
        if (!(params[index] instanceof Class)) {
            logger.warn(clazz.getSimpleName() + " not set the actual class on superclass generic parameter");
            return Object.class;
        }

        return (Class) params[index];
    }

    /**
     * 将反射时的checked exception转换为unchecked exception.
     */
    public static RuntimeException convertReflectionExceptionToUnchecked(Exception e) {
        if (e instanceof IllegalAccessException || e instanceof IllegalArgumentException
                || e instanceof NoSuchMethodException) {
            return new IllegalArgumentException("Reflection Exception.", e);
        } else if (e instanceof InvocationTargetException) {
            return new RuntimeException("Reflection Exception.", ((InvocationTargetException) e).getTargetException());
        } else if (e instanceof RuntimeException) {
            return (RuntimeException) e;
        }
        return new RuntimeException("Unexpected Checked Exception.", e);
    }

    /**
     * 获取当前运行时方法名(jdk1.5+)
     * jdk1.4的方法:new Exception().getStackTrace()[i].getMethodName();//其中i == 0就是当前的类的方法名字 ;i == 1就是调用者的方法
     * @return
     */
    public static String getCurrentMethodName(){
        return Thread.currentThread().getStackTrace()[1].getMethodName();
    }

    public static String getJavaVersion(){
        return System.getProperty("java.version");
    }

    /**
     * 判断是否是java8
     * @return
     */
    public static boolean isJava8(){
        if (getJavaVersion().contains("1.8.")) {
            return true;
        }
        return false;
    }



    /**
     * 私有方法<br/>
     * 递归获取接口及所有父接口中的所有方法,并强制设置为可访问，最后放入到methods对象中
     * @param methods
     * @param intfClasses
     */
    private static void getAccessibleMethodsRecursive(List<Method> methods, Class<?> intfClasses){
        Assert.notNull(intfClasses, "intfClasses不能为空");
        Assert.notNull(methods, "methods不能为空");
        for(Method method : intfClasses.getDeclaredMethods()){
            method.setAccessible(true);
            methods.add(method);
        }
        for (Class<?> intfClass : intfClasses.getInterfaces()) {
            getAccessibleMethodsRecursive(methods, intfClass);
        }
    }

    // ----------------- 另一个反射工具 ----------------------

    /**
     * The constant MAX_NEST_DEPTH.
     */
    public static final int MAX_NEST_DEPTH = 20;

    /**
     * Gets class by name.
     *
     * @param className the class name
     * @return the class by name
     * @throws ClassNotFoundException the class not found exception
     */
    public static Class<?> getClassByName(String className) throws ClassNotFoundException {
        return Class.forName(className, true, Thread.currentThread().getContextClassLoader());
    }

    /**
     * get Field Value
     *
     * @param target    the target
     * @param fieldName the field name
     * @return field value
     * @throws NoSuchFieldException the no such field exception
     * @throws SecurityException the security exception
     * @throws IllegalArgumentException the illegal argument exception
     * @throws IllegalAccessException the illegal access exception
     */
//    public static Object getFieldValue(Object target, String fieldName)
//            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
//        Class<?> cl = target.getClass();
//        int i = 0;
//        while ((i++) < MAX_NEST_DEPTH && cl != null) {
//            try {
//                Field field = cl.getDeclaredField(fieldName);
//                field.setAccessible(true);
//                return field.get(target);
//            } catch (Exception e) {
//                cl = cl.getSuperclass();
//            }
//        }
//        throw new NoSuchFieldException("class:" + target.getClass() + ", field:" + fieldName);
//    }

    /**
     * invoke Method
     *
     * @param target     the target
     * @param methodName the method name
     * @return object
     * @throws NoSuchMethodException the no such method exception
     * @throws SecurityException the security exception
     * @throws IllegalAccessException the illegal access exception
     * @throws IllegalArgumentException the illegal argument exception
     * @throws InvocationTargetException the invocation target exception
     */
    public static Object invokeMethod(Object target, String methodName)
            throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        Class<?> cl = target.getClass();
        int i = 0;
        while ((i++) < MAX_NEST_DEPTH && cl != null) {
            try {
                Method m = cl.getDeclaredMethod(methodName);
                m.setAccessible(true);
                return m.invoke(target);
            } catch (Exception e) {
                cl = cl.getSuperclass();
            }
        }
        throw new NoSuchMethodException("class:" + target.getClass() + ", methodName:" + methodName);
    }

    /**
     * invoke Method
     *
     * @param target         the target
     * @param methodName     the method name
     * @param parameterTypes the parameter types
     * @param args           the args
     * @return object
     * @throws NoSuchMethodException the no such method exception
     * @throws SecurityException the security exception
     * @throws IllegalAccessException the illegal access exception
     * @throws IllegalArgumentException the illegal argument exception
     * @throws InvocationTargetException the invocation target exception
     */
//    public static Object invokeMethod(Object target, String methodName, Class<?>[] parameterTypes, Object[] args)
//            throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
//            InvocationTargetException {
//        Class<?> cl = target.getClass();
//        int i = 0;
//        while ((i++) < MAX_NEST_DEPTH && cl != null) {
//            try {
//                Method m = cl.getDeclaredMethod(methodName, parameterTypes);
//                m.setAccessible(true);
//                return m.invoke(target, args);
//            } catch (Exception e) {
//                cl = cl.getSuperclass();
//            }
//        }
//        throw new NoSuchMethodException("class:" + target.getClass() + ", methodName:" + methodName);
//    }

    /**
     * invoke static Method
     *
     * @param targetClass     the target class
     * @param methodName      the method name
     * @param parameterTypes  the parameter types
     * @param parameterValues the parameter values
     * @return object
     * @throws NoSuchMethodException the no such method exception
     * @throws SecurityException the security exception
     * @throws IllegalAccessException the illegal access exception
     * @throws IllegalArgumentException the illegal argument exception
     * @throws InvocationTargetException the invocation target exception
     */
    public static Object invokeStaticMethod(Class<?> targetClass, String methodName, Class<?>[] parameterTypes,
                                            Object[] parameterValues)
            throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        int i = 0;
        while ((i++) < MAX_NEST_DEPTH && targetClass != null) {
            try {
                Method m = targetClass.getMethod(methodName, parameterTypes);
                return m.invoke(null, parameterValues);
            } catch (Exception e) {
                targetClass = targetClass.getSuperclass();
            }
        }
        throw new NoSuchMethodException("class:" + targetClass + ", methodName:" + methodName);
    }

    /**
     * get Method by name
     *
     * @param classType      the class type
     * @param methodName     the method name
     * @param parameterTypes the parameter types
     * @return method
     * @throws NoSuchMethodException the no such method exception
     * @throws SecurityException the security exception
     */
    public static Method getMethod(Class<?> classType, String methodName, Class<?>[] parameterTypes)
            throws NoSuchMethodException, SecurityException {
        return classType.getMethod(methodName, parameterTypes);
    }

    /**
     * get all interface of the clazz
     *
     * @param clazz the clazz
     * @return set
     */
    public static Set<Class<?>> getInterfaces(Class<?> clazz) {
        if (clazz.isInterface()) {
            return Collections.singleton(clazz);
        }
        Set<Class<?>> interfaces = new LinkedHashSet<Class<?>>();
        while (clazz != null) {
            Class<?>[] ifcs = clazz.getInterfaces();
            for (Class<?> ifc : ifcs) {
                interfaces.addAll(getInterfaces(ifc));
            }
            clazz = clazz.getSuperclass();
        }
        return interfaces;
    }

    /**
     * 调用接口默认方法
     * @param proxy
     * @param method
     * @param args
     * @return
     * @throws Throwable
     */
    public static Object invokeDefaultMethod(Object proxy, Method method, Object[] args) throws Throwable {
        Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class
                .getDeclaredConstructor(Class.class, int.class);
        constructor.setAccessible(true);

        Class<?> declaringClass = method.getDeclaringClass();
        int allModes = MethodHandles.Lookup.PUBLIC | MethodHandles.Lookup.PRIVATE | MethodHandles.Lookup.PROTECTED | MethodHandles.Lookup.PACKAGE;
        return constructor.newInstance(declaringClass, allModes)
                .unreflectSpecial(method, declaringClass)
                .bindTo(proxy)
                .invokeWithArguments(args);
//        final Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class
//                .getDeclaredConstructor(Class.class, int.class);
//        if (!constructor.isAccessible()) {
//            constructor.setAccessible(true);
//        }
//        final Class<?> declaringClass = method.getDeclaringClass();
//        return constructor.newInstance(declaringClass, MethodHandles.Lookup.PRIVATE)
//                .unreflectSpecial(method, declaringClass).bindTo(proxy).invokeWithArguments(args);
    }
}


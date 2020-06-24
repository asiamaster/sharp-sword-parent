package com.dili.ss.mvc.boot;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.dto.IDTO;
import com.dili.ss.mvc.annotation.Cent2Yuan;
import com.dili.ss.mvc.controller.DTOResponseBodyAdvice;
import com.dili.ss.util.AopTargetUtils;
import com.dili.ss.util.POJOUtils;
import com.dili.ss.util.SpringUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 控制器响应监听
 */
@Component
@ConditionalOnExpression("'${responseBodyAdvice.enable}'=='true'")
public class ControllerResponseApplicationListener implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        Map<String, Object> controllerBeans = SpringUtil.getBeansWithAnnotation(Controller.class);
        if(controllerBeans == null){
            controllerBeans = SpringUtil.getBeansWithAnnotation(RestController.class);
        }else {
            Map<String, Object> restControllerBeans = SpringUtil.getBeansWithAnnotation(RestController.class);
            if(restControllerBeans != null) {
                controllerBeans.putAll(restControllerBeans);
            }
        }
        if(controllerBeans == null || controllerBeans.isEmpty()){
            return;
        }
        //迭代bean
        for(Map.Entry<String, Object> entry : controllerBeans.entrySet()){
            try {
                Class<?> controllerClass = AopTargetUtils.getTarget(entry.getValue()).getClass();
                //迭代bean类的方法
                for(Method method : controllerClass.getMethods()){
                    //判断方法返回值的对象包括Cent2Yuan注解
                    if(supportsCent2Yuan(method)){
                        List<Object> methodFields = getCent2YuanMethodFields(getReturnTypeDTO(method));
                        if(methodFields.isEmpty()){
                            continue;
                        }
                        DTOResponseBodyAdvice.cent2YuanMethodFieldCache.put(method, methodFields);
                    }
                }
            } catch (Exception e) {
                continue;
            }
        }
    }

    /**
     * 判断方法返回值类型中的DTO对象是否包括Cent2Yuan注解
     * @param method
     * @return
     */
    private boolean supportsCent2Yuan(Method method) {
        //如果是List<DTO>
        if(List.class.isAssignableFrom(method.getReturnType()) && method.getGenericReturnType() != null) {
            //获取返回对象的泛型参数
            Type[] parameterizedType = ((ParameterizedTypeImpl) method.getGenericReturnType()).getActualTypeArguments();
            if(parameterizedType == null || parameterizedType.length == 0 || !(parameterizedType[0] instanceof Class)){
                return false;
            }
            //泛型参数是DTO接口
//            if(((Class)parameterizedType[0]).isInterface() && IDTO.class.isAssignableFrom((Class)parameterizedType[0])){
//                return true;
//            }

            //如果泛型参数实现了IDTO接口，无论是类还是接口
            if(IDTO.class.isAssignableFrom((Class)parameterizedType[0])){
                return true;
            }
            return false;
        }
        //如果泛型参数实现了IDTO接口，无论是类还是接口
        else if(IDTO.class.isAssignableFrom(method.getReturnType())) {
            return true;
        }
        //如果是BaseOutput
        else if(BaseOutput.class.isAssignableFrom(method.getReturnType())) {
            //获取返回对象BaseOutput有泛型参数
            if(method.getGenericReturnType() instanceof ParameterizedTypeImpl){
                Type[] parameterizedType = ((ParameterizedTypeImpl) method.getGenericReturnType()).getActualTypeArguments();
                if(parameterizedType == null || parameterizedType.length == 0){
                    return false;
                }
                //泛型参数实现了IDTO接口，无论是类还是接口
                if((parameterizedType[0] instanceof Class) && IDTO.class.isAssignableFrom((Class)parameterizedType[0])){
                    return true;
                }
                //如果BaseOutput的第一级泛型参数下还有第二级泛型参数，即BaseOutput<List<DTO>>>的情况
                if(parameterizedType[0] instanceof ParameterizedTypeImpl) {
                    Type[] parameterizedType1 =  ((ParameterizedTypeImpl) parameterizedType[0]).getActualTypeArguments();
                    if(parameterizedType1 == null || parameterizedType1.length == 0){
                        return false;
                    }
                    //不支持更深的泛型参数
                    if(parameterizedType1[0] instanceof ParameterizedTypeImpl){
                        return false;
                    }
                    //泛型参数是List集合
                    if (List.class.isAssignableFrom(((ParameterizedTypeImpl) parameterizedType[0]).getRawType())) {
                        Class parameterizedClass = (Class) parameterizedType1[0];
                        //如果当前泛型参数实现了IDTO接口，无论是类还是接口
                        if (IDTO.class.isAssignableFrom(parameterizedClass)) {
                            return true;
                        }
                        return false;
                    }
                }
                return false;
            }//获取返回对象BaseOutput没有泛型参数
            else{
                return false;
            }
        }
        return false;
    }

    /**
     * 根据Controller方法获取返回值中的DTO接口对象
     * 因为在supportsCent2Yuan方法中作了类型判断，这里为了提高效率，仅作了必要的判断
     * @param method
     * @return
     */
    private Class getReturnTypeDTO(Method method){
        //泛型参数实现了IDTO接口，无论是类还是接口
        if(IDTO.class.isAssignableFrom(method.getReturnType())) {
            return (Class) method.getReturnType();
        }//List<DTO>接口
        else if(List.class.isAssignableFrom(method.getReturnType()) && method.getGenericReturnType() != null) {
            return (Class)((ParameterizedTypeImpl) method.getGenericReturnType()).getActualTypeArguments()[0];
        }else if(BaseOutput.class.isAssignableFrom(method.getReturnType())) {
            Type[] parameterizedType = ((ParameterizedTypeImpl) method.getGenericReturnType()).getActualTypeArguments();
            //BaseOutput<DTO>
            if((parameterizedType[0] instanceof Class)){
                return (Class) parameterizedType[0];
            }//BaseOutput<List<DTO>>
            else if(parameterizedType[0] instanceof ParameterizedTypeImpl) {
                Type[] parameterizedType1 =  ((ParameterizedTypeImpl) parameterizedType[0]).getActualTypeArguments();
                //泛型参数是List集合
                if (List.class.isAssignableFrom(((ParameterizedTypeImpl) parameterizedType[0]).getRawType())) {
                    return (Class) parameterizedType1[0];
                }
            }
        }
        return null;
    }
    /**
     * 获取类中有分转元注解的方法和属性
     * @param clazz
     * @return 可能是List<Method>或者List<Field>
     */
    private List<Object> getCent2YuanMethodFields(Class clazz){
        List<Object> cent2YuanMethodFields = new ArrayList<>();
        if(clazz.isInterface()){
            for(Method method : clazz.getMethods()){
                //只处理返回类型为Long的getter方法
                if(POJOUtils.isGetMethod(method) && Long.class == method.getReturnType()) {
                    Cent2Yuan cent2Yuan = method.getAnnotation(Cent2Yuan.class);
                    if (cent2Yuan != null) {
                        cent2YuanMethodFields.add(method);
                    }
                }
            }
        }else{
            for(Field field : clazz.getFields()){
                //只处理返回类型为Long的getter方法
                if(Long.class == field.getType()) {
                    Cent2Yuan cent2Yuan = field.getAnnotation(Cent2Yuan.class);
                    if (cent2Yuan != null) {
                        cent2YuanMethodFields.add(field);
                    }
                }
            }
        }
        return cent2YuanMethodFields;
    }
}

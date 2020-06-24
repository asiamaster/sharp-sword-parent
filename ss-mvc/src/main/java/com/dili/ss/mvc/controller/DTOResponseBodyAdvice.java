package com.dili.ss.mvc.controller;

import com.dili.ss.domain.BaseOutput;
import com.dili.ss.dto.DTO;
import com.dili.ss.dto.DTOUtils;
import com.dili.ss.dto.IDTO;
import com.dili.ss.mvc.annotation.Cent2Yuan;
import com.dili.ss.util.BeanConver;
import com.dili.ss.util.POJOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * http response body处理器
 */
@ControllerAdvice
@ConditionalOnExpression("'${responseBodyAdvice.enable}'=='true'")
public class DTOResponseBodyAdvice implements ResponseBodyAdvice {

    protected static final Logger log = LoggerFactory.getLogger(DTOResponseBodyAdvice.class);

    /**
     * 分转元注解的Controller方法缓存
     * key是方法，value是返回类型泛型中的DTO对象包括Cent2Yuan注解的方法
     */
    public static final Map<Method, List<Object>> cent2YuanMethodFieldCache = new HashMap<>();

    @Override
    public Object beforeBodyWrite(Object returnValue, MethodParameter methodParameter,
                                  MediaType mediaType, Class clas, ServerHttpRequest serverHttpRequest,
                                  ServerHttpResponse serverHttpResponse) {
        //通过 ServerHttpRequest的实现类ServletServerHttpRequest 获得HttpServletRequest
        //ServletServerHttpRequest sshr=(ServletServerHttpRequest) serverHttpRequest;
        if(returnValue == null){
            return null;
        }
        List<Object> cent2YuanMethodFields = cent2YuanMethodFieldCache.get(methodParameter.getMethod());
        //处理返回值为DTO接口集合
        if(returnValue instanceof List){
            List<IDTO> retList = (List)returnValue;
            return handleListDto(retList, cent2YuanMethodFields);
        }//处理返回值为DTO接口
        else if(returnValue instanceof IDTO){
            List<Object> cent2YuanMethods = cent2YuanMethodFieldCache.get(methodParameter.getMethod());
            if(cent2YuanMethods.isEmpty()){
                return returnValue;
            }
            try {
                if(DTOUtils.getDTOClass(returnValue).isInterface()) {
                    return handleDto((IDTO) returnValue, (List) cent2YuanMethods);
                }else{
                    return handleBean((IDTO) returnValue, (List) cent2YuanMethods);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return returnValue;
            }
        }
        else if(returnValue instanceof BaseOutput){
            try {
                return handleBaseOutput(methodParameter.getMethod(), (BaseOutput)returnValue);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return returnValue;
            }
        }
        //返回修改后的值
        return returnValue;
    }

    @Override
    public boolean supports(MethodParameter methodParameter, Class clazz) {
        return cent2YuanMethodFieldCache.containsKey(methodParameter.getMethod());
//        Method method = methodParameter.getMethod();
//        //如果是List<DTO接口>
//        if(List.class.isAssignableFrom(method.getReturnType()) && method.getGenericReturnType() != null) {
//            //获取返回对象的泛型参数
//            Type[] parameterizedType = ((ParameterizedTypeImpl) method.getGenericReturnType()).getActualTypeArguments();
//            if(parameterizedType == null || parameterizedType.length == 0 || !(parameterizedType[0] instanceof Class)){
//                return false;
//            }
//            //泛型参数是DTO接口
//            if(((Class)parameterizedType[0]).isInterface() && IDTO.class.isAssignableFrom((Class)parameterizedType[0])){
//                return true;
//            }
//            return false;
//        }
//        //如果是DTO接口
//        else if(IDTO.class.isAssignableFrom(method.getReturnType()) && method.getReturnType().isInterface()) {
//            return true;
//        }
//        //如果是BaseOutput
//        else if(BaseOutput.class.isAssignableFrom(method.getReturnType())) {
//            //获取返回对象BaseOutput有泛型参数
//            if(method.getGenericReturnType() instanceof ParameterizedTypeImpl){
//                Type[] parameterizedType = ((ParameterizedTypeImpl) method.getGenericReturnType()).getActualTypeArguments();
//                if(parameterizedType == null || parameterizedType.length == 0){
//                    return false;
//                }
//                //泛型参数是DTO接口
//                if((parameterizedType[0] instanceof Class) && ((Class)parameterizedType[0]).isInterface() && IDTO.class.isAssignableFrom((Class)parameterizedType[0])){
//                    return true;
//                }
//                //如果BaseOutput的第一级泛型参数下还有第二级泛型参数，即BaseOutput<List<DTO>>>的情况
//                if(parameterizedType[0] instanceof ParameterizedTypeImpl) {
//                    Type[] parameterizedType1 =  ((ParameterizedTypeImpl) parameterizedType[0]).getActualTypeArguments();
//                    if(parameterizedType1 == null || parameterizedType1.length == 0){
//                        return false;
//                    }
//                    //不支持更深的泛型参数
//                    if(parameterizedType1[0] instanceof ParameterizedTypeImpl){
//                        return false;
//                    }
//                    //泛型参数是List集合
//                    if (List.class.isAssignableFrom(((ParameterizedTypeImpl) parameterizedType[0]).getRawType())) {
//                        Class parameterizedClass = (Class) parameterizedType1[0];
//                        //如果当前泛型参数是DTO接口
//                        if (parameterizedClass.isInterface() && IDTO.class.isAssignableFrom(parameterizedClass)) {
//                            return true;
//                        }
//                        return false;
//                    }
//                }
//                return false;
//            }//获取返回对象BaseOutput没有泛型参数
//            else{
//                return false;
//            }
//        }
//        return false;
    }

    /**
     * 处理DTO集合
     * @param retList
     * @return
     */
    private Object handleListDto(List<IDTO> retList, List<Object> cent2YuanMethodFields){
        if(retList == null || retList.isEmpty()){
            return retList;
        }
        List<Map> dtos = new ArrayList<>(retList.size());
        if(cent2YuanMethodFields.get(0) instanceof Method){
            for (IDTO idto : retList){
                try {
                    dtos.add(handleDto(idto, (List)cent2YuanMethodFields));
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    continue;
                }
            }
        }else{
            for (IDTO idto : retList){
                try {
                    dtos.add(handleBean(idto, (List)cent2YuanMethodFields));
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    continue;
                }
            }
        }
        return dtos;
    }

    /**
     * 处理DTO接口
     * @param idto
     * @param cent2YuanMethods
     * @return
     */
    private DTO handleDto(IDTO idto, List<Method> cent2YuanMethods) throws InvocationTargetException, IllegalAccessException {
        DTO dto = DTOUtils.go(idto);
        for(Method getMethod : cent2YuanMethods){
            Long cent = (Long)getMethod.invoke(idto);
            if(cent == null){
                continue;
            }
            dto.put(POJOUtils.getBeanField(getMethod), cent2Yuan(cent));
        }
        return dto;
    }

    /**
     * 处理DTO实体
     * @param idto
     * @param cent2YuanFields
     * @return
     */
    private Map handleBean(IDTO idto, List<Field> cent2YuanFields) throws Exception {
        Map dto = BeanConver.transformObjectToMap(idto);
        for(Field field : cent2YuanFields){
            Long cent = (Long)field.get(idto);
            if(cent == null){
                continue;
            }
            dto.put(field, cent2Yuan(cent));
        }
        return dto;
    }

    /**
     * 处理BaseOutput返回值
     * @param method 当前方法
     * @param baseOutput
     * @return
     */
    private BaseOutput handleBaseOutput(Method method, BaseOutput baseOutput) throws InvocationTargetException, IllegalAccessException {
        Type[] parameterizedType = ((ParameterizedTypeImpl) method.getGenericReturnType()).getActualTypeArguments();
        List<Object> cent2YuanMethodFields = cent2YuanMethodFieldCache.get(method);
        //如果是类(在support方法中已经判断了是DTO)
        if((parameterizedType[0] instanceof Class)){
            return baseOutput.setData(handleDto((IDTO) baseOutput.getData(), (List)cent2YuanMethodFields));
        }//另一种支持的情况就只有List<DTO>了
        else{
            return baseOutput.setData(handleListDto((List)baseOutput.getData(), cent2YuanMethodFields));
        }
    }

    /**
     * 获取类中的Cent2Yuan注解的方法
     * @param clazz
     * @return
     */
    private List<Method> getCent2YuanMethods(Class clazz){
        List<Method> cent2YuanMethods = new ArrayList<>();
        for(Method method : clazz.getMethods()){
            //只处理返回类型为Long的getter方法
            if(POJOUtils.isGetMethod(method) && Long.class == method.getReturnType()) {
                Cent2Yuan cent2Yuan = method.getAnnotation(Cent2Yuan.class);
                if (cent2Yuan != null) {
                    cent2YuanMethods.add(method);
                }
            }
        }
        return cent2YuanMethods;
    }

    /**
     * 分转元
     * @param cent
     * @return
     */
    private String cent2Yuan(Long cent){
        return new BigDecimal(cent).divide(new BigDecimal(100)).toString();
    }

}

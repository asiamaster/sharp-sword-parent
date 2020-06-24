package com.dili.ss.mvc.boot;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.dili.ss.dto.DTO;
import com.dili.ss.dto.DTOUtils;
import com.dili.ss.dto.IDTO;
import com.dili.ss.dto.ReturnTypeHandlerFactory;
import com.dili.ss.exception.AppException;
import com.dili.ss.mvc.annotation.Cent2Yuan;
import com.dili.ss.mvc.servlet.RequestReaderHttpServletRequestWrapper;
import com.dili.ss.mvc.util.BeanValidator;
import com.dili.ss.util.POJOUtils;
import com.google.common.collect.Lists;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.ServletRequest;
import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.springframework.web.bind.support.WebArgumentResolver.UNRESOLVED;

/**
 * springMVC controller方法参数注入DTO
 * Created by asiamaster on 2017/8/2 0002.
 */
@SuppressWarnings("all")
public class DTOInstArgumentResolver implements HandlerMethodArgumentResolver {

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return IDTO.class.isAssignableFrom(parameter.getParameterType()) && parameter.getParameterType().isInterface();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		Class<?> clazz = parameter.getParameterType();
		if(clazz != null && IDTO.class.isAssignableFrom(clazz)){
			return getDTO((Class<IDTO>)clazz, webRequest, parameter);
		}
		return UNRESOLVED;
	}

	//验证List<Obj>格式的参数，如:users[1][name]
	Pattern listObjPattern = Pattern.compile("(\\[)([0-9])+(\\])(\\[)([\\w])+(\\])$");

	/**
	 * 取得当前的DTO对象<br>
	 * 注意： <li>此处没有考虑缓冲dto，主要是不ResourceData和QueryData是完全不同的类</li> <li>
	 * 应减少调用本方法的次数,如果今后有需求，可考虑加入缓冲</li>
	 *
	 * @param clazz
	 *            DTO对象的类，不允许为空
	 * @return 正常情况下不可能为空，但如果程序内部有问题时只能以null返回
	 */
	@SuppressWarnings("unchecked")
	protected <T extends IDTO> T getDTO(Class<T> clazz, NativeWebRequest webRequest, MethodParameter parameter) {
		//处理restful调用时，传入的参数不在getParameterMap，而在getInputStream中的情况
		//注解掉此处，修正URL上有问号参数，body也有内容时，没有取body的缺陷，此时body内容在servletInputStream中
//		if(webRequest.getParameterMap().isEmpty()){
//			return getDTO4Restful(clazz, webRequest, parameter);
//		}

		//构建fields Map，用于getParamValuesAndConvert()
		Map<String, Class<?>> fields = new HashMap<>();
		for(Method method : clazz.getMethods()){
			if(POJOUtils.isGetMethod(method) && method.getParameterTypes().length == 0){
				fields.put(POJOUtils.getBeanField(method), method.getReturnType());
			}
		}
		// 实例化一个DTO数据对象
		DTO streamDto = getDTO4Restful(fields, webRequest, parameter);
		DTO dto = new DTO();
		// 填充值
		for (Map.Entry<String, String[]> entry : webRequest.getParameterMap().entrySet()) {
			String attrName = entry.getKey();
			//处理metadata，目前只支持一层
			if(attrName.startsWith("metadata[") && attrName.endsWith("]")){
				dto.setMetadata(attrName.substring(9, attrName.length()-1), getParamValuesAndConvert(entry, fields));
			}else if (Character.isLowerCase(attrName.charAt(0))) {
				//处理普通类型数组，前台传入的多个相同name的value是数组，这里key以[]结尾
				//此时entry.getValue()是一个数组
				if(attrName.endsWith("[]")){
					handleListValue(dto, clazz, attrName, entry.getValue());
				}
				//处理List<对象>类型
				else if(listObjPattern.matcher(attrName).find()){
					handleListObjValue(dto, clazz, attrName, entry.getValue());
				}
				//处理IDTO、Map或Java对象，目前只处理一层，后期考虑支持递归多层对象
				//这里特殊处理[数字]，且返回值为数组的
				else if(attrName.endsWith("]") && attrName.contains("[")){
					handleMapValue(dto, clazz, attrName, entry.getValue());
				}
				//处理前台传参的key为xxx.xxx形式，设置参数的对象、DTO或Map属性
				else if(attrName.split("\\.").length == 2){
                    handleDotMapValue(dto, clazz, attrName, entry.getValue());
                }
                //处理普通属性
				else{
					Method method = getMethod(clazz, "get"+ attrName.substring(0, 1).toUpperCase() + attrName.substring(1));
					if (method == null) {
						dto.put(attrName, getParamValuesAndConvert(entry, fields));
					} else {
						//返回值为数组或List，统一在这里放List，在下面的代码将进行转换
						if(List.class.isAssignableFrom(method.getReturnType()) || method.getReturnType().isArray()){
							dto.put(attrName, Lists.newArrayList(getParamValuesAndConvert(entry, fields)));
						}else{
							dto.put(attrName, getParamValuesAndConvert(entry, fields));
						}
					}
				}
			}else{
                dto.put(attrName, getParamValuesAndConvert(entry, fields));
            }
		}

		//再循环一次DTO，把属性中处理成List的Array转成数组
		for (Method method : clazz.getMethods()) {
			//非getter不处理
			if(!method.getName().startsWith("get")){
				continue;
			}
			String field = POJOUtils.getBeanField(method);
			//返回值是array，值是List的，转为Array
			if(method.getReturnType().isArray() && dto.get(field) != null && dto.get(field) instanceof List){
				Class<?> retType = method.getReturnType().getComponentType();
				//没有泛型参数，则统一转成Object[]数组
				if(retType == null) {
					dto.put(field, ((List) dto.get(field)).toArray());
				}else{
					Object arr = Array.newInstance(retType, ((List)dto.get(field)).size());
					dto.put(field, ((List) dto.get(field)).toArray((Object[])arr));
				}
			}
		}
		//body覆盖queryParam
		if(!streamDto.isEmpty()) {
			dto.putAll(streamDto);
		}
		//body覆盖metadata
		if(!streamDto.getMetadata().isEmpty()){
			if(dto.getMetadata() == null){
				dto.setMetadata(streamDto.getMetadata());
			}else {
				dto.getMetadata().putAll(streamDto.getMetadata());
			}
		}
		//处理DTO接口上的入参注解参数有@Cent2Yuan注解
		Method[] methods = clazz.getMethods();
		for(Method method : methods){
			if(!method.getName().startsWith("get") || !Long.class.isAssignableFrom(method.getReturnType())){
				continue;
			}
			Cent2Yuan cent2Yuan = method.getAnnotation(Cent2Yuan.class);
			if(cent2Yuan != null){
				String field = POJOUtils.getBeanField(method);
				Object fieldValue = dto.get(field);
				if(fieldValue != null) {
					dto.put(field, yuan2Cent(fieldValue.toString()));
				}
			}
		}
		T t = (T) DTOUtils.proxyInstance(dto, (Class<IDTO>) clazz);
		asetErrorMsg(t, parameter);
		return t;
	}

	@SuppressWarnings("unchecked")
	private Method getMethod(Class<?> clazz, String methodName){
		try {
			return clazz.getMethod(methodName);
		} catch (NoSuchMethodException e) {
			return null;
		}
	}

	/**
	 * 元转分
	 * @param cent
	 * @return
	 */
	private Long yuan2Cent(String yuan){
		return new BigDecimal(yuan).multiply(new BigDecimal(100)).longValue();
	}

    /**
     * 处理前台传参的key为xxx.xxx形式，设置参数的对象、DTO或Map属性
     * @param dto
     * @param clazz
     * @param attrName
     * @param entryValue
     * @param <T>
     */
	@SuppressWarnings("unchecked")
    private <T extends IDTO> void handleDotMapValue(DTO dto, Class<T> clazz, String attrName, Object entryValue){
        String[] names = attrName.split("\\.");
        String attrObjKey = names[1].trim();
        //去掉属性名后面的[]
        attrName = names[0].trim();
        //有get方法的属性，需要判断返回值如果是Array或List，需要转换前台传的有多个相同name的value数组。
        Method getMethod = null;
        try {
            getMethod = clazz.getMethod("get"+ attrName.substring(0, 1).toUpperCase() + attrName.substring(1));
            //根据返回值判断value的类型
            Class<?> returnType = getMethod.getReturnType();
            //先初始化一个对象作为value
            if(dto.get(attrName) == null){
                if (returnType.isInterface() && IDTO.class.isAssignableFrom(returnType)){//初始化成DTO接口
                    dto.put(attrName, DTOUtils.newInstance((Class<IDTO>)returnType));
                }else if (!Map.class.isAssignableFrom(returnType)){//未实现Map接口，初始化成普通Java对象
                    dto.put(attrName, returnType.newInstance());
                }else{//初始化成Map
                    dto.put(attrName, new HashMap<String, Object>());
                }
            }
            //处理value中数据
            if (returnType.isInterface() && IDTO.class.isAssignableFrom(returnType)){
                ((IDTO)dto.get(attrName)).aset(attrObjKey, getParamValueByForce(entryValue));
            }else if (!Map.class.isAssignableFrom(returnType)){
                PropertyUtils.setProperty(dto.get(attrName), attrObjKey, getParamValueByForce(entryValue));
            }else{
                ((HashMap)dto.get(attrName)).put(attrObjKey, getParamValueByForce(entryValue));
            }
        } catch (NoSuchMethodException e) {
            //没get方法的属性处理为HashMap
            if(dto.get(attrName) == null) {
                dto.put(attrName, new HashMap<String, Object>());
            }
            ((HashMap)dto.get(attrName)).put(attrObjKey, getParamValueByForce(entryValue));
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException ex) {
            //其它异常不处理
        }
    }

	/**
	 * 处理Controller参数中的对象、DTO或Map属性
	 * 这里特殊处理[数字]，且返回值为数组的
	 * @param dto
	 * @param clazz
	 * @param attrName
	 * @param entryValue
	 * @param <T>
	 */
	@SuppressWarnings("unchecked")
	private <T extends IDTO> void handleMapValue(DTO dto, Class<T> clazz, String attrName, Object entryValue){
		String attrObjKey = attrName.substring(attrName.lastIndexOf("[")+1, attrName.length()-1);
		//去掉属性名后面的[]
		attrName = attrName.substring(0, attrName.lastIndexOf("["));
		//有get方法的属性，需要判断返回值如果是Array或List，需要转换前台传的有多个相同name的value数组。
		Method getMethod = null;
		try {
			getMethod = clazz.getMethod("get"+ attrName.substring(0, 1).toUpperCase() + attrName.substring(1));
			//根据返回值判断value的类型
			Class<?> returnType = getMethod.getReturnType();
			//先初始化一个对象作为value
			if(dto.get(attrName) == null){
				if (returnType.isInterface() && IDTO.class.isAssignableFrom(returnType)){//初始化成DTO接口
					dto.put(attrName, DTOUtils.newInstance((Class<IDTO>)returnType));
				}//这里特殊处理[数字]为数组
				else if(StringUtils.isNumeric(attrObjKey)){
					dto.put(attrName, new ArrayList<>());
				}
				//未实现Map接口，初始化成普通Java对象
				else if (!Map.class.isAssignableFrom(returnType)){
					dto.put(attrName, returnType.newInstance());
				}
				else{//初始化成Map
					dto.put(attrName, new HashMap<>());
				}
			}
			//处理value中数据
			if (returnType.isInterface() && IDTO.class.isAssignableFrom(returnType)){
				Class<?> fieldType = getFieldType((Class<?>) returnType, attrObjKey);
				if(fieldType == null){
					((IDTO) dto.get(attrName)).aset(attrObjKey, getParamValueByForce(entryValue));
				}else {
					((IDTO) dto.get(attrName)).aset(attrObjKey, ReturnTypeHandlerFactory.convertValue(fieldType, getParamValueByForce(entryValue)));
				}
			}//这里特殊处理[数字]为数组
			else if(StringUtils.isNumeric(attrObjKey)){
				((ArrayList)dto.get(attrName)).add(Integer.parseInt(attrObjKey), getParamValueByForce(entryValue));
			}
			else if (!Map.class.isAssignableFrom(returnType)){
				Class<?> fieldType = getFieldType((Class<?>) returnType, attrObjKey);
				if(fieldType == null){
					PropertyUtils.setProperty(dto.get(attrName), attrObjKey, getParamValueByForce(entryValue));
				}else {
					PropertyUtils.setProperty(dto.get(attrName), attrObjKey, ReturnTypeHandlerFactory.convertValue(fieldType, getParamValueByForce(entryValue)));
				}
			}else{
				((HashMap)dto.get(attrName)).put(attrObjKey, getParamValueByForce(entryValue));
			}
		} catch (NoSuchMethodException e) {
			//没get方法的属性处理为HashMap
			if(dto.get(attrName) == null) {
				dto.put(attrName, new HashMap<String, Object>());
			}
			((HashMap)dto.get(attrName)).put(attrObjKey, getParamValueByForce(entryValue));
		} catch (IllegalAccessException | InstantiationException | InvocationTargetException ex) {
			//其它异常不处理
		}
	}

	/**
	 * 处理Controller参数中的List对象属性
	 * @param dto
	 * @param clazz
	 * @param attrName
	 * @param entryValue
	 * @param <T>
	 */
	@SuppressWarnings("unchecked")
	private <T extends IDTO> void handleListObjValue(DTO dto, Class<T> clazz, String attrName, Object entryValue){
		Object paramValue = null;
		String attrObjKey = attrName.substring(attrName.lastIndexOf("][")+2, attrName.length()-1);
		int index = Integer.valueOf(attrName.substring(attrName.indexOf("[")+1, attrName.lastIndexOf("][")));
		//去掉属性名后面的[]
		attrName = listObjPattern.matcher(attrName).replaceAll("");
		//初始化一个ArrayList
		if(dto.get(attrName) == null){
			dto.put(attrName, new ArrayList<Object>());
		}
		//有get方法的属性，需要判断返回值如果是Array或List，需要转换前台传的有多个相同name的value数组。
		Method getMethod = null;
		try {
			getMethod = clazz.getMethod("get"+ attrName.substring(0, 1).toUpperCase() + attrName.substring(1));
			//根据返回值判断value的类型
			Class<?> returnType = getMethod.getReturnType();
			//如果返回类型是List并且带泛型参数(第二个判断是判断是否有泛型，这里是为了避免编译警告)
			//第二个判断也可以改为getter.getGenericReturnType() instanceof java.lang.reflect.ParameterizedType
			if(List.class.isAssignableFrom(getMethod.getReturnType())
					&& getMethod.getGenericReturnType().getTypeName().endsWith(">")) {
				Type retType = ((java.lang.reflect.ParameterizedType) getMethod.getGenericReturnType()).getActualTypeArguments()[0];
				//如果泛型参数是Class类，这里应该都是Class
				if ((retType.getClass() instanceof Class<?>)) {
					//并且泛型参数是IDTO接口
					if (IDTO.class.isAssignableFrom((Class<?>) retType) && ((Class<?>) retType).isInterface()) {
						ArrayList<Object> list = ((ArrayList<Object>)dto.get(attrName));
						if(CollectionUtils.isEmpty(list) || list.size() <= index){
							IDTO idto = DTOUtils.newInstance((Class<IDTO>) retType);
							list.add(index, idto);
						}
						Class<?> fieldType = getFieldType((Class<?>) retType, attrObjKey);
						if(fieldType == null){
							((IDTO) list.get(index)).aset(attrObjKey, getParamValueByForce(entryValue));
						}else{
							((IDTO) list.get(index)).aset(attrObjKey, ReturnTypeHandlerFactory.convertValue(fieldType, getParamValueByForce(entryValue)));
						}
					}else if(Map.class.isAssignableFrom((Class<?>) retType)){
						ArrayList<Object> list = ((ArrayList<Object>)dto.get(attrName));
						if(CollectionUtils.isEmpty(list) || list.size() <= index){
							Map<String, Object> map = new HashMap<String, Object>();
							list.add(index, map);
						}
						((Map) list.get(index)).put(attrObjKey, getParamValueByForce(entryValue));
					}else{ // Java Bean
						ArrayList<Object> list = ((ArrayList<Object>)dto.get(attrName));
						if(CollectionUtils.isEmpty(list) || list.size() <= index){
							Object obj = ((Class<?>) retType).newInstance();
							list.add(index, obj);
						}
						Class<?> fieldType = getFieldType((Class<?>) retType, attrObjKey);
						if(fieldType == null){
							PropertyUtils.setProperty(list.get(index), attrObjKey, getParamValueByForce(entryValue));
						}else{
							PropertyUtils.setProperty(list.get(index), attrObjKey, ReturnTypeHandlerFactory.convertValue(fieldType, getParamValueByForce(entryValue)));
						}
					}
				}
			}else{//没有泛型参数，处理为HashMap
				ArrayList<Object> list = ((ArrayList<Object>)dto.get(attrName));
				if(CollectionUtils.isEmpty(list) || list.size() <= index){
					Map<String, Object> map = new HashMap<String, Object>();
					list.add(index, map);
				}
				((Map<String, Object>) list.get(index)).put(attrObjKey, getParamValueByForce(entryValue));
			}
		} catch (NoSuchMethodException e) {
			//没get方法的属性处理为HashMap
			ArrayList<Object> list = ((ArrayList<Object>)dto.get(attrName));
			if(CollectionUtils.isEmpty(list) || list.size() <= index){
				Map<String, Object> map = new HashMap<String, Object>();
				list.add(index, map);
			}
			((Map<String, Object>) list.get(index)).put(attrObjKey, getParamValueByForce(entryValue));
		} catch (IllegalAccessException | InstantiationException | InvocationTargetException ex) {
			//其它异常不处理
		}
	}

	/**
	 * 搜索clazz的fieldName属性，返回其类型
	 * @param clazz
	 * @param fieldName
	 * @return
	 */
	private Class<?> getFieldType(Class<?> clazz, String fieldName){
		for(Method method : clazz.getMethods()){
			//getter方法，参数个数为0，字段名相同
			if(POJOUtils.isGetMethod(method) && method.getParameterTypes().length == 0 && fieldName.equals(POJOUtils.getBeanField(method))){
				return method.getReturnType();
			}
		}
		return null;
	}

	/**
	 * 处理Controller参数中的List属性
	 * @param dto
	 * @param clazz
	 * @param attrName
	 * @param entryValue
	 * @param <T>
	 */
	@SuppressWarnings("unchecked")
	private <T extends IDTO> void handleListValue(DTO dto, Class<T> clazz, String attrName, Object entryValue){
		Object paramValue = null;
		int index = attrName.lastIndexOf("[");
		//去掉属性名后面的[]
		attrName = index >= 0 ? attrName.substring(0, index) : attrName;
		try {
			//有get方法的属性，需要判断返回值如果是Array或List，需要转换前台传的有多个相同name的value数组。
			Method getMethod = clazz.getMethod("get"+ attrName.substring(0, 1).toUpperCase() + attrName.substring(1));
			Class<?> returnType = getMethod.getReturnType();
			//List需要转换数组
			//这里entry.getValue()肯定是String[]
			if(List.class.isAssignableFrom(returnType)){
				paramValue = Lists.newArrayList((Object[])getParamObjValue(entryValue));
			}else if(returnType.isArray()){
				paramValue = getParamObjValue(entryValue);
			}else{//默认就是数组
				paramValue = getParamObjValue(entryValue);
			}
		} catch (NoSuchMethodException e) {
			//没get方法的属性处理为List
			paramValue = Lists.newArrayList((Object[])getParamObjValue(entryValue));
		}
		if(paramValue == null) {
			paramValue = getParamValueByForce(entryValue);
		}
		dto.put(attrName, paramValue);
	}

	/**
	 * 处理restful接口的DTO参数
	 * @param clazz
	 * @param webRequest
	 * @param parameter
	 * @param <T>
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private DTO getDTO4Restful(Map<String, Class<?>> fields, NativeWebRequest webRequest, MethodParameter parameter) {
		// 实例化一个DTO数据对象
		DTO dto = new DTO();
		try {
//			ServletInputStream servletInputStream = ((RequestFacade)webRequest.getNativeRequest()).getInputStream();
			String inputString = webRequest.getNativeRequest() instanceof RequestReaderHttpServletRequestWrapper ? getBodyString((RequestReaderHttpServletRequestWrapper)webRequest.getNativeRequest()) : "";
			//下面的方法不能重复读取
//			ServletInputStream servletInputStream = (((RequestReaderHttpServletRequestWrapper)webRequest.getNativeRequest()).getInputStream());
//			inputString = InputStream2String(servletInputStream, "UTF-8");
			if(StringUtils.isNotBlank(inputString)) {
				JSONObject jsonObject = null;
				try {
					inputString = java.net.URLDecoder.decode(inputString, "UTF-8");
				} catch (UnsupportedEncodingException | IllegalArgumentException e) {
				}
				if(JSON.isValid(inputString)) {
					jsonObject = JSONObject.parseObject(inputString);
				}else{
					jsonObject = JSONObject.parseObject(getJsonStrByQueryUrl(inputString));
				}
				for(Map.Entry<String, Object> entry : jsonObject.entrySet()){
					//单独处理metadata
					if(entry.getKey().startsWith("metadata[") && entry.getKey().endsWith("]")){
						dto.setMetadata(entry.getKey().substring(9, entry.getKey().length()-1), entry.getValue());
					}else{
						dto.put(entry.getKey(), getParamValueAndConvert(entry, fields));
					}
				}
			}
			return dto;
		} catch (Exception e) {
			e.printStackTrace();
			return dto;
		}
	}

	/**
	 * 如果有Validated注解，设置异常信息到IDTO.ERROR_MSG_KEY属性中
	 * @param t
	 * @param parameter
	 * @param <T>
	 */
	@SuppressWarnings("unchecked")
	private <T extends IDTO> void asetErrorMsg(T t, MethodParameter parameter){
		Validated validated = parameter.getParameter().getAnnotation(Validated.class);
		//有Validated注解则进行校验
		if(validated != null) {
			t.aset(IDTO.ERROR_MSG_KEY, BeanValidator.validator(t, validated.value()));
		}
	}

	/**
	 * 转换DTO值
     * 用于处理restful中的参数
	 * @param entry
	 * @param clazz
	 * @param <T>
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private <T extends IDTO> Object convertValue(Map.Entry<String, Object> entry, Class<T> clazz){
		Method getter = null;
		try {
			getter = clazz.getMethod("get"+ entry.getKey().substring(0, 1).toUpperCase() + entry.getKey().substring(1));
		} catch (NoSuchMethodException e) {
			try {
				getter = clazz.getMethod("is"+ entry.getKey().substring(0, 1).toUpperCase() + entry.getKey().substring(1));
			} catch (NoSuchMethodException e1) {
			}
		}
		if(getter == null){
			return entry.getValue();
		}
		//如果返回类型是List并且带泛型参数(第二个判断是判断是否有泛型，这里是为了避免编译警告)
		//第二个判断也可以改为getter.getGenericReturnType() instanceof java.lang.reflect.ParameterizedType
		if(List.class.isAssignableFrom(getter.getReturnType())
				&& getter.getGenericReturnType().getTypeName().endsWith(">")){
			Type retType = ((java.lang.reflect.ParameterizedType)getter.getGenericReturnType()).getActualTypeArguments()[0];
			//如果泛型参数是Class类，这里应该都是Class
			if((retType.getClass() instanceof Class<?>)){
				//并且泛型参数是IDTO接口
				if(IDTO.class.isAssignableFrom((Class<?>)retType) && ((Class<?>)retType).isInterface()) {
					List<Object> values = (List<Object>) entry.getValue();
					if (CollectionUtils.isEmpty(values)) {
						return values;
					}
					List<Object> convertedValues = new ArrayList<Object>(values.size());
					values.stream().forEach(v -> {
						if (!(v instanceof Map)) {
							return;
						}
						convertedValues.add(DTOUtils.proxyInstance(new DTO((Map) v), (Class<IDTO>) retType));
					});
					return convertedValues;
				}else{//根据泛型参数转换类型
					List<Object> values = (List) entry.getValue();
					if (CollectionUtils.isEmpty(values)) {
						return values;
					}
					//如果List中第一个对象的类型和返回类型一致，则不处理
					if(values.get(0).getClass().equals(retType)){
						return values;
					}
                    List<Object> convertedValues = new ArrayList<Object>(values.size());
					values.stream().forEach(v -> {
                        //通过转换工厂减少if else， 提高效率
                        Object convertedValue = ReturnTypeHandlerFactory.convertValue((Class<?>)retType, v);
                        if(convertedValue != null){
                            convertedValues.add(convertedValue);
                        }
					});
					return convertedValues;
				}
			}
		}
		//如果返回类型是IDTO接口
		else if(IDTO.class.isAssignableFrom(getter.getReturnType()) && getter.getReturnType().isInterface()){
			Object obj = entry.getValue();
			if(!(obj instanceof Map)) {
				return obj;
			}
			return DTOUtils.proxyInstance(new DTO((Map)obj), (Class<IDTO>) getter.getReturnType());
		}
		return entry.getValue();
	}


	/**
	 * 强制取参数的值，支持转型
	 *
	 * @param entry
	 *            当前的值对象
	 * @return 如果返回的字符串为空串,则认为是null
	 */
	@SuppressWarnings("unchecked")
	private Object getParamValuesAndConvert(Map.Entry<String, String[]> entry, Map<String, Class<?>> fields) {
		if(entry == null || entry.getValue() == null){
			return null;
		}
		String val = getParamValue(entry.getValue());
		if(StringUtils.isEmpty(val)){
			val = null;
		}
		if(fields.containsKey(entry.getKey())){
			if(String.class.equals(fields.get(entry.getKey()))){
				return val;
			}
			return ReturnTypeHandlerFactory.convertValue(fields.get(entry.getKey()), val);
		}
		return val == null ? null : StringUtils.isBlank(val) ? null : val;
	}

	private Object getParamValueAndConvert(Map.Entry<String, Object> entry, Map<String, Class<?>> fields) {
		if(entry == null || entry.getValue() == null){
			return null;
		}
		Object val = entry.getValue();
		if(fields.containsKey(entry.getKey())){
			if(String.class.equals(fields.get(entry.getKey()))){
				return val.toString();
			}
			return ReturnTypeHandlerFactory.convertValue(fields.get(entry.getKey()), val);
		}
		return val == null ? null : val;
	}

	/**
	 * 强制取参数的值
	 *
	 * @param obj
	 *            当前的值对象
	 * @return 如果返回的字符串为空串,则认为是null
	 */
	@SuppressWarnings("unchecked")
	private String getParamValueByForce(Object obj) {
		String val = getParamValue(obj);
		return val == null ? null : StringUtils.isBlank(val) ? null : val;
	}

	/**
	 * 取参数的对象值
	 * @param obj
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Object getParamObjValue(Object obj) {
		return obj == null ? null : obj.getClass().isArray() ? java.io.File.class.isAssignableFrom(((Object[]) obj)[0].getClass()) ? null  : obj : obj;
	}

	/**
	 * 直接从值对象中取得其值<br>
	 * 由于Structs将值全部处理成了数组,但在通用情况下都是取数组中的一个值,但对Radio和checkbox等情况,可能会有多个值
	 *
	 * @param obj
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private String getParamValue(Object obj) {
		return (String) (obj == null ? null : obj.getClass().isArray() ? java.io.File.class.isAssignableFrom(((Object[]) obj)[0].getClass()) ? null  : ((Object[]) obj)[0] : obj);
	}

	final static int BUFFER_SIZE = 4096;
	/**
	 * 将InputStream转换成某种字符编码的String
	 * @param in
	 * @param encoding
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private String InputStream2String(InputStream in, String encoding) throws IOException {
		BufferedInputStream bufferedInputStream = new BufferedInputStream(in);
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		bufferedInputStream.mark(0);
		byte[] data = new byte[BUFFER_SIZE];
		int count = -1;
		while((count = bufferedInputStream.read(data,0,BUFFER_SIZE)) != -1) {
			outStream.write(data, 0, count);
		}
		bufferedInputStream.reset();
		data = null;
		return new String(outStream.toByteArray(), encoding);
	}

	/**
	 * 获取请求Body
	 * 可重复获取
	 * @param request 过滤后被的request
	 * @return 返回body
	 */
	private String getBodyString(ServletRequest request) {
		StringBuilder sb = new StringBuilder();
		InputStream inputStream = null;
		BufferedReader reader = null;
		try {
			inputStream = request.getInputStream();
			reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
			String line = "";
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
		} catch (Exception e) {
			throw new AppException("获取requestBody出错：" + e.getMessage());
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
				if (reader != null) {
					reader.close();
				}
			} catch (IOException ignored) {
			}
		}
		return sb.toString();
	}

	/**
	 * 将url参数转为json对象
	 *
	 * @param paramStr
	 * @returns {{}}
	 */
	public static String getJsonStrByQueryUrl(String paramStr){
		//String paramStr = "a=a1&b=b1&c=c1";
		String[] params = paramStr.split("&");
		JSONObject obj = new JSONObject();
		for (int i = 0; i < params.length; i++) {
			String[] param = params[i].split("=");
			if (param.length >= 2) {
				String key = param[0];
				String value = param[1];
				for (int j = 2; j < param.length; j++) {
					value += "=" + param[j];
				}
				try {
					obj.put(key,value);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		return obj.toString();
	}
}
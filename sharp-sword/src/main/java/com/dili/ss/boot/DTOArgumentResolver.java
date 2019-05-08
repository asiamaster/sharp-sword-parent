package com.dili.ss.boot;

import com.alibaba.fastjson.JSONObject;
import com.dili.ss.dto.DTO;
import com.dili.ss.dto.DTOUtils;
import com.dili.ss.dto.IDTO;
import com.dili.ss.dto.ReturnTypeHandlerFactory;
import com.dili.ss.util.BeanValidator;
import com.google.common.collect.Lists;
import org.apache.catalina.connector.RequestFacade;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.ServletInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
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
public class DTOArgumentResolver implements HandlerMethodArgumentResolver {

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return IDTO.class.isAssignableFrom(parameter.getParameterType()) && parameter.getParameterType().isInterface();
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		Class clazz = parameter.getParameterType();
		if(clazz != null && IDTO.class.isAssignableFrom(clazz)){
			return getDTO(clazz, webRequest, parameter);
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
		if(webRequest.getParameterMap().isEmpty()){
			return getDTO4Restful(clazz, webRequest, parameter);
		}
		// 实例化一个DTO数据对象
		DTO dto = new DTO();
		// 填充值
		for (Map.Entry<String, String[]> entry : webRequest.getParameterMap().entrySet()) {
			String attrName = entry.getKey();
			//处理metadata，目前只支持一层
			if(attrName.startsWith("metadata[") && attrName.endsWith("]")){
				dto.setMetadata(attrName.substring(9, attrName.length()-1), getParamValueByForce(entry.getValue()));
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
				else if(attrName.endsWith("]") && attrName.contains("[")){
					handleMapValue(dto, clazz, attrName, entry.getValue());
				}
				//处理前台传参的key为xxx.xxx形式，设置参数的对象、DTO或Map属性
				else if(attrName.split("\\.").length == 2){
                    handleDotMapValue(dto, clazz, attrName, entry.getValue());
                }
                //处理普通属性
				else{
					dto.put(attrName, getParamValueByForce(entry.getValue()));
				}
			}else{
                dto.put(attrName, getParamValueByForce(entry.getValue()));
            }
		}
		T t = (T) DTOUtils.proxy(dto, (Class<IDTO>) clazz);
		asetErrorMsg(t, parameter);
		return t;
	}

    /**
     * 处理前台传参的key为xxx.xxx形式，设置参数的对象、DTO或Map属性
     * @param dto
     * @param clazz
     * @param attrName
     * @param entryValue
     * @param <T>
     */
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
                    dto.put(attrName, DTOUtils.newDTO((Class<IDTO>)returnType));
                }else if (!Map.class.isAssignableFrom(returnType)){//未实现Map接口，初始化成普通Java对象
                    dto.put(attrName, returnType.newInstance());
                }else{//初始化成Map
                    dto.put(attrName, new HashMap<>());
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
                dto.put(attrName, new HashMap<>());
            }
            ((HashMap)dto.get(attrName)).put(attrObjKey, getParamValueByForce(entryValue));
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException ex) {
            //其它异常不处理
        }
    }

	/**
	 * 处理Controller参数中的对象、DTO或Map属性
	 * @param dto
	 * @param clazz
	 * @param attrName
	 * @param entryValue
	 * @param <T>
	 */
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
					dto.put(attrName, DTOUtils.newDTO((Class<IDTO>)returnType));
				}else if (!Map.class.isAssignableFrom(returnType)){//未实现Map接口，初始化成普通Java对象
					dto.put(attrName, returnType.newInstance());
				}else{//初始化成Map
					dto.put(attrName, new HashMap<>());
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
				dto.put(attrName, new HashMap<>());
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
	private <T extends IDTO> void handleListObjValue(DTO dto, Class<T> clazz, String attrName, Object entryValue){
		Object paramValue = null;
		String attrObjKey = attrName.substring(attrName.lastIndexOf("][")+2, attrName.length()-1);
		int index = Integer.valueOf(attrName.substring(attrName.indexOf("[")+1, attrName.lastIndexOf("][")));
		//去掉属性名后面的[]
		attrName = listObjPattern.matcher(attrName).replaceAll("");
		//初始化一个ArrayList
		if(dto.get(attrName) == null){
			dto.put(attrName, new ArrayList<>());
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
				if ((retType.getClass() instanceof Class)) {
					//并且泛型参数是IDTO接口
					if (IDTO.class.isAssignableFrom((Class<?>) retType) && ((Class<?>) retType).isInterface()) {
						ArrayList list = ((ArrayList)dto.get(attrName));
						if(CollectionUtils.isEmpty(list) || list.size() <= index){
							IDTO idto = DTOUtils.newDTO((Class<IDTO>) retType);
							list.add(index, idto);
						}
						((IDTO) list.get(index)).aset(attrObjKey, getParamValueByForce(entryValue));
					}else if(Map.class.isAssignableFrom((Class<?>) retType)){
						ArrayList list = ((ArrayList)dto.get(attrName));
						if(CollectionUtils.isEmpty(list) || list.size() <= index){
							Map map = new HashMap();
							list.add(index, map);
						}
						((Map) list.get(index)).put(attrObjKey, getParamValueByForce(entryValue));
					}else{ // Java Bean
						ArrayList list = ((ArrayList)dto.get(attrName));
						if(CollectionUtils.isEmpty(list) || list.size() <= index){
							Object obj = ((Class<?>) retType).newInstance();
							list.add(index, obj);
						}
						PropertyUtils.setProperty(list.get(index), attrObjKey, getParamValueByForce(entryValue));
					}
				}
			}else{//没有泛型参数，处理为HashMap
				ArrayList list = ((ArrayList)dto.get(attrName));
				if(CollectionUtils.isEmpty(list) || list.size() <= index){
					Map map = new HashMap();
					list.add(index, map);
				}
				((Map) list.get(index)).put(attrObjKey, getParamValueByForce(entryValue));
			}
		} catch (NoSuchMethodException e) {
			//没get方法的属性处理为HashMap
			ArrayList list = ((ArrayList)dto.get(attrName));
			if(CollectionUtils.isEmpty(list) || list.size() <= index){
				Map map = new HashMap();
				list.add(index, map);
			}
			((Map) list.get(index)).put(attrObjKey, getParamValueByForce(entryValue));
		} catch (IllegalAccessException | InstantiationException | InvocationTargetException ex) {
			//其它异常不处理
		}
	}

	/**
	 * 处理Controller参数中的List属性
	 * @param dto
	 * @param clazz
	 * @param attrName
	 * @param entryValue
	 * @param <T>
	 */
	private <T extends IDTO> void handleListValue(DTO dto, Class<T> clazz, String attrName, Object entryValue){
		Object paramValue = null;
		//去掉属性名后面的[]
		attrName = attrName.substring(0, attrName.length()-2);
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
	private  <T extends IDTO> T getDTO4Restful(Class<T> clazz, NativeWebRequest webRequest, MethodParameter parameter) {
		// 实例化一个DTO数据对象
		DTO dto = new DTO();
		try {
			ServletInputStream servletInputStream = ((RequestFacade)webRequest.getNativeRequest()).getInputStream();
			String inputString = InputStream2String(servletInputStream, "UTF-8");
			if(StringUtils.isNotBlank(inputString)) {
				JSONObject jsonObject = JSONObject.parseObject(inputString);
				for(Map.Entry<String, Object> entry : jsonObject.entrySet()){
					//单独处理metadata
					if(entry.getKey().startsWith("metadata[") && entry.getKey().endsWith("]")){
						dto.setMetadata(entry.getKey().substring(9, entry.getKey().length()-1), entry.getValue());
					}else{
						dto.put(entry.getKey(), convertValue(entry, clazz));
					}
				}
			}
			T t = (T)DTOUtils.proxy(dto, clazz);
			asetErrorMsg(t, parameter);
			return t;
		} catch (IOException e) {
			e.printStackTrace();
			return DTOUtils.proxy(dto, clazz);
		}
	}

	/**
	 * 如果有Validated注解，设置异常信息到IDTO.ERROR_MSG_KEY属性中
	 * @param t
	 * @param parameter
	 * @param <T>
	 */
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
	@SuppressWarnings("all")
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
			if((retType.getClass() instanceof Class)){
				//并且泛型参数是IDTO接口
				if(IDTO.class.isAssignableFrom((Class<?>)retType) && ((Class<?>)retType).isInterface()) {
					List values = (List) entry.getValue();
					if (CollectionUtils.isEmpty(values)) {
						return values;
					}
					List convertedValues = new ArrayList(values.size());
					values.stream().forEach(v -> {
						if (!(v instanceof Map)) {
							return;
						}
						convertedValues.add(DTOUtils.proxy(new DTO((Map) v), (Class<IDTO>) retType));
					});
					return convertedValues;
				}else{//根据泛型参数转换类型
					List values = (List) entry.getValue();
					if (CollectionUtils.isEmpty(values)) {
						return values;
					}
					//如果List中第一个对象的类型和返回类型一致，则不处理
					if(values.get(0).getClass().equals(retType)){
						return values;
					}
                    List convertedValues = new ArrayList(values.size());
					values.stream().forEach(v -> {
                        //通过转换工厂减少if else， 提高效率
                        Object convertedValue = ReturnTypeHandlerFactory.convertValue((Class)retType, v);
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
			return DTOUtils.proxy(new DTO((Map)obj), (Class<IDTO>) getter.getReturnType());
		}
		return entry.getValue();
	}

	/**
	 * 强制取参数的值
	 *
	 * @param obj
	 *            当前的值对象
	 * @return 如果返回的字符串为空串,则认为是null
	 */
	private String getParamValueByForce(Object obj) {
		String val = getParamValue(obj);
		return val == null ? null : StringUtils.isBlank(val) ? null : val;
	}

	/**
	 * 取参数的对象值
	 * @param obj
	 * @return
	 */
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
	public static String InputStream2String(InputStream in, String encoding) throws IOException {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] data = new byte[BUFFER_SIZE];
		int count = -1;
		while((count = in.read(data,0,BUFFER_SIZE)) != -1) {
			outStream.write(data, 0, count);
		}
		data = null;
		return new String(outStream.toByteArray(), encoding);
	}

}

package com.dili.ss.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.dili.ss.util.POJOUtils;
import org.apache.commons.collections.map.HashedMap;

import javax.persistence.Transient;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * Created by asiamaster on 2017/7/31 0031.
 */
public interface IDTO extends Serializable {
	//用于BaseServiceImpl中构建自定义Example条件的Key，请从metadata中获取和操作

	String AND_CONDITION_EXPR = "_andConditionExpr";

	//强制空值, DTO或实体的metadata中的key，用于BaseServiceImpl中的ByExample查询
	@Transient
	String NULL_VALUE_FIELD = "null_value_field";
	//错误消息的key
	String ERROR_MSG_KEY = "errorMessage";
	/**
	 * 代理取值
	 *
	 * @param property
	 * @return
	 */
	default Object aget(String property){
		return POJOUtils.getProperty(this, property);
	}

	/**
	 * 代理取值
	 *
	 * @return
	 */
	default DTO aget(){
		return new DTO();
	}

	/**
	 * 代理设置
	 *
	 * @param property
	 * @param value
	 */
	default void aset(String property, Object value){
		POJOUtils.setProperty(this, property, value);
	}

	/**
	 * 代理设置
	 *
	 * @param dto
	 */
	default void aset(DTO dto){
	}

	/**
	 * meta取值
	 * @param key
	 * @return
	 */
	default Object mget(String key){return null;}

	/**
	 * meta取所有值
	 * @return
	 */
	default Map<String, Object> mget(){return new HashedMap();}

	/**
	 * meta设值
	 * @param key
	 * @param value
	 */
	default void mset(String key, Object value){}

	/**
	 * meta设值
	 * @return
	 */
	default void mset(Map<String, Object> metadata){}

	/**
	 * 获取所有属性
	 * @return
	 */
	@JSONField(serialize=false)
	@Transient
	default Map<String, Field> getFields(){return null;}
}

package com.dili.ss.metadata;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.dili.ss.dto.DTOUtils;
import com.dili.ss.dto.IDTO;
import com.dili.ss.dto.IDomain;
import com.dili.ss.util.BeanConver;
import com.dili.ss.util.POJOUtils;
import com.dili.ss.util.SpringUtil;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 值提供者工具类
 *
 * @author ASIAMASTER
 * @create 2017-5-30
 */
@Component
public class ValueProviderUtils {

	//原始值保留前缀
	public static final String ORIGINAL_KEY_PREFIX = "$_";

	protected static final Logger LOGGER = LoggerFactory.getLogger(ValueProviderUtils.class);
	@Autowired
	private Map<String, ValueProvider> valueProviderMap;

	/**
	 * 根据Provider构造表格显示数据，保留原始值，前缀为ORIGINAL_KEY_PREFIX
	 *
	 * @param domain
	 * @param list
	 * @param <T>
	 * @return
	 * @throws Exception
	 */
	public static <T extends IDomain> List<Map> buildDataByProvider(T domain, List list) throws Exception {
		Map metadata = null;
		if (DTOUtils.isProxy(domain) || DTOUtils.isInstance(domain)) {
			metadata = domain.mget();
		} else {
			metadata = domain.getMetadata();
		}
		return buildDataByProvider(metadata, list, MetadataUtils.getDTOMeta(DTOUtils.getDTOClass(domain)));
	}

	/**
	 * 根据Provider构造表格显示数据，保留原始值，前缀为ORIGINAL_KEY_PREFIX
	 *
	 * @param medadata
	 * @param list
	 * @param <T>
	 * @return
	 * @throws Exception
	 */
	public static <T extends IDomain> List<Map> buildDataByProvider(Map medadata, List list) throws Exception {
		return buildDataByProvider(medadata, list, null);
	}

	/**
	 * 根据Provider构造表格显示数据，保留原始值，前缀为ORIGINAL_KEY_PREFIX
	 * @param medadata
	 * @param list
	 * @param objectMeta
	 * @param <T>
	 * @return
	 * @throws Exception
	 */
	public static <T extends IDomain> List<Map> buildDataByProvider(Map medadata, List list, ObjectMeta objectMeta) throws Exception {
		if(medadata == null){
			medadata = Maps.newHashMap();
		}
		if(objectMeta != null) {
			for (FieldMeta fieldMeta : objectMeta) {
				if (StringUtils.isNotBlank(fieldMeta.getProvider())) {
					JSONObject meta = new JSONObject();
					meta.put("field", fieldMeta.getName());
					meta.put("index", fieldMeta.getIndex());
					meta.put("provider", fieldMeta.getProvider());
					meta.put("queryParams", fieldMeta.getParams());
					medadata.put(fieldMeta.getName(), meta);
				}
			}
		}
		if (medadata.isEmpty()) {
			return list;
		}
		//复制一个出来，避免修改，这里用putAll进行简单的深拷贝就行了，因为只是删除元素进行性能优化
		Map metadataCopy = new HashMap(medadata.size());
		metadataCopy.putAll(medadata);
//		构建metadata的字符串value
		convertStringProvider(metadataCopy);
		//将map.entrySet()转换成list，再进行排序
		List<Map.Entry<String, Object>> metadataCopyList = new ArrayList<Map.Entry<String, Object>>(metadataCopy.entrySet());
		Collections.sort(metadataCopyList, (o1, o2) -> {
			try {
				JSONObject jsonValue1 = o1.getValue() instanceof JSONObject ? (JSONObject) o1.getValue() : JSONObject.parseObject(o1.getValue().toString());
				JSONObject jsonValue2 = o2.getValue() instanceof JSONObject ? (JSONObject) o2.getValue() : JSONObject.parseObject(o2.getValue().toString());
				int index1 = Integer.parseInt(jsonValue1.getOrDefault(ValueProvider.INDEX_KEY, "0").toString());
				int index2 = Integer.parseInt(jsonValue2.getOrDefault(ValueProvider.INDEX_KEY, "0").toString());
				return index1 > index2 ? 1 : index1 < index2 ? -1 : 0;
			} catch (JSONException e) {
				return 0;
			} catch (Exception e){
				return 0;
			}
		});
		//返回提供者转义后的列表
		List<Map> results = new ArrayList<>(list.size());
		for (Object t : list) {
			results.add(BeanConver.transformObjectToMap(t));
		}
		Iterator<Map.Entry<String, Object>> it = metadataCopyList.iterator();
		while(it.hasNext()){
			Map.Entry<String, Object> entry = it.next();
			BatchValueProvider batchValueProvider = null;
			//key是字段field
			String key = entry.getKey();
			//value是provider相关的json对象
			JSONObject jsonValue = null;
			try {
				jsonValue = entry.getValue() instanceof String ? JSONObject.parseObject((String)entry.getValue()) : (JSONObject)JSONObject.toJSON(entry.getValue());
			} catch (JSONException e) {
				continue;
			}
			if(jsonValue.get(ValueProvider.FIELD_KEY) == null){
				jsonValue.put(ValueProvider.FIELD_KEY, key) ;
			}
			String providerBeanId = jsonValue.get("provider").toString();
			Object bean = SpringUtil.getBean(providerBeanId);
			if (bean instanceof BatchValueProvider) {
				batchValueProvider = (BatchValueProvider) bean;
				//批量设置列表
				try {
					batchValueProvider.setDisplayList(results, jsonValue, objectMeta);
				}catch (Exception e){
					e.printStackTrace();
					LOGGER.error("批量提供者报错:"+batchValueProvider.getClass().getName());
				}
			} else {//普通值提供者
				String field = key;
				String childField = null;
				boolean hasChild = false;
				if(field.contains(".")) {
					childField = field.substring(field.indexOf(".") + 1, field.length());
					field = field.substring(0, field.indexOf("."));
					hasChild = true;
				}
				for (Map dataMap : results) {
					//处理provider中field为object.field的场景，只取一级对象，框架不主动获取未知对象的属性
					String dataKey = key.contains(".") ? key.substring(0, key.indexOf(".")) : key;
					jsonValue.put(ValueProvider.ROW_DATA_KEY, dataMap);
					ValueProvider valueProvider = (ValueProvider) bean;
					FieldMeta fieldMeta = objectMeta == null ? null : objectMeta.getFieldMetaById(dataKey);
					try {
						String text = valueProvider.getDisplayText(dataMap.get(dataKey), jsonValue, fieldMeta);
						//保留原值，用于在修改时提取表单加载，但是需要过滤掉日期类型，
						// 因为前台无法转换Long类型的日期格式,并且也没法判断是日期格式
						// 配合批量提供者处理，如果转换后的显示值返回null，则不保留原值
						if (text != null &&  !(dataMap.get(dataKey) instanceof Date)) {
							//记录原始值
							if(hasChild){
								//如果有子对象，则原始值格式为object.$_field
								String originalKey = new StringBuilder(field).append(".").append(ValueProviderUtils.ORIGINAL_KEY_PREFIX).append(childField).toString();
								Object fkValueObj = dataMap.get(dataKey);
								if(fkValueObj != null){
									Object fkValue = getObjectValueByKey(fkValueObj, childField);
									if(fkValue != null){
										dataMap.put(originalKey, fkValue);
									}
								}
							}else {
								dataMap.put(ORIGINAL_KEY_PREFIX + field, dataMap.get(dataKey));
							}
						}
						//批量提供者只put转换后不为null的值
						if(text != null && valueProvider instanceof BatchValueProvider) {
							dataMap.put(key, text);
							//普通值提供者put所有转化后的值(无论是否为空)
						}else if(!(valueProvider instanceof BatchValueProvider)){
							dataMap.put(key, text);
						}
					}catch (Exception e){
						e.printStackTrace();
						LOGGER.error("提供者报错:"+valueProvider.getClass().getName());
					}
				}//end of for
			}// end of else
		}
		return results;
	}
	/**
	 * 根据key获取对象中的属性，key支持obj.field形式
	 * @param obj
	 * @param key
	 */
	private static Object getObjectValueByKey(Object obj, String key){
		if(obj instanceof Map){
			Map map = (Map)obj;
			return map.get(key);
		}else if(IDTO.class.isAssignableFrom(DTOUtils.getDTOClass(obj))){
			//代理类
			if(DTOUtils.isProxy(obj)){
				return ((IDTO) obj).aget(key);
			}//实例类
			else{
				return POJOUtils.getProperty(obj, key);
			}
		}
		//其它javaBean
		return POJOUtils.getProperty(obj, key);
	}
	/**
	 * 根据providerId取提供者对象
	 *
	 * @param providerId
	 * @return
	 */
	public ValueProvider getProviderObject(String providerId) {
		return valueProviderMap.get(providerId);
	}

	/**
	 * 根据providerId取显示的文本值
	 *
	 * @return
	 */
	public String getDisplayText(String providerId, Object obj, Map<String, Object> paramMap) {
		ValueProvider providerObj = valueProviderMap.get(providerId);
		return providerObj == null ? "" : providerObj.getDisplayText(obj, paramMap, null);
	}

	/**
	 * 根据FieldMeta取显示的文本值
	 *
	 * @param fieldMeta
	 * @param theVal
	 * @param paramMap
	 * @return
	 */
	public String getDisplayText(FieldMeta fieldMeta, Object theVal, Map<String, Object> paramMap) {
		assert (fieldMeta.getProvider() != null);
		ValueProvider providerObj = valueProviderMap.get(fieldMeta.getProvider());
		return providerObj == null ? "" : providerObj.getDisplayText(theVal, paramMap, fieldMeta);
	}

	/**
	 * 清除某个特定的Provider
	 *
	 * @param providerId
	 */
	public void clearProvider(String providerId) {
		valueProviderMap.remove(providerId);
	}

	/**
	 * 清除全部的缓冲
	 */
	public void clearProviders() {
		valueProviderMap.clear();
	}

	/**
	 * 根据providerId取下拉项
	 */
	public List<ValuePair<?>> getLookupList(String providerId, Object val, Map<String, Object> paramMap) {
		ValueProvider providerObj = valueProviderMap.get(providerId);
		Object queryParamsObj = paramMap.get(ValueProvider.QUERY_PARAMS_KEY);
		String emptyText = ValueProvider.EMPTY_ITEM_TEXT;
		List<ValuePair<?>> valuePairs =  providerObj == null ? Collections.EMPTY_LIST : providerObj.getLookupList(val, paramMap, null);
		if(valuePairs == null) {
			valuePairs = new ArrayList<ValuePair<?>>(1);
		}
		if(queryParamsObj != null){
			//获取查询参数
			JSONObject queryParams = JSONObject.parseObject(queryParamsObj.toString());
			//获取自定义空值显示内容
			String customEmptyText = queryParams.getString(ValueProvider.EMPTY_ITEM_TEXT_KEY);
			if(customEmptyText != null){
				emptyText = customEmptyText;
			}
			//获取是否必填
			Boolean required = queryParams.getBoolean(ValueProvider.REQUIRED_KEY);
			//非必填才在首位添加空值内容
			if(required == null || required.equals(false)){
				//有提供者信息， 值对列表不为空，且第一位不是空串，则添加“请选择”
				if(providerObj != null && !valuePairs.isEmpty() && !"".equals(valuePairs.get(0).getValue())) {
					valuePairs.add(0, new ValuePairImpl<String>(emptyText, ""));
				}
			}
		}else{
			if(providerObj != null && !valuePairs.isEmpty() && !"".equals(valuePairs.get(0).getValue())) {
				valuePairs.add(0, new ValuePairImpl<String>(emptyText, ""));
			}
        }
		return valuePairs;
	}

	/**
	 * 根据FieldMeta取下拉项
	 *
	 * @param fieldMeta
	 * @param theVal
	 * @param paramMap
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<ValuePair<?>> getLookupList(FieldMeta fieldMeta, Object theVal, Map<String, Object> paramMap) {
		assert (fieldMeta.getProvider() != null);
		ValueProvider providerObj = valueProviderMap.get(fieldMeta.getProvider());
		return providerObj == null ? Collections.EMPTY_LIST : providerObj.getLookupList(theVal, paramMap, fieldMeta);
	}

	/**
	 * 值提供者的工厂类
	 */
	private static class ValueProviderFactory {
		protected static final Logger log = LoggerFactory.getLogger(ValueProviderFactory.class);
		// 缓冲对象
		private static final Map<Class<? extends ValueProvider>, ValueProvider> BUFFERS = new ConcurrentHashMap<Class<? extends ValueProvider>, ValueProvider>();

		/**
		 * 根据类型取提供者对象
		 *
		 * @return
		 */
		public static ValueProvider getProviderObj(Class<? extends ValueProvider> providerClazz) {
			ValueProvider retval = BUFFERS.get(providerClazz);
			if (retval == null) {
				try {
					retval = providerClazz.newInstance();
					BUFFERS.put(providerClazz, retval);
				} catch (Exception e) {
					log.warn(e.getMessage());
				}
			}
			return retval;
		}
	}


	/**
	 * 构建metadata的字符串value
	 * @param medadata
	 */
	private static void convertStringProvider(Map<String, Object> medadata){
		if(medadata == null){
			return;
		}
		Iterator<Map.Entry<String, Object>> it = medadata.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<String, Object> entry = it.next();
			if(!isJson(entry.getValue().toString())){
				if(entry.getKey().equals(IDTO.NULL_VALUE_FIELD) || entry.getKey().equals(IDTO.AND_CONDITION_EXPR) || entry.getKey().equals(IDTO.OR_CONDITION_EXPR)){
					continue;
				}
				Map<String, Object> value = Maps.newHashMap();
				value.put(ValueProvider.PROVIDER_KEY, entry.getValue());
				value.put(ValueProvider.FIELD_KEY, entry.getKey());
				value.put(ValueProvider.INDEX_KEY, 0);
				entry.setValue(value);
			}
		}
	}

	private static boolean isJson(String content){
		try {
			JSONObject.parseObject(content);
			return  true;
		} catch (Exception e) {
			return false;
		}
	}

}

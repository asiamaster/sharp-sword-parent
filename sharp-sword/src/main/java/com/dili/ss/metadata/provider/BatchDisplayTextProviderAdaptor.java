package com.dili.ss.metadata.provider;

import com.dili.ss.dto.DTOUtils;
import com.dili.ss.dto.IDTO;
import com.dili.ss.metadata.*;
import com.dili.ss.metadata.handler.DefaultMismatchHandler;
import com.dili.ss.util.BeanConver;
import com.dili.ss.util.POJOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 批量提供者适配器
 */
@Component
public abstract class BatchDisplayTextProviderAdaptor implements BatchValueProvider {
    protected static final Logger log = LoggerFactory.getLogger(BatchDisplayTextProviderAdaptor.class);
    //    转义字段json，如果为string，则key为filed属性
    protected static final String ESCAPE_FILEDS_KEY = "_escapeFileds";
    //    主DTO与关联DTO的关联(java bean)属性，即外键
    protected static final String FK_FILED_KEY = "_fkField";

    @Autowired
    protected DefaultMismatchHandler defaultMismatchHandler;

    //不提供下拉数据
    @Override
    public List<ValuePair<?>> getLookupList(Object val, Map metaMap, FieldMeta fieldMeta) {
        return null;
    }

    @Override
    public String getDisplayText(Object obj, Map metaMap, FieldMeta fieldMeta) {
        return null;
    }

    @Override
    public void setDisplayList(List list, Map metaMap, ObjectMeta fieldMeta) {
        if (CollectionUtils.isEmpty(metaMap) || CollectionUtils.isEmpty(list)) {
            return;
        }
        //列头上必须要有field字段
        if (metaMap.containsKey(FIELD_KEY)){
            String field = (String)metaMap.get(FIELD_KEY);
            Map<String, String> escapeFields = getEscapeFileds(metaMap);
            //只要第一个字段匹配就统一转换所有的字段，再次进来就不转换了，因为一次批量转换只关联一张表
            for(Map.Entry<String, String> entry : escapeFields.entrySet()){
                if(entry.getKey().equals(field)){
                    int size = list.size();
                    int capacity = size/2 < 10 ? size : size/2;
                    //构建关联id List
                    List<String> relationIds = buildRelationIdList(list, capacity, metaMap);
                    if(relationIds.isEmpty()){
                        break;
                    }
                    //从外键关联表获取数据
                    List relationDatas = getFkList(relationIds, metaMap);
                    if(relationDatas == null || relationDatas.isEmpty()){
                        break;
                    }
                    //缓存key为id，value为关联DTO
                    Map<String, Map> id2RelTable = new HashMap<>(relationDatas.size());
                    //关联(数据库)表的主键的字段名
                    String relactionTablePkField = getRelationTablePkField(metaMap);
                    boolean ignoreCaseToRef = ignoreCaseToRef(metaMap);
                    for(Object obj : relationDatas){
                        try {
                            Map map = BeanConver.transformObjectToMap(obj);
                            //这里有可能关联表的字段为空
                            Object relationTablePkFieldValue = map.get(relactionTablePkField);
                            if(relationTablePkFieldValue != null) {
                                //如果大小写不敏感，则统一关联字段转小写
                                if(ignoreCaseToRef) {
                                    id2RelTable.put(relationTablePkFieldValue.toString().toLowerCase(), map);
                                }else{
                                    id2RelTable.put(relationTablePkFieldValue.toString(), map);
                                }
                            }
                        } catch (Exception e) {
                            log.error("批量提供者转换(getFkList方法的结果)失败:"+e.getLocalizedMessage());
                            break;
                        }
                    }
                    //设置转义值
                    setDtoData(list, id2RelTable, metaMap);
                    break;
                }
            }
        }
    }

    /**
     * 构建DTO列表的关联id
     * @param list
     * @param capacity
     * @param metaMap
     * @return
     */
    private List<String> buildRelationIdList(List list, int capacity, Map metaMap){
        List<String> relationIds = new ArrayList(capacity);
        String fkField = getFkField(metaMap);
        if(fkField.contains(".")) {
            String childKey = fkField.substring(fkField.indexOf(".") + 1, fkField.length());
            fkField = fkField.substring(0, fkField.indexOf("."));
            for(Object obj : list) {
                Object fkObj = getObjectValueByKey(obj, fkField);
                if(fkObj == null){
                    continue;
                }
                Object fkValue = getObjectValueByKey(fkObj, childKey);
                if(fkValue == null){
                    continue;
                }
                relationIds.add(fkValue.toString());
            }
        }else {
            for (Object obj : list) {
                Object fkValue = getObjectValueByKey(obj, fkField);
                if (fkValue == null) {
                    continue;
                }
                relationIds.add(fkValue.toString());
            }
        }
        return relationIds;
    }

    /**
     * 根据key获取对象中的属性，key支持obj.field形式
     * @param obj
     * @param key
     */
    private Object getObjectValueByKey(Object obj, String key){
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
     * 判断是否忽略大小写进行主表和外表数据关联
     * 子类可以实现，默认大小写敏感
     * @return
     */
    protected boolean ignoreCaseToRef(Map metaMap){
        return false;
    }

    /**
     * 获取关联表数据
     * @param relationIds 根据主DTO的外键字典(FkField)中的值列表
     * @param metaMap meta信息
     * @return 可能返回DTO, Map或JavaBean的列表
     */
    protected abstract List getFkList(List<String> relationIds, Map metaMap);

    /**
     * 返回主DTO和关联DTO需要转义的字段名，可同时转义多个字段
     * 返回的Map中key为主DTO在页面(datagrid)渲染时需要的(field)字段名， value为关联DTO中对应的显示值的字段名
     * @return
     */
    //    {
//        Map<String, String> excapeFields = new HashMap<>();
//        excapeFields.put("customerName", "name");
//        excapeFields.put("customerPhone", "phone");
//        return excapeFields;
//    }
    protected Map<String, String> getEscapeFileds(Map metaMap){
        if(metaMap.get(ESCAPE_FILEDS_KEY) instanceof Map){
            return (Map)metaMap.get(ESCAPE_FILEDS_KEY);
        }else {
            Map<String, String> map = new HashMap<>();
            map.put(metaMap.get(FIELD_KEY).toString(), getEscapeFiled(metaMap));
            return map;
        }
    }

    /**
     * 返回主DTO和关联DTO需要转义的字段名
     * @param metaMap
     * @return
     */
    protected String getEscapeFiled(Map metaMap){return null;};


    /**
     * 主DTO与关联DTO的关联(java bean)属性(外键)
     * 先从_fkField属性取，没取到再取field属性
     * 子类可自行实现
     * @return
     */
    protected String getFkField(Map metaMap) {
        String field = (String)metaMap.get(FIELD_KEY);
        String fkField = (String)metaMap.get(FK_FILED_KEY);
        return fkField == null ? field : fkField;
    }

    /**
     * 关联(数据库)表的主键的字段名
     * 默认取id，子类可自行实现
     * @return
     */
    protected String getRelationTablePkField(Map metaMap) {
        return "id";
    }

    /**
     * 值不匹配处理器
     * 如果找不到对应的值，使用defaultMismatchHandler处理器
     * @return
     */
    protected Function getMismatchHandler(Map metaMap){
        return defaultMismatchHandler;
    }

    /**
     * 设置转义值，支持dto,map和javaBean
     * @param list 原始列表
     * @param id2RelTable key为id，value为关联DTO
     */
    private void setDtoData(List list, Map<String, Map> id2RelTable, Map metaMap){
        if(list.get(0) instanceof IDTO && list.get(0).getClass().isInterface()) {
            handleDtoData(list, id2RelTable, metaMap);
        }else if(list.get(0) instanceof Map) {
            handleMapData(list, id2RelTable, metaMap);
        }else{//java bean
            handleBeanData(list, id2RelTable, metaMap);
        }
    }

    /**
     * 处理DTO接口
     * @param list
     * @param id2RelTable
     * @param metaMap
     */
    private void handleDtoData(List list, Map<String, Map> id2RelTable, Map metaMap){
        String fkField = getFkField(metaMap);
        String childField = null;
        boolean hasChild = false;
        Map<String, String> escapeFields = getEscapeFileds(metaMap);
        if(fkField.contains(".")) {
            childField = fkField.substring(fkField.indexOf(".") + 1, fkField.length());
            fkField = fkField.substring(0, fkField.indexOf("."));
            hasChild = true;
        }
        for (Object obj : list) {
            IDTO dto = (IDTO) obj;
            //记录要转义的字段，避免被覆盖
            Object keyObj = dto.aget(fkField);
            //判断如果主表的外键没值就跳过
            if(keyObj == null){
                continue;
            }
            if(hasChild){
                keyObj = getObjectValueByKey(keyObj, childField);
                if(keyObj == null){
                    continue;
                }
            }
            String key = keyObj.toString();
            //如果大小写不敏感，则统一关联字段转小写
            if(ignoreCaseToRef(metaMap)) {
                key = key.toLowerCase();
            }
            Function mismatchHandler = getMismatchHandler(metaMap);
            for (Map.Entry<String, String> entry : escapeFields.entrySet()) {
                //记录原始值
                if(hasChild){
                    //如果有子对象，则原始值格式为object.$_field
                    String originalKey = new StringBuilder(fkField).append(".").append(ValueProviderUtils.ORIGINAL_KEY_PREFIX).append(childField).toString();
                    dto.aset(originalKey, keyObj);
                }else {
                    dto.aset(ValueProviderUtils.ORIGINAL_KEY_PREFIX + entry.getKey(), keyObj);
                }
                //有可能外键有值，但是关联表没数据，即是左关联为空的场景
                if(id2RelTable.get(key) == null){
                    dto.aset(entry.getKey(), mismatchHandler.apply(keyObj));
                }else {
                    dto.aset(entry.getKey(), id2RelTable.get(key).get(entry.getValue()));
                }
            }
        }
    }

    /**
     * 处理Map类型
     * @param list
     * @param id2RelTable
     * @param metaMap
     */
    private void handleMapData(List list, Map<String, Map> id2RelTable, Map metaMap){
        String fkField = getFkField(metaMap);
        String childField = null;
        boolean hasChild = false;
        Map<String, String> escapeFields = getEscapeFileds(metaMap);
        if(fkField.contains(".")) {
            childField = fkField.substring(fkField.indexOf(".") + 1, fkField.length());
            fkField = fkField.substring(0, fkField.indexOf("."));
            hasChild = true;
        }
        for (Object obj : list) {
            Map map = (Map) obj;
            Object keyObj = map.get(fkField);
            //判断如果主表的外键没值就跳过
            if(keyObj == null){
                continue;
            }
            if(hasChild){
                keyObj = getObjectValueByKey(keyObj, childField);
                if(keyObj == null){
                    continue;
                }
            }
            //记录要转义的字段，避免被覆盖
            String key = keyObj.toString();
            //如果大小写不敏感，则统一关联字段转小写
            if(ignoreCaseToRef(metaMap)) {
                key = key.toLowerCase();
            }
            for (Map.Entry<String, String> entry : escapeFields.entrySet()) {
                //记录原始值
                if(hasChild){
                    //如果有子对象，则原始值格式为object.$_field
                    String originalKey = new StringBuilder(fkField).append(".").append(ValueProviderUtils.ORIGINAL_KEY_PREFIX).append(childField).toString();
                    map.put(originalKey, keyObj);
                }else {
                    map.put(ValueProviderUtils.ORIGINAL_KEY_PREFIX + entry.getKey(), keyObj);
                }
                //有可能外键有值，但是关联表没数据，即是左关联为空的场景
                if(id2RelTable.get(key) == null){
                    map.put(entry.getKey(), getMismatchHandler(metaMap).apply(keyObj));
                }else {
                    map.put(entry.getKey(), id2RelTable.get(key).get(entry.getValue()));
                }
            }
        }
    }

    /**
     * 处理bean类型
     * @param list
     * @param id2RelTable
     * @param metaMap
     */
    private void handleBeanData(List list, Map<String, Map> id2RelTable, Map metaMap){
        String fkField = getFkField(metaMap);
        String childField = null;
        boolean hasChild = false;
        Map<String, String> escapeFields = getEscapeFileds(metaMap);
        if(fkField.contains(".")) {
            childField = fkField.substring(fkField.indexOf(".") + 1, fkField.length());
            fkField = fkField.substring(0, fkField.indexOf("."));
            hasChild = true;
        }
        //注意java bean如果没有关联属性可能报错，而且非字符串和字符串转换也可能报错，所以不建议使用javaBean
        for (Object obj : list) {
            Object keyObj = POJOUtils.getProperty(obj, fkField);
            //判断如果主表的外键没值就跳过
            if(keyObj == null){
                continue;
            }
            if(hasChild){
                keyObj = getObjectValueByKey(keyObj, childField);
                if(keyObj == null){
                    continue;
                }
            }
            //记录要转义的字段，避免被覆盖
            String key = keyObj.toString();
            //如果大小写不敏感，则统一关联字段转小写
            if(ignoreCaseToRef(metaMap)) {
                key = key.toLowerCase();
            }
            for (Map.Entry<String, String> entry : escapeFields.entrySet()) {
                //有可能外键有值，但是关联表没数据，即是左关联为空的场景
                if(id2RelTable.get(key) == null){
                    POJOUtils.setProperty(obj, entry.getKey(), getMismatchHandler(metaMap).apply(keyObj));
                }else {
                    //java bean无法记录原始值，而且设置转义值也可能因为类型转换报错
                    POJOUtils.setProperty(obj, entry.getKey(), id2RelTable.get(key).get(entry.getValue()));
                }
            }
        }
    }

}
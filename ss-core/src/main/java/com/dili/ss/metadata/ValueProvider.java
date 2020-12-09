package com.dili.ss.metadata;

import java.util.List;
import java.util.Map;

/**
 * 值提供者统一接口
 *
 * @author asiamaster
 * @date 2017/5/29 0029
 */
public interface ValueProvider {

    String EMPTY_ITEM_TEXT = "-- 请选择 --";
    //如果EMPTY_ITEM_TEXT不满足需要，可以添加自定义内容，方法为_queryParams="{emptyText:'全部'}"，需要子类实现
    String EMPTY_ITEM_TEXT_KEY = "emptyText";
    //下拉框就否必选， 如果必填_queryParams="{required:true}"，下拉框就没有空值内容
    String REQUIRED_KEY = "required";
    //查询参数key
    String QUERY_PARAMS_KEY = "queryParams";
    //metaMap中的key，获取字段名, 该值暂时是由easyui_extend.js中的bindMetadata方法提供的，后期考虑改为后台设置
    String FIELD_KEY = "field";
    //metaMap中的key，获取当前行的JSON数据，可能是DTO或Domain
    String ROW_DATA_KEY = "_rowData";
    //提供者的排序索引
    String INDEX_KEY = "index";
    //提供者bean id
    String PROVIDER_KEY = "provider";
    //列表查询参数key(需要手动设置， DTO是mset, BEAN是setMetadata)
    String GRID_QUERY_PARAMS_KEY = "gridQueryParams";
    /**
     * 取下拉列表的选项
     * @param val 值对象
     * @param metaMap   meta信息
     * @return
     */
    List<ValuePair<?>> getLookupList(Object val, Map metaMap, FieldMeta fieldMeta);

    /**
     * 取显示文本的值
     * @param val 值对象
     * @param metaMap   meta信息，包括:当前行数据:_rowData,当前字段名:_field及其它DTO中的meta信息
     * @param fieldMeta 当前字段的注解封装对象
     * @return
     */
    String getDisplayText(Object val, Map metaMap, FieldMeta fieldMeta);


}

package com.dili.ss.metadata.provider;

import com.alibaba.fastjson.JSONObject;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 简单批量提供者实现,根据datagrid表头参数动态提供数据。
 */
@Component
@ConditionalOnBean(name = "commonMapper")
public class SimpleBatchDisplayTextProvider extends BatchSqlDisplayTextProviderAdaptor {
    //    关联(数据库)表名
    protected static final String RELATION_TABLE_KEY = "_relationTable";
    //    关联(数据库)表的主键名
    protected static final String RELATION_TABLE_PK_FIELD_KEY = "_relationTablePkField";
    //    查询参数json
    protected static final String QUERY_PARAMS_KEY = "queryParams";
    /**
     * 返回主DTO和关联DTO需要转义的字段名
     * Map中key为主DTO在页面(datagrid)渲染时需要的字段名， value为关联DTO中对应的字段名
     * @return
     */
    @Override
    protected Map<String, String> getEscapeFileds(Map metaMap){
        if(metaMap.get(ESCAPE_FILEDS_KEY) instanceof Map){
            return (Map)metaMap.get(ESCAPE_FILEDS_KEY);
        }else{
            String escapeField = (String)metaMap.get(ESCAPE_FILEDS_KEY);
            Map<String, String> map = new HashMap(1);
            map.put((String)metaMap.get(FIELD_KEY), escapeField);
            return map;
        }
    }
//    {
//        Map<String, String> excapeFields = new HashMap<>();
//        excapeFields.put("customerName", "name");
//        excapeFields.put("customerPhone", "phone");
//        return excapeFields;
//    }

    /**
     * 主DTO与关联DTO的关联(java bean)属性(外键)
     * @return
     */
    @Override
    protected String getFkField(Map metaMap){
        return (String)metaMap.get(FK_FILED_KEY);
    }

    /**
     * 关联(数据库)表名
     * @return
     */
    @Override
    protected String getRelationTable(Map metaMap){
        return (String)metaMap.get(RELATION_TABLE_KEY);
    }

    /**
     * 关联(数据库)表的主键名,默认为"id"
     * @return
     */
    @Override
    protected String getRelationTablePkField(Map metaMap){
        return metaMap.get(RELATION_TABLE_PK_FIELD_KEY) == null ? "id" : (String)metaMap.get(RELATION_TABLE_PK_FIELD_KEY);
    }

    @Override
    protected JSONObject getQueryParams(Map metaMap){
        return metaMap.get(QUERY_PARAMS_KEY) == null ? null : JSONObject.parseObject(metaMap.get(QUERY_PARAMS_KEY).toString());
    }
}
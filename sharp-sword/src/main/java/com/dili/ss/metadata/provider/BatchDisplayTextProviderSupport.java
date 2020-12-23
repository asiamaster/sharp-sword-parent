package com.dili.ss.metadata.provider;

import com.dili.ss.metadata.BatchProviderMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 批量提供者支持
 * 提供更方便的实现
 */
@Component
public abstract class BatchDisplayTextProviderSupport extends BatchDisplayTextProviderAdaptor {
    protected static final Logger log = LoggerFactory.getLogger(BatchDisplayTextProviderSupport.class);

    /**
     * 批量提供者的元数据信息
     * 方便开发者一次性组装数据
     * @return
     */
    protected abstract BatchProviderMeta getBatchProviderMeta(Map metaMap);

    /**
     * 判断是否忽略大小写进行主表和外表数据关联
     * 子类可以实现，默认大小写敏感
     * @return
     */
    @Override
    protected boolean ignoreCaseToRef(Map metaMap){
        Boolean ignoreCaseToRef = getBatchProviderMeta(metaMap).getIgnoreCaseToRef();
        return ignoreCaseToRef == null ? false : ignoreCaseToRef;
    }

    /**
     * 获取关联表数据
     * @param relationIds 根据主DTO的外键字典(FkField)中的值列表
     * @param metaMap meta信息
     * @return 可能返回DTO, Map或JavaBean的列表
     */
    @Override
    protected abstract List getFkList(List<String> relationIds, Map metaMap);

    /**
     * 返回主DTO和关联DTO需要转义的字段名，可同时转义多个字段
     * Map中key为主DTO在页面(datagrid)渲染时需要的(field)字段名， value为关联DTO中对应的显示值的字段名
     * @return
     */
    //    {
//        Map<String, String> excapeFields = new HashMap<>();
//        excapeFields.put("customerName", "name");
//        excapeFields.put("customerPhone", "phone");
//        return excapeFields;
//    }
    @Override
    protected Map<String, String> getEscapeFileds(Map metaMap){
        BatchProviderMeta batchProviderMeta = getBatchProviderMeta(metaMap);
        if(batchProviderMeta.getEscapeFileds() != null){
            return batchProviderMeta.getEscapeFileds();
        }else if(batchProviderMeta.getEscapeFiled() != null){
            Map<String, String> map = new HashMap<>();
            map.put(metaMap.get(FIELD_KEY).toString(), batchProviderMeta.getEscapeFiled());
            return map;
        }
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
     * 由于先取batchProviderMeta，所以子类可不用实现
     * @param metaMap
     * @return
     */
    @Override
    protected String getEscapeFiled(Map metaMap){return null;}


    /**
     * 主DTO与关联DTO的关联(java bean)属性(外键)
     * 先从field属性取，没取到再取_fkField属性
     * 子类可自行实现
     * @return
     */
    @Override
    protected String getFkField(Map metaMap) {
        BatchProviderMeta batchProviderMeta = getBatchProviderMeta(metaMap);
        if(batchProviderMeta.getFkField() != null){
            return batchProviderMeta.getFkField();
        }
        String field = (String)metaMap.get(FIELD_KEY);
        String fkField = (String)metaMap.get(FK_FILED_KEY);
        return fkField == null ? field : fkField;
    }

    /**
     * 关联(数据库)表的主键的字段名
     * 默认取id，子类可自行实现
     * @return
     */
    @Override
    protected String getRelationTablePkField(Map metaMap) {
        BatchProviderMeta batchProviderMeta = getBatchProviderMeta(metaMap);
        if(batchProviderMeta.getRelationTablePkField() != null){
            return batchProviderMeta.getRelationTablePkField();
        }
        return "id";
    }

    @Override
    protected Function getMismatchHandler(Map metaMap) {
        return getBatchProviderMeta(metaMap).getMismatchHandler();
    }
}
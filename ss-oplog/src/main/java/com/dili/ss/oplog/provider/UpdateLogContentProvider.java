package com.dili.ss.oplog.provider;

import com.alibaba.fastjson.JSONObject;
import com.dili.ss.base.BaseService;
import com.dili.ss.dto.DTO;
import com.dili.ss.dto.DTOUtils;
import com.dili.ss.dto.IDTO;
import com.dili.ss.exception.ParamErrorException;
import com.dili.ss.metadata.annotation.FieldDef;
import com.dili.ss.oplog.base.LogContentProvider;
import com.dili.ss.oplog.dto.LogContext;
import com.dili.ss.oplog.dto.UpdatedLogInfo;
import com.dili.ss.util.BeanConver;
import com.dili.ss.util.DateUtils;
import com.dili.ss.util.POJOUtils;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.Transient;
import java.beans.Introspector;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 修改方法 的日志内容提供者
 */
@Component
public class UpdateLogContentProvider implements LogContentProvider {


    //注入所有BaseService，用于获取旧值
    @Autowired
    private Map<String, BaseService> serviceMap;

    /**
     * 获取日志内容
     * 有异常需要返回null
     * @param method
     * @param args
     * @return
     */
    @Override
    public String content(Method method, Object[] args, String params, LogContext logContext){
        //修改取第一个参数
        Object updatedBean = args[0];
//        OpLog opLog = method.getAnnotation(OpLog.class);
        Class<?> clazz = DTOUtils.getDTOClass(updatedBean);
        JSONObject jsonObject = JSONObject.parseObject(params);
        String serviceName = jsonObject.getString("serviceName");
        BaseService<? extends IDTO, Long> service = null;
        if(StringUtils.isBlank(serviceName)){
            service = serviceMap.get(Introspector.decapitalize(clazz.getSimpleName())+"ServiceImpl");
        }else{
            service = serviceMap.get(serviceName);
        }
        //记录被修改过的字段
        Map<String, UpdatedLogInfo> updatedFields = new HashMap<>();
        List<String> excludes = Lists.newArrayList("id", "page", "sort", "rows", "order", "metadata"
                , "setForceParams", "insertForceParams", "selectColumns", "whereSuffixSql", "checkInjection");
        Object id = null;
        //如果是DTO接口，则取getter上的FieldDef注解的label
        if(clazz.isInterface()) {
            id = buildUpdatedFieldsByDto(updatedFields, clazz, updatedBean, service, excludes);
        }
        //否则是普通JAVABean，取字段上的FieldDef注解的label
        else{
            id = buildUpdatedFieldsByBean(updatedFields, clazz, updatedBean, service, excludes);
        }
        StringBuilder stringBuilder = new StringBuilder("[目标id]:"+id+"\r\n");
        if(updatedFields.isEmpty()){
            stringBuilder.append("无字段修改");
        }else {
            for (String key : updatedFields.keySet()) {
                UpdatedLogInfo updatedLogInfo = updatedFields.get(key);
                Object oldValue = updatedLogInfo.getOldValue();
                if (oldValue == null) {
                    stringBuilder.append("[" + updatedLogInfo.getLabel() + "]:修改为'" + updatedLogInfo.getNewValue() + "'\r\n");
                } else {
                    if (oldValue instanceof Date) {
                        oldValue = DateUtils.format((Date) oldValue);
                    }
                    stringBuilder.append("[" + updatedLogInfo.getLabel() + "]:从'" + oldValue + "'修改为'" + updatedLogInfo.getNewValue() + "'\r\n");
                }
            }
        }
        return stringBuilder.toString();
    }

    /**
     * 构建修改的字段信息
     * service名称必须是 "实体Service"
     * @param updatedFields
     * @paramclazz
     * @param param1
     * @param service
     * @param excludes
     * @return
     */
    private Object buildUpdatedFieldsByDto(Map<String, UpdatedLogInfo> updatedFields, Class<?> clazz, Object param1, BaseService<? extends IDTO, Long> service, List<String> excludes){
        DTO dto = DTOUtils.go(param1);
        Object idObj = dto.get("id");
        IDTO oldObj = null;
        //根据id从数据库获取原始对象
        if(service != null && idObj != null){
            oldObj = service.get(Long.parseLong(idObj.toString()));
        }
        DTO oldObjDTO = DTOUtils.go(oldObj);
        Method[] methods = clazz.getMethods();
        for (Method dtoMethod : methods) {
            if (POJOUtils.isGetMethod(dtoMethod)) {
                Transient aTransient = dtoMethod.getAnnotation(Transient.class);
                if(aTransient != null){
                    continue;
                }
                FieldDef fieldDef = dtoMethod.getAnnotation(FieldDef.class);
                String field = POJOUtils.getBeanField(dtoMethod);
                if(excludes.contains(field)){
                    continue;
                }
                String label = fieldDef == null ? field : fieldDef.label();
                Object value = dto.get(field);
                if(null != value){
                    UpdatedLogInfo updatedLogInfo = new UpdatedLogInfo();
                    updatedLogInfo.setNewValue(value);
                    updatedLogInfo.setLabel(label);
                    if(oldObj != null){
                        //新旧值相同，则不记录
                        if(Objects.equals(value, oldObjDTO.get(field))){
                            continue;
                        }
                        updatedLogInfo.setOldValue(oldObjDTO.get(field));
                    }
                    updatedFields.put(field, updatedLogInfo);
                }
            }
        }
        return idObj;
    }

    /**
     * 构建修改的字段信息
     * service名称必须是 "实体Service"
     * @param updatedFields
     * @param clazz
     * @param param1
     * @param service
     * @param excludes
     * @return
     */
    private Object buildUpdatedFieldsByBean(Map<String, UpdatedLogInfo> updatedFields, Class<?> clazz, Object param1, BaseService<? extends IDTO, Long> service, List<String> excludes){
        Map<String, Object> objMap = null;
        try {
            objMap = BeanConver.transformObjectToMap(param1);
        } catch (Exception e) {
            throw new ParamErrorException(e);
        }
        //数据库中的原始对象的Map
        Map<String, Object> oldObjMap = null;
        if(service != null && objMap != null && objMap.get("id") != null){
            Object oldBean = service.get(Long.parseLong(objMap.get("id").toString()));
            if(oldBean != null){
                try {
                    oldObjMap = BeanConver.transformObjectToMap(oldBean);
                } catch (Exception e) {
                }
            }
        }
        Field[] fields = clazz.getFields();
        for(Field field : fields){
            Transient aTransient = field.getAnnotation(Transient.class);
            if(aTransient != null){
                continue;
            }
            if(excludes.contains(field.getName())){
                continue;
            }
            FieldDef fieldDef = field.getAnnotation(FieldDef.class);
            String label = fieldDef == null ? field.getName() : fieldDef.label();
            field.setAccessible(true);
            try {
                Object value = field.get(param1);
                if(null != value){
                    UpdatedLogInfo updatedLogInfo = new UpdatedLogInfo();
                    updatedLogInfo.setNewValue(value);
                    updatedLogInfo.setLabel(label);
                    if(oldObjMap != null){
                        //新旧值相同，则不记录
                        if(value.equals(oldObjMap.get(field))){
                            continue;
                        }
                        updatedLogInfo.setOldValue(oldObjMap.get(field));
                    }
                    updatedFields.put(field.getName(), updatedLogInfo);
                }
            } catch (IllegalAccessException e) {
            }
        }
        return objMap.get("id");
    }
}

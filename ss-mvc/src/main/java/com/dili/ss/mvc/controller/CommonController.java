package com.dili.ss.mvc.controller;

import com.alibaba.fastjson.JSONObject;
import com.dili.ss.base.BaseServiceImpl;
import com.dili.ss.constant.SsConstants;
import com.dili.ss.domain.ConditionItems;
import com.dili.ss.domain.EasyuiPageOutput;
import com.dili.ss.glossary.RelationOperator;
import com.dili.ss.metadata.ValueProviderUtils;
import com.dili.ss.service.CommonService;
import com.dili.ss.util.POJOUtils;
import com.dili.ss.util.ReflectionUtils;
import com.github.pagehelper.Page;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.persistence.Column;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 通用控制器操作
 */
@Controller
@ConditionalOnBean(name = "commonMapper")
@RequestMapping("/common")
public class CommonController {
    private static final Logger logger = LoggerFactory.getLogger(BaseServiceImpl.class);
    @Autowired
    CommonService commonService;

    /**
     * 动态查询选择框
     * @param conditionItems
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/listEasyuiPageByConditionItems.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody String listEasyuiPageByConditionItems(@ModelAttribute ConditionItems conditionItems) throws Exception {
        if(conditionItems.getConditionRelationField() == null || conditionItems.getConditionItems() == null || conditionItems.getDtoClass() == null) {
            return null;
        }
        Class<?> dtoClass = Class.forName(conditionItems.getDtoClass());
        Table table = dtoClass.getAnnotation(Table.class);
        //获取数据库表名
        String tableName = table == null ? POJOUtils.humpToLineFast(dtoClass.getSimpleName()) : table.name();
        StringBuilder stringBuilder = new StringBuilder("select ");
        if(dtoClass.isInterface()){
            //根据字段信息拼接查询的字段，排除static和final的字段
            for (Method method : ReflectionUtils.getAccessibleMethods(dtoClass)) {
                if(!POJOUtils.isGetMethod(method)){
                    continue;
                }
                //忽略Transient注解的字段
                Transient aTransient = method.getAnnotation(Transient.class);
                if (aTransient != null) {
                    continue;
                }
                Column column = method.getAnnotation(Column.class);
                String dbFieldName = column == null ? POJOUtils.humpToLineFast(POJOUtils.getBeanField(method)) : column.name();
                stringBuilder.append(dbFieldName).append(" ").append(POJOUtils.getBeanField(method)).append(", ");
            }
        }else {
            //根据字段信息拼接查询的字段，排除static和final的字段
            for (Field field : ReflectionUtils.getAccessibleFields(dtoClass, true, true)) {
                //忽略Transient注解的字段
                Transient aTransient = field.getAnnotation(Transient.class);
                if (aTransient != null) {
                    continue;
                }
                Column column = field.getAnnotation(Column.class);
                String dbFieldName = column == null ? POJOUtils.humpToLineFast(field.getName()) : column.name();
                stringBuilder.append(dbFieldName).append(" ").append(field.getName()).append(", ");
            }
        }
        //from之前的sql
        String beforeFromSql = stringBuilder.substring(0, stringBuilder.length()-2);
        stringBuilder = new StringBuilder(beforeFromSql);
        stringBuilder.append(" from ").append(tableName);
        //where以前的sql
        String sqlStart = stringBuilder.toString();
        //声明最终执行的sql变量
        String sql = null;
        if("none".equals(conditionItems.getConditionRelationField())){
            sql = sqlStart;
        }else {
            stringBuilder = new StringBuilder();
            for (String str : conditionItems.getConditionItems()) {
                if(StringUtils.isBlank(str)){
                    continue;
                }
                String[] condition = str.split(":");
                String conditionField = condition[0];
                String relationField = condition[1];
                //将:条件值中的冒号替换回来
                String conditionValueField = condition.length<3 ? "" : condition[2].replaceAll(SsConstants.COLON_ENCODE, ":");
                stringBuilder.append(" ").append(conditionItems.getConditionRelationField()).append(" ")
                        .append(conditionField).append(" ").append(RelationOperator.valueOf(relationField).getValue()).append(" ");
                //如果是like或not like，在两边加%号
                if (relationField.equals(RelationOperator.Match.name()) || relationField.equals(RelationOperator.NotMatch.name())) {
                    stringBuilder.append("'%")
                            .append(conditionValueField)
                            .append("%'");
                    //如果是Is或IsNot，conditionValueField直接写死成null
                } else if (relationField.equals(RelationOperator.Is.name()) || relationField.equals(RelationOperator.IsNot.name())) {
                    stringBuilder.append("null");
                } else {
                    stringBuilder.append("'")
                            .append(conditionValueField)
                            .append("'");
                }
            }
            //组合sql，第三段where之后的sql需要去掉前面的空格和and/or操作符
            //没有条件则不加where部分
            if(stringBuilder.length()<=0){
                sql = sqlStart;
            }else {
                sql = sqlStart + " where" + stringBuilder.substring(conditionItems.getConditionRelationField().length() + 1);
            }
        }
        logger.info("listEasyuiPageByConditionItems_sql:"+sql);
        Integer page = conditionItems.getPage();
        page = (page == null) ? Integer.valueOf(1) : page;
        Integer rows = conditionItems.getRows() == null ? Integer.valueOf(Integer.MAX_VALUE) : conditionItems.getRows();
        EasyuiPageOutput easyuiPageOutput = new EasyuiPageOutput();
        List<JSONObject> list = commonService.selectJSONObject(sql, page, rows);
        Page<T> pageList = (Page)list;
        ValueProviderUtils.buildDataByProvider(conditionItems.getMetadata(), list);
        return new EasyuiPageOutput(pageList.getTotal(), pageList).toString();
    }




}
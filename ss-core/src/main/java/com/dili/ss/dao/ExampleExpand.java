package com.dili.ss.dao;

import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.util.StringUtil;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 扩展Example
 * Created by asiam on 2018/5/3 0003.
 */
public class ExampleExpand<A> extends Example {

    //在Example中添加where后缀
    protected String whereSuffixSql;

    public ExampleExpand(Class<A> entityClass) {
        super(entityClass);
    }

    public ExampleExpand(Class<A> entityClass, boolean exists) {
        super(entityClass, exists);
    }

    public ExampleExpand(Class<A> entityClass, boolean exists, boolean notNull) {
        super(entityClass, exists, notNull);
    }

    public static <A> ExampleExpand<A> of(Class<A> clazz, Boolean exists, boolean notNull) {
        return new ExampleExpand(clazz, exists.booleanValue(), notNull);
    }

    public static <A> ExampleExpand<A> of(Class<A> clazz, Boolean exists) {
        return new ExampleExpand(clazz, exists.booleanValue(), Boolean.FALSE.booleanValue());
    }

    public static <A> ExampleExpand<A> of(Class<A> clazz) {
        return new ExampleExpand(clazz, Boolean.TRUE.booleanValue());
    }

    public static <A> ExampleExpand<A> of(Class<A> clazz, Example.Builder builder) {
        return new ExampleExpand(clazz, builder);
    }

    public String getWhereSuffixSql() {
        return whereSuffixSql;
    }

    public void setWhereSuffixSql(String whereSuffixSql) {
        this.whereSuffixSql = whereSuffixSql;
    }

    public ExampleExpand(Class<A> entityClass, Example.Builder builder) {
        super(entityClass);
        try {
            this.distinct = getProperty(builder, "distinct", Boolean.class);
            this.propertyMap = getProperty(builder, "propertyMap", Map.class);
            this.selectColumns = getProperty(builder, "selectColumns", Set.class);
            this.excludeColumns = getProperty(builder, "excludeColumns", Set.class);
            this.oredCriteria = getProperty(builder, "exampleCriterias", List.class);
            this.forUpdate = getProperty(builder, "forUpdate", Boolean.class);
            this.tableName = getProperty(builder, "tableName", String.class);
            StringBuilder orderByClauseTmp = getProperty(builder, "orderByClause", StringBuilder.class);
            if(!StringUtil.isEmpty(orderByClauseTmp.toString())) {
                this.orderByClause = orderByClauseTmp.toString();
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private <T> T getProperty(Example.Builder builder, String field, Class<T> retType) throws NoSuchFieldException, IllegalAccessException {
        return (T) Example.Builder.class.getDeclaredField(field).get(builder);
    }
}
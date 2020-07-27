package com.dili.ss.domain.annotation;

import java.lang.annotation.*;

/**
 * sql操作符， 可能是: >,<,>=,<=,=五种
 * Created by asiamaster on 2017/5/26 0026.
 */
@Documented
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Operator {
    //    public static final String GREAT_EQUAL_THAN = "&gte;";
//    public static final String GREAT_THAN = "&gt;";
//    public static final String LITTLE_EQUAL_THAN = "&lte;";
//    public static final String LITTLE_THAN = "&lt;";
    public static final String GREAT_EQUAL_THAN = ">=";
    public static final String GREAT_THAN = ">";
    public static final String LITTLE_EQUAL_THAN = "<=";
    public static final String LITTLE_THAN = "<";
    public static final String EQUAL = "=";
    public static final String NOT_EQUAL = "!=";
    public static final String IN = "in";
    public static final String NOT_IN = "not in";
    public static final String BETWEEN = "between";
    public static final String NOT_BETWEEN = "not between";
    String value() default EQUAL;
}

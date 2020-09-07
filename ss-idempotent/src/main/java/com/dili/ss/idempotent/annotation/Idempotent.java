package com.dili.ss.idempotent.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标识一个接口是否需要幂等验证
 */
@Target(value = ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {

    public static final String HEADER = "header";

    public static final String PARAMETER = "parameter";

    /**
     * 验证类型分为header和parameter
     * @return
     */
    @AliasFor("type")
    String value() default "header";

    @AliasFor("value")
    String type() default "header";
}

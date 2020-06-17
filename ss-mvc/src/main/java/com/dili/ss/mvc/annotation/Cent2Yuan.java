package com.dili.ss.mvc.annotation;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

@Documented
@Inherited
@Target({METHOD, FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Cent2Yuan {
    /**
     * 为空时的默认值
     * @return
     */
    String defPrintVal() default "";
}

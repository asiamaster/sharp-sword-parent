package com.dili.ss.redis.delayqueue.annotation;

import java.lang.annotation.*;

/**
 * 延迟队列方法标记
 *
 * @author wm
 * @date 2021-01-26
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface StreamListener {

    /**
     * 监听主题
     *
     * @return
     */
    String value() default "delay:list:default";
}

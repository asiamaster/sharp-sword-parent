package com.dili.ss.activiti.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author: WangMi
 * @Date: 2019/12/3 9:05
 * @Description:
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BpmAction {

    /**
     * 表单key
     * @return
     */
    String formKey();

    /**
     * 任务定义Key
     * @return
     */
    String defKey();
}

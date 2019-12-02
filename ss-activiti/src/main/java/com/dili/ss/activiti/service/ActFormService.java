package com.dili.ss.activiti.service;

import com.dili.ss.activiti.domain.ActForm;
import com.dili.ss.base.BaseService;

/**
 * 由MyBatis Generator工具自动生成
 * This file was generated on 2019-03-21 16:02:46.
 */
public interface ActFormService extends BaseService<ActForm, Long> {
    /**
     * 根据formKey查询
     * @param formKey
     * @return
     */
    ActForm getByKey(String formKey);
}
package com.dili.ss.activiti.service.impl;

import com.dili.ss.activiti.dao.ActFormMapper;
import com.dili.ss.activiti.domain.ActForm;
import com.dili.ss.activiti.service.ActFormService;
import com.dili.ss.base.BaseServiceImpl;
import com.dili.ss.dto.DTOUtils;
import org.springframework.stereotype.Service;

/**
 * 由MyBatis Generator工具自动生成
 * This file was generated on 2019-03-21 16:02:46.
 */
@Service
public class ActFormServiceImpl extends BaseServiceImpl<ActForm, Long> implements ActFormService {

    public ActFormMapper getActualDao() {
        return (ActFormMapper)getDao();
    }

    @Override
    public ActForm getByKey(String formKey) {
        ActForm actForm = DTOUtils.newInstance(ActForm.class);
        actForm.setFormKey(formKey);
        return getActualDao().selectOne(actForm);
    }
}
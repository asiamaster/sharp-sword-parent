package com.dili.ss.activiti.listener;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 业务编号执行监听
 * 支持从传递变量(businessKey)中获取指定业务编号，如果没有传递变量，则使用父流程实例的businessKey
 * @author: WM
 * @time: 2020/12/3 9:36
 */
@Component
public class BusinessKeyExecutionListener implements ExecutionListener {
    @Autowired
    RuntimeService runtimeService;
    @Override
    public void notify(DelegateExecution execution) throws Exception {
        if(!StringUtils.isBlank(execution.getProcessBusinessKey())){
            return;
        }

        Object businessKeyObj = execution.getVariable("businessKey");
        if(businessKeyObj != null){
            runtimeService.updateBusinessKey(execution.getProcessInstanceId(), businessKeyObj.toString());
        }
        if(!(execution instanceof ExecutionEntity)){
            return;
        }
        ExecutionEntity processInstance = ((ExecutionEntity) execution).getSuperExecution().getProcessInstance();
        if(processInstance == null || processInstance.getBusinessKey() == null){
            return;
        }
        runtimeService.updateBusinessKey(execution.getProcessInstanceId(), processInstance.getBusinessKey());
    }
}

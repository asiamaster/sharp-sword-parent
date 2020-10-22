package com.dili.ss.activiti.listener;

import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 解决创建子流程时，businesskey 不传递。
 * @author: WM
 * @time: 2020/10/22 15:42
 */
public class BusinessKeyInjectionActivitiEventListener implements ActivitiEventListener {
    private Logger log = LoggerFactory.getLogger(getClass());
    @Override
    public void onEvent(ActivitiEvent event) {
        switch (event.getType()) {
            case TASK_CREATED:
                if (event instanceof ActivitiEntityEvent) {
                    ActivitiEntityEvent activityEntityEvent = (ActivitiEntityEvent) event;
                    TaskEntity taskEntity = (TaskEntity) activityEntityEvent.getEntity();
                    ExecutionEntity exEntity = taskEntity.getExecution();
                    String key = exEntity.getBusinessKey();
                    log.info("获取当前任务的流程实例的businessKey:{}",key);
                    if(StringUtils.isEmpty(key)){
                        ExecutionEntity superExecEntity = exEntity.getSuperExecution();
                        key = superExecEntity.getBusinessKey();
                        if(StringUtils.isEmpty(key)){
                            key = superExecEntity.getProcessInstance().getBusinessKey();
                        }
                        if(StringUtils.isBlank(key)){
                            break;
                        }
                        log.info("获取当前任务 上一个流程实例的businessKey:{}",key);
                        log.info("设置当前流程实例的businessKey:{}",key);
                        exEntity.setBusinessKey(key);
                        //让businessKey生效 此处非常关键。
                        exEntity.updateProcessBusinessKey(key);
                    }
                    break;
                }
            default:
                break;
        }
    }

    @Override
    public boolean isFailOnException() {
        // TODO Auto-generated method stub
        return false;
    }
}

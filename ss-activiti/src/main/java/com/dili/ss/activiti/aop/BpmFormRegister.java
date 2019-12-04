package com.dili.ss.activiti.aop;

import com.dili.ss.activiti.annotation.BpmAction;
import com.dili.ss.activiti.annotation.BpmRedirect;
import com.dili.ss.activiti.annotation.BpmTask;
import com.dili.ss.activiti.domain.ActForm;
import com.dili.ss.activiti.rpc.BpmcFormRpc;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.dto.DTOUtils;
import com.dili.ss.util.SpringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * BPM表单自动注册器
 */
@Component
public class BpmFormRegister implements EnvironmentAware {

    @Autowired
    private BpmcFormRpc bpmcFormRpc;
    //流控中心http路径
    private String contextPath;
    @Autowired
    private SpringUtil springUtil;

    private static final String DEFAULT_CONTEXT_PATH = "http://bpmc.diligrp.com:8617";

    @PostConstruct
    public void init() {
        Map<String, Object> controllerMap = springUtil.applicationContext().getBeansWithAnnotation(org.springframework.stereotype.Controller.class);
        //存放所有扫描到的需要注册的动态表单
        List<ActForm> actFormList = new ArrayList<>();
        for(Map.Entry<String, Object> entry : controllerMap.entrySet()){
            //获取到Controller对象的class信息
            Class<? extends Object> clazz  = entry.getValue().getClass();
            Method[] methods = clazz.getMethods();
            for(Method method : methods){
                BpmTask bpmTask = method.getAnnotation(BpmTask.class);
                BpmAction bpmAction = method.getAnnotation(BpmAction.class);
                BpmRedirect bpmRedirect = method.getAnnotation(BpmRedirect.class);
                if(bpmTask == null && bpmAction == null && bpmRedirect == null){
                    continue;
                }
                RequestMapping classRequestMapping = clazz.getAnnotation(RequestMapping.class);
                RequestMapping methodRequestMapping = method.getAnnotation(RequestMapping.class);
                //用于取Controller类上的RequestMapping地址
                String classRequestMappingName = classRequestMapping.value()[0];
                //用于取方法上的RequestMapping地址
                String methodRequestMappingName = methodRequestMapping.value()[0];
                //构建该Controller方法的完整http路径
                String restfulUrl = new StringBuilder().append(contextPath).append(classRequestMappingName).append(methodRequestMappingName).toString();

                ActForm actForm = DTOUtils.newInstance(ActForm.class);
                actForm.setFormKey(bpmTask.formKey());
                actForm.setDefKey(bpmTask.defKey());
                if(bpmTask != null) {
                    actForm.setTaskUrl(restfulUrl);
                }
                if(bpmAction != null){
                    actForm.setActionUrl(restfulUrl);
                }
                if(bpmRedirect != null){
                    actForm.setRedirectUrl(restfulUrl);
                }
                actFormList.add(actForm);
            }
        }
        //注册动态表单
        for(ActForm actForm : actFormList){
            BaseOutput<ActForm> output = bpmcFormRpc.getByKey(actForm.getFormKey());
            if(output.isSuccess()) {
                ActForm form = output.getData();
                //根据formKey没查询到则新增ActForm
                if (form == null) {
                    bpmcFormRpc.insert(actForm);
                } else {//查询到ActForm则修改
                    bpmcFormRpc.updateByKey(actForm);
                }
            }
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        contextPath = environment.getProperty("bpm.server.address", DEFAULT_CONTEXT_PATH);
    }
}

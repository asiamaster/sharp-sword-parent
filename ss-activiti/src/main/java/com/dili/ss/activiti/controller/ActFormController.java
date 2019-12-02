package com.dili.ss.activiti.controller;

import com.dili.ss.activiti.domain.ActControl;
import com.dili.ss.activiti.domain.ActForm;
import com.dili.ss.activiti.service.ActControlService;
import com.dili.ss.activiti.service.ActFormService;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.dto.DTOUtils;
import com.dili.ss.exception.ParamErrorException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.FormService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 由MyBatis Generator工具自动生成
 * This file was generated on 2019-03-21 16:02:46.
 */
@Api("/actForm")
@Controller
@RequestMapping("/actForm")
public class ActFormController {
    @Autowired
    ActFormService actFormService;

    @Autowired
    ActControlService actControlService;

    @Autowired
    FormService formService;

    @Autowired
    TaskService taskService;

    @Autowired
    RuntimeService runtimeService;

    @ApiOperation("跳转到ActForm页面")
    @RequestMapping(value="/index.html", method = RequestMethod.GET)
    public String index(ModelMap modelMap) {
        return "actForm/index";
    }

    @ApiOperation(value="分页查询ActForm", notes = "分页查询ActForm，返回easyui分页信息")
    @ApiImplicitParams({
		@ApiImplicitParam(name="ActForm", paramType="form", value = "ActForm的form信息", required = false, dataType = "string")
	})
    @RequestMapping(value="/listPage.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody String listPage(ActForm actForm) throws Exception {
        return actFormService.listEasyuiPageByExample(actForm, true).toString();
    }

    @ApiOperation("新增ActForm")
    @ApiImplicitParams({
		@ApiImplicitParam(name="ActForm", paramType="form", value = "ActForm的form信息", required = true, dataType = "string")
	})
    @RequestMapping(value="/insert.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody BaseOutput insert(ActForm actForm) {
        //设置默认表单提交地址
        if(StringUtils.isBlank(actForm.getActionUrl())){
            actForm.setActionUrl("/actForm/submit.action");
        }
        //设置默认任务表单URL,必填参数1(@RequestParam):formKey,参数2/3(@RequestParam):processDefinitionId或taskId必填一个
        if(StringUtils.isBlank(actForm.getTaskUrl())){
            actForm.setTaskUrl("/actForm/dynamicForm.html");
        }
        actFormService.insertSelective(actForm);
        return BaseOutput.success("新增成功");
    }

    @ApiOperation("修改ActForm")
    @ApiImplicitParams({
		@ApiImplicitParam(name="ActForm", paramType="form", value = "ActForm的form信息", required = true, dataType = "string")
	})
    @RequestMapping(value="/update.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody BaseOutput update(ActForm actForm) {
        actFormService.updateExactSimple(actForm);
        return BaseOutput.success("修改成功");
    }

    @ApiOperation("删除ActForm")
    @ApiImplicitParams({
		@ApiImplicitParam(name="id", paramType="form", value = "ActForm的主键", required = true, dataType = "long")
	})
    @RequestMapping(value="/delete.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody BaseOutput delete(Long id) {
        actFormService.delete(id);
        return BaseOutput.success("删除成功");
    }

    /**
     * processDefinitionId和taskId至少填一个，分别代码开始节点表单和任务节点表单
     * @param formKey   非必填，可以根据processDefinitionId或taskId获取到。建议填写，以提高性能
     * @param processDefinitionId   如果是流程启动需要传入定义id
     * @param taskId    如果是任务需要传入任务id
     * @param cover 是否显示任务遮照
     * @param modelMap
     * @return
     */
    @ApiOperation("跳转到动态表单页面")
    @RequestMapping(value="/dynamicForm.html", method = {RequestMethod.GET, RequestMethod.POST})
    public String dynamicForm(@RequestParam(required = false) String formKey, @RequestParam(required = false) String processDefinitionId, @RequestParam(required = false) String taskId, @RequestParam(required = false, defaultValue = "false") boolean cover, ModelMap modelMap) {
        if(StringUtils.isBlank(formKey)){
            if(StringUtils.isNotBlank(processDefinitionId)){
                formKey = formService.getStartFormKey(processDefinitionId);
            }else if(StringUtils.isNotBlank(processDefinitionId)){
                Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
                if(task != null) {
                    formKey = task.getFormKey();
                }
            }
        }
        if(StringUtils.isBlank(formKey)){
            modelMap.put("msg", "formKey不存在");
            return "actForm/actException";
        }
        ActForm actForm = DTOUtils.newInstance(ActForm.class);
        List<ActForm> forms = actFormService.list(actForm);
        if(CollectionUtils.isEmpty(forms)){
            modelMap.put("msg", "formKey[\"+formKey+\"]找不到对应表单");
            return "actForm/actException";
        }
        ActControl actControl = DTOUtils.newInstance(ActControl.class);
        actControl.setFormKey(formKey);
        List<ActControl> actControls = actControlService.list(actControl);
        if(actControls.isEmpty()){
            modelMap.put("msg", "未配置表单控件");
            return "actForm/actException";
        }
        modelMap.put("cover", cover);
        modelMap.put("actForm", forms.get(0));
        modelMap.put("actControls", actControls);
        modelMap.put("processDefinitionId", processDefinitionId);
        modelMap.put("taskId", taskId);
        return "actForm/dynamicForm";
    }

    /**
     * ajax提交动态表单
     * @param variables
     * @param request
     * @return
     */
    @RequestMapping(value="/submitAsyc.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public BaseOutput<String> submitAsyc(@RequestParam Map<String, String> variables, HttpServletRequest request) {
        if(variables.get("processDefinitionId") != null){
            try {
                ProcessInstance processInstance = formService.submitStartFormData(variables.get("processDefinitionId"), variables);
                return BaseOutput.success("流程启动成功").setData(processInstance.getId());
            } catch (ActivitiException e) {
                return BaseOutput.failure(e.getMessage());
            }
        }else if(variables.get("taskId") != null){
            try {
                String taskId = variables.get("taskId");
                Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
                if(task == null){
                    return BaseOutput.failure("任务["+task.getId()+"]不存在");
                }
                Map<String, Object> processVariables = runtimeService.getVariables(task.getExecutionId());
                processVariables.putAll(variables);
                formService.submitTaskFormData(taskId, (Map)processVariables);
                return BaseOutput.success("任务提交成功");
            } catch (ActivitiException e) {
                return BaseOutput.failure(e.getMessage());
            }
        }else{
            return BaseOutput.success("无任务启动");
        }
    }

    /**
     * 提交动态表单
     * @param variables
     * @param attr
     * @return
     */
    @RequestMapping(value="/submit.action", method = {RequestMethod.GET, RequestMethod.POST})
    public String submit(@RequestParam Map<String, String> variables, RedirectAttributes attr) {
        if(StringUtils.isNotBlank(variables.get("processDefinitionId"))){
            ProcessInstance processInstance = formService.submitStartFormData(variables.get("processDefinitionId"), variables);
            attr.addAttribute("processInstanceId", processInstance.getId());
        }else if(StringUtils.isNotBlank(variables.get("taskId"))){
            String taskId = variables.get("taskId");
            Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
            if(task == null){
                throw new ParamErrorException("任务[" + task.getId() + "]不存在");
            }
            Map<String, Object> processVariables = runtimeService.getVariables(task.getExecutionId());
            processVariables.putAll(variables);
            //将变量信息加入taskId,保证ACT_RU_VARIABLE表中TASK_ID_有值
            taskService.setVariables(taskId, variables);
            formService.submitTaskFormData(taskId, (Map)processVariables);
//            taskService.complete(taskId, processVariables);
        }else{
            //无任务启动
        }
        String redirectUrl = variables.get("redirectUrl");
        if(StringUtils.isBlank(redirectUrl)){
            redirectUrl = "http://bpmc.diligrp.com:8617/task/taskCenter.html";
        }
        return "redirect:"+redirectUrl;
    }

}
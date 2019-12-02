package com.dili.ss.activiti.controller;

import com.dili.ss.activiti.domain.ActControl;
import com.dili.ss.activiti.service.ActControlService;
import com.dili.ss.domain.BaseOutput;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 由MyBatis Generator工具自动生成
 * This file was generated on 2019-03-19 17:14:28.
 */
@Api("/actControl")
@Controller
@RequestMapping("/actControl")
public class ActControlController {
    @Autowired
    ActControlService actControlService;

    /**
     * 跳转到ActControl页面
     * @param modelMap
     * @return
     */
    @RequestMapping(value="/index.html", method = RequestMethod.GET)
    public String index(ModelMap modelMap) {
        return "actControl/index";
    }

    /**
     * 分页查询ActControl，返回easyui分页信息
     * @param actControl
     * @return
     * @throws Exception
     */
    @ApiImplicitParams({
		@ApiImplicitParam(name="ActControl", paramType="form", value = "ActControl的form信息", required = false, dataType = "string")
	})
    @RequestMapping(value="/listPage.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody String listPage(ActControl actControl) throws Exception {
        return actControlService.listEasyuiPageByExample(actControl, true).toString();
    }

    /**
     * 新增ActControl
     * @param actControl
     * @return
     */
    @ApiImplicitParams({
		@ApiImplicitParam(name="ActControl", paramType="form", value = "ActControl的form信息", required = true, dataType = "string")
	})
    @RequestMapping(value="/insert.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody BaseOutput insert(ActControl actControl) {
        actControl.setControlId(actControl.getControlId().trim());
        if(StringUtils.isBlank(actControl.getName())){
            actControl.setName(actControl.getControlId());
        }
        actControlService.insertSelective(actControl);
        return BaseOutput.success("新增成功");
    }

    /**
     * 修改ActControl
     * @param actControl
     * @return
     */
    @ApiImplicitParams({
		@ApiImplicitParam(name="ActControl", paramType="form", value = "actControl的form信息", required = true, dataType = "string")
	})
    @RequestMapping(value="/update.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody BaseOutput update(ActControl actControl) {
        actControl.setControlId(actControl.getControlId().trim());
        if(StringUtils.isBlank(actControl.getName())){
            actControl.setName(actControl.getControlId());
        }
        actControlService.updateExactSimple(actControl);
        return BaseOutput.success("修改成功");
    }

    /**
     * 删除ActControl
     * @param id
     * @return
     */
    @ApiImplicitParams({
		@ApiImplicitParam(name="id", paramType="form", value = "ActControl的主键", required = true, dataType = "long")
	})
    @RequestMapping(value="/delete.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody BaseOutput delete(Long id) {
        actControlService.delete(id);
        return BaseOutput.success("删除成功");
    }


}
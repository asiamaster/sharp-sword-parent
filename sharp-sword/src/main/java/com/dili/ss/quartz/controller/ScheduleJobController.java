package com.dili.ss.quartz.controller;

import com.dili.ss.domain.BaseOutput;
import com.dili.ss.quartz.domain.ScheduleJob;
import com.dili.ss.quartz.service.ScheduleJobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 由MyBatis Generator工具自动生成
 * This file was generated on 2017-10-24 09:32:32.
 */
//@Api("/scheduleJob")
@Controller
@RequestMapping("/scheduleJob")
@ConditionalOnProperty(name = "quartz.enabled")
public class ScheduleJobController {

	protected static final Logger log = LoggerFactory.getLogger(ScheduleJobController.class);
    @Autowired
    ScheduleJobService scheduleJobService;

	/**
	 * 动态刷新调度器
	 * @return
	 */
	@RequestMapping("/refreshAll.aspx")
	public @ResponseBody
	BaseOutput refreshAll(){
		scheduleJobService.refresh(scheduleJobService.list(null));
		return BaseOutput.success("调度器刷新完成");
	}

//    @ApiOperation("跳转到ScheduleJob页面")
    @RequestMapping(value="/index.html", method = RequestMethod.GET)
    public String index(ModelMap modelMap) {
        return "scheduleJob/index";
    }

//    @ApiOperation(value="查询ScheduleJob", notes = "查询ScheduleJob，返回列表信息")
//    @ApiImplicitParams({
//		@ApiImplicitParam(name="ScheduleJob", paramType="form", value = "ScheduleJob的form信息", required = false, dataType = "string")
//	})
    @RequestMapping(value="/list.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody List<ScheduleJob> list(ScheduleJob scheduleJob) {
        return scheduleJobService.list(scheduleJob);
    }

//    @ApiOperation(value="分页查询ScheduleJob", notes = "分页查询ScheduleJob，返回easyui分页信息")
//    @ApiImplicitParams({
//		@ApiImplicitParam(name="ScheduleJob", paramType="form", value = "ScheduleJob的form信息", required = false, dataType = "string")
//	})
    @RequestMapping(value="/listPage.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody String listPage(ScheduleJob scheduleJob) throws Exception {
        return scheduleJobService.listEasyuiPageByExample(scheduleJob, true).toString();
    }

//    @ApiOperation("新增ScheduleJob")
//    @ApiImplicitParams({
//		@ApiImplicitParam(name="ScheduleJob", paramType="form", value = "ScheduleJob的form信息", required = true, dataType = "string")
//	})
    @RequestMapping(value="/insert.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody
    BaseOutput insert(ScheduleJob scheduleJob) {
        scheduleJobService.insertSelective(scheduleJob, true);
        return BaseOutput.success("新增成功");
    }

//    @ApiOperation("修改ScheduleJob")
//    @ApiImplicitParams({
//		@ApiImplicitParam(name="ScheduleJob", paramType="form", value = "ScheduleJob的form信息", required = true, dataType = "string")
//	})
    @RequestMapping(value="/update.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody
    BaseOutput update(ScheduleJob scheduleJob) {
        scheduleJobService.updateSelective(scheduleJob, true);
        return BaseOutput.success("修改成功");
    }

//    @ApiOperation("删除ScheduleJob")
//    @ApiImplicitParams({
//		@ApiImplicitParam(name="id", paramType="form", value = "ScheduleJob的主键", required = true, dataType = "long")
//	})
    @RequestMapping(value="/delete.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody
    BaseOutput delete(Long id) {
        scheduleJobService.delete(id, true);
        return BaseOutput.success("删除成功");
    }

//	@ApiOperation("暂停ScheduleJob")
//	@ApiImplicitParams({
//			@ApiImplicitParam(name="scheduleJob", paramType="form", value = "ScheduleJob的主键", required = true)
//	})
	@RequestMapping(value="/pause.action", method = {RequestMethod.GET, RequestMethod.POST})
	public @ResponseBody
	BaseOutput pause(ScheduleJob scheduleJob) {
		scheduleJobService.pauseJob(scheduleJob);
		return BaseOutput.success("暂停成功");
	}

//	@ApiOperation("恢复ScheduleJob")
//	@ApiImplicitParams({
//			@ApiImplicitParam(name="scheduleJob", paramType="form", value = "ScheduleJob的主键", required = true)
//	})
	@RequestMapping(value="/resume.action", method = {RequestMethod.GET, RequestMethod.POST})
	public @ResponseBody
	BaseOutput resume(ScheduleJob scheduleJob) {
		scheduleJobService.resumeJob(scheduleJob);
		return BaseOutput.success("恢复成功");
	}
}
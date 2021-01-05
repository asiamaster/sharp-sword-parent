package com.dili.ss.quartz.service.impl;

import com.dili.ss.base.BaseServiceImpl;
import com.dili.ss.dto.DTOUtils;
import com.dili.ss.quartz.dao.ScheduleJobMapper;
import com.dili.ss.quartz.domain.QuartzConstants;
import com.dili.ss.quartz.domain.ScheduleJob;
import com.dili.ss.quartz.service.JobTaskService;
import com.dili.ss.quartz.service.ScheduleJobService;
import com.dili.ss.util.CronDateUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 * 由MyBatis Generator工具自动生成
 * This file was generated on 2017-10-24 09:32:32.
 */
@Service
@ConditionalOnProperty(name = "quartz.enabled")
public class ScheduleJobServiceImpl extends BaseServiceImpl<ScheduleJob, Long> implements ScheduleJobService {

	@Autowired
	private JobTaskService jobTaskService;

	public ScheduleJobMapper getActualDao() {
		return (ScheduleJobMapper) getDao();
	}

	@Override
	public void addJob(ScheduleJob scheduleJob, boolean overwrite) {
		try {
			try {
				if(StringUtils.isBlank(scheduleJob.getCronExpression())){
					jobTaskService.addJob(scheduleJob, overwrite);
				}else {
					//解析成功的话就是个固定的时间，需要判断当前cron表达式是否超时，超时就不添加调度任务，避免报错
					Date date = CronDateUtils.getDate(scheduleJob.getCronExpression());
					if (date.getTime() >= System.currentTimeMillis() + 5000) {
						jobTaskService.addJob(scheduleJob, overwrite);
					}
				}
			} catch (ParseException e) {
				//解析出错就不解析，继续添加调度任务
				jobTaskService.addJob(scheduleJob, overwrite);
			}
		} catch (SchedulerException e) {
			LOGGER.error(e.getMessage());
			throw new RuntimeException(e);
		}
	}

	@Override
	public int insert(ScheduleJob scheduleJob, boolean schedule) {
		if(!checkRepeat(scheduleJob.getJobGroup(), scheduleJob.getJobName())){
			return 0;
		}
		if(schedule) {
			try {
				jobTaskService.addJob(scheduleJob, true);
				scheduleJob.setJobStatus(QuartzConstants.JobStatus.NORMAL.getCode());
			} catch (SchedulerException e) {
				handleSchedulerException(e, scheduleJob);
			}
		}
		return insert(scheduleJob);
	}

	@Override
	public int insertSelective(ScheduleJob scheduleJob, boolean schedule) {
		if(!checkRepeat(scheduleJob.getJobGroup(), scheduleJob.getJobName())){
			return 0;
		}
		if(schedule) {
			try {
				jobTaskService.addJob(scheduleJob, true);
				scheduleJob.setJobStatus(QuartzConstants.JobStatus.NORMAL.getCode());
			} catch (SchedulerException e) {
				handleSchedulerException(e, scheduleJob);
			}
		}
		return insertSelective(scheduleJob);
	}

	@Override
	public int batchInsert(List<ScheduleJob> list, boolean schedule) {
		for (ScheduleJob job : list) {
			if (!checkRepeat(job.getJobGroup(), job.getJobName())) {
				return 0;
			}
		}
		if(schedule) {
			for (ScheduleJob job : list) {
				try {
					jobTaskService.addJob(job, true);
					job.setJobStatus(QuartzConstants.JobStatus.NORMAL.getCode());
				} catch (SchedulerException e) {
					handleSchedulerException(e, job);
				}
			}
		}
		return batchInsert(list);
	}

	@Override
	public int update(ScheduleJob job, boolean schedule) {
		if (!checkUpdateRepeat(job.getId(), job.getJobGroup(), job.getJobName())) {
			return 0;
		}
		if(schedule) {
			try {
				jobTaskService.updateJob(job);
				job.setJobStatus(QuartzConstants.JobStatus.NORMAL.getCode());
			} catch (SchedulerException e) {
				handleSchedulerException(e, job);
			}
		}
		return update(job);
	}

	@Override
	public int updateSelective(ScheduleJob job, boolean schedule) {
		if (!checkUpdateRepeat(job.getId(), job.getJobGroup(), job.getJobName())) {
			return 0;
		}
		if(schedule) {
			try {
				jobTaskService.updateJob(job);
				job.setJobStatus(QuartzConstants.JobStatus.NORMAL.getCode());
			} catch (SchedulerException e) {
				handleSchedulerException(e, job);
			}
		}
		return updateExactSimple(job);
	}

	@Override
	public int batchUpdateSelective(List<ScheduleJob> list, boolean schedule) {
		if(schedule) {
			for (ScheduleJob job : list) {
				try {
					jobTaskService.updateJob(job);
					job.setJobStatus(QuartzConstants.JobStatus.NORMAL.getCode());
				} catch (SchedulerException e) {
					handleSchedulerException(e, job);
				}
			}
		}
		return batchUpdateSelective(list);
	}

	@Override
	public int delete(List<Long> ids, boolean schedule) {
		if(schedule) {
			try {
				for (Long id : ids) {
					jobTaskService.deleteJob(get(id));
				}
			} catch (SchedulerException e) {
				LOGGER.error(e.getMessage());
				throw new RuntimeException(e);
			}
		}
		return delete(ids);
	}

	@Override
	public int delete(Long id, boolean schedule) {
		if(schedule) {
			try {
				jobTaskService.deleteJob(get(id));
			} catch (SchedulerException e) {
				LOGGER.error(e.getMessage());
				throw new RuntimeException(e);
			}
		}
		return delete(id);
	}

	@Override
	public void resumeJob(ScheduleJob scheduleJob) {
		try {
			jobTaskService.resumeJob(scheduleJob);
			scheduleJob.setJobStatus(QuartzConstants.JobStatus.NORMAL.getCode());
		} catch (SchedulerException e) {
			handleSchedulerException(e, scheduleJob);
		}
		//有id就不用查了,直接改状态为正常
		if (scheduleJob.getId() != null) {
			ScheduleJob job = DTOUtils.newDTO(ScheduleJob.class);
			job.setId(scheduleJob.getId());
			job.setJobStatus(QuartzConstants.JobStatus.NORMAL.getCode());
			updateSelective(job);
		} else {
			List<ScheduleJob> jobs = list(scheduleJob);
			if (ListUtils.emptyIfNull(jobs).isEmpty()) {
				return;
			}
			ScheduleJob job = DTOUtils.newDTO(ScheduleJob.class);
			job.setId(jobs.get(0).getId());
			job.setJobStatus(QuartzConstants.JobStatus.NORMAL.getCode());
			updateSelective(job);
		}
	}

	@Override
	public void pauseJob(ScheduleJob scheduleJob) {
		try {
			jobTaskService.pauseJob(scheduleJob);
			scheduleJob.setJobStatus(QuartzConstants.JobStatus.PAUSED.getCode());
		} catch (SchedulerException e) {
			handleSchedulerException(e, scheduleJob);
		}
		//有id就不用查了,直接改状态为STATUS_RUNNING
		if (scheduleJob.getId() != null) {
			ScheduleJob job = DTOUtils.newDTO(ScheduleJob.class);
			job.setId(scheduleJob.getId());
			job.setJobStatus(QuartzConstants.JobStatus.PAUSED.getCode());
			updateSelective(job);
		} else {
			List<ScheduleJob> jobs = list(scheduleJob);
			if (ListUtils.emptyIfNull(jobs).isEmpty()) {
				return;
			}
			ScheduleJob job = DTOUtils.newDTO(ScheduleJob.class);
			job.setId(jobs.get(0).getId());
			job.setJobStatus(QuartzConstants.JobStatus.PAUSED.getCode());
			updateSelective(job);
		}
	}

	@Override
	public void refresh(ScheduleJob scheduleJob) {
		try {
			jobTaskService.updateJob(scheduleJob);
		} catch (SchedulerException e) {
			LOGGER.error(e.getMessage());
			throw new RuntimeException(e);
		}
	}

	@Override
	public void refresh(List<ScheduleJob> scheduleJobs) {
		for (ScheduleJob scheduleJob : scheduleJobs) {
			try {
				jobTaskService.updateJob(scheduleJob);
			} catch (SchedulerException e) {
				LOGGER.error(e.getMessage());
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public List<ScheduleJob> getAllJob() throws SchedulerException {
		return jobTaskService.getAllJob();
	}

	@Override
	public List<ScheduleJob> getRunningJob() throws SchedulerException {
		return jobTaskService.getRunningJob();
	}

	/**
	 * 统一处理调度异常
	 *
	 * @param e
	 * @param scheduleJob
	 */
	private void handleSchedulerException(SchedulerException e, ScheduleJob scheduleJob) {
		LOGGER.error(e.getMessage());
		//如果是无法触发的调度器，则也是成功的，需要修改任务状态为无，
		if (e.getMessage().endsWith("will never fire.")) {
			scheduleJob.setJobStatus(QuartzConstants.JobStatus.NONE.getCode());
		} else {//否则修改任务状态为错误
			scheduleJob.setJobStatus(QuartzConstants.JobStatus.ERROR.getCode());
		}
	}

	/**
	 * 调度删除一个job
	 *
	 * @param scheduleJob
	 * @throws SchedulerException
	 */
	@Override
	public void deleteJob(ScheduleJob scheduleJob) throws SchedulerException{
		jobTaskService.deleteJob(scheduleJob);
	}

	/**
	 * 检查调度器名称和组名不重复, 重复返回false
	 * @param group
	 * @param name
	 * @return
	 */
	private boolean checkRepeat(String group, String name){
		ScheduleJob scheduleJob = DTOUtils.newDTO(ScheduleJob.class);
		scheduleJob.setJobName(name);
		scheduleJob.setJobGroup(group);
		return getActualDao().select(scheduleJob).size() > 0 ? false: true;
	}

	/**
	 * 检查修改调度器时，名称和组名除当前名称外，不重复, 重复返回false
	 * @param group
	 * @param name
	 * @return
	 */
	private boolean checkUpdateRepeat(Long id, String group, String name){
		ScheduleJob scheduleJob = DTOUtils.newDTO(ScheduleJob.class);
		scheduleJob.setJobName(name);
		scheduleJob.setJobGroup(group);
		List<ScheduleJob> scheduleJobs = getActualDao().select(scheduleJob);
		if(scheduleJobs.isEmpty()){
			return true;
		}
		//如果名称没有变动
		if(id.equals(scheduleJobs.get(0).getId())){
			return true;
		}
		return false;
	}

}
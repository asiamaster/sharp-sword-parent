package com.dili.ss.quartz.service;

import com.dili.ss.dto.DTOUtils;
import com.dili.ss.quartz.domain.QuartzConstants;
import com.dili.ss.quartz.domain.ScheduleJob;
import com.dili.ss.quartz.job.QuartzJobDisallowConcurrentExecutionFactory;
import com.dili.ss.quartz.job.QuartzJobFactory;
import com.dili.ss.util.SpringUtil;
import org.apache.commons.lang3.StringUtils;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by asiam on 2017/3/21 0021.
 */
@Service
@ConditionalOnProperty(name = "quartz.enabled")
//public class JobTaskService{
public class JobTaskService implements ApplicationListener<ContextRefreshedEvent> {

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${quartz.enabled:}")
    private String quartzEnabled;
    //不使用注解，而使用延迟加载的原因是:如果没配置quartz.enabled=true，则会加载失败
//    @Autowired
    private SchedulerFactoryBean schedulerFactoryBean;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        if(contextRefreshedEvent.getApplicationContext().getParent() == null
                && SpringUtil.getApplicationContext().containsBean("schedulerFactoryBean")
                && "true".equals(quartzEnabled)) {
            schedulerFactoryBean = (SchedulerFactoryBean) SpringUtil.getBean(SchedulerFactoryBean.class);
        }
//            //        Scheduler scheduler = schedulerFactoryBean.getScheduler();
//            // 这里获取任务信息数据
////        List<QuartzJobFactory> test = scheduleJobMapper.getAll();
//            List<ScheduleJob> jobList = new ArrayList<>();
//            ScheduleJob scheduleJob = new ScheduleJob();
//            scheduleJob.setSpringId("pingService");
//            scheduleJob.setCreateTime(new Date());
//            scheduleJob.setDescription("ping任务1");
//            scheduleJob.setId(1l);
//            scheduleJob.setIsConcurrent(1);
//            scheduleJob.setJobName("job1");
//            scheduleJob.setJobGroup("group1");
//            scheduleJob.setMethodName("ping");
//            scheduleJob.setStartDelay(0);
//            scheduleJob.setRepeatInterval(10);
//            jobList.add(scheduleJob);
//        scheduleJob = new ScheduleJob();
//        scheduleJob.setSpringId("pingService");
//        scheduleJob.setCreateTime(new Date());
//        scheduleJob.setDescription("ping任务2");
//        scheduleJob.setIsConcurrent(1);
//        scheduleJob.setId(2l);
//        scheduleJob.setJobName("job2");
//        scheduleJob.setJobGroup("group2");
//        scheduleJob.setMethodName("ping");
//        scheduleJob.setStartDelay(0);
//        scheduleJob.setRepeatInterval(10);
//        jobList.add(scheduleJob);
//
//            //只循环一次，测试放入多台设备ping
//            for (ScheduleJob job : jobList) {
//                Device tmp = new Device();
//                List<Device> devices = deviceService.list(tmp);
//                List<Long> ids = Lists.newArrayList();
//                for(Device device : devices){
//                    ids.add(device.getId());
//                    break;
//                }
//                try {
//                    addJob(job, ids, true);
//                } catch (SchedulerException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
    }

    /**
     * 添加任务
     *
     * @param job   任务属性
     * @param overwrite 是否覆盖原JobData数据
     * @throws SchedulerException
     */
    public void addJob(ScheduleJob job, boolean overwrite) throws SchedulerException {
//        if (job == null || QuartzConstants.JobStatus.PAUSED.getCode().equals(job.getJobStatus())) {
        if (job == null) {
            return;
        }
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        log.debug(scheduler + ".......................................................................................add");
        TriggerKey triggerKey = TriggerKey.triggerKey(job.getJobName(), job.getJobGroup());
        Trigger trigger = scheduler.getTrigger(triggerKey);
        // 不存在，创建一个
        if (null == trigger) {
            Class clazz = QuartzConstants.Concurrent.Async.getCode().equals(job.getIsConcurrent()) ? QuartzJobFactory.class : QuartzJobDisallowConcurrentExecutionFactory.class;
            JobDetail jobDetail = JobBuilder.newJob(clazz).withIdentity(job.getJobName(), job.getJobGroup()).build();
            jobDetail.getJobDataMap().put(QuartzConstants.jobDataMapScheduleJobKey, job);
            ScheduleBuilder scheduleBuilder = null;
            //优先执行有表达式的job，没有表达式则使用简单调度器，间隔调度
            if (StringUtils.isBlank(job.getCronExpression())) {
                scheduleBuilder = SimpleScheduleBuilder.repeatSecondlyForever(job.getRepeatInterval());
            } else {
                scheduleBuilder = CronScheduleBuilder.cronSchedule(job.getCronExpression());
            }
            if (job.getStartDelay() != null && job.getStartDelay() > 0) {
                Long startDelayTime = System.currentTimeMillis() + (job.getStartDelay() * 1000);
                trigger = TriggerBuilder.newTrigger().withIdentity(job.getJobName(), job.getJobGroup()).startAt(new Date(startDelayTime)).withSchedule(scheduleBuilder).build();
            } else {
                trigger = TriggerBuilder.newTrigger().withIdentity(job.getJobName(), job.getJobGroup()).withSchedule(scheduleBuilder).build();
            }
            scheduler.scheduleJob(jobDetail, trigger);
        } else {
            //优先执行有表达式的job，没有表达式则使用简单调度器，间隔调度
            if (StringUtils.isBlank(job.getCronExpression())) {
                //按新的间隔构建trigger
                SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.repeatSecondlyForever(job.getRepeatInterval());
                SimpleTrigger simpleTrigger = (SimpleTrigger) trigger;
                trigger = simpleTrigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();
            } else {
                // Trigger已存在，那么更新相应的定时设置
                CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(job.getCronExpression());
                CronTrigger cronTrigger = (CronTrigger) trigger;
                // 按新的cronExpression表达式重新构建trigger
                trigger = cronTrigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();
            }
            Class clazz = QuartzConstants.Concurrent.Async.getCode().equals(job.getIsConcurrent()) ? QuartzJobFactory.class : QuartzJobDisallowConcurrentExecutionFactory.class;
            JobDetail jobDetail = JobBuilder.newJob(clazz).withIdentity(job.getJobName(), job.getJobGroup()).storeDurably(true).build();

//            Flowable.fromArray(jobData).filter(s->!currentTargetIds.contains(s)).subscribe(ids ->currentTargetIds.addAll(ids));
            jobDetail.getJobDataMap().put(QuartzConstants.jobDataMapScheduleJobKey, job);
            //这里是反的，如果不覆盖，才取出以前的数据，再填回去
            if(!overwrite) {
                ScheduleJob currentScheduleJob = (ScheduleJob)scheduler.getJobDetail(JobKey.jobKey(job.getJobName(),job.getJobGroup())).getJobDataMap().get(QuartzConstants.jobDataMapScheduleJobKey);
                job.setJobData(currentScheduleJob.getJobData());
            }
            // durable, 指明任务就算没有绑定Trigger仍保留在Quartz的JobStore中,
            scheduler.addJob(jobDetail, true);
            // 按新的trigger重新设置job执行
            scheduler.rescheduleJob(triggerKey, trigger);
        }
    }

    /**
     * 获取正在运行中的ScheduleJob
     *
     * @return
     * @throws SchedulerException
     */
    public List<ScheduleJob> getRunningJob() throws SchedulerException {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        List<JobExecutionContext> executingJobs = scheduler.getCurrentlyExecutingJobs();
        List<ScheduleJob> jobList = new ArrayList<ScheduleJob>(executingJobs.size());
        for (JobExecutionContext executingJob : executingJobs) {
            ScheduleJob job = DTOUtils.newDTO(ScheduleJob.class);
            JobDetail jobDetail = executingJob.getJobDetail();
            JobKey jobKey = jobDetail.getKey();
            Trigger trigger = executingJob.getTrigger();
            job.setJobName(jobKey.getName());
            job.setJobGroup(jobKey.getGroup());
            job.setDescription("触发器:" + trigger.getKey());

            Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
//          QuartzConstants.JobStatus的code和triggerState.ordinal()完成对应
            job.setJobStatus(triggerState.ordinal());
            if (trigger instanceof CronTrigger) {
                CronTrigger cronTrigger = (CronTrigger) trigger;
                String cronExpression = cronTrigger.getCronExpression();
                job.setCronExpression(cronExpression);
            }else{
                SimpleTrigger simpleTrigger = (SimpleTrigger) trigger;
                job.setRepeatInterval(new Long(simpleTrigger.getRepeatInterval()).intValue()/1000);
            }
            jobList.add(job);
        }
        return jobList;
    }

    /**
     * 获取触发器
     * @param triggerName   触发器名称
     * @param triggerGroup  触发器分组
     * @return
     */
    public Trigger getTrigger(String triggerName, String triggerGroup){
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        try {
            return scheduler.getTrigger(TriggerKey.triggerKey(triggerName, triggerGroup));
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取JobDetail
     * @param jobName   名称
     * @param jobGroup  分组
     * @return
     */
    public JobDetail getJobDetail(String jobName, String jobGroup){
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        try {
            return scheduler.getJobDetail(JobKey.jobKey(jobName, jobGroup));
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 暂停一个job
     *
     * @param scheduleJob
     * @throws SchedulerException
     */
    public void pauseJob(ScheduleJob scheduleJob) throws SchedulerException {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        JobKey jobKey = JobKey.jobKey(scheduleJob.getJobName(), scheduleJob.getJobGroup());
        scheduler.pauseJob(jobKey);
    }

    /**
     * 恢复一个暂停的job
     *
     * @param scheduleJob
     * @throws SchedulerException
     */
    public void resumeJob(ScheduleJob scheduleJob) throws SchedulerException {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        JobKey jobKey = JobKey.jobKey(scheduleJob.getJobName(), scheduleJob.getJobGroup());
        scheduler.resumeJob(jobKey);
    }

    /**
     * 删除一个job
     *
     * @param scheduleJob
     * @throws SchedulerException
     */
    public void deleteJob(ScheduleJob scheduleJob) throws SchedulerException {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        JobKey jobKey = JobKey.jobKey(scheduleJob.getJobName(), scheduleJob.getJobGroup());
        scheduler.deleteJob(jobKey);

    }

    /**
     * 获取所有计划中的任务列表
     *
     * @return
     * @throws SchedulerException
     */
    public List<ScheduleJob> getAllJob() throws SchedulerException {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        GroupMatcher<JobKey> matcher = GroupMatcher.anyJobGroup();
        Set<JobKey> jobKeys = scheduler.getJobKeys(matcher);
        List<ScheduleJob> jobList = new ArrayList<ScheduleJob>();
        for (JobKey jobKey : jobKeys) {
            JobDetail jobDetail = scheduler.getJobDetail(jobKey);
            ScheduleJob scheduleJob = (ScheduleJob) jobDetail.getJobDataMap().get(QuartzConstants.jobDataMapScheduleJobKey);
            List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
            for (Trigger trigger : triggers) {
                if(jobKey.equals(trigger.getJobKey())){
                    scheduleJob.setJobStatus(scheduler.getTriggerState(trigger.getKey()).ordinal());
                }
            }
            jobList.add(scheduleJob);
        }
        return jobList;
//        for (JobKey jobKey : jobKeys) {
//            List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
//            for (Trigger trigger : triggers) {
//                ScheduleJob job = DTOUtils.newDTO(ScheduleJob.class);
//                job.setJobName(jobKey.getName());
//                job.setJobGroup(jobKey.getGroup());
//                job.setDescription("触发器:" + trigger.getKey());
//                Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
////                QuartzConstants.JobStatus的code和triggerState.ordinal()完成对应
//                job.setJobStatus(triggerState.ordinal());
//
//                if (trigger instanceof CronTrigger) {
//                    CronTrigger cronTrigger = (CronTrigger) trigger;
//                    String cronExpression = cronTrigger.getCronExpression();
//                    job.setCronExpression(cronExpression);
//                } else if(trigger instanceof SimpleTrigger){
//                    SimpleTrigger simpleTrigger = (SimpleTrigger) trigger;
//                    job.setRepeatInterval(new Long(simpleTrigger.getRepeatInterval()/1000L).intValue());
//                }
//                jobList.add(job);
//            }
//        }
//        return jobList;
    }

    /**
     * 根据JobName和JobGroup立即执行计划中的job
     *
     * @param scheduleJob
     * @throws SchedulerException
     */
    public void runAJobNow(ScheduleJob scheduleJob) throws SchedulerException {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        JobKey jobKey = JobKey.jobKey(scheduleJob.getJobName(), scheduleJob.getJobGroup());
        scheduler.triggerJob(jobKey);
    }

    /**
     * 根据triggerKey(任务名+任务分组)更新job时间表达式
     *
     * @param scheduleJob
     * @throws SchedulerException
     */
    public void updateJob(ScheduleJob scheduleJob) throws SchedulerException {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        TriggerKey triggerKey = TriggerKey.triggerKey(scheduleJob.getJobName(), scheduleJob.getJobGroup());
        Trigger trigger = null;
//        if (StringUtils.isBlank(scheduleJob.getCronExpression())) {
//            trigger = (SimpleTrigger)scheduler.getTrigger(triggerKey);
//        }else{
//            trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
//        }
        trigger = scheduler.getTrigger(triggerKey);
        //如果不存在trigger(可能是从数据源读取出来的信息，而调度器中并不存在)，则新增调度信息
        if(trigger == null) {
            addJob(scheduleJob, true);
            return;
        }
        //优先执行有表达式的job，没有表达多则使用简单调度器，间隔调度
        if (StringUtils.isBlank(scheduleJob.getCronExpression())) {
            //按新的间隔构建trigger
            SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.repeatSecondlyForever(scheduleJob.getRepeatInterval());
//            SimpleTrigger simpleTrigger = (SimpleTrigger) trigger;
//            trigger = simpleTrigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();
            trigger = TriggerBuilder.newTrigger().withIdentity(triggerKey).withSchedule(scheduleBuilder).forJob(scheduleJob.getJobName(), scheduleJob.getJobGroup()).build();
            trigger.getJobDataMap().put(QuartzConstants.jobDataMapScheduleJobKey, scheduleJob);
        } else {
            // Trigger已存在，那么更新相应的定时设置
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(scheduleJob.getCronExpression());
//            CronTrigger cronTrigger = (CronTrigger) trigger;
//            // 按新的cronExpression表达式重新构建trigger
//            trigger = cronTrigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder).forJob(scheduleJob.getJobName(), scheduleJob.getJobGroup()).build();
            trigger = TriggerBuilder.newTrigger().withIdentity(triggerKey).withSchedule(scheduleBuilder).forJob(scheduleJob.getJobName(), scheduleJob.getJobGroup()).build();
            trigger.getJobDataMap().put(QuartzConstants.jobDataMapScheduleJobKey, scheduleJob);
        }
        scheduler.rescheduleJob(triggerKey, trigger);
    }

}

package com.dili.ss.quartz.job;

import com.dili.ss.quartz.TaskUtils;
import com.dili.ss.quartz.domain.QuartzConstants;
import com.dili.ss.quartz.domain.ScheduleJob;
import com.dili.ss.quartz.domain.ScheduleMessage;
import com.dili.ss.quartz.listener.SchedulerRetryListener;
import com.github.rholder.retry.*;
import com.google.common.base.Predicates;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


/**
 *  @Author: wangmi
 *  @Description: 串行job，若一个方法一次执行不完下次轮转时则等待改方法执行完后才执行下一次操作
 *  否则会在到时间后再启用新的线程执行
 */
@DisallowConcurrentExecution
public class QuartzJobDisallowConcurrentExecutionFactory implements Job {

    protected Logger log = LoggerFactory.getLogger(this.getClass());
    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        ScheduleJob scheduleJob = (ScheduleJob) jobExecutionContext.getMergedJobDataMap().get(QuartzConstants.jobDataMapScheduleJobKey);
        ScheduleMessage scheduleMessage = new ScheduleMessage();
        scheduleMessage.setJobData(scheduleJob.getJobData());
//        TaskUtils.invokeMethod(scheduleJob, scheduleMessage);
        invoke(scheduleJob, scheduleMessage);
    }

    /**
     * 调用，支持重试,间隔三秒，三次重试都失败后打印异常
     * @param scheduleJob
     * @param scheduleMessage
     */
    private void invoke(ScheduleJob scheduleJob, ScheduleMessage scheduleMessage) {
        //默认不重试
        if(scheduleJob.getRetryCount() == null || scheduleJob.getRetryCount() < 0){
            scheduleJob.setRetryCount(0);
        }
        //默认重试间隔1秒
        if(scheduleJob.getRetryInterval() == null){
            scheduleJob.setRetryInterval(1000L);
        }
        // RetryerBuilder 构建重试实例 retryer,可以设置重试源且可以支持多个重试源，可以配置重试次数或重试超时时间，以及可以配置等待时间间隔
        Retryer<Boolean> retryer = RetryerBuilder.<Boolean> newBuilder()
                //设置异常重试源
                .retryIfException()
                //返回false也需要重试
                .retryIfResult(Predicates.equalTo(false))
                //设置重试3次，同样可以设置重试超时时间
                .withStopStrategy(StopStrategies.stopAfterAttempt(scheduleJob.getRetryCount() + 1))
                //设置每次重试间隔3秒
                .withWaitStrategy(WaitStrategies.fixedWait(scheduleJob.getRetryInterval(), TimeUnit.MILLISECONDS))
                .withRetryListener(new SchedulerRetryListener(scheduleJob))
                .build();
        try {
            retryer.call(()-> {
                return TaskUtils.invokeMethod(scheduleJob, scheduleMessage);
            });
        } catch (RetryException | ExecutionException e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }
    }

//    public static void main(String[] args) {
//        ScheduleJob scheduleJob = DTOUtils.newDTO(ScheduleJob.class);
//        scheduleJob.setRetryCount(3);
//        scheduleJob.setRepeatInterval(2);
//        // RetryerBuilder 构建重试实例 retryer,可以设置重试源且可以支持多个重试源，可以配置重试次数或重试超时时间，以及可以配置等待时间间隔
//        Retryer<Boolean> retryer = RetryerBuilder.<Boolean> newBuilder()
//                //设置异常重试源
//                .retryIfException()
//                //返回false也需要重试
//                .retryIfResult(Predicates.equalTo(false))
//                //设置重试3次，同样可以设置重试超时时间
//                .withStopStrategy(StopStrategies.stopAfterAttempt(scheduleJob.getRetryCount() + 1))
//                //设置每次重试间隔3秒
//                .withWaitStrategy(WaitStrategies.fixedWait(scheduleJob.getRepeatInterval(), TimeUnit.SECONDS))
//                .withRetryListener(new SchedulerRetryListener(scheduleJob))
//                .build();
//        try {
//            retryer.call(()-> {
//                if(true){
//                    throw new RuntimeException("aaa");
//                }
//                return false;
//            });
//        } catch (RetryException | ExecutionException e) {
//            System.out.println(ExceptionUtils.getStackTrace(e));
//        }
//        System.out.println("完成");
//    }

}

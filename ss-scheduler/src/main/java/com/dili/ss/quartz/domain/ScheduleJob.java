package com.dili.ss.quartz.domain;

import com.dili.ss.dto.IBaseDomain;
import com.dili.ss.dto.IMybatisForceParams;
import com.dili.ss.metadata.FieldEditor;
import com.dili.ss.metadata.annotation.EditMode;
import com.dili.ss.metadata.annotation.FieldDef;

import javax.persistence.*;
import java.util.Date;

/**
 * 由MyBatis Generator工具自动生成
 * 
 * This file was generated on 2017-10-24 09:32:32.
 */
@Table(name = "`schedule_job`")
public interface ScheduleJob extends IBaseDomain, IMybatisForceParams {
//    //无
//    public static final Integer STATUS_NONE = 0;
//    //正常
//    public static final Integer STATUS_RUNNING = 1;
//    //暂停
//    public static final Integer STATUS_PAUSE = 2;
//    //完成
//    public static final Integer STATUS_COMPLETE = 3;
//    //错误
//    public static final Integer STATUS_ERROR = 4;
//    //阻塞
//    public static final Integer STATUS_BLOCK = 5;
//
//    //异步
//    public static final Integer CONCURRENT_IS = 1;
//    //同步
//    public static final Integer CONCURRENT_NOT = 0;

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`id`")
    @FieldDef(label="id")
    @EditMode(editor = FieldEditor.Number, required = true)
    Long getId();

    @Override
    void setId(Long id);

    @Column(name = "`created`")
    @FieldDef(label="创建时间")
    @EditMode(editor = FieldEditor.Datetime, required = true)
    Date getCreated();

    void setCreated(Date created);

    @Column(name = "`modified`")
    @FieldDef(label="修改时间")
    @EditMode(editor = FieldEditor.Datetime, required = true)
    Date getModified();

    void setModified(Date modified);

    @Column(name = "`job_name`")
    @FieldDef(label="任务名", maxLength = 40)
    @EditMode(editor = FieldEditor.Text, required = false)
    String getJobName();

    void setJobName(String jobName);

    @Column(name = "`job_group`")
    @FieldDef(label="任务分组", maxLength = 40)
    @EditMode(editor = FieldEditor.Text, required = false)
    String getJobGroup();

    void setJobGroup(String jobGroup);

    @Column(name = "`job_status`")
    @FieldDef(label="任务状态")
    @EditMode(editor = FieldEditor.Combo, required = false, params="{\"data\":[{\"text\":\"无\",\"value\":0},{\"text\":\"正常\",\"value\":1},{\"text\":\"暂停\",\"value\":2},{\"text\":\"完成\",\"value\":3},{\"text\":\"错误\",\"value\":4},{\"text\":\"阻塞\",\"value\":5}],\"provider\":\"jobStatusProvider\"}")
    Integer getJobStatus();

    void setJobStatus(Integer jobStatus);

    @Column(name = "`job_data`")
    @FieldDef(label="json", maxLength = 1000)
    @EditMode(editor = FieldEditor.Text, required = false)
    String getJobData();

    void setJobData(String jobData);

    @Column(name = "`cron_expression`")
    @FieldDef(label="cron表达式", maxLength = 40)
    @EditMode(editor = FieldEditor.Text, required = false)
    String getCronExpression();

    void setCronExpression(String cronExpression);

    @Column(name = "`repeat_interval`")
    @FieldDef(label="重复间隔(s)")
    @EditMode(editor = FieldEditor.Number, required = false)
    Integer getRepeatInterval();

    void setRepeatInterval(Integer repeatInterval);

    @Column(name = "`start_delay`")
    @FieldDef(label="启动间隔(s)")
    @EditMode(editor = FieldEditor.Number, required = false)
    Integer getStartDelay();

    void setStartDelay(Integer startDelay);

    @Column(name = "`description`")
    @FieldDef(label="描述", maxLength = 200)
    @EditMode(editor = FieldEditor.Text, required = false)
    String getDescription();

    void setDescription(String description);

    @Column(name = "`bean_class`")
    @FieldDef(label="调用类全名", maxLength = 100)
    @EditMode(editor = FieldEditor.Text, required = false)
    String getBeanClass();

    void setBeanClass(String beanClass);

    @Column(name = "`spring_id`")
    @FieldDef(label="SpringBeanId", maxLength = 40)
    @EditMode(editor = FieldEditor.Text, required = false)
    String getSpringId();

    void setSpringId(String springId);

    @Column(name = "`url`")
    @FieldDef(label="url", maxLength = 100)
    @EditMode(editor = FieldEditor.Text, required = false)
    String getUrl();

    void setUrl(String url);

    @Column(name = "`is_concurrent`")
    @FieldDef(label="同步/异步")
    @EditMode(editor = FieldEditor.Combo, required = false, params="{\"data\":[{\"text\":\"同步\",\"value\":0},{\"text\":\"并发\",\"value\":1}],\"provider\":\"isConcurrentProvider\"}")
    Integer getIsConcurrent();

    void setIsConcurrent(Integer isConcurrent);

    @Column(name = "`method_name`")
    @FieldDef(label="方法名", maxLength = 40)
    @EditMode(editor = FieldEditor.Text, required = false)
    String getMethodName();

    void setMethodName(String methodName);

    @FieldDef(label="重试次数", maxLength = 2)
    @Column(name = "`retry_count`")
    @EditMode(editor = FieldEditor.Number, required = false)
    Integer getRetryCount();
    void setRetryCount(Integer retryCount);

    @Column(name = "`retry_interval`")
    @FieldDef(label="重试间隔/毫秒", maxLength = 2)
    @EditMode(editor = FieldEditor.Number, required = false)
    Long getRetryInterval();
    void setRetryInterval(Long retryInterval);

    @FieldDef(label="兜底回调", maxLength = 40)
    @Column(name = "`recovery_callback`")
    @EditMode(editor = FieldEditor.Text, required = false)
    String getRecoveryCallback();
    void setRecoveryCallback(String recoveryCallback);
}
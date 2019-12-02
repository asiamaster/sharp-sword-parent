package com.dili.ss.activiti.domain;

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
 * This file was generated on 2019-03-21 16:02:46.
 */
@Table(name = "`act_form`")
public interface ActForm extends IBaseDomain, IMybatisForceParams {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`id`")
    @FieldDef(label="id")
    @EditMode(editor = FieldEditor.Number, required = true)
    Long getId();

    void setId(Long id);

    @Column(name = "`form_key`")
    @FieldDef(label="表单key", maxLength = 20)
    @EditMode(editor = FieldEditor.Text, required = true)
    String getFormKey();

    void setFormKey(String formKey);

    @Column(name = "`def_key`")
    @FieldDef(label="任务定义key", maxLength = 50)
    @EditMode(editor = FieldEditor.Text, required = true)
    String getDefKey();

    void setDefKey(String defKey);

    @Column(name = "`action_url`")
    @FieldDef(label="表单提交URL", maxLength = 120)
    @EditMode(editor = FieldEditor.Text, required = false)
    String getActionUrl();

    void setActionUrl(String actionUrl);

    @Column(name = "`task_url`")
    @FieldDef(label="任务URL", maxLength = 120)
    @EditMode(editor = FieldEditor.Text, required = false)
    String getTaskUrl();

    void setTaskUrl(String taskUrl);

    @Column(name = "`redirect_url`")
    @FieldDef(label="提交后重定向页面URL", maxLength = 120)
    @EditMode(editor = FieldEditor.Text, required = false)
    String getRedirectUrl();

    void setRedirectUrl(String redirectUrl);


    @Column(name = "`created`")
    @FieldDef(label="创建时间")
    @EditMode(editor = FieldEditor.Datetime, required = false)
    Date getCreated();

    void setCreated(Date created);

    @Column(name = "`modified`")
    @FieldDef(label="修改时间")
    @EditMode(editor = FieldEditor.Datetime, required = false)
    Date getModified();

    void setModified(Date modified);
}
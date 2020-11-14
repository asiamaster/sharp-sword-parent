package com.dili.ss.uid.domain;

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
 * This file was generated on 2020-01-21 14:38:55.
 */
@Table(name = "`biz_number_rule`")
public interface BizNumberRuleDomain extends BizNumberRule, IBaseDomain, IMybatisForceParams {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`id`")
    @FieldDef(label="id")
    @EditMode(editor = FieldEditor.Number, required = true)
    Long getId();

    void setId(Long id);

    @Column(name = "`name`")
    @FieldDef(label="名称", maxLength = 20)
    @EditMode(editor = FieldEditor.Text, required = true)
    String getName();

    void setName(String name);

    @Column(name = "`type`")
    @FieldDef(label="业务类型", maxLength = 20)
    @EditMode(editor = FieldEditor.Text, required = true)
    String getType();

    void setType(String type);

    @Column(name = "`prefix`")
    @FieldDef(label="前缀", maxLength = 10)
    @EditMode(editor = FieldEditor.Text, required = false)
    String getPrefix();

    void setPrefix(String prefix);

    @Column(name = "`date_format`")
    @FieldDef(label="日期格式", maxLength = 20)
    @EditMode(editor = FieldEditor.Text, required = false)
    String getDateFormat();

    void setDateFormat(String dateFormat);

    @Column(name = "`length`")
    @FieldDef(label="自增位数")
    @EditMode(editor = FieldEditor.Number, required = true)
    Integer getLength();

    void setLength(Integer length);

    @Column(name = "`range`")
    @FieldDef(label="自增步长范围", maxLength = 5)
    @EditMode(editor = FieldEditor.Text, required = false)
    String getRange();

    void setRange(String range);

    @FieldDef(label="步长")
    @Column(name = "`step`")
    Long getStep();
    void setStep(Long step);

    @Column(name = "`create_time`")
    @FieldDef(label="创建时间")
    @EditMode(editor = FieldEditor.Datetime, required = true)
    Date getCreateTime();

    void setCreateTime(Date createTime);

    @Column(name = "`update_time`")
    @FieldDef(label="修改时间")
    @EditMode(editor = FieldEditor.Datetime, required = true)
    Date getUpdateTime();

    void setUpdateTime(Date updateTime);

    @Column(name = "`is_enable`")
    @FieldDef(label="是否可用")
    Boolean getIsEnable();
    void setIsEnable(Boolean isEnable);
}
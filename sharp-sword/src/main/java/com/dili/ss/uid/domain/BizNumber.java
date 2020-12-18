package com.dili.ss.uid.domain;

import com.dili.ss.dto.IBaseDomain;
import com.dili.ss.metadata.FieldEditor;
import com.dili.ss.metadata.annotation.EditMode;
import com.dili.ss.metadata.annotation.FieldDef;
import tk.mybatis.mapper.annotation.Version;

import javax.persistence.*;
import java.util.Date;

/**
 * 由MyBatis Generator工具自动生成
 *
 * This file was generated on 2018-11-01 14:56:53.
 */
@Table(name = "`biz_number`")
public interface BizNumber extends IBaseDomain {
    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`id`")
    @FieldDef(label="id")
    @EditMode(editor = FieldEditor.Number, required = true)
    Long getId();

    @Override
    void setId(Long id);

    @Column(name = "`type`")
    @FieldDef(label="业务类型", maxLength = 50)
    @EditMode(editor = FieldEditor.Text, required = true)
    String getType();

    void setType(String type);

    @Column(name = "`value`")
    @FieldDef(label="编号值")
    @EditMode(editor = FieldEditor.Number, required = false)
    Long getValue();

    void setValue(Long value);

    @Column(name = "`memo`")
    @FieldDef(label="备注", maxLength = 50)
    @EditMode(editor = FieldEditor.Text, required = false)
    String getMemo();

    void setMemo(String memo);

    @Version
    @Column(name = "`version`")
    @FieldDef(label="版本号", maxLength = 20)
    @EditMode(editor = FieldEditor.Text, required = false)
    Long getVersion();

    void setVersion(Long version);

    @Column(name = "`modified`")
    @FieldDef(label="修改时间")
    @EditMode(editor = FieldEditor.Datetime, required = true)
    Date getModified();

    void setModified(Date modified);

    @Column(name = "`created`")
    @FieldDef(label="创建时间")
    @EditMode(editor = FieldEditor.Datetime, required = false)
    Date getCreated();

    void setCreated(Date created);
}
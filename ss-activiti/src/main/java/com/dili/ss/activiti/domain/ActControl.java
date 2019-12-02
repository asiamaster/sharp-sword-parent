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
 * This file was generated on 2019-03-19 17:14:28.
 */
@Table(name = "`act_control`")
public interface ActControl extends IBaseDomain, IMybatisForceParams {
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

    @Column(name = "`control_id`")
    @FieldDef(label="控件id", maxLength = 20)
    @EditMode(editor = FieldEditor.Text, required = true)
    String getControlId();

    void setControlId(String controlId);

    @Column(name = "`name`")
    @FieldDef(label="控件name", maxLength = 20)
    @EditMode(editor = FieldEditor.Text, required = false)
    String getName();

    void setName(String name);

    @Column(name = "`label`")
    @FieldDef(label="标签", maxLength = 20)
    @EditMode(editor = FieldEditor.Text, required = true)
    String getLabel();

    void setLabel(String label);

    @Column(name = "`min_length`")
    @FieldDef(label="最小长度")
    @EditMode(editor = FieldEditor.Number, required = false)
    Integer getMinLength();

    void setMinLength(Integer minLength);

    @Column(name = "`max_length`")
    @FieldDef(label="最大长度")
    @EditMode(editor = FieldEditor.Number, required = false)
    Integer getMaxLength();

    void setMaxLength(Integer maxLength);

    @Column(name = "`type`")
    @FieldDef(label="控件类型", maxLength = 10)
    @EditMode(editor = FieldEditor.Text, required = false)
    String getType();

    void setType(String type);

    @Column(name = "`required`")
    @FieldDef(label="是否必填")
    @EditMode(editor = FieldEditor.Text, required = false)
    Boolean getRequired();

    void setRequired(Boolean required);

    @Column(name = "`writable`")
    @FieldDef(label="是否可写")
    @EditMode(editor = FieldEditor.Text, required = false)
    Boolean getWritable();

    void setWritable(Boolean writable);

    @Column(name = "`readable`")
    @FieldDef(label="是否可读")
    @EditMode(editor = FieldEditor.Text, required = false)
    Boolean getReadable();

    void setReadable(Boolean readable);

    @Column(name = "`value`")
    @FieldDef(label="值或默认值", maxLength = 40)
    @EditMode(editor = FieldEditor.Text, required = false)
    String getValue();

    void setValue(String value);

    @Column(name = "`order_number`")
    @OrderBy("asc")
    @FieldDef(label="排序号", maxLength = 40)
    @EditMode(editor = FieldEditor.Text, required = false)
    Integer getOrderNumber();

    void setOrderNumber(Integer orderNumber);

    @Column(name = "`style`")
    @FieldDef(label="行内样式", maxLength = 80)
    @EditMode(editor = FieldEditor.Text, required = false)
    String getStyle();

    void setStyle(String style);

    @Column(name = "`meta`")
    @FieldDef(label="元数据", maxLength = 200)
    @EditMode(editor = FieldEditor.Text, required = false)
    String getMeta();

    void setMeta(String meta);

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
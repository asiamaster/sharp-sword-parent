package com.dili.ss.uid.domain;

import com.dili.ss.dto.IDTO;
import com.dili.ss.metadata.annotation.FieldDef;

/**
 * 业务号规则
 */
public interface BizNumberRule extends IDTO {

    @FieldDef(label="业务类型", maxLength = 50)
    String getType();
    void setType(String type);


    @FieldDef(label="名称")
    String getName();
    void setName(String name);

    @FieldDef(label="前缀")
    String getPrefix();
    void setPrefix(String prefix);

    @FieldDef(label="日期格式")
    String getDateFormat();
    void setDateFormat(String dateFormat);

    @FieldDef(label="自增位数")
    Integer getLength();
    void setLength(Integer length);

    /**
     * 自增步长范围,默认(null时)为1, 示例"5,20"，即5到20位随机步长
     * @return
     */
    @FieldDef(label="自增步长范围")
    String getRange();
    void setRange(String range);

    /**
     * 步长范围
     * @return
     */
    @FieldDef(label="步长")
    Long getStep();
    void setStep(Long step);
}
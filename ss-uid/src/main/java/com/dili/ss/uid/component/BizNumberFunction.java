package com.dili.ss.uid.component;

import com.dili.ss.uid.constants.BizNumberConstant;
import com.dili.ss.uid.domain.BizNumberRule;
import com.dili.ss.uid.service.BizNumberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class BizNumberFunction {
    @Autowired
    private BizNumberService bizNumberService;

    /**
     * 获取枚举获取业务号
     * @param bizNumberType
     * @return
     */
    public String getBizNumberByType(String bizNumberType){
        return bizNumberService.getBizNumberByRule(getBizNumberRule(bizNumberType));
    }

    /**
     * 根据类型获取BizNumberRule
     * @param bizNumberType
     * @return
     */
    private BizNumberRule getBizNumberRule(String bizNumberType){
        return BizNumberConstant.bizNumberCache.get(bizNumberType);
    }

    /**
     * 当前日期格式化(时区为GMT+08:00)
     * @param format
     * @return
     */
    public static String format(String format) {
        return format(LocalDateTime.now(ZoneId.of("GMT+08:00")), format);
    }

    /**
     * 日期格式化
     * @param localDateTime
     * @param format
     * @return
     */
    public static String format(LocalDateTime localDateTime, String format) {
        return DateTimeFormatter.ofPattern(format).format(localDateTime);
    }
}

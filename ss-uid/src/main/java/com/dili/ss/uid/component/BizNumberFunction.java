package com.dili.ss.uid.component;

import com.dili.ss.dto.DTOUtils;
import com.dili.ss.uid.constants.BizNumberConstant;
import com.dili.ss.uid.domain.BizNumber;
import com.dili.ss.uid.domain.BizNumberRule;
import com.dili.ss.uid.service.BizNumberRuleService;
import com.dili.ss.uid.service.BizNumberService;
import com.dili.ss.uid.util.BizNumberUtils;
import com.dili.ss.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
@ConditionalOnExpression("'${uid.enable}'=='true'")
public class BizNumberFunction {
    @Autowired
    private BizNumberService bizNumberService;

    @Autowired
    private BizNumberRuleService bizNumberRuleService;
    /**
     * 获取枚举获取业务号
     * @param bizNumberType
     * @return
     */
    public String getBizNumberByType(String bizNumberType){
        return bizNumberService.getBizNumberByRule(getBizNumberRule(bizNumberType));
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

    /**
     * 根据类型获取BizNumberRule
     * @param bizNumberType
     * @return
     */
    private BizNumberRule getBizNumberRule(String bizNumberType){
        BizNumberRule bizNumberRule = BizNumberConstant.bizNumberCache.get(bizNumberType);
        return bizNumberRule == null ? initBizNumberAndRule(bizNumberType) : bizNumberRule;
    }

    /**
     * 根据业务类型初始化业务规则和业务号
     * @param bizNumberType
     * @return
     */
    private synchronized BizNumberRule initBizNumberAndRule(String bizNumberType){
        //查询数据库，没配置则直接返回null
        BizNumberRule bizNumberRule = bizNumberRuleService.getByType(bizNumberType);
        if(bizNumberRule == null){
            return null;
        }
        BizNumber bizNumberCondition = DTOUtils.newInstance(BizNumber.class);
        bizNumberCondition.setType(bizNumberType);
        BizNumber bizNumber = bizNumberService.selectOne(bizNumberCondition);
        //初始化biz_number表数据，多实例场景下须通过type字段添加唯一索引来保证
        if(bizNumber == null){
            bizNumber = DTOUtils.newInstance(BizNumber.class);
            bizNumber.setType(bizNumberRule.getType());
            String dateStr = bizNumberRule.getDateFormat() == null ? null : DateUtils.format(bizNumberRule.getDateFormat());
            bizNumber.setValue(BizNumberUtils.getInitBizNumber(dateStr, bizNumberRule.getLength()));
            bizNumber.setMemo(bizNumberRule.getName());
            bizNumber.setVersion(1L);
            bizNumberService.insertSelective(bizNumber);
        }
        BizNumberConstant.bizNumberCache.put(bizNumberType, bizNumberRule);
        return bizNumberRule;
    }
}

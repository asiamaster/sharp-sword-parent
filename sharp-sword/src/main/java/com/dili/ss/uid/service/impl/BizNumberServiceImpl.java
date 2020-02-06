package com.dili.ss.uid.service.impl;

import com.dili.ss.uid.domain.BizNumberRule;
import com.dili.ss.uid.handler.BizNumberHandler;
import com.dili.ss.uid.service.BizNumberService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

/**
 * 业务号生成服务
 *
 * @author asiamaster
 */
@Service
@ConditionalOnExpression("'${uid.enable}'=='true'")
public class BizNumberServiceImpl implements BizNumberService {

    @Autowired
    private BizNumberHandler bizNumberHandler;

    @Override
    public String getBizNumberByType(BizNumberRule bizNumberRule) {
        if(bizNumberRule == null){
            return null;
        }
//        String range = bizNumberRule.getRange();
//        //默认自增步长为1
//        if(StringUtils.isBlank(range)){
//            range = "1";
//        }else {
//            //验证自增范围
//            String[] ranges = range.split(",");
//            if (ranges.length > 2) {
//                throw new RuntimeException("非法自增步长范围");
//            }
//        }
        Long bizNumber = bizNumberHandler.getBizNumberByType(bizNumberRule.getType(), bizNumberRule.getDateFormat(), bizNumberRule.getLength(), bizNumberRule.getRange());
        if(StringUtils.isBlank(bizNumberRule.getDateFormat())){
            return bizNumberRule.getPrefix() + String.format("%0" + bizNumberRule.getLength() + "d", bizNumber);
        }else {
            return bizNumberRule.getPrefix() + bizNumber;
        }
    }

}
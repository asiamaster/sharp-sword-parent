package com.dili.ss.uid.service.impl;

import com.dili.ss.base.BaseServiceImpl;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.dto.DTOUtils;
import com.dili.ss.uid.constants.BizNumberConstant;
import com.dili.ss.uid.domain.BizNumber;
import com.dili.ss.uid.domain.BizNumberRule;
import com.dili.ss.uid.mapper.BizNumberRuleMapper;
import com.dili.ss.uid.service.BizNumberRuleService;
import com.dili.ss.uid.service.BizNumberService;
import com.dili.ss.uid.util.BizNumberUtils;
import com.dili.ss.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

/**
 * 由MyBatis Generator工具自动生成
 * This file was generated on 2020-01-21 14:38:55.
 */
@Service
@ConditionalOnExpression("'${uid.enable}'=='true'")
public class BizNumberRuleServiceImpl extends BaseServiceImpl<BizNumberRule, Long> implements BizNumberRuleService {

    @Autowired
    private BizNumberService bizNumberService;

    public BizNumberRuleMapper getActualDao() {
        return (BizNumberRuleMapper)getDao();
    }

    @Override
    public BizNumberRule getByType(String type){
        BizNumberRule bizNumberRuleDomain = DTOUtils.newInstance(BizNumberRule.class);
        bizNumberRuleDomain.setType(type);
        return getActualDao().selectOne(bizNumberRuleDomain);
    }

    @Override
    public int updateSelective(BizNumberRule bizNumberRule) {
        int count = super.updateSelective(bizNumberRule);
        BizNumberConstant.bizNumberCache.put(get(bizNumberRule.getId()).getType(), bizNumberRule);
        return count;
    }

    @Override
    public int updateExactSimple(BizNumberRule bizNumberRule) {
        int count = super.updateExactSimple(bizNumberRule);
        BizNumberConstant.bizNumberCache.put(bizNumberRule.getType(), bizNumberRule);
        return count;
    }

    @Override
    public int insertSelective(BizNumberRule bizNumberRule) {
        int count = super.insertSelective(bizNumberRule);
        BizNumber condition = DTOUtils.newInstance(BizNumber.class);
        condition.setType(bizNumberRule.getType());
        BizNumber bizNumber = bizNumberService.selectOne(condition);
        //初始化biz_number表数据
        if(bizNumber == null){
            bizNumber = DTOUtils.newInstance(BizNumber.class);
            bizNumber.setType(bizNumberRule.getType());
            String dateStr = bizNumberRule.getDateFormat() == null ? null : DateUtils.format(bizNumberRule.getDateFormat());
            bizNumber.setValue(BizNumberUtils.getInitBizNumber(dateStr, bizNumberRule.getLength()));
            bizNumber.setMemo(bizNumberRule.getName());
            bizNumber.setVersion(1L);
            bizNumberService.insertSelective(bizNumber);
        }
        //当前实例插入缓存，其它实例在调用时自动初始化
        BizNumberConstant.bizNumberCache.put(bizNumberRule.getType(), bizNumberRule);
        return count;
    }

    @Override
    public int delete(Long key) {
        String type = get(key).getType();
        int count = super.delete(key);
        BizNumberConstant.bizNumberCache.remove(type);
        bizNumberService.clear(type);
        BizNumber bizNumber = DTOUtils.newInstance(BizNumber.class);
        bizNumber.setType(type);
        bizNumberService.deleteByExample(bizNumber);
        return count;
    }

    @Override
    public BaseOutput updateEnable(Long id, Boolean enable) {
        BizNumberRule bizNumberRule = DTOUtils.newInstance(BizNumberRule.class);
        bizNumberRule.setId(id);
        bizNumberRule.setIsEnable(enable);
        getActualDao().updateByPrimaryKeySelective(bizNumberRule);
        return BaseOutput.success();
    }
}
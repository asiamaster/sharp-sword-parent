package com.dili.ss.uid.service.impl;

import com.dili.ss.base.BaseServiceImpl;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.dto.DTOUtils;
import com.dili.ss.uid.domain.BizNumber;
import com.dili.ss.uid.domain.BizNumberRuleDomain;
import com.dili.ss.uid.mapper.BizNumberRuleMapper;
import com.dili.ss.uid.service.BizNumberRuleService;
import com.dili.ss.uid.service.BizNumberService;
import com.dili.ss.uid.util.BizNumberUtils;
import com.dili.ss.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 由MyBatis Generator工具自动生成
 * This file was generated on 2020-01-21 14:38:55.
 */
@Service
public class BizNumberRuleServiceImpl extends BaseServiceImpl<BizNumberRuleDomain, Long> implements BizNumberRuleService {

    @Autowired
    private BizNumberService bizNumberService;

    public BizNumberRuleMapper getActualDao() {
        return (BizNumberRuleMapper)getDao();
    }

    @Override
    public BizNumberRuleDomain getByType(String type){
        BizNumberRuleDomain bizNumberRuleDomain = DTOUtils.newInstance(BizNumberRuleDomain.class);
        bizNumberRuleDomain.setType(type);
        return getActualDao().selectOne(bizNumberRuleDomain);
    }

    @Override
    public int updateSelective(BizNumberRuleDomain bizNumberRuleDomain) {
        int count = super.updateSelective(bizNumberRuleDomain);
//        BizNumberConstant.bizNumberCache.put(get(bizNumberRuleDomain.getId()).getType(), bizNumberRuleDomain);
        return count;
    }

    @Override
    public int updateExactSimple(BizNumberRuleDomain bizNumberRuleDomain) {
        int count = super.updateExactSimple(bizNumberRuleDomain);
//        BizNumberConstant.bizNumberCache.put(bizNumberRuleDomain.getType(), bizNumberRuleDomain);
        return count;
    }

    @Override
    public int insertSelective(BizNumberRuleDomain bizNumberRuleDomain) {
        int count = super.insertSelective(bizNumberRuleDomain);
        BizNumber condition = DTOUtils.newInstance(BizNumber.class);
        condition.setType(bizNumberRuleDomain.getType());
        BizNumber bizNumber = bizNumberService.selectOne(condition);
        //初始化biz_number表数据
        if(bizNumber == null){
            bizNumber = DTOUtils.newInstance(BizNumber.class);
            bizNumber.setType(bizNumberRuleDomain.getType());
            String dateStr = bizNumberRuleDomain.getDateFormat() == null ? null : DateUtils.format(bizNumberRuleDomain.getDateFormat());
            bizNumber.setValue(BizNumberUtils.getInitBizNumber(dateStr, bizNumberRuleDomain.getLength()));
            bizNumber.setMemo(bizNumberRuleDomain.getName());
            bizNumber.setVersion(1L);
            bizNumberService.insertSelective(bizNumber);
        }
//        BizNumberConstant.bizNumberCache.put(bizNumberRuleDomain.getType(), bizNumberRuleDomain);
        return count;
    }

    @Override
    public int delete(Long key) {
        String type = get(key).getType();
        int count = super.delete(key);
//        BizNumberConstant.bizNumberCache.remove(type);
        bizNumberService.clear(type);
        BizNumber bizNumber = DTOUtils.newInstance(BizNumber.class);
        bizNumber.setType(type);
        bizNumberService.deleteByExample(bizNumber);
        return count;
    }

    @Override
    public BaseOutput updateEnable(Long id, Boolean enable) {
        BizNumberRuleDomain bizNumberRuleDomain = DTOUtils.newInstance(BizNumberRuleDomain.class);
        bizNumberRuleDomain.setId(id);
        bizNumberRuleDomain.setIsEnable(enable);
        getActualDao().updateByPrimaryKeySelective(bizNumberRuleDomain);
//        BizNumberRuleDomain domain = get(id);
//        if(enable){
//            BizNumberConstant.bizNumberCache.put(domain.getType(), domain);
//        }else{
//            BizNumberConstant.bizNumberCache.remove(domain.getType());
//        }
        return BaseOutput.success();
    }
}
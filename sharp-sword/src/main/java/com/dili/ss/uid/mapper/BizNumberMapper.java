package com.dili.ss.uid.mapper;

import com.dili.ss.base.MyMapper;
import com.dili.ss.uid.domain.BizNumber;
import com.dili.ss.uid.domain.BizNumberAndRule;

public interface BizNumberMapper extends MyMapper<BizNumber> {

    BizNumberAndRule getBizNumberAndRule(String type);
}
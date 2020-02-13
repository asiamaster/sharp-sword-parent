package com.dili.ss.uid.service;

import com.dili.ss.uid.domain.BizNumberRule;

/**
 * 业务号服务
 */
public interface BizNumberService {

	/**
	 * 根据业务类型规则获取编号
	 * @param bizNumberRule 业务类型规则
	 * @return
	 */
	String getBizNumberByType(BizNumberRule bizNumberRule);

}
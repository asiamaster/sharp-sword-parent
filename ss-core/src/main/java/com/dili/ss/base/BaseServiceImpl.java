package com.dili.ss.base;


import com.dili.ss.dto.IBaseDomain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;


/**
 *	服务基类
 *
 * @author asiamastor
 * @date 2016/12/28
 */
@Service
public abstract class BaseServiceImpl<T extends IBaseDomain, KEY extends Serializable> extends BaseServiceAdaptor<T, KEY> {
	protected static final Logger LOGGER = LoggerFactory.getLogger(BaseServiceImpl.class);

	@Autowired
	private MyMapper<T> mapper;

	/**
	 * 如果不使用通用mapper，可以自行在子类覆盖getDao方法
	 */
	@Override
	public MyMapper<T> getDao(){
		return this.mapper;
	}

}
package com.dili.ss.base;


import com.dili.ss.domain.EasyuiPageOutput;
import com.dili.ss.dto.IBaseDomain;
import com.dili.ss.metadata.ValueProviderUtils;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;


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

	/**
	 * 用于支持like, order by 的easyui分页查询
	 * @param domain
	 * @return
	 */
	@Override
	public EasyuiPageOutput listEasyuiPageByExample(T domain, boolean useProvider) throws Exception {
		List<T> list = listByExample(domain);
		long total = list instanceof Page ? ( (Page) list).getTotal() : list.size();
		List results = useProvider ? ValueProviderUtils.buildDataByProvider(domain, list) : list;
		return new EasyuiPageOutput(total, results);
	}

	/**
	 * 根据实体查询easyui分页结果
	 * @param domain
	 * @return
	 */
	@Override
	public EasyuiPageOutput listEasyuiPage(T domain, boolean useProvider) throws Exception {
		if(domain.getRows() != null && domain.getRows() >= 1) {
			//为了线程安全,请勿改动下面两行代码的顺序
			PageHelper.startPage(domain.getPage(), domain.getRows());
		}
		List<T> list = getDao().select(domain);
		long total = list instanceof Page ? ( (Page) list).getTotal() : list.size();
		List results = useProvider ? ValueProviderUtils.buildDataByProvider(domain, list) : list;
		return new EasyuiPageOutput(total, results);
	}
}
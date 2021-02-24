package com.dili.ss.base;

import com.dili.ss.domain.BasePage;
import com.dili.ss.domain.EasyuiPageOutput;

import java.io.Serializable;
import java.util.List;


/**
 * baseService
 * @param <T>
 * @param <KEY>
 */
public interface BaseService<T,KEY extends Serializable> {

	/**
	 * 保存一个实体，null的属性也会保存，不会使用数据库默认值
	 *
	 * @param t
	 * @return
	 */
	int insert(T t);

	/**
	 * 保存一个实体，null的属性不会保存，会使用数据库默认值
	 *
	 * @param t
	 * @return
	 */
	int insertSelective(T t);
	
	/**
	 * 批量插入
	 * @param list
	 * @return
	 */
	int batchInsert(List<T> list);
	
	/**
	 * 删除对象,主键
	 * @param key 主键
	 * @return 影响条数
	 */
	int delete(KEY key);

	/**
	 * 根据条件删除对象
	 * @param t 条件对象
	 * @return 影响条数
	 */
	int deleteByExample(T t);

	/**
	 * 批量删除对象,主键集合
	 * @param keys 主键数组
	 * @return 影响条数
	 */
	int delete(List<KEY> keys);
	
	/**
	 * 更新对象,条件主键Id
	 * @param condtion 更新对象
	 * @return 影响条数
	 */
	int update(T condtion);

	/**
	 * 更新对象,条件主键Id，忽略空字段
	 * @param condtion 更新对象
	 * @return 影响条数
	 */
	int updateSelective(T condtion);

	/**
	 * 用于支持@Like, @Operator和空值的条件更新
	 * @param domain 更新的字段
	 * @param condition  更新的条件
	 * @return
	 */
	int updateByExample(T domain, T condition);

	/**
	 * 批量更新，忽略空字段
	 * @param list
	 * @return
	 */
	int batchUpdateSelective(List<T> list);

	/**
	 * 用于支持@Like, @Operator和空值的条件更新
	 * @param domain 更新的字段
	 * @param condition  更新的条件
	 * @return
	 */
	int updateSelectiveByExample(T domain, T condition);

	/**
	 * 用于支持@Like, @Operator和空值的精确条件更新，基于Selective设计
	 * 必须实现IMybatisForceParams接口，并且设置setForceParams参数
	 * 比如要将某个字段改为null，可以这样:
	 * <br/>
	 * Map params = new HashMap();<br/>
	 * params.put("field_name", null);<br/>
	 * domain.setSetForceParams(params);
	 * @param domain 更新的字段
	 * @param condition  更新的条件
	 * @return
	 */
	int updateExactByExample(T domain, T condition);

	/**
	 * 简单精确更新，参见updateExactByExample方法
	 * domain不需要再实现IMybatisForceParams接口，
	 * domain中有key都会更新，不论是否为null，没有key则不更新
	 * @param domain
	 * @param condition
	 * @return
	 */
	int updateExactByExampleSimple(T domain, T condition);

	/**
	 * 根据主键精确更新，必须实现IMybatisForceParams接口，并且设置setForceParams参数
	 * 比如要将某个字段改为null，可以这样:
	 * <br/>
	 * Map params = new HashMap();<br/>
	 * params.put("field_name", null);<br/>
	 * domain.setSetForceParams(params);
	 * @param record
	 * @return
	 */
	int updateExact(T record);

	/**
	 * 根据主键精确更新，必须实现IMybatisForceParams接口，会根据字段为null或不存在自动设置setForceParams参数
	 * @param record
	 * @return
	 */
	int updateExactSimple(T record);

	/**
	 * 批量更新
	 * @param list
	 * @return
	 */
	int batchUpdate(List<T> list);
	
	/**
	 * 保存或更新对象(条件主键Id)
	 * @param t 需更新的对象
	 * @return 影响条数
	 */
	int saveOrUpdate(T t);

	/**
	 * 保存或更新对象(条件主键Id)，忽略空字段
	 * @param t 需更新的对象
	 * @return 影响条数
	 */
	int saveOrUpdateSelective(T t);

	/**
	 * 查询对象,条件主键
	 * @param key
	 * @return 实体对象
	 */
	T get(KEY key);

	/**
	 * 查询对象,只要不为NULL与空则为条件
	 * @param condtion 查询条件
	 * @return 对象列表
	 */
	List<T> list(T condtion);
	
	/**
	 * 分页查询
	 * @param t 分页查询对象
	 * @return 分页对象
	 */
	BasePage<T> listPage(T t);

	/**
	 * 根据条件查询
	 * @param example
	 * @return
	 */
	List<T> selectByExample(Object example);

	/**
	 * 根据主键查询对象
	 * @param key
	 * @return
	 */
	T selectByPrimaryKey(KEY key);

	/**
	 * 用于支持@Like, @Operator，@OrderBy和空值的查询
	 * @param domain
	 * @return
	 */
	List<T> listByExample(T domain);

	/**
	 * 用于支持@Like, @Operator和@OrderBy 的分页查询
	 * @param domain
	 * @return
	 */
	BasePage<T> listPageByExample(T domain);

	/**
	 * 根据实体查询easyui分页结果， 支持用metadata信息中字段对应的provider构建数据
	 * @param domain
	 * @return
	 */
	EasyuiPageOutput listEasyuiPage(T domain, boolean useProvider) throws Exception;

	/**
	 * 用于支持@Like, @Operator和@OrderBy 的easyui分页查询, 支持用metadata信息中字段对应的provider构建数据
	 * @param domain
	 * @return
	 */
	EasyuiPageOutput listEasyuiPageByExample(T domain, boolean useProvider) throws Exception;

	/**
	 * 根据主键判断是否存在
	 * @param key
	 * @return
	 */
	boolean existsWithPrimaryKey(KEY key);

	/**
	 * 保存一个实体，默认功能同insertSelective
	 * 必须实现IMybatisForceParams接口，并且设置insertForceParams参数
	 * 比如要将某个字段改为null，可以这样:
	 * <br/>
	 * Map params = new HashMap();<br/>
	 * params.put("field", null);<br/>
	 * domain.setSetForceParams(params);
	 *
	 * @param t
	 * @return
	 */
	int insertExact(T t);

	/**
	 * 保存一个实体，null的属性会保存，没有set过的属性则会使用数据库默认值
	 *
	 * @param t
	 * @return
	 */
	int insertExactSimple(T t);
}

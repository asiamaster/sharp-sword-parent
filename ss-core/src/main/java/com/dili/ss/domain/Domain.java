package com.dili.ss.domain;

import com.dili.ss.dto.IDomain;

import javax.persistence.Transient;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 基础实体类
 */
public class Domain<KEY extends Serializable> implements IDomain<KEY> {

	/** id */
//	@Id
//	@Column(name = "id")
//	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Transient
	protected KEY id;
	@Transient
	private Integer page;	//页码，从1开始
	@Transient
	private Integer rows; //每页行数
	//sort和order作为easyui的远程排序的关键字，同时也不建议数据库字段使用这两个词
	@Transient
	private String sort;    //排序字段，以逗号分隔
	@Transient
	private String order;   //排序类型: asc,desc
	@Transient
	private Map metadata;

	@Override
	public KEY getId() {
		return id;
	}
	@Override
	public void setId(KEY id) {
		this.id = id;
	}

	@Override
	public Integer getPage() {
		return page;
	}

	@Override
	public void setPage(Integer page) {
		this.page = page;
	}

	@Override
	public Integer getRows() {
		return rows;
	}

	@Override
	public void setRows(Integer rows) {
		this.rows = rows;
	}

	@Override
	public String getSort() {
		return sort;
	}

	@Override
	public void setSort(String sort) {
		this.sort = sort;
	}

	@Override
	public String getOrder() {
		return order;
	}

	@Override
	public void setOrder(String order) {
		this.order = order;
	}

	@Override
	public void setMetadata(String key, Object value){
		if(metadata == null){
			metadata = new HashMap();
		}
		metadata.put(key, value);
	}

	@Override
	public Object getMetadata(String key){
		return metadata == null ? null : metadata.get(key);
	}

	@Override
	public Map getMetadata() {
		return metadata;
	}

	@Override
	public void setMetadata(Map metadata) {
		this.metadata = metadata;
	}

	/**
	 * 附加属性中是否存在
	 * @param key
	 * @return
	 */
	@Override
	public Boolean containsMetadata(String key) {
		return metadata == null?false:metadata.containsKey(key);
	}
}

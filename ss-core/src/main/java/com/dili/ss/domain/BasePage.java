package com.dili.ss.domain;

import java.util.Collections;
import java.util.List;

/**
 * 基本分页结果对象
 * 建议使用com.github.pagehelper.Page
 * @param <T>
 */
//@ApiModel(value = "basePage",description = "分页查询对象")
@Deprecated
public class BasePage<T> extends BaseQuery{

	private static final long serialVersionUID = 11234564786156318L;
	//参照easyui的分页属性
	public static final String PAGE_SIZE_KEY="rows";

	public static final String PAGE_INDEX_KEY="page";

	public static final Integer DEFAULT_PAGE_SIZE = 20;

//	@ApiModelProperty(value = "页码，默认1")
	private Integer page = 1;	//页码

//	@ApiModelProperty(value = "每页显示条数，默认20")
	private Integer rows = DEFAULT_PAGE_SIZE;	//每页显示行数

//	@ApiModelProperty(hidden = true,value = "查询扩展,无需传入")
	private List<T> datas = Collections.EMPTY_LIST;	//返回数据

//	@ApiParam
	private Integer totalPage = 0; //总页数

	private transient Long startIndex = 1L;// 开始索引

	/**
	 * total count
	 */
	private Long totalItem = 0L;	//总记录数


	public BasePage() {
		// 默认构造器
	}


//	@JSONField(serialize=false)
	public Long getStartIndex() {
		return startIndex;
	}
	public void setStartIndex(Long startIndex) {
		this.startIndex = startIndex;
	}

	/**
	 * 获取开始索引
	 * @return
	 */
	public Integer startIndex() {
		return (getPage() - 1) * this.rows;
	}

	/**
	 * 获取结束索引
	 * @return
	 */
	public Integer endIndex() {
		return getPage() * this.rows;
	}

	/**
	 * 是否第一页
	 * @return
	 */
	public boolean firstPage() {
		return getPage() <= 1;
	}

	/**
	 * 是否末页
	 * @return
	 */
	public boolean lastPage() {
		return getPage() >= pageCount();
	}

	/**
	 * 获取下一页页码
	 * @return
	 */
	public Integer nextPage() {
		if (lastPage()) {
			return getPage();
		} 
		return getPage() + 1;
	}

	/**
	 * 获取上一页页码
	 * @return
	 */
	public Integer previousPage() {
		if (firstPage()) {
			return 1;
		}
		return getPage() - 1;
	}

	/**
	 * 获取当前页页码
	 * @return
	 */
	public Integer getPage() {
		if (page == 0) {
			page = 1;
		}
		return page;
	}

	/**
	 * 取得总页数
	 * @return
	 */
	public Long pageCount() {
		if (totalItem % rows == 0) {
			return totalItem / rows;
		} else {
			return totalItem / rows + 1;
		}
	}

	/**
	 * 取总记录数.
	 * @return
	 */
	public Long getTotalItem() {
		return this.totalItem;
	}

	/**
	 * 设置当前页
	 * @param
	 */
	public void setPage(Integer page) {
		this.page = page;
	}

	public Integer getTotalPage() {
		return totalPage;
	}

	public void setTotalPage(Integer totalPage) {
		this.totalPage = totalPage;
	}

	/**
	 * 获取每页数据容量.
	 * @return
	 */
	public Integer getRows() {
		return rows;
	}
	
	public void setRows(Integer rows) {
		this.rows = rows;
	}
	/**
	 * 该页是否有下一页.
	 * @return
	 */
	public boolean hasNextPage() {
		return getPage() < pageCount();
	}

	/**
	 * 该页是否有上一页.
	 * @return
	 */
	public boolean hasPreviousPage() {
		return getPage() > 1;
	}

	/**
	 * 获取数据集
	 * @return
	 */
//	@ApiModelProperty(hidden=true)
	public List<T> getDatas() {
		return datas;
	}

	/**
	 * 设置数据集
	 * @param data
	 */
	public void setDatas(List<T> data) {
		this.datas = data;
	}

	/**
	 * 设置总记录条数
	 */
	public void setTotalItem(Long totalItem) {
		this.totalItem = totalItem;
//		repaginate();
	}

	public boolean isNextPageAvailable() {
		return this.page >this.totalPage;
	}
	public boolean isPreviousPageAvailable() {
		return this.page <this.totalPage&&this.page >1;
	}

	public void repaginate() {
		if (totalItem > 0) {
			setTotalPage((int) (totalItem / rows + (totalItem % rows > 0 ? 1 : 0)));//计算出最大页数
			if(page > totalPage) {//当前页数大于最大页，设置为最大页
				setPage(totalPage); //最大页
			}
			this.setTotalPage(totalPage);
			setStartIndex((long) ((page - 1)* rows)); //计算出页开始行数
			if(startIndex<0){
				setStartIndex(0L);
			}
			if(startIndex>totalItem){
				setStartIndex(totalItem);
			}
		}
	}

}

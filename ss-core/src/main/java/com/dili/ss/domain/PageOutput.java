package com.dili.ss.domain;


import com.dili.ss.constant.ResultCode;

/**
 * 分页输出对象
 * @author wangmi
 * @since 2016-11-25
 */
public class PageOutput<T> extends BaseOutput<T> {

    /**
     * 页码，从1开始
     */
    private Integer pageNum;
    /**
     * 页大小，每页记录数
     */
    private Integer pageSize;
    /**
     * 总记录数
     */
    private Integer total;
    /**
     * 起始行
     */
    private int startRow;
    /**
     * 末行
     */
    private int endRow;


    public PageOutput() {
    }

    public PageOutput(String code, String result) {
        super(code, result);
    }

    @Override
    public T getData() {
        return super.getData();
    }

    @Override
    public PageOutput setData(T data) {
        super.setData(data);
        return this;
    }

    /**
     * 页码，获取第page页数据
     */
    public Integer getPageNum() {
        return pageNum;
    }

    public PageOutput setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
        return this;
    }

    /**
     * 页大小，每页记录数
     */
    public Integer getPageSize() {
        return pageSize;
    }

    public PageOutput setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    /**
     * 总记录数
     */
    public Integer getTotal() {
        return total;
    }

    public PageOutput setTotal(Integer total) {
        this.total = total;
        return this;
    }

    public int getStartRow() {
        return startRow;
    }

    public void setStartRow(int startRow) {
        this.startRow = startRow;
    }

    public int getEndRow() {
        return endRow;
    }

    public void setEndRow(int endRow) {
        this.endRow = endRow;
    }

    public static <T> PageOutput<T> create(String code, String result) {
        return new PageOutput<T>(code, result);
    }

    public static <T> PageOutput<T> success() {
        return success("OK");
    }

    public static <T> PageOutput<T> success(String msg) {
        return create(ResultCode.OK, msg);
    }

    public static <T> PageOutput<T> failure() {
        return failure("操作失败!");
    }

    public static <T> PageOutput<T> failure(String msg) {
        return create(ResultCode.APP_ERROR, msg);
    }

    @Override
    public String getCode() {
        return super.getCode();
    }

    @Override
    public PageOutput setCode(String code) {
        super.setCode(code);
        return this;
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }

    @Override
    public PageOutput setMessage(String message) {
        super.setMessage(message);
        return this;
    }
}

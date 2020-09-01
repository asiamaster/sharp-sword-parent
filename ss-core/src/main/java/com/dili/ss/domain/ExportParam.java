package com.dili.ss.domain;

import java.util.List;
import java.util.Map;

/**
 * 导出参数
 * Created by asiamaster on 2017/5/27 0027.
 */
public class ExportParam {
    /**
     * 导出文件名称
     */
    private String title ="EXPORT";

    /**
     * 表头数据,支持复合表头
     */
    private List<List<Map<String, Object>>> columns;

    /**
     * 请求参数
     */
    private Map<String, String> queryParams;

    /**
     * 请求url
     */
    private String url;

    /**
     * 请求类型，默认为application/x-www-form-urlencoded
     */
    private String contentType;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<List<Map<String, Object>>> getColumns() {
        return columns;
    }

    public void setColumns(List<List<Map<String, Object>>> columns) {
        this.columns = columns;
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(Map<String, String> queryParams) {
        this.queryParams = queryParams;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}

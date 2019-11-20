package com.dili.ss.domain;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.util.List;

/**
 * Created by asiamaster on 2017/5/26 0026.
 */
public class EasyuiPageOutput {
    private Integer total;
    private List rows;
    private List footer;

    public EasyuiPageOutput(){}
    public EasyuiPageOutput(Integer total, List rows){
        this.total = total;
        this.rows = rows;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public List getRows() {
        return rows;
    }

    public void setRows(List rows) {
        this.rows = rows;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this, SerializerFeature.IgnoreErrorGetter);
    }

    public List getFooter() {
        return footer;
    }

    public void setFooter(List footer) {
        this.footer = footer;
    }
}
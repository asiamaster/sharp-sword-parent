package com.dili.ss.domain;

/**
 * 表头
 * Created by asiamaster on 2017/6/15 0015.
 */
public class TableHeader {
    //字段名
    private String field;
    //标题
    private String title;
    //强制单元格类型，目前只支持number
    private String type;
    //number类型的格式，参见org.apache.poi.ss.usermodel.BuiltinFormats,默认为0
    private String format;

    public TableHeader(String field, String title) {
        this.field = field;
        this.title = title;
    }

    public TableHeader(String field, String title, String type, String format) {
        this.field = field;
        this.title = title;
        this.type = type;
        this.format = format;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }
}

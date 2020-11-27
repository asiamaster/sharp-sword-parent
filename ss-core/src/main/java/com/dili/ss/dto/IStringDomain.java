package com.dili.ss.dto;

/**
 * 基础实体类
 */
public interface IStringDomain extends IDomain<String> {
    @Override
    String getId();
    @Override
    void setId(String id);
}

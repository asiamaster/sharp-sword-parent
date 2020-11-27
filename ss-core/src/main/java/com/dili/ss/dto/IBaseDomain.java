package com.dili.ss.dto;

/**
 * 基础实体类
 */
public interface IBaseDomain extends IDomain<Long> {

    @Override
    Long getId();
    @Override
    void setId(Long id);
}

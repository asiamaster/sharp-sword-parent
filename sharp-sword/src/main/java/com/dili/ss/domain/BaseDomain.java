package com.dili.ss.domain;

import com.dili.ss.dto.IBaseDomain;

/**
 * 基础实体类
 */
public class BaseDomain extends Domain<Long> implements IBaseDomain {

    @Override
    public Long getId() {
        return id;
    }
    @Override
    public void setId(Long id) {
        this.id = id;
    }
}

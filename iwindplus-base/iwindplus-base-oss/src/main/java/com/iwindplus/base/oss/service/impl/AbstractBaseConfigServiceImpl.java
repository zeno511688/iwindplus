/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.oss.service.impl;

import com.iwindplus.base.oss.service.BaseConfigService;
import lombok.extern.slf4j.Slf4j;

/**
 * 通用配置抽象类.
 *
 * @param <T> 配置实体类型
 * @author zengdegui
 * @since 2020/3/13
 */
@Slf4j
public abstract class AbstractBaseConfigServiceImpl<T> extends AbstractBaseServiceImpl implements BaseConfigService<T> {

    /**
     * 配置.
     */
    protected T config;

    @Override
    public T getConfig() {
        return config;
    }

    @Override
    public void setConfig(T config) {
        this.config = config;
    }
}

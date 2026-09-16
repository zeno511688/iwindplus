/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.sms.service.impl;

import com.iwindplus.base.sms.service.BaseConfigService;
import lombok.extern.slf4j.Slf4j;

/**
 * 短信业务基础配置抽象类.
 *
 * @param <T> 配置类型
 * @author zengdegui
 * @since 2020/3/13
 */
@Slf4j
public abstract class AbstractBaseConfigServiceImpl<T> implements BaseConfigService<T> {

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

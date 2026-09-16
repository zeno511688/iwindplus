/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.ocr.service;

/**
 * ocr业务层基础配置接口类.
 *
 * @author zengdegui
 * @since 2020/4/28
 */
public interface BaseConfigService<T> {
    /**
     * 获取配置.
     *
     * @return T
     */
    T getConfig();

    /**
     * 设置配置.
     *
     * @param config 对象
     */
    void setConfig(T config);
}

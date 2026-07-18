/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.oss.service;

/**
 * 通用配置业务层接口.
 *
 * @param <T> 配置实体类型
 * @author zengdegui
 * @since 2025/09/17 22:33
 */
public interface BaseConfigService<T> {

    /**
     * 获取配置
     *
     * @return 配置对象
     */
    T getConfig();

    /**
     * 设置配置
     *
     * @param config 配置对象
     */
    void setConfig(T config);
}

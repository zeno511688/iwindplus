/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.sumsub.service;

/**
 * 通用业务层接口.
 *
 * @author zengdegui
 * @since 2026/9/7
 */
public interface BaseService {

    /**
     * 健康检查.
     *
     * @return 是否健康
     */
    default boolean isHealthy() {
        return true;
    }

    /**
     * 获取优先级.
     *
     * @return 优先级（数字越小优先级越高）
     */
    default int getPriority() {
        return Integer.MAX_VALUE;
    }

    /**
     * 获取配置编码.
     *
     * @return 配置编码
     */
    String getCode();
}

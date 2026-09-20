/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.gateway.infrastructure.configuration.constant;

import com.iwindplus.base.domain.constant.CommonConstant;

/**
 * 网关缓存常数.
 *
 * @author zengdegui
 * @since 2026/09/15 00:00
 */
public class GatewayCacheConstant {

    private GatewayCacheConstant() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 限流缓存key.
     */
    public static final String RATE_LIMITER_KEY = "gateway_rate_limiter:";

    /**
     * 缓存key:all.
     */
    public static final String CACHE_KEY_ALL = "ALL";
}

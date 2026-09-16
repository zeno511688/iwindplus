/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.gateway.infrastructure.constant;

import com.iwindplus.base.domain.constant.CommonConstant;
import org.springframework.core.Ordered;

/**
 * 网关过滤器常数.
 *
 * @author zengdegui
 * @since 2026/09/14 23:57
 */
public class GatewayFilterConstant {

    private GatewayFilterConstant() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 全局耗时统计顺序.
     */
    public static final Integer FILTER_TIMING_GATEWAY_ORDER = Ordered.HIGHEST_PRECEDENCE;

    /**
     * 请求体过滤器顺序.
     */
    public static final Integer FILTER_REQUEST_BODY_ORDER = Ordered.HIGHEST_PRECEDENCE + 5;

    /**
     * 限流过滤器顺序.
     */
    public static final Integer RATE_LIMITER_ORDER = Ordered.HIGHEST_PRECEDENCE + 10;

    /**
     * 基础过滤器顺序.
     */
    public static final Integer FILTER_BASE_ORDER = Ordered.HIGHEST_PRECEDENCE + 15;

    /**
     * API白名单过滤器顺序.
     */
    public static final Integer FILTER_API_WHITE_LIST_ORDER = Ordered.HIGHEST_PRECEDENCE + 20;

    /**
     * IP黑名单过滤器顺序.
     */
    public static final Integer FILTER_IP_BLACK_LIST_ORDER = Ordered.HIGHEST_PRECEDENCE + 25;

    /**
     * API签名过滤器顺序.
     */
    public static final Integer FILTER_API_SIGN_ORDER = Ordered.HIGHEST_PRECEDENCE + 30;

    /**
     * 认证过滤器顺序.
     */
    public static final Integer FILTER_AUTH_ORDER = Ordered.HIGHEST_PRECEDENCE + 35;

    /**
     * 操作扩展过滤器顺序.
     */
    public static final Integer FILTER_OPERATE_EXTEND_ORDER = Ordered.HIGHEST_PRECEDENCE + 40;

    /**
     * 日志过滤器顺序.
     */
    public static final Integer FILTER_LOG_ORDER = Ordered.HIGHEST_PRECEDENCE + 45;
}

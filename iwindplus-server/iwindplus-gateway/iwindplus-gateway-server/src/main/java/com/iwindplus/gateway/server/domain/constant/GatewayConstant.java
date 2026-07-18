/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.gateway.server.domain.constant;

import com.iwindplus.base.domain.constant.CommonConstant;
import org.springframework.core.Ordered;

/**
 * 常数.
 *
 * @author zengdegui
 * @since 2020/11/8
 */
public class GatewayConstant {

    private GatewayConstant() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 限流缓存key.
     */
    public static final String RATE_LIMITER_KEY = "gateway_rate_limiter:";

    /**
     * filter顺序相关常数（注意：顺序有讲究，可能存在依赖关系） .
     */
    public static class FilterConstant {

        private FilterConstant() {
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

    /**
     * 上下文相关常数 .
     */
    public static class ServerWebExchangeContextConstant {

        private ServerWebExchangeContextConstant() {
            throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
        }

        /**
         * 请求开始时间.
         */
        public static final String REQUEST_TIME = "requestTime";

        /**
         * 白名单标记.
         */
        public static final String WHITED_FLAG = "whitedFlag";

        /**
         * 用户信息.
         */
        public static final String USER_INFO = "userInfo";
    }

    /**
     * 缓存相关常数 .
     */
    public static class CacheContextConstant {

        private CacheContextConstant() {
            throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
        }

        /**
         * 缓存key:all.
         */
        public static final String CACHE_KEY_ALL = "ALL";
    }

}

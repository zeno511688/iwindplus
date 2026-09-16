/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.log.common.constant;

import com.iwindplus.base.domain.constant.CommonConstant;

/**
 * 常数.
 *
 * @author zengdegui
 * @since 2020/11/8
 */
public class LogConstant {
    private LogConstant() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 日志服务名.
     */
    public static final String LOG_SERVER_NAME = "iwindplus-log";

    /**
     * 日志服务客户端扫描包名.
     */
    public static final String LOG_CLIENT_SCAN_BASE_PACKAGE = "com.iwindplus.log.client";

    /**
     * redis 缓存相关常数.
     */
    public static class RedisCacheConstant {

        private RedisCacheConstant() {
            throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
        }

        /**
         * 登陆日志缓存名称.
         */
        public static final String CACHE_LOGIN_LOG = "loginLog";

        /**
         * binlog日志缓存名称.
         */
        public static final String CACHE_BINLOG_ALERT = "binlogAlert";

        /**
         * 网关日志缓存名称.
         */
        public static final String CACHE_GATEWAY_LOG = "gatewayLog";

        /**
         * 邮箱验证码日志缓存名称.
         */
        public static final String CACHE_MAIL_CAPTCHA_LOG = "mailCaptchaLog";

        /**
         * 邮箱日志缓存名称.
         */
        public static final String CACHE_MAIL_LOG = "mailLog";

        /**
         * 操作日志缓存名称.
         */
        public static final String CACHE_OPERATION_LOG = "operationLog";

        /**
         * 短信验证码日志缓存名称.
         */
        public static final String CACHE_SMS_CAPTCHA_LOG = "smsCaptchaLog";
    }
}

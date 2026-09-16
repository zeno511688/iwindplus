/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.integr.common.constant;

import com.iwindplus.base.domain.constant.CommonConstant;

/**
 * 常数.
 *
 * @author zengdegui
 * @since 2020/11/8
 */
public class IntegrConstant {

    private IntegrConstant() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 集成服务名.
     */
    public static final String INTEGR_SERVER_NAME = "iwindplus-integr";

    /**
     * 集成服务客户端扫描包名.
     */
    public static final String INTEGR_CLIENT_SCAN_BASE_PACKAGE = "com.iwindplus.integr.client";

    /**
     * 线程池bean名称（oss）.
     */
    public static final String THREAD_POOL_BEAN_NAME_OSS = "ossThreadPool";

    /**
     * 线程池bean名称（sms）.
     */
    public static final String THREAD_POOL_BEAN_NAME_SMS = "smsThreadPool";

    /**
     * redis 缓存相关常数.
     */
    public static class RedisCacheConstant {

        private RedisCacheConstant() {
            throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
        }

        /**
         * 邮箱模板缓存名称.
         */
        public static final String CACHE_MAIL_TPL = "mailTpl";

        /**
         * 对象存储模板缓存名称.
         */
        public static final String CACHE_OSS_TPL = "ossTpl";

        /**
         * 短信模板缓存名称.
         */
        public static final String CACHE_SMS_TPL = "smsTpl";
    }
}

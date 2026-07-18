/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.setup.domain.constant;

import com.iwindplus.base.domain.constant.CommonConstant;

/**
 * 常数.
 *
 * @author zengdegui
 * @since 2020/11/8
 */
public class SetupConstant {

    private SetupConstant() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 通用设置服务名.
     */
    public static final String SETUP_SERVER_NAME = "iwindplus-setup";

    /**
     * 通用设置服务客户端扫描包名.
     */
    public static final String SETUP_CLIENT_SCAN_BASE_PACKAGE = "com.iwindplus.setup.client";

    /**
     * redis 缓存相关常数.
     */
    public static class RedisCacheConstant {

        private RedisCacheConstant() {
            throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
        }

        /**
         * 邮箱配置缓存名称.
         */
        public static final String CACHE_MAIL_CONFIG = "mailConfig";

        /**
         * 邮箱模板缓存名称.
         */
        public static final String CACHE_MAIL_TPL = "mailTpl";

        /**
         * 对象存储配置缓存名称.
         */
        public static final String CACHE_OSS_CONFIG = "ossConfig";

        /**
         * 对象存储模板缓存名称.
         */
        public static final String CACHE_OSS_TPL = "ossTpl";

        /**
         * 省市区缓存名称.
         */
        public static final String CACHE_REGION = "region";

        /**
         * 短信配置缓存名称.
         */
        public static final String CACHE_SMS_CONFIG = "smsConfig";

        /**
         * 短信模板缓存名称.
         */
        public static final String CACHE_SMS_TPL = "smsTpl";

        /**
         * 视频点播配置缓存名称.
         */
        public static final String CACHE_VOD_CONFIG = "vodConfig";

        /**
         * 微信小程序配置缓存名称.
         */
        public static final String CACHE_WECHAT_CONFIG_MA = "WechatConfigMa";

        /**
         * 微信公众号配置缓存名称.
         */
        public static final String CACHE_WECHAT_CONFIG_MP = "WechatConfigMp";

    }

}

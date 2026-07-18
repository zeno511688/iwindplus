/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.wechat.domain.property;

import com.github.binarywang.wxpay.config.WxPayConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * 微信相关属性.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "wechat")
public class WechatProperty {

    /**
     * 微信小程序配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private MaConfig ma = new MaConfig();

    /**
     * 微信公众号配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private MpConfig mp = new MpConfig();

    /**
     * 微信支付配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private PayConfig pay = new PayConfig();

    /**
     * 微信小程序相关属性.
     *
     * @author zengdegui
     * @since 2023/6/1
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MaConfig {

        /**
         * 是否启用.
         */
        @Builder.Default
        private Boolean enabled = Boolean.FALSE;

        /**
         * 是否使用redis存储.
         */
        private Boolean useRedis;

        /**
         * 设置微信小程序的appid.
         */
        private String appId;

        /**
         * 设置微信小程序的Secret.
         */
        private String secret;

        /**
         * 设置微信小程序消息服务器配置的token.
         */
        private String token;

        /**
         * 设置微信小程序消息服务器配置的EncodingAESKey.
         */
        private String aesKey;

        /**
         * 消息格式，XML或者JSON.
         */
        private String msgDataFormat;
    }

    /**
     * 微信公众号相关属性.
     *
     * @author zengdegui
     * @since 2023/6/1
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MpConfig {

        /**
         * 是否启用.
         */
        @Builder.Default
        private Boolean enabled = Boolean.FALSE;

        /**
         * 是否使用redis存储.
         */
        private Boolean useRedis;

        /**
         * 设置微信公众号的appid.
         */
        private String appId;

        /**
         * 设置微信公众号的Secret.
         */
        private String secret;

        /**
         * 设置微信公众号的token.
         */
        private String token;

        /**
         * 设置微信公众号的EncodingAESKey.
         */
        private String aesKey;
    }

    /**
     * 微信支付相关属性.
     *
     * @author zengdegui
     * @since 2023/6/1
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PayConfig extends WxPayConfig {

        /**
         * 是否启用.
         */
        @Builder.Default
        private Boolean enabled = Boolean.FALSE;
    }
}
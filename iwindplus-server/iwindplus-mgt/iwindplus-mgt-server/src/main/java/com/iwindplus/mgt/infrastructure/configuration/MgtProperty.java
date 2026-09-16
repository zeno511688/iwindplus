/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.infrastructure.configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * 管理服务配置相关属性.
 *
 * @author zengdegui
 * @since 2019/6/12
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "mgt")
public class MgtProperty {

    /**
     * ws配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private WsConfig ws = new WsConfig();

    /**
     * 邮件配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private MailConfig mail = new MailConfig();

    /**
     * 短信配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private SmsConfig sms = new SmsConfig();

    /**
     * 对象存储配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private OssConfig oss = new OssConfig();

    /**
     * 微信配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private WechatConfig wechat = new WechatConfig();

    /**
     * ws相关属性.
     *
     * @author zengdegui
     * @since 2024/4/6
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WsConfig {

        /**
         * 是否开启ws推送.
         */
        @Builder.Default
        private Boolean enabled = true;

        /**
         * 是否开启角色权限推送.
         */
        @Builder.Default
        private Boolean enabledRolePermission = true;

        /**
         * 是否开启按钮权限推送.
         */
        @Builder.Default
        private Boolean enabledButtonPermission = true;
    }

    /**
     * 邮件相关属性.
     *
     * @author zengdegui
     * @since 2024/4/6
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MailConfig {

        /**
         * 邮件模板编码.
         */
        private String tplCode;
    }

    /**
     * 短信相关属性.
     *
     * @author zengdegui
     * @since 2024/4/6
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SmsConfig {

        /**
         * 短信模板编码.
         */
        private String tplCode;
    }

    /**
     * 对象存储相关属性.
     *
     * @author zengdegui
     * @since 2024/4/6
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OssConfig {

        /**
         * 对象存储配置编码.
         */
        private String code;

        /**
         * 对象存储模板编码.
         */
        private String tplCode;
    }

    /**
     * 微信相关属性.
     *
     * @author zengdegui
     * @since 2024/4/6
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WechatConfig {

        /**
         * 微信公众号配置编码.
         */
        private String mpCode;

        /**
         * 微信公众号回调地址（外网，扫码登陆用）.
         */
        private String mpCallbackUrl;

        /**
         * 微信公众号回调成功地址（扫码登陆用）.
         */
        private String mpCallbackSuccessUrl;

        /**
         * 微信小程序配置编码.
         */
        private String maCode;

        /**
         * 微信小程序回调成功地址（授权登陆用）.
         */
        private String maCallbackSuccessUrl;
    }
}

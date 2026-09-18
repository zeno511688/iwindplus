/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.auth.infrastructure.configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.core.env.Environment;

/**
 * 服务配置.
 *
 * @author zengdegui
 * @since 2026/09/14 20:21
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "auth.server-api")
public class ServerApiProperty {

    /**
     * mgt服务名前缀占位符.
     */
    public static final String MGT_SERVER_PREFIX = "${auth.server-api.mgt-server-name:lb://iwindplus-mgt}";

    /**
     * log服务名前缀占位符.
     */
    public static final String LOG_SERVER_PREFIX = "${auth.server-api.log-server-name:lb://iwindplus-log}";

    /**
     * 环境（用于解析占位符）.
     */
    @Autowired
    private transient Environment environment;

    /**
     * mgt api配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private MgtApiConfig mgt = new MgtApiConfig();

    /**
     * log api配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private LogApiConfig log = new LogApiConfig();

    /**
     * 解析url模板中的占位符，获取真实可访问地址.
     *
     * @param urlTemplate url模板
     * @return 解析后的url
     */
    public String resolveUrl(String urlTemplate) {
        return environment.resolvePlaceholders(urlTemplate);
    }

    /**
     * mgt api配置.
     *
     * @author zengdegui
     * @since 2024/4/6
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MgtApiConfig {

        /**
         * 用户登录（支持用户名/手机/邮箱/身份证）.
         */
        @Builder.Default
        private String userLoginByParamUrl = MGT_SERVER_PREFIX + "/inner/user/getLoginByParam";

        /**
         * 用户登录（支持唯一编码，用于绑定授权方式，如微信公众号，小程序等）.
         */
        @Builder.Default
        private String userLoginByCodeUrl = MGT_SERVER_PREFIX + "/inner/user/getLoginByCode";

        /**
         * 根据客户端id获取客户端信息.
         */
        @Builder.Default
        private String clientGetByClientIdUrl = MGT_SERVER_PREFIX + "/inner/client/getByClientId";

        /**
         * 根据客户端id获取客户端详情.
         */
        @Builder.Default
        private String clientGetDetailUrl = MGT_SERVER_PREFIX + "/inner/client/getDetail";

        /**
         * 保存客户端信息.
         */
        @Builder.Default
        private String clientSaveUrl = MGT_SERVER_PREFIX + "/inner/client/save";
    }

    /**
     * log api配置.
     *
     * @author zengdegui
     * @since 2024/4/6
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LogApiConfig {

        /**
         * 校验邮箱验证码.
         */
        @Builder.Default
        private String mailCaptchaLogValidateUrl = LOG_SERVER_PREFIX + "/inner/mail/captcha/log/validate";

        /**
         * 校验短信验证码.
         */
        @Builder.Default
        private String smsCaptchaLogValidateUrl = LOG_SERVER_PREFIX + "/inner/sms/captcha/log/validate";
    }
}

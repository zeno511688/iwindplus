/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.auth.infrastructure.configuration;

import com.iwindplus.auth.common.enums.AuthTokenModeEnum;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * 登录日志配置相关属性.
 *
 * @author zengdegui
 * @since 2019/6/12
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "auth")
public class AuthProperty {

    /**
     * Token模式：JWT-生成jwt令牌（默认）；OPAQUE-只生成不透明令牌id，用户信息仅存储在redis中.
     */
    @Builder.Default
    private AuthTokenModeEnum tokenMode = AuthTokenModeEnum.JWT;

    /**
     * 忽略的路径.
     */
    private List<String> ignoredPatterns;

    /**
     * 是否启用校验访问token过期.
     */
    @Builder.Default
    private Boolean enabledTokenExpiredValid = Boolean.FALSE;

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
     * Cookie配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private CookieConfig cookie = new CookieConfig();

    /**
     * 登录日志配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private LogConfig log = new LogConfig();

    /**
     * 登录安全配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private LoginSecurityConfig loginSecurity = new LoginSecurityConfig();

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
     * 日志相关属性.
     *
     * @author zengdegui
     * @since 2024/4/6
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CookieConfig {

        /**
         * 是否启用.
         */
        @Builder.Default
        private Boolean enabled = Boolean.TRUE;

        /**
         * Cookie路径.
         */
        @Builder.Default
        private String path = "/";

        /**
         * Cookie是否启用HttpOnly.
         */
        @Builder.Default
        private Boolean httpOnly = Boolean.TRUE;

        /**
         * Cookie是否启用安全.
         */
        @Builder.Default
        private Boolean secure = Boolean.FALSE;

        /**
         * Cookie的SameSite.
         */
        @Builder.Default
        private String sameSite = "LAX";

        /**
         * Cookie的domain.
         */
        private String domain;
    }

    /**
     * 日志相关属性.
     *
     * @author zengdegui
     * @since 2024/4/6
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LogConfig {

        /**
         * 是否启用.
         */
        @Builder.Default
        private Boolean enabled = Boolean.TRUE;

        /**
         * 是否启用记录登陆日志.
         */
        @Builder.Default
        private Boolean enabledLogin = Boolean.TRUE;

        /**
         * 是否启用记录刷新token日志.
         */
        @Builder.Default
        private Boolean enabledRefreshToken = Boolean.FALSE;

        /**
         * 是否启用记录退出日志.
         */
        @Builder.Default
        private Boolean enabledLogout = Boolean.TRUE;
    }

    /**
     * 登录安全配置.
     *
     * @author zengdegui
     * @since 2026/09/20
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginSecurityConfig {

        /**
         * 是否启用登录安全策略.
         */
        @Builder.Default
        private Boolean enabled = Boolean.TRUE;

        /**
         * 触发图形验证码的错误次数（默认1次）.
         */
        @Builder.Default
        private Integer captchaThreshold = 1;

        /**
         * 触发账号锁定的最大错误次数（默认5次）.
         */
        @Builder.Default
        private Integer maxAttemptCount = 5;

        /**
         * 指数退避频率配置，超过最大错误次数后按此频率锁定.
         * <p>
         * 例如 "5m,10m,20m,30m"，第5次错误锁定5分钟，第6次锁定10分钟，依此类推.
         */
        @Builder.Default
        private String lockFrequency = "5m,10m,20m,30m";

        /**
         * 登录尝试记录缓存key前缀.
         */
        @Builder.Default
        private String attemptKeyPrefix = "login:attempt:";

        /**
         * 账号锁定缓存key前缀.
         */
        @Builder.Default
        private String lockKeyPrefix = "login:lock:";

        /**
         * 图形验证码缓存key前缀.
         */
        @Builder.Default
        private String captchaKeyPrefix = "login:captcha:";
    }
}

/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.auth.server.config.property;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.List;

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
     * 忽略的路径.
     */
    private List<String> ignoredPatterns;

    /**
     * 是否启用校验访问token过期.
     */
    @Builder.Default
    private Boolean enabledTokenExpiredValid = Boolean.FALSE;

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
        private Boolean enabledRefreshToken = Boolean.TRUE;

        /**
         * 是否启用记录退出日志.
         */
        @Builder.Default
        private Boolean enabledLogout = Boolean.TRUE;
    }
}

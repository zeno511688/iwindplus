/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.shiro.domain.property;

import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * shiro权限相关属性.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "shiro")
public class ShiroProperty {
    /**
     * jwt方式配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private JwtConfig jwt = new JwtConfig();

    /**
     * session方式配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private SessionConfig session = new SessionConfig();

    /**
     * jwt方式相关属性.
     *
     * @author zengdegui
     * @since 2023/6/1
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JwtConfig {
        /**
         * 是否启用.
         */
        @Builder.Default
        private Boolean enabled = Boolean.FALSE;

        /**
         * 认证过滤器key.
         */
        @Builder.Default
        private String authenticationFilterName = "authc";

        /**
         * 满足任一角色过滤器key.
         */
        @Builder.Default
        private String rolesAuthorizationFilterName = "roles";

        /**
         * 满足任一权限过滤器key.
         */
        @Builder.Default
        private String permsAuthorizationFilterName = "perms";

        /**
         * 访问token过期时间，单位秒.
         */
        @Builder.Default
        private Duration accessTokenExpireTime = Duration.ofMinutes(5);

        /**
         * 更新令牌时间，单位秒.
         */
        @Builder.Default
        private Duration refreshTokenExpireTime = Duration.ofDays(7);
    }

    /**
     * session方式相关属性.
     *
     * @author zengdegui
     * @since 2023/6/1
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionConfig {
        /**
         * 是否启用.
         */
        @Builder.Default
        private Boolean enabled = Boolean.FALSE;

        /**
         * 登录地址.
         */
        @Builder.Default
        private String loginUrl = "/login";

        /**
         * 登录成功地址.
         */
        @Builder.Default
        private String successUrl = "/index";

        /**
         * 无权限地址.
         */
        @Builder.Default
        private String unauthorizedUrl = "/unauthorized";

        /**
         * 缓存超时时间，单位秒.
         */
        @Builder.Default
        private Duration cacheTimeout = Duration.ofHours(2);

        /**
         * sessionId cookie名称.
         */
        @Builder.Default
        private String sessionIdCookieName = "sid";

        /**
         * 记住密码cookie名称.
         */
        @Builder.Default
        private String rememberName = "rememberMe";

        /**
         * 记住密码cookie失效时间（一周），单位:秒.
         */
        @Builder.Default
        private Duration rememberMeTimeout = Duration.ofDays(7);

        /**
         * 记住密码cookie加密密匙.
         */
        @Builder.Default
        private String rememberCipherKey = "3AvVhmFLUs0KTA3Kprsdag==";

        /**
         * 认证过滤器key.
         */
        @Builder.Default
        private String authenticationFilterName = "authc";

        /**
         * 满足任一角色过滤器key.
         */
        @Builder.Default
        private String rolesAuthorizationFilterName = "roles";

        /**
         * 满足任一权限过滤器key.
         */
        @Builder.Default
        private String permsAuthorizationFilterName = "perms";
    }
}

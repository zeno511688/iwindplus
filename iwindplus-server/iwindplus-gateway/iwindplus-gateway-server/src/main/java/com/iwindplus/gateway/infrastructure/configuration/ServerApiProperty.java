/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.gateway.infrastructure.configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.cloud.context.config.annotation.RefreshScope;
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
@RefreshScope
@ConfigurationProperties(prefix = "gateway.server-api")
public class ServerApiProperty {

    /**
     * mgt服务名前缀占位符.
     */
    public static final String MGT_SERVER_PREFIX = "${gateway.server-api.mgt-server-name:lb://iwindplus-mgt}";

    /**
     * auth服务名前缀占位符.
     */
    public static final String AUTH_SERVER_PREFIX = "${gateway.server-api.auth-server-name:lb://iwindplus-auth}";

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
     * auth api配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private AuthApiConfig auth = new AuthApiConfig();

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
         * 获取路由定义的url.
         */
        @Builder.Default
        private String serverListRouteDefinitionUrl = MGT_SERVER_PREFIX + "/inner/server/listRouteDefinition";

        /**
         * 获取应用凭证api签名信息.
         */
        @Builder.Default
        private String appCertGetByAccessKeyUrl = MGT_SERVER_PREFIX + "/inner/appCert/getByAccessKey";

        /**
         * 获取API白名单所有API.
         */
        @Builder.Default
        private String apiWhiteListListApiUrl = MGT_SERVER_PREFIX + "/inner/apiWhiteList/listApi";

        /**
         * 获取IP黑名单所有IP.
         */
        @Builder.Default
        private String ipBlackListListIpUrl = MGT_SERVER_PREFIX + "/inner/ipBlackList/listIp";

        /**
         * 获取用户API权限.
         */
        @Builder.Default
        private String resourceListApiCheckedByUserIdUrl = MGT_SERVER_PREFIX + "/inner/resource/listApiCheckedByUserId";

        /**
         * 校验用户API权限.
         */
        @Builder.Default
        private String resourceCheckApiByUserIdUrl = MGT_SERVER_PREFIX + "/inner/resource/checkApiByUserId";

        /**
         * 校验用户扩展功能（GA，邮箱，短信，yubikey）.
         */
        @Builder.Default
        private String userCheckExtendFunctionByUserIdUrl = MGT_SERVER_PREFIX + "/inner/user/checkExtendFunctionByUserId";

        /**
         * 获取所有服务API.
         */
        @Builder.Default
        private String serverApiListApiUrl = MGT_SERVER_PREFIX + "/inner/serverApi/listApi";
    }

    /**
     * auth api配置.
     *
     * @author zengdegui
     * @since 2024/4/6
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthApiConfig {

        /**
         * 获取用户信息.
         */
        @Builder.Default
        private String authorizationCheckAccessTokenUrl = AUTH_SERVER_PREFIX + "/inner/authorization/checkAccessToken";
    }
}

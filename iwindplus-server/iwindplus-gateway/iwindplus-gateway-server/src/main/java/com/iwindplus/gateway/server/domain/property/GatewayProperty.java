/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.gateway.server.domain.property;

import java.time.Duration;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.redisson.api.RateType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * gateway配置相关属性.
 *
 * @author zengdegui
 * @since 2024/4/6
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@RefreshScope
@ConfigurationProperties(prefix = "gateway")
public class GatewayProperty {

    /**
     * 限流过滤器配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private RateLimiterConfig rateLimiter = new RateLimiterConfig();

    /**
     * 基础过滤器配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private BaseConfig base = new BaseConfig();

    /**
     * API白名单配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private ApiWhiteListConfig apiWhiteList = new ApiWhiteListConfig();

    /**
     * IP黑名单配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private IpBlackListConfig ipBlackList = new IpBlackListConfig();

    /**
     * API签名配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private ApiSignConfig apiSign = new ApiSignConfig();

    /**
     * 认证配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private AuthConfig auth = new AuthConfig();

    /**
     * 操作扩展配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private OperateExtendConfig operateExtend = new OperateExtendConfig();

    /**
     * 日志配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private LogConfig log = new LogConfig();


    /**
     * 限流相关属性.
     *
     * @author zengdegui
     * @since 2024/4/6
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RateLimiterConfig {

        /**
         * 是否启用.
         */
        @Builder.Default
        private Boolean enabled = Boolean.TRUE;

        /**
         * 是否按路径限流（可选，默认：true）.
         *
         * @return boolean
         */
        @Builder.Default
        private Boolean enabledLimitPath = Boolean.FALSE;

        /**
         * 最大缓存数量.
         */
        @Builder.Default
        private long maxSize = 1000L;

        /**
         * 缓存超时时间，单位：分钟.
         */
        @Builder.Default
        private Duration cacheTimeout = Duration.ofMinutes(10L);

        /**
         * 缓存刷新时间，单位：分钟.
         */
        @Builder.Default
        private Duration cacheRefresh = Duration.ofMinutes(15L);

        /**
         * 限流类型（可选，默认：OVERALL）.
         */
        @Builder.Default
        private RateType rateType = RateType.OVERALL;

        /**
         * 限流次数，每个时间窗口允许请求数量（可选，默认：2000）.
         */
        @Builder.Default
        private Long rate = 2000L;

        /**
         * 限流速率（可选，默认：1s）.
         */
        @Builder.Default
        private Duration rateInterval = Duration.ofSeconds(1);
    }

    /**
     * 基础相关属性.
     *
     * @author zengdegui
     * @since 2024/6/1
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BaseConfig {

        /**
         * 是否启用.
         */
        @Builder.Default
        private Boolean enabled = Boolean.TRUE;

        /**
         * 是否启用从cookie中读取token.
         */
        @Builder.Default
        private Boolean enabledAuthCookie = Boolean.FALSE;

        /**
         * 是否启用相对路径.
         */
        @Builder.Default
        private Boolean enabledRelativePath = Boolean.FALSE;
    }

    /**
     * API白名单相关属性.
     *
     * @author zengdegui
     * @since 2024/4/6
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApiWhiteListConfig {

        /**
         * 是否启用.
         */
        @Builder.Default
        private Boolean enabled = Boolean.TRUE;

        /**
         * 最大缓存数量.
         */
        @Builder.Default
        private long maxSize = 1000L;

        /**
         * 缓存超时时间，单位：分钟.
         */
        @Builder.Default
        private Duration cacheTimeout = Duration.ofMinutes(10L);

        /**
         * 缓存刷新时间，单位：分钟.
         */
        @Builder.Default
        private Duration cacheRefresh = Duration.ofMinutes(15L);

        /**
         * 忽略的API.
         */
        private List<String> ignoredApi;
    }

    /**
     * IP黑名单相关属性.
     *
     * @author zengdegui
     * @since 2024/4/6
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IpBlackListConfig {

        /**
         * 是否启用.
         */
        @Builder.Default
        private Boolean enabled = Boolean.TRUE;

        /**
         * 最大缓存数量.
         */
        @Builder.Default
        private long maxSize = 1000L;

        /**
         * 缓存超时时间，单位：分钟.
         */
        @Builder.Default
        private Duration cacheTimeout = Duration.ofMinutes(10L);

        /**
         * 缓存刷新时间，单位：分钟.
         */
        @Builder.Default
        private Duration cacheRefresh = Duration.ofMinutes(15L);
    }

    /**
     * API签名相关属性.
     *
     * @author zengdegui
     * @since 2024/6/1
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApiSignConfig {

        /**
         * 是否启用.
         */
        @Builder.Default
        private Boolean enabled = Boolean.TRUE;

        /**
         * 是否启用检查只执行一次.
         */
        @Builder.Default
        private Boolean enabledExecuteOnlyOnce = Boolean.TRUE;

        /**
         * 检查只执行一次过期时间.
         */
        @Builder.Default
        private Duration onlyCheckOnceTtl = Duration.ofMinutes(1);

        /**
         * 最大缓存数量.
         */
        @Builder.Default
        private long maxSize = 1000L;

        /**
         * 缓存超时时间，单位：分钟.
         */
        @Builder.Default
        private Duration cacheTimeout = Duration.ofMinutes(10L);

        /**
         * 缓存刷新时间，单位：分钟.
         */
        @Builder.Default
        private Duration cacheRefresh = Duration.ofMinutes(15L);

        /**
         * 忽略的API.
         */
        private List<String> ignoredApi;
    }

    /**
     * 认证相关属性.
     *
     * @author zengdegui
     * @since 2024/4/6
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthConfig {

        /**
         * 是否启用.
         */
        @Builder.Default
        private Boolean enabled = Boolean.TRUE;

        /**
         * 是否启用远程token校验.
         */
        @Builder.Default
        private Boolean enabledRemoteToken = Boolean.TRUE;

        /**
         * 是否启用API权限验证.
         */
        @Builder.Default
        private Boolean enabledApiPermission = Boolean.TRUE;

        /**
         * token最大缓存数量.
         */
        @Builder.Default
        private long tokenCacheMaxSize = 1000L;

        /**
         * token缓存超时时间，单位：分钟.
         */
        @Builder.Default
        private Duration tokenCacheTimeout = Duration.ofMinutes(5L);

        /**
         * token缓存刷新时间，单位：分钟.
         */
        @Builder.Default
        private Duration tokenCacheRefresh = Duration.ofMinutes(10L);

        /**
         * 权限最大缓存数量.
         */
        @Builder.Default
        private long permissionCacheMaxSize = 1000L;

        /**
         * 权限缓存超时时间，单位：分钟.
         */
        @Builder.Default
        private Duration permissionCacheTimeout = Duration.ofMinutes(10L);

        /**
         * 权限缓存刷新时间，单位：分钟.
         */
        @Builder.Default
        private Duration permissionCacheRefresh = Duration.ofMinutes(15L);

        /**
         * 忽略API鉴权的API.
         */
        private List<String> ignoredApi;

        /**
         * 通用都有权限的API.
         */
        private List<String> generalApi;
    }

    /**
     * 操作扩展相关属性.
     *
     * @author zengdegui
     * @since 2024/4/6
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OperateExtendConfig {

        /**
         * 是否启用.
         */
        @Builder.Default
        private Boolean enabled = Boolean.FALSE;

        /**
         * 忽略的账号.
         */
        private List<String> ignoredUser;

        /**
         * 忽略的API.
         */
        @Builder.Default
        private List<String> ignoredApi = List.of(
            "admin/mgt/user/getGaQrcode",
            "admin/mgt/user/editGaBindFlag",
            "admin/mgt/user/userExtendYubikey/save"
        );

        /**
         * 需要GA校验的API.
         */
        private List<String> includeGaApi;

        /**
         * 需要邮箱校验的API.
         */
        private List<String> includeMailApi;

        /**
         * 需要短信校验的API.
         */
        private List<String> includeSmsApi;

        /**
         * 需要yubikey校验的API.
         */
        private List<String> includeYubikeyApi;
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
         * 是否启用记录日志（用户登陆后）.
         */
        @Builder.Default
        private Boolean enabled = Boolean.FALSE;

        /**
         * 是否启用记录错误日志.
         */
        @Builder.Default
        private Boolean enabledError = Boolean.FALSE;

        /**
         * 忽略记录日志的API.
         */
        private List<String> ignoredApi;

        /**
         * 采样率（10：代表10%的采样率）.
         */
        @Builder.Default
        private Integer sampleRate = 100;

        /**
         * 是否启用获取请求头.
         */
        @Builder.Default
        private Boolean enabledRequestHeader = Boolean.TRUE;

        /**
         * 是否启用获取请求参数.
         */
        @Builder.Default
        private Boolean enabledRequestParam = Boolean.TRUE;

        /**
         * 是否启用获取请求体.
         */
        @Builder.Default
        private Boolean enabledRequestBody = Boolean.TRUE;

        /**
         * 请求体限制大小，超过部分丢弃，单位：KB.
         */
        @Builder.Default
        private Integer limitRequestBody = 16;

        /**
         * 是否启用获取响应头.
         */
        @Builder.Default
        private Boolean enabledResponseHeader = Boolean.FALSE;

        /**
         * 是否启用获取响应体.
         */
        @Builder.Default
        private Boolean enabledResponseBody = Boolean.FALSE;

        /**
         * 响应体限制大小，超过部分丢弃，单位：KB.
         */
        @Builder.Default
        private Integer limitResponseBody = 16;
    }
}

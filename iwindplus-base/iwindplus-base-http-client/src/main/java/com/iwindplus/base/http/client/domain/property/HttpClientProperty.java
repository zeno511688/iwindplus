/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.domain.property;

import cn.hutool.core.util.URLUtil;
import com.iwindplus.base.http.client.domain.enums.HttpClientTypeEnum;
import java.time.Duration;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import reactor.netty.transport.ProxyProvider;

/**
 * HttpClient相关属性.
 *
 * @author zengdegui
 * @since 2018/10/10
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "http.client")
public class HttpClientProperty {

    /**
     * 是否启用.
     */
    @Builder.Default
    private Boolean enabled = Boolean.TRUE;

    /**
     * 是否启用限流熔断观察.
     */
    @Builder.Default
    private Boolean enabledCircuitBreaker = Boolean.FALSE;

    /**
     * 是否启用每次请求观察（客户端自带的）.
     */
    @Builder.Default
    private Boolean enabledObservation = Boolean.TRUE;

    /**
     * 是否启用每次请求观察（自定义）.
     */
    @Builder.Default
    private Boolean enabledObservationCustom = Boolean.FALSE;

    /**
     * 默认客户端
     */
    @Builder.Default
    private HttpClientTypeEnum defaultHttpClient = HttpClientTypeEnum.REST_CLIENT;

    /**
     * API防护配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private ApiProtectionConfig apiProtection = new ApiProtectionConfig();

    /**
     * apache客户端配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private ApacheHttpClientConfig apache = new ApacheHttpClientConfig();

    /**
     * OkHttp客户端配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private OkHttpClientConfig ok = new OkHttpClientConfig();

    /**
     * RestClient客户端配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private RestClientConfig rest = new RestClientConfig();

    /**
     * webClient客户端配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private WebClientConfig web = new WebClientConfig();

    /**
     * API防护相关属性.
     *
     * @author zengdegui
     * @since 2024/6/1
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApiProtectionConfig {

        /**
         * 是否启用.
         */
        @Builder.Default
        private Boolean enabled = Boolean.FALSE;

        /**
         * 是否启用远程应用凭证配置.
         */
        @Builder.Default
        private Boolean enabledRemote = Boolean.FALSE;

        /**
         * 获取远程应用凭证配置的URL.
         */
        @Builder.Default
        private String url = "lb://iwindplus-mgt/inner/appCert/getByCertType";

        /**
         * 是否启用本地缓存应用凭证配置.
         */
        @Builder.Default
        private Boolean enabledLocalCache = Boolean.TRUE;

        /**
         * 忽略的API.
         */
        private List<String> ignoredApi;

        /**
         * 访问key.
         */
        private String accessKey;

        /**
         * 密钥.
         */
        private String secretKey;

        /**
         * 签名超时时间（单位：秒）.
         */
        @Builder.Default
        private Integer timeout = 30;

        /**
         * 获取 host 后面的 path.
         */
        public String getPath() {
            String fullUrl = getUrl();
            return URLUtil.getPath(fullUrl);
        }
    }

    /**
     * Apache HttpClient相关属性.
     */
    @EqualsAndHashCode(callSuper = false)
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApacheHttpClientConfig {

        /**
         * 是否启用.
         */
        @Builder.Default
        private Boolean enabled = Boolean.TRUE;

        /**
         * 连接保持时间。如果超过这个时间没有任务调度，则会被回收（单位：秒）.
         */
        @Builder.Default
        private Duration connectionKeepAlive = Duration.ofSeconds(300);

        /**
         * 从连接池中获取到连接的最长时间（单位：秒）.
         */
        @Builder.Default
        private Duration connectionRequestTimeout = Duration.ofSeconds(5);

        /**
         * 响应超时时间（单位：秒）.
         */
        @Builder.Default
        private Duration responseTimeout = Duration.ofSeconds(60);

        /**
         * 是否启用压缩.
         */
        @Builder.Default
        private Boolean enabledCompression = Boolean.TRUE;

        /**
         * 是否开启重定向.
         */
        @Builder.Default
        private Boolean redirectsEnabled = true;

        /**
         * 重定向的最大次数.
         */
        @Builder.Default
        private Integer maxRedirects = 5;

        /**
         * 线程池相关属性.
         */
        @Builder.Default
        @NestedConfigurationProperty
        private Pool pool = new Pool();

        /**
         * 代理相关属性.
         */
        @Builder.Default
        @NestedConfigurationProperty
        private Proxy proxy = new Proxy();

        /**
         * 重试相关属性.
         */
        @Builder.Default
        @NestedConfigurationProperty
        private Retry retry = new Retry();

        /**
         * 线程池相关属性.
         */
        @Data
        @SuperBuilder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Pool {

            /**
             * 最大连接数.
             */
            @Builder.Default
            private Integer maxConnTotal = 25;

            /**
             * 同路由并发数.
             */
            @Builder.Default
            private Integer maxConnPerRoute = 50;
        }

        /**
         * 代理相关属性.
         */
        @Data
        @SuperBuilder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Proxy {

            /**
             * 是否开启代理.
             */
            @Builder.Default
            private Boolean enabled = Boolean.FALSE;

            /**
             * 主机.
             */
            private String host;

            /**
             * 端口.
             */
            private Integer port;

            /**
             * 用户名.
             */
            private String username;

            /**
             * 密码.
             */
            private String password;
        }

        /**
         * 重试相关属性.
         */
        @Data
        @SuperBuilder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Retry {

            /**
             * 是否启用.
             */
            @Builder.Default
            private Boolean enabled = Boolean.TRUE;

            /**
             * 最大重试次数.
             */
            @Builder.Default
            private Integer maxAttempts = 3;

            /**
             * 重试间隔（单位：秒）.
             */
            @Builder.Default
            private Duration period = Duration.ofSeconds(1);
        }
    }

    /**
     * OkHttpClient相关属性.
     */
    @EqualsAndHashCode(callSuper = false)
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OkHttpClientConfig {

        /**
         * 是否启用.
         */
        @Builder.Default
        private Boolean enabled = Boolean.TRUE;

        /**
         * 协议.
         */
        @Builder.Default
        private List<String> protocols = List.of("HTTP_2", "HTTP_1_1");

        /**
         * 连接超时时间（单位：秒）.
         */
        @Builder.Default
        private Duration connectTimeout = Duration.ofSeconds(5);

        /**
         * 读取超时时间（单位：秒）.
         */
        @Builder.Default
        private Duration readTimeout = Duration.ofSeconds(60);

        /**
         * 写入超时时间（单位：秒）.
         */
        @Builder.Default
        private Duration writeTimeout = Duration.ofSeconds(10);

        /**
         * 调用超时时间（单位：秒）.
         */
        @Builder.Default
        private Duration callTimeout = Duration.ofSeconds(30);

        /**
         * 是否允许重定向.
         */
        @Builder.Default
        private Boolean followRedirects = Boolean.TRUE;

        /**
         * 线程池相关属性.
         */
        @Builder.Default
        @NestedConfigurationProperty
        private Pool pool = new Pool();

        /**
         * 代理相关属性.
         */
        @Builder.Default
        @NestedConfigurationProperty
        private Proxy proxy = new Proxy();

        /**
         * 重试相关属性.
         */
        @Builder.Default
        @NestedConfigurationProperty
        private Retry retry = new Retry();

        @Data
        @SuperBuilder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Pool {

            /**
             * 最大连接数.
             */
            @Builder.Default
            private Integer maxConnTotal = 25;

            /**
             * 连接保持时间（单位：秒）.
             */
            @Builder.Default
            private Duration connectionKeepAlive = Duration.ofSeconds(900);
        }

        @Data
        @SuperBuilder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Proxy {

            /**
             * 是否开启代理.
             */
            @Builder.Default
            private Boolean enabled = Boolean.FALSE;

            private String host;
            private Integer port;
            private String username;
            private String password;
        }

        @Data
        @SuperBuilder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Retry {

            /**
             * 是否启用.
             */
            @Builder.Default
            private Boolean enabled = Boolean.TRUE;
        }
    }

    /**
     * RestClient相关属性.
     */
    @EqualsAndHashCode(callSuper = false)
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RestClientConfig {

        /**
         * 是否启用.
         */
        @Builder.Default
        private Boolean enabled = Boolean.TRUE;
    }

    /**
     * WebClient配置相关属性.
     */
    @EqualsAndHashCode(callSuper = false)
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WebClientConfig {

        /**
         * 是否启用.
         */
        @Builder.Default
        private Boolean enabled = Boolean.TRUE;

        /**
         * 是否开启日志详情.
         */
        @Builder.Default
        private Boolean enableLoggingRequestDetails = Boolean.TRUE;

        /**
         * 是否启用Wiretap.
         */
        @Builder.Default
        private Boolean enabledWiretap = Boolean.FALSE;

        /**
         * 是否启用压缩.
         */
        @Builder.Default
        private Boolean enabledCompression = Boolean.TRUE;

        /**
         * 最大内存大小.
         */
        @Builder.Default
        private Integer maxInMemorySize = 512 * 1024;

        /**
         * 连接超时时间（单位：秒）.
         */
        @Builder.Default
        private Duration connectTimeout = Duration.ofSeconds(5);

        /**
         * 响应超时时间（单位：秒）.
         */
        @Builder.Default
        private Duration responseTimeout = Duration.ofSeconds(60);

        /**
         * 读取超时时间（单位：秒）.
         */
        @Builder.Default
        private Duration readTimeout = Duration.ofSeconds(30);

        /**
         * 写入超时时间（单位：秒）.
         */
        @Builder.Default
        private Duration writeTimeout = Duration.ofSeconds(10);

        /**
         * 最大初始行长度.
         */
        @Builder.Default
        private Integer maxInitialLineLength = 4096;

        /**
         * 最大头长度.
         */
        @Builder.Default
        private Integer maxHeaderSize = 16 * 1024;

        /**
         * 线程池属性.
         */
        @Builder.Default
        @NestedConfigurationProperty
        private Pool pool = new Pool();

        /**
         * 代理相关属性.
         */
        @Builder.Default
        @NestedConfigurationProperty
        private Proxy proxy = new Proxy();

        /**
         * 重试相关属性.
         */
        @Builder.Default
        @NestedConfigurationProperty
        private Retry retry = new Retry();

        /**
         * 线程池类型.
         */
        @Data
        @SuperBuilder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Pool {

            /**
             * 线程池名称.
             */
            @Builder.Default
            private String name = "webClientPool";

            /**
             * 最大连接数.
             */
            @Builder.Default
            private Integer maxConnections = 2000;

            /**
             * 最大等待获取连接数.
             */
            @Builder.Default
            private Integer pendingAcquireMaxCount = 2000;

            /**
             * 等待超时时间（单位：秒）.
             */
            @Builder.Default
            private Duration pendingAcquireTimeout = Duration.ofSeconds(2);

            /**
             * 最大空闲时间（单位：秒）.
             */
            @Builder.Default
            private Duration maxIdleTime = Duration.ofSeconds(30);

            /**
             * 最大生命周期时间（单位：秒）.
             */
            @Builder.Default
            private Duration maxLifeTime = Duration.ofSeconds(300);

            /**
             * 驱逐检查间隔.
             */
            @Builder.Default
            private Duration evictionInterval = Duration.ofSeconds(300);

            /**
             * 是否启用指标.
             */
            @Builder.Default
            private Boolean metrics = Boolean.TRUE;
        }

        /**
         * 代理属性.
         */
        @Data
        @SuperBuilder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Proxy {

            /**
             * 是否开启代理.
             */
            @Builder.Default
            private Boolean enabled = Boolean.FALSE;

            /**
             * 代理类型.
             */
            @Builder.Default
            private ProxyProvider.Proxy type = ProxyProvider.Proxy.HTTP;

            /**
             * 主机.
             */
            private String host;

            /**
             * 端口.
             */
            private Integer port;

            /**
             * 用户名.
             */
            private String username;

            /**
             * 密码.
             */
            private String password;

            /**
             * 不代理的域名.
             */
            private String nonProxyHostsPattern;
        }

        /**
         * 重试属性.
         */
        @Data
        @SuperBuilder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Retry {

            /**
             * 是否启用.
             */
            @Builder.Default
            private Boolean enabled = Boolean.TRUE;

            /**
             * 最大重试次数.
             */
            @Builder.Default
            private Integer maxAttempts = 3;

            /**
             * 重试间隔（单位：秒）.
             */
            @Builder.Default
            private Duration period = Duration.ofSeconds(1);

            /**
             * 最大回退超时时间（单位：秒）.
             */
            @Builder.Default
            private Duration maxBackoffTimeout = Duration.ofSeconds(2);
        }
    }
}

/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client;

import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.domain.constant.CommonConstant.ObservationConstant;
import com.iwindplus.base.http.client.domain.property.HttpClientProperty;
import com.iwindplus.base.http.client.domain.property.HttpClientProperty.ApacheHttpClientConfig;
import com.iwindplus.base.http.client.domain.property.HttpClientProperty.ApacheHttpClientConfig.Pool;
import com.iwindplus.base.http.client.domain.property.HttpClientProperty.ApacheHttpClientConfig.Proxy;
import com.iwindplus.base.http.client.domain.property.HttpClientProperty.ApacheHttpClientConfig.Retry;
import com.iwindplus.base.http.client.interceptor.ApacheHttpClientRequestInterceptor;
import com.iwindplus.base.http.client.support.ApiProtectionProvider;
import io.micrometer.core.instrument.binder.httpcomponents.hc5.ObservationExecChainHandler;
import io.micrometer.observation.ObservationRegistry;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.Credentials;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.ChainElement;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * HttpClient配置.
 *
 * @author zengdegui
 * @since 2023/08/31 20:32
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({HttpClientProperty.class})
@ConditionalOnProperty(prefix = "http.client.apache", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ApacheHttpClientConfiguration {

    @Resource
    private HttpClientProperty property;

    @Resource
    private ApiProtectionProvider apiProtectionProvider;

    /**
     * 创建 PoolingHttpClientConnectionManager
     *
     * @return PoolingHttpClientConnectionManager
     */
    @Bean
    public PoolingHttpClientConnectionManager poolingHttpClientConnectionManager() {
        final ApacheHttpClientConfig cfg = this.property.getApache();
        final Pool pool = cfg.getPool();
        final PoolingHttpClientConnectionManager manager =
            PoolingHttpClientConnectionManagerBuilder.create()
                .setMaxConnTotal(pool.getMaxConnTotal())
                .setMaxConnPerRoute(pool.getMaxConnPerRoute())
                .build();
        log.info("PoolingHttpClientConnectionManager={}", manager);
        return manager;
    }

    /**
     * 创建Async连接池管理器
     */
    @Bean
    public PoolingAsyncClientConnectionManager poolingAsyncClientConnectionManager() {
        final ApacheHttpClientConfig cfg = this.property.getApache();
        final Pool pool = cfg.getPool();
        PoolingAsyncClientConnectionManager manager =
            PoolingAsyncClientConnectionManagerBuilder.create()
                .setMaxConnTotal(pool.getMaxConnTotal())
                .setMaxConnPerRoute(pool.getMaxConnPerRoute())
                .build();
        log.info("PoolingAsyncClientConnectionManager={}", manager);
        return manager;
    }

    /**
     * 创建ObservationExecChainHandler
     *
     * @return ObservationExecChainHandler
     */
    @Bean
    public ObservationExecChainHandler observationExecChainHandler(
        ObservationRegistry observationRegistry) {
        return new ObservationExecChainHandler(observationRegistry);
    }

    /**
     * 创建 HttpClientBuilder.
     *
     * @param connectionManager                  连接池管理器
     * @param apacheHttpClientRequestInterceptor 请求拦截器
     * @param observationExecChainHandler        observationExecChainHandler
     * @return HttpClientBuilder
     */
    @Bean
    public HttpClientBuilder httpClientBuilder(PoolingHttpClientConnectionManager connectionManager,
        ApacheHttpClientRequestInterceptor apacheHttpClientRequestInterceptor,
        ObservationExecChainHandler observationExecChainHandler) {
        final ApacheHttpClientConfig cfg = this.property.getApache();
        RequestConfig requestConfig = RequestConfig.custom()
            .setConnectionKeepAlive(TimeValue.of(cfg.getConnectionKeepAlive()))
            .setConnectionRequestTimeout(Timeout.of(cfg.getConnectionRequestTimeout()))
            .setRedirectsEnabled(cfg.getRedirectsEnabled())
            .setMaxRedirects(cfg.getMaxRedirects())
            .setCircularRedirectsAllowed(Boolean.FALSE)
            .setResponseTimeout(Timeout.of(cfg.getResponseTimeout()))
            .setContentCompressionEnabled(cfg.getEnabledCompression())
            .build();
        HttpClientBuilder builder = HttpClients.custom()
            .setDefaultRequestConfig(requestConfig)
            .setConnectionManager(connectionManager)
            .addRequestInterceptorFirst(apacheHttpClientRequestInterceptor);

        // 重试
        final Retry retry = cfg.getRetry();
        if (Boolean.TRUE.equals(retry.getEnabled())) {
            // 重试次数
            builder.setRetryStrategy(
                new DefaultHttpRequestRetryStrategy(retry.getMaxAttempts(), TimeValue.of(retry.getPeriod())));
        } else {
            builder.disableAutomaticRetries();
        }

        if (Boolean.TRUE.equals(property.getEnabledObservation())) {
            builder.addExecInterceptorBefore(
                ChainElement.MAIN_TRANSPORT.name(),
                ObservationConstant.HTTP_OBSERVATION_NAME,
                observationExecChainHandler
            );
        }

        // 代理
        final Proxy proxy = cfg.getProxy();
        if (Boolean.TRUE.equals(proxy.getEnabled())) {
            HttpHost httpHost = new HttpHost(proxy.getHost(), proxy.getPort());
            builder.setProxy(httpHost);
            String username = proxy.getUsername();
            String password = proxy.getPassword();
            if (CharSequenceUtil.isAllNotBlank(username, password)) {
                AuthScope authscope = new AuthScope(httpHost);
                Credentials credentials = new UsernamePasswordCredentials(username, password.toCharArray());
                BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(authscope, credentials);
                builder.setDefaultCredentialsProvider(credentialsProvider);
            }
        }

        log.info("HttpClientBuilder={}", builder);
        return builder;
    }

    /**
     * 创建 HttpAsyncClientBuilder.
     *
     * @param connectionManager                  连接池管理器
     * @param apacheHttpClientRequestInterceptor 请求拦截器
     * @param observationExecChainHandler        observationExecChainHandler
     * @return HttpAsyncClientBuilder
     */
    @Bean
    public HttpAsyncClientBuilder httpAsyncClientBuilder(
        PoolingAsyncClientConnectionManager connectionManager,
        ApacheHttpClientRequestInterceptor apacheHttpClientRequestInterceptor,
        ObservationExecChainHandler observationExecChainHandler) {
        final ApacheHttpClientConfig cfg = this.property.getApache();
        RequestConfig requestConfig = RequestConfig.custom()
            .setConnectionKeepAlive(TimeValue.of(cfg.getConnectionKeepAlive()))
            .setConnectionRequestTimeout(Timeout.of(cfg.getConnectionRequestTimeout()))
            .setRedirectsEnabled(cfg.getRedirectsEnabled())
            .setMaxRedirects(cfg.getMaxRedirects())
            .setCircularRedirectsAllowed(Boolean.FALSE)
            .setResponseTimeout(Timeout.of(cfg.getResponseTimeout()))
            .setContentCompressionEnabled(cfg.getEnabledCompression())
            .build();

        IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
            .setSoTimeout(Timeout.of(cfg.getResponseTimeout()))
            .setIoThreadCount(Runtime.getRuntime().availableProcessors())
            .build();

        HttpAsyncClientBuilder builder = HttpAsyncClients.custom()
            .setDefaultRequestConfig(requestConfig)
            .setIOReactorConfig(ioReactorConfig)
            .setConnectionManager(connectionManager)
            .addRequestInterceptorFirst(apacheHttpClientRequestInterceptor);

        if (Boolean.TRUE.equals(property.getEnabledObservation())) {
            builder.addExecInterceptorBefore(
                ChainElement.MAIN_TRANSPORT.name(),
                ObservationConstant.HTTP_OBSERVATION_NAME,
                observationExecChainHandler
            );
        }

        // 代理
        final Proxy proxy = cfg.getProxy();
        if (Boolean.TRUE.equals(proxy.getEnabled())) {
            HttpHost httpHost = new HttpHost(proxy.getHost(), proxy.getPort());
            builder.setProxy(httpHost);

            if (CharSequenceUtil.isAllNotBlank(proxy.getUsername(), proxy.getPassword())) {
                BasicCredentialsProvider provider = new BasicCredentialsProvider();
                provider.setCredentials(
                    new AuthScope(httpHost),
                    new UsernamePasswordCredentials(
                        proxy.getUsername(),
                        proxy.getPassword().toCharArray()
                    )
                );
                builder.setDefaultCredentialsProvider(provider);
            }
        }

        log.info("HttpAsyncClientBuilder={}", builder);
        return builder;
    }

    /**
     * 创建 CloseableHttpClient.
     *
     * @param builder builder
     * @return CloseableHttpClient
     */
    @Bean
    public CloseableHttpClient closeableHttpClient(HttpClientBuilder builder) {
        final CloseableHttpClient httpClient = builder.build();
        log.info("CloseableHttpClient={}", httpClient);
        return httpClient;
    }

    /**
     * 创建 CloseableHttpAsyncClient
     *
     * @param builder builder
     * @return CloseableHttpAsyncClient
     */
    @Bean(initMethod = "start", destroyMethod = "close")
    public CloseableHttpAsyncClient closeableHttpAsyncClient(HttpAsyncClientBuilder builder) {
        CloseableHttpAsyncClient client = builder.build();
        log.info("CloseableHttpAsyncClient={}", client);
        return client;
    }

    /**
     * 创建 ApacheHttpClientRequestInterceptor.
     *
     * @return ApacheHttpClientRequestInterceptor
     */
    @Bean
    public ApacheHttpClientRequestInterceptor apacheHttpClientRequestInterceptor() {
        return new ApacheHttpClientRequestInterceptor(property, apiProtectionProvider);
    }
}

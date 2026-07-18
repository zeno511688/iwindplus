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
import com.iwindplus.base.http.client.domain.property.HttpClientProperty.OkHttpClientConfig;
import com.iwindplus.base.http.client.domain.property.HttpClientProperty.OkHttpClientConfig.Pool;
import com.iwindplus.base.http.client.interceptor.OkHttpClientInterceptor;
import com.iwindplus.base.http.client.support.ApiProtectionProvider;
import com.iwindplus.base.monitor.support.TraceContextPropagator;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.micrometer.core.instrument.binder.okhttp3.OkHttpObservationInterceptor;
import io.micrometer.observation.ObservationRegistry;
import jakarta.annotation.Resource;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

/**
 * OkHttp配置.
 *
 * @author zengdegui
 * @since 2023/08/31
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({HttpClientProperty.class})
@ConditionalOnProperty(prefix = "http.client.ok", name = "enabled", havingValue = "true", matchIfMissing = true)
public class OkHttpClientConfiguration {

    @Resource
    private HttpClientProperty property;

    @Resource
    private TraceContextPropagator traceContextPropagator;

    @Resource
    private ApiProtectionProvider apiProtectionProvider;

    @Resource
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Resource
    private ObservationRegistry observationRegistry;

    /**
     * 创建 ConnectionPool
     *
     * @return ConnectionPool
     */
    @Bean
    public ConnectionPool okHttpClientConnectionPool() {
        final OkHttpClientConfig cfg = property.getOk();
        final Pool pool = cfg.getPool();
        ConnectionPool connectionPool = new ConnectionPool(pool.getMaxConnTotal(), pool.getConnectionKeepAlive().toSeconds(), TimeUnit.SECONDS);
        log.info("OkHttpClient ConnectionPool={}", connectionPool);
        return connectionPool;
    }

    /**
     * 创建 OkHttpClient.Builder.
     *
     * @param pool                         连接池
     * @param okHttpClientInterceptor      拦截器
     * @param okHttpObservationInterceptor Observation拦截器
     * @return OkHttpClient.Builder
     */
    @Bean
    public OkHttpClient.Builder okHttpClientBuilder(
        ConnectionPool pool,
        OkHttpClientInterceptor okHttpClientInterceptor,
        OkHttpObservationInterceptor okHttpObservationInterceptor) {
        final OkHttpClientConfig cfg = property.getOk();
        List<Protocol> protocols = cfg.getProtocols().stream().map(Protocol::valueOf).toList();

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
            .protocols(protocols)
            .connectionPool(pool)
            .connectTimeout(cfg.getConnectTimeout())
            .readTimeout(cfg.getReadTimeout())
            .writeTimeout(cfg.getWriteTimeout())
            .callTimeout(cfg.getCallTimeout())
            .retryOnConnectionFailure(cfg.getRetry().getEnabled())
            .followRedirects(cfg.getFollowRedirects())
            .addInterceptor(okHttpClientInterceptor);

        if (Boolean.TRUE.equals(property.getEnabledObservation())) {
            builder.addInterceptor(okHttpObservationInterceptor);
        }

        // 代理配置
        final OkHttpClientConfig.Proxy proxy = cfg.getProxy();
        if (Boolean.TRUE.equals(proxy.getEnabled())) {
            builder.proxy(new Proxy(Proxy.Type.HTTP,
                new InetSocketAddress(proxy.getHost(), proxy.getPort())));

            String user = proxy.getUsername();
            String pwd = proxy.getPassword();
            if (CharSequenceUtil.isAllNotBlank(user, pwd)) {
                builder.proxyAuthenticator((route, resp) -> {
                    String credential = Credentials.basic(user, pwd);
                    return resp.request().newBuilder()
                        .header(HttpHeaders.PROXY_AUTHORIZATION, credential)
                        .build();
                });
            }
        }

        log.info("OkHttpClient.Builder={}", builder);
        return builder;
    }

    /**
     * 创建OkHttpClient实例.
     *
     * @param builder builder
     * @return OkHttpClient
     */
    @Bean
    public OkHttpClient okHttpClient(OkHttpClient.Builder builder) {
        OkHttpClient okHttpClient = builder.build();
        log.info("OkHttpClient={}", okHttpClient);
        return okHttpClient;
    }

    /**
     * 创建 OkHttpClientInterceptor.
     *
     * @return OkHttpClientInterceptor
     */
    @Bean
    public OkHttpClientInterceptor okHttpClientInterceptor() {
        return new OkHttpClientInterceptor(property, traceContextPropagator,
            circuitBreakerRegistry, apiProtectionProvider);
    }

    /**
     * 创建 OkHttpObservationInterceptor.
     *
     * @return OkHttpObservationInterceptor
     */
    @Bean
    public OkHttpObservationInterceptor okHttpObservationInterceptor() {
        return OkHttpObservationInterceptor.builder(
                observationRegistry,
                ObservationConstant.HTTP_OBSERVATION_NAME)
            .build();
    }
}

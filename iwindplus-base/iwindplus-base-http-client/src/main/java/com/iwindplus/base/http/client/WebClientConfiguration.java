/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwindplus.base.domain.constant.CommonConstant.ApiSignConstant;
import com.iwindplus.base.domain.constant.CommonConstant.HeaderConstant;
import com.iwindplus.base.http.client.domain.enums.HttpClientTypeEnum;
import com.iwindplus.base.http.client.domain.property.HttpClientProperty;
import com.iwindplus.base.http.client.domain.property.HttpClientProperty.WebClientConfig;
import com.iwindplus.base.http.client.domain.property.HttpClientProperty.WebClientConfig.Pool;
import com.iwindplus.base.http.client.support.ApiProtectionProvider;
import com.iwindplus.base.util.ApiSignUtil;
import com.iwindplus.base.util.domain.dto.ApiSignGenerateDTO;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.micrometer.observation.ObservationRegistry;
import io.netty.channel.ChannelOption;
import io.netty.channel.ConnectTimeoutException;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutException;
import io.netty.handler.timeout.WriteTimeoutHandler;
import jakarta.annotation.Resource;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.ClientCodecConfigurer.ClientDefaultCodecs;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.PrematureCloseException;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.transport.ProxyProvider;
import reactor.util.retry.Retry;

/**
 * WebClient配置.
 *
 * @author zengdegui
 * @since 2025/08/24 01:38
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({HttpClientProperty.class})
@ConditionalOnProperty(prefix = "http.client.web", name = "enabled", havingValue = "true", matchIfMissing = true)
public class WebClientConfiguration {

    @Resource
    private HttpClientProperty property;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Resource
    private ObservationRegistry observationRegistry;

    @Resource
    private ApiProtectionProvider apiProtectionProvider;

    /**
     * 创建HttpClient实例.
     *
     * @return HttpClient
     */
    @Primary
    @Bean
    public HttpClient webClientHttpClient() {
        final WebClientConfig cfg = property.getWeb();
        HttpClient httpClient =
            HttpClient.create(this.buildConnectionPool())
                .keepAlive(true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,
                    (int) Optional.of(cfg.getConnectTimeout()).orElse(Duration.ofSeconds(30)).toMillis()
                )
                .responseTimeout(Optional.of(cfg.getResponseTimeout()).orElse(Duration.ofSeconds(30)))
                .doOnConnected(conn ->
                    conn.addHandlerLast(new ReadTimeoutHandler(cfg.getReadTimeout().getSeconds(), TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(cfg.getWriteTimeout().getSeconds(), TimeUnit.SECONDS))
                )
                .httpResponseDecoder(spec ->
                    spec.maxHeaderSize(cfg.getMaxHeaderSize())
                        .maxInitialLineLength(cfg.getMaxInitialLineLength()))
                .protocol(HttpProtocol.HTTP11, HttpProtocol.H2)
                .wiretap(cfg.getEnabledWiretap())
                .compress(cfg.getEnabledCompression());
        httpClient = this.configureProxy(httpClient).proxyWithSystemProperties();
        log.info("HttpClient={}", httpClient);
        return httpClient;
    }

    /**
     * 创建ExchangeStrategies实例.
     *
     * @return ExchangeStrategies
     */
    @Bean
    public ExchangeStrategies webClientExchangeStrategies() {
        final WebClientConfig cfg = property.getWeb();
        final ExchangeStrategies exchangeStrategies =
            ExchangeStrategies.builder()
                .codecs(configurer -> {
                    final ClientDefaultCodecs clientDefaultCodecs = configurer.defaultCodecs();
                    clientDefaultCodecs.jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper));
                    clientDefaultCodecs.jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper));
                    clientDefaultCodecs.maxInMemorySize(cfg.getMaxInMemorySize());
                    clientDefaultCodecs.enableLoggingRequestDetails(cfg.getEnableLoggingRequestDetails());
                }).build();
        log.info("ExchangeStrategies={}", exchangeStrategies);
        return exchangeStrategies;
    }

    /**
     * 创建负载均衡 WebClient.Builder.
     *
     * @param strategies 策略
     * @param httpClient http客户端
     * @return WebClient.Builder
     */
    @LoadBalanced
    @Bean("loadBalancedWebClientBuilder")
    public WebClient.Builder loadBalancedWebClientBuilder(ExchangeStrategies strategies, HttpClient httpClient) {
        final WebClient.Builder webClientBuilder = createWebClientBuilder(strategies, httpClient);
        log.info("LoadBalanced WebClient.Builder={}", webClientBuilder);
        return webClientBuilder;
    }

    /**
     * 创建 WebClient.Builder.
     *
     * @param strategies 策略
     * @param httpClient http客户端
     * @return WebClient
     */
    @Bean
    public WebClient.Builder webClientBuilder(ExchangeStrategies strategies, HttpClient httpClient) {
        final WebClient.Builder webClientBuilder = createWebClientBuilder(strategies, httpClient);
        log.info("WebClient.Builder={}", webClientBuilder);
        return webClientBuilder;
    }

    /**
     * 创建负载均衡 WebClient.
     *
     * @param loadBalancedWebClientBuilder loadBalancedWebClientBuilder
     * @return WebClient
     */
    @Bean("loadBalancedWebClient")
    public WebClient loadBalancedWebClient(@LoadBalanced WebClient.Builder loadBalancedWebClientBuilder) {
        final WebClient webClient = loadBalancedWebClientBuilder.build();
        log.info("LoadBalanced WebClient={}", webClient);
        return webClient;
    }

    /**
     * 创建 WebClient.
     *
     * @param webClientBuilder webClientBuilder
     * @return WebClient
     */
    @Primary
    @Bean
    public WebClient webClient(WebClient.Builder webClientBuilder) {
        final WebClient webClient = webClientBuilder.build();
        log.info("WebClient={}", webClient);
        return webClient;
    }

    private WebClient.Builder createWebClientBuilder(ExchangeStrategies strategies, HttpClient httpClient) {
        WebClient.Builder builder = WebClient.builder()
            .exchangeStrategies(strategies)
            .clientConnector(new ReactorClientHttpConnector(httpClient));
        // Observation（最外层）
        if (Boolean.TRUE.equals(property.getEnabledObservation())
            && observationRegistry != null) {
            builder.observationRegistry(observationRegistry);
        }
        builder.filter(tccFilter())
            .filter(signFilter());
        // CircuitBreaker（在 retry 之外）
        if (Boolean.TRUE.equals(property.getEnabledCircuitBreaker())
            && circuitBreakerRegistry != null) {
            builder.filter(circuitBreakerFilter());
        }
        builder.filter(retryFilter());
        return builder;
    }

    private ConnectionProvider buildConnectionPool() {
        final WebClientConfig cfg = property.getWeb();
        final Pool pool = cfg.getPool();

        final ConnectionProvider build = ConnectionProvider
            .builder(pool.getName())
            .maxConnections(Optional.of(pool.getMaxConnections()).orElse(1000))
            .pendingAcquireMaxCount(Optional.of(pool.getPendingAcquireMaxCount()).orElse(2000))
            .pendingAcquireTimeout(Optional.of(pool.getPendingAcquireTimeout()).orElse(Duration.ofMillis(2)))
            .maxIdleTime(Optional.of(pool.getMaxIdleTime()).orElse(Duration.ofMillis(30)))
            .maxLifeTime(Optional.of(pool.getMaxLifeTime()).orElse(Duration.ofMillis(5)))
            .evictInBackground(Optional.of(pool.getEvictionInterval()).orElse(Duration.ofMillis(30)))
            .metrics(pool.getMetrics())
            .build();

        log.info("ConnectionProvider={}", build);
        return build;
    }

    private HttpClient configureProxy(HttpClient client) {
        final WebClientConfig cfg = property.getWeb();
        final WebClientConfig.Proxy proxy = cfg.getProxy();

        if (proxy == null || Boolean.FALSE.equals(proxy.getEnabled())) {
            return client;
        }

        return client.proxy(ps -> {
            ProxyProvider.Builder b = ps.type(proxy.getType()).host(proxy.getHost());
            PropertyMapper m = PropertyMapper.get();
            m.from(proxy::getPort).whenNonNull().to(b::port);
            m.from(proxy::getUsername).whenHasText().to(b::username);
            m.from(proxy::getPassword).whenHasText().to(pw -> b.password(s -> pw));
            m.from(proxy::getNonProxyHostsPattern).whenHasText().to(b::nonProxyHosts);
        });
    }

    private ExchangeFilterFunction circuitBreakerFilter() {
        return (request, next) -> {
            CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(HttpClientTypeEnum.WEB_CLIENT.getDesc());

            return Mono.defer(() ->
                next.exchange(request)
            ).transformDeferred(
                CircuitBreakerOperator.of(circuitBreaker)
            );
        };
    }

    public ExchangeFilterFunction tccFilter() {
        return (request, next) ->
            Mono.deferContextual(ctx -> {
                if (ctx.hasKey(HeaderConstant.X_TCC_XID)) {
                    return next.exchange(
                        ClientRequest.from(request)
                            .header(HeaderConstant.X_TCC_XID, ctx.get(HeaderConstant.X_TCC_XID))
                            .build());
                }
                return next.exchange(request);
            });
    }

    private ExchangeFilterFunction signFilter() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            try {
                final URI uri = clientRequest.url();
                final String host = uri.getHost();
                final String path = uri.getPath();
                log.info("WebClient request path={} host={}", path, host);

                // 加载签名配置
                final ApiSignGenerateDTO entity = this.apiProtectionProvider.buildSignGenerate(path, clientRequest.method().name());
                if (entity == null) {
                    return Mono.just(clientRequest);
                }

                return Mono.just(
                    ClientRequest.from(clientRequest)
                        .header(ApiSignConstant.X_TIMESTAMP, entity.getTimestamp())
                        .header(ApiSignConstant.X_NONCE, entity.getNonce())
                        .header(ApiSignConstant.X_PATH, entity.getPath())
                        .header(ApiSignConstant.X_METHOD, entity.getMethod())
                        .header(ApiSignConstant.X_SIGN, ApiSignUtil.generateSign(entity))
                        .header(ApiSignConstant.APPLICATION, entity.getApplication())
                        .build()
                );
            } catch (Exception e) {
                log.error("WebClient sign error", e);
                return Mono.error(e);
            }
        });
    }

    private ExchangeFilterFunction retryFilter() {
        final WebClientConfig cfg = property.getWeb();
        final WebClientConfig.Retry retry = cfg.getRetry();

        // 开关判断
        if (retry == null || Boolean.FALSE.equals(retry.getEnabled())) {
            return ExchangeFilterFunction.ofRequestProcessor(Mono::just);
        }

        return (request, next) ->
            next.exchange(request)
                // 超时（必须有）
                .timeout(Optional.of(cfg.getReadTimeout()).orElse(Duration.ofMillis(30)))
                .retryWhen(
                    Retry.backoff(3, Duration.ofMillis(200))
                        .maxBackoff(Duration.ofSeconds(2))
                        .jitter(0.5)
                        .filter(this::isRetryable)
                        .transientErrors(true)
                        .doBeforeRetry(signal -> {
                            Throwable ex = signal.failure();

                            log.warn(
                                "[Webclient Retry] retry={}, exception={}: {}",
                                signal.totalRetries() + 1,
                                ex.getClass().getSimpleName(),
                                ex.getMessage()
                            );
                        })
                        .onRetryExhaustedThrow((spec, signal) -> signal.failure())
                );
    }

    /**
     * 判定是否可重试：仅网络、IO、超时异常.
     */
    private boolean isRetryable(Throwable ex) {
        if (ex instanceof ReadTimeoutException
            || ex instanceof WriteTimeoutException
            || ex instanceof ConnectException
            || ex instanceof ConnectTimeoutException
            || ex instanceof SocketTimeoutException
            || ex instanceof TimeoutException
            || ex instanceof PrematureCloseException) {
            return true;
        }

        // 谨慎处理 IOException
        if (ex instanceof IOException) {
            String msg = ex.getMessage();
            return msg != null
                && (msg.contains("Connection reset")
                || msg.contains("Broken pipe")
                || msg.contains("connection aborted"));
        }

        return false;
    }
}

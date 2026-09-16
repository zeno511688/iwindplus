/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.gateway.interfaces.filter;

import cn.hutool.core.collection.CollUtil;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.iwindplus.base.domain.constant.CommonConstant.SymbolConstant;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.redis.executor.RedissonExecutor;
import com.iwindplus.base.util.CryptoUtil;
import com.iwindplus.gateway.interfaces.filter.base.BaseGatewayFilter;
import com.iwindplus.gateway.infrastructure.client.MgtClient;
import com.iwindplus.gateway.infrastructure.client.vo.ServerApiVO;
import com.iwindplus.gateway.infrastructure.configuration.RateLimiterProperty;
import com.iwindplus.gateway.infrastructure.constant.GatewayCacheConstant;
import com.iwindplus.gateway.infrastructure.constant.GatewayFilterConstant;
import com.iwindplus.gateway.infrastructure.support.GatewayUtil;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.context.event.EventListener;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * 限流过滤器.
 *
 * @author zengdegui
 * @since 2020/4/15
 */
@Slf4j
@Component
public class RateLimiterFilter extends BaseGatewayFilter {

    private final RateLimiterProperty property;
    private final RedissonExecutor redissonExecutor;
    private final MgtClient mgtClient;
    private final AsyncLoadingCache<String, List<ServerApiVO>> cache;
    private final Map<String, Long> localApiRateCache = new ConcurrentHashMap<>(16);

    public RateLimiterFilter(
        RateLimiterProperty property,
        RedissonExecutor redissonExecutor,
        MgtClient mgtClient) {
        this.property = property;
        this.redissonExecutor = redissonExecutor;
        this.mgtClient = mgtClient;
        this.cache = Caffeine.newBuilder()
            .maximumSize(property.getMaxSize())
            .expireAfterWrite(property.getCacheTimeout())
            .refreshAfterWrite(property.getCacheRefresh())
            .recordStats()
            .buildAsync((key, executor) -> loadRateLimiterList().toFuture());
    }

    @Override
    public int getOrder() {
        return GatewayFilterConstant.RATE_LIMITER_ORDER;
    }

    @Override
    protected boolean shouldSkip(ServerWebExchange exchange) {
        return GatewayUtil.shouldSkip(
            exchange,
            property.getEnabled()
        );
    }

    @Override
    protected Mono<Void> filterInternal(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String method = request.getMethod().name();
        String path = request.getPath().value();

        log.info("{} method={}, path={}", getClass().getSimpleName(), method, path);

        return prepareLimiterConfig(method, path)
            .flatMap(tuple ->
                redissonExecutor.rateLimiter().executeReactive(
                    tuple.getT1(),
                    property.getRateType(),
                    tuple.getT2(),
                    property.getRateInterval(),
                    () -> chain.filter(exchange)
                )
            );
    }

    private Mono<Tuple2<String, Long>> prepareLimiterConfig(String requestMethod, String path) {
        String key = GatewayCacheConstant.RATE_LIMITER_KEY + CryptoUtil.encryptBySm3(path);
        // 路径限流模式：使用路径作为key，全局速率
        if (Boolean.TRUE.equals(property.getEnabledLimitPath())) {
            return Mono.just(Tuples.of(key, property.getRate()));
        }

        // API配置限流模式
        return getRateFromApiConfig(requestMethod, path)
            .map(rate -> Tuples.of(key, rate));
    }

    private Mono<Long> getRateFromApiConfig(String requestMethod, String path) {
        String cacheKey = requestMethod + SymbolConstant.COLON + path;

        // 先查本地缓存
        Long cached = localApiRateCache.get(cacheKey);
        if (cached != null) {
            return Mono.just(cached);
        }

        return getRateLimiterList()
            .map(list -> list.stream()
                .filter(r -> requestMethod.equals(r.getRequestMethod())
                    && path.equals(r.getApiUrl()))
                .findFirst()
                .map(ServerApiVO::getRate)
                .orElse(property.getRate())
            )
            .doOnNext(rate -> localApiRateCache.put(cacheKey, rate));
    }

    private Mono<List<ServerApiVO>> getRateLimiterList() {
        return Mono.fromFuture(() -> cache.get(GatewayCacheConstant.CACHE_KEY_ALL))
            .onErrorResume(e -> {
                // 降级使用同步缓存
                List<ServerApiVO> fallback = cache.synchronous().get(GatewayCacheConstant.CACHE_KEY_ALL);
                if (CollUtil.isNotEmpty(fallback)) {
                    log.warn("限流配置加载失败，使用缓存降级", e);
                    return Mono.just(fallback);
                }
                log.warn("限流配置加载失败，返回空集合", e);
                return Mono.just(List.of());
            });
    }

    private Mono<List<ServerApiVO>> loadRateLimiterList() {
        return mgtClient.listServerApi()
            .defaultIfEmpty(List.of())
            .doOnNext(list -> {
                log.info("限流配置加载完成，条目数={}", list.size());
                localApiRateCache.clear();
            })
            .doOnError(ex -> {
                if (ex instanceof BizException bizEx) {
                    throw bizEx;
                } else {
                    log.error("限流配置加载失败", ex);
                }
            });
    }

    @EventListener(ApplicationReadyEvent.class)
    public void run() {
        log.info("开始预热加载限流配置");
        getRateLimiterList().subscribe();
    }
}

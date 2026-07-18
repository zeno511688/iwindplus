/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.gateway.server.filter;

import cn.hutool.core.collection.CollUtil;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.iwindplus.base.domain.constant.CommonConstant.SymbolConstant;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.redis.service.RedissonService;
import com.iwindplus.base.util.CryptoUtil;
import com.iwindplus.gateway.server.client.MgtClient;
import com.iwindplus.gateway.server.domain.constant.GatewayConstant;
import com.iwindplus.gateway.server.domain.constant.GatewayConstant.CacheContextConstant;
import com.iwindplus.gateway.server.domain.constant.GatewayConstant.FilterConstant;
import com.iwindplus.gateway.server.domain.property.GatewayProperty;
import com.iwindplus.gateway.server.domain.property.GatewayProperty.RateLimiterConfig;
import com.iwindplus.gateway.server.filter.base.BaseGatewayFilter;
import com.iwindplus.gateway.server.util.GatewayUtil;
import com.iwindplus.mgt.domain.vo.system.ServerApiBaseVO;
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

    private final GatewayProperty property;
    private final RedissonService redissonService;
    private final MgtClient mgtClient;
    private final AsyncLoadingCache<String, List<ServerApiBaseVO>> cache;
    private final Map<String, Long> localApiRateCache = new ConcurrentHashMap<>(16);

    public RateLimiterFilter(GatewayProperty property, RedissonService redissonService, MgtClient mgtClient) {
        this.property = property;
        this.redissonService = redissonService;
        this.mgtClient = mgtClient;
        this.cache = Caffeine.newBuilder()
            .maximumSize(property.getRateLimiter().getMaxSize())
            .expireAfterWrite(property.getRateLimiter().getCacheTimeout())
            .refreshAfterWrite(property.getRateLimiter().getCacheRefresh())
            .recordStats()
            .buildAsync((key, executor) -> loadRateLimiterList().toFuture());
    }

    @Override
    public int getOrder() {
        return FilterConstant.RATE_LIMITER_ORDER;
    }

    @Override
    protected boolean shouldSkip(ServerWebExchange exchange) {
        return GatewayUtil.shouldSkip(
            exchange,
            property.getRateLimiter().getEnabled()
        );
    }

    @Override
    protected Mono<Void> filterInternal(ServerWebExchange exchange, GatewayFilterChain chain) {
        final RateLimiterConfig cfg = property.getRateLimiter();

        ServerHttpRequest request = exchange.getRequest();
        String method = request.getMethod().name();
        String path = request.getPath().value();

        log.info("{} method={}, path={}", getClass().getSimpleName(), method, path);

        return prepareLimiterConfig(method, path)
            .flatMap(tuple ->
                redissonService.rateLimiter().executeReactive(
                    tuple.getT1(),
                    cfg.getRateType(),
                    tuple.getT2(),
                    cfg.getRateInterval(),
                    () -> chain.filter(exchange)
                )
            );
    }

    private Mono<Tuple2<String, Long>> prepareLimiterConfig(String requestMethod, String path) {
        final RateLimiterConfig rateLimiterCfg = property.getRateLimiter();

        String key = GatewayConstant.RATE_LIMITER_KEY + CryptoUtil.encryptBySm3(path);
        // 路径限流模式：使用路径作为key，全局速率
        if (Boolean.TRUE.equals(rateLimiterCfg.getEnabledLimitPath())) {
            return Mono.just(Tuples.of(key, rateLimiterCfg.getRate()));
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
                .map(ServerApiBaseVO::getRate)
                .orElse(property.getRateLimiter().getRate())
            )
            .doOnNext(rate -> localApiRateCache.put(cacheKey, rate));
    }

    private Mono<List<ServerApiBaseVO>> getRateLimiterList() {
        return Mono.fromFuture(() -> cache.get(CacheContextConstant.CACHE_KEY_ALL))
            .onErrorResume(e -> {
                // 降级使用同步缓存
                List<ServerApiBaseVO> fallback = cache.synchronous().get(CacheContextConstant.CACHE_KEY_ALL);
                if (CollUtil.isNotEmpty(fallback)) {
                    log.warn("限流配置加载失败，使用缓存降级", e);
                    return Mono.just(fallback);
                }
                log.warn("限流配置加载失败，返回空集合", e);
                return Mono.just(List.of());
            });
    }

    private Mono<List<ServerApiBaseVO>> loadRateLimiterList() {
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

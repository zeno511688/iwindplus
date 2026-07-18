/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.gateway.server.filter;

import cn.hutool.core.collection.CollUtil;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.iwindplus.base.domain.dto.MessageBaseDTO;
import com.iwindplus.base.domain.enums.BaseEnum;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.enums.OperateTypeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.util.PathMatchUtil;
import com.iwindplus.base.util.ReactorUtil;
import com.iwindplus.gateway.server.client.MgtClient;
import com.iwindplus.gateway.server.domain.constant.GatewayConstant.CacheContextConstant;
import com.iwindplus.gateway.server.domain.constant.GatewayConstant.FilterConstant;
import com.iwindplus.gateway.server.domain.constant.GatewayConstant.ServerWebExchangeContextConstant;
import com.iwindplus.gateway.server.domain.property.GatewayProperty;
import com.iwindplus.gateway.server.filter.base.BaseGatewayFilter;
import com.iwindplus.mgt.domain.dto.system.ApiWhiteListChangeDTO;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * API白名单过滤器.
 *
 * @author zengdegui
 * @since 2020/4/15
 */
@Slf4j
@Component
public class ApiWhiteListFilter extends BaseGatewayFilter {

    private static final List<String> DEFAULT_IGNORED_APIS = List.of(
        "/api/auth/oauth2/token",
        "/api/auth/oauth2/authorize",
        "/api/auth/oauth2/introspect",
        "/api/auth/oauth2/revoke",
        "/api/auth/oauth2/jwks",
        "/api/auth/oauth2/consent",
        "/api/**/**/api-docs",
        "/doc.html",
        "/swagger-ui.html",
        "/v3/api-docs",
        "/v3/api-docs/**",
        "/webjars/**",
        "/swagger-resources/**",
        "/swagger-ui/**",
        "/actuator/**",
        "/favicon.ico",
        "/api/imWs/ws"
    );

    private final GatewayProperty property;
    private final MgtClient mgtClient;
    private final AsyncLoadingCache<String, Set<String>> cache;

    public ApiWhiteListFilter(GatewayProperty property, MgtClient mgtClient) {
        this.property = property;
        this.mgtClient = mgtClient;
        this.cache = Caffeine.newBuilder()
            .maximumSize(property.getApiWhiteList().getMaxSize())
            .expireAfterWrite(property.getApiWhiteList().getCacheTimeout())
            .refreshAfterWrite(property.getApiWhiteList().getCacheRefresh())
            .recordStats()
            .buildAsync((key, executor) -> loadApiWhiteList().toFuture());
    }

    @Override
    public int getOrder() {
        return FilterConstant.FILTER_API_WHITE_LIST_ORDER;
    }

    @Override
    protected boolean shouldSkip(ServerWebExchange exchange) {
        return Boolean.FALSE.equals(property.getApiWhiteList().getEnabled());
    }

    @Override
    protected Mono<ServerWebExchange> before(ServerWebExchange exchange) {

        final String path = exchange.getRequest().getPath().value();

        // 1. 默认白名单（同步快速路径）
        if (PathMatchUtil.match(DEFAULT_IGNORED_APIS, path)) {
            markWhited(exchange);
            return Mono.just(exchange);
        }

        // 2. 动态白名单（异步）
        return getApiWhiteList()
            .doOnNext(set -> {
                if (PathMatchUtil.match(set, path)) {
                    markWhited(exchange);
                }
            })
            .thenReturn(exchange);
    }

    private void markWhited(ServerWebExchange exchange) {
        ReactorUtil.setAttribute(exchange, ServerWebExchangeContextConstant.WHITED_FLAG, true);
    }

    private Mono<Set<String>> getApiWhiteList() {
        return Mono.fromFuture(() -> cache.get(CacheContextConstant.CACHE_KEY_ALL))
            .defaultIfEmpty(Set.of())
            .onErrorResume(e -> {
                // 降级使用同步缓存
                Set<String> fallback = cache.synchronous().getIfPresent(CacheContextConstant.CACHE_KEY_ALL);
                if (CollUtil.isNotEmpty(fallback)) {
                    log.warn("获取 API 白名单失败，使用缓存降级", e);
                    return Mono.just(fallback);
                }
                log.warn("获取 API 白名单失败，返回空集合", e);
                return Mono.just(Set.of());
            });
    }

    private Mono<Set<String>> loadApiWhiteList() {
        return mgtClient.listApi()
            .defaultIfEmpty(List.of())
            .map(remoteList -> {
                Set<String> merged = new HashSet<>(remoteList);
                List<String> local = property.getApiWhiteList().getIgnoredApi();
                if (CollUtil.isNotEmpty(local)) {
                    merged.addAll(local);
                }
                return Set.copyOf(merged);
            })
            .doOnSuccess(set -> log.info("API 白名单加载完成，条目数={}", set.size()))
            .doOnError(ex -> {
                if (ex instanceof BizException bizEx) {
                    throw bizEx;
                } else {
                    log.error("API 白名单加载失败", ex);
                }
            });
    }

    /**
     * 刷新白名单.
     *
     * @param message 消息
     */
    public void refreshWhiteList(MessageBaseDTO<ApiWhiteListChangeDTO> message) {
        if (message == null || message.getOperateType() == null || message.getData() == null) {
            return;
        }

        OperateTypeEnum op = BaseEnum.fromValue(message.getOperateType(), OperateTypeEnum.class);
        ApiWhiteListChangeDTO data = message.getData();

        cache.synchronous().asMap().compute(CacheContextConstant.CACHE_KEY_ALL, (key, old) -> {
            Set<String> newSet = old == null ? new HashSet<>() : new HashSet<>(old);
            switch (op) {
                case ADD:
                    Optional.ofNullable(data.getNewApiUrl()).ifPresent(newSet::addAll);
                    break;
                case DELETE:
                    Optional.ofNullable(data.getOldApiUrl()).ifPresent(newSet::removeAll);
                    break;
                case MODIFY:
                    Optional.ofNullable(data.getOldApiUrl()).ifPresent(newSet::removeAll);
                    Optional.ofNullable(data.getNewApiUrl()).ifPresent(newSet::addAll);
                    break;
                default:
                    throw new BizException(BizCodeEnum.UNSUPPORTED_TYPE, message.getOperateType());
            }
            log.info("API 白名单刷新完成，条目数={}", newSet.size());
            return newSet;
        });
    }

    @EventListener(ApplicationReadyEvent.class)
    public void run() {
        log.info("开始预热加载API 白名单");
        getApiWhiteList().subscribe();
    }

}

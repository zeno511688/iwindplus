/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.gateway.server.filter.base;

import com.iwindplus.gateway.server.util.GatewayUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 基础通用网关过滤器.
 *
 * @author zengdegui
 * @since 2026/04/29 00:39
 */
public abstract class BaseGatewayFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 统一打点
        GatewayUtil.logTiming(exchange, this.getClass().getSimpleName());

        if (shouldSkip(exchange)) {
            return chain.filter(exchange);
        }

        return before(exchange)
            .flatMap(newExchange ->
                filterInternal(newExchange, chain)
                    // after 保证一定执行（成功 / 失败 / cancel）
                    .doFinally(signal -> afterFinally(newExchange))
            )
            .onErrorResume(e -> onError(exchange, e));
    }

    /**
     * 子类实现（可完全控制 chain）
     */
    protected Mono<Void> filterInternal(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange);
    }

    /**
     * 跳过过滤器
     */
    protected boolean shouldSkip(ServerWebExchange exchange) {
        return false;
    }

    /**
     * 创建新的 exchange
     */
    protected Mono<ServerWebExchange> before(ServerWebExchange exchange) {
        return Mono.just(exchange);
    }

    /**
     * 释放 exchange（关键）
     */
    protected void afterFinally(ServerWebExchange exchange) {
        // default empty
    }

    /**
     * 错误处理
     */
    protected Mono<Void> onError(ServerWebExchange exchange, Throwable e) {
        return Mono.error(e);
    }
}

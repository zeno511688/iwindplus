/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.gateway.server.filter.base;

import com.iwindplus.base.domain.constant.CommonConstant.HeaderConstant;
import com.iwindplus.gateway.server.util.GatewayUtil;
import java.util.UUID;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.util.StringUtils;
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
        // 统一生成并透传请求唯一标识，保证同一请求链路使用相同的采样 Key。
        ServerWebExchange requestExchange = ensureRequestId(exchange);

        // 统一打点
        GatewayUtil.logTiming(requestExchange, this.getClass().getSimpleName());

        if (shouldSkip(requestExchange)) {
            return chain.filter(requestExchange);
        }

        return before(requestExchange)
            .flatMap(newExchange ->
                filterInternal(newExchange, chain)
                    // after 保证一定执行（成功 / 失败 / cancel）
                    .doFinally(signal -> afterFinally(newExchange))
            )
            .onErrorResume(e -> onError(requestExchange, e));
    }

    /**
     * 确保请求包含唯一标识，并将其写入下游请求头。
     *
     * @param exchange 当前 exchange
     * @return 包含请求标识的 exchange
     */
    private ServerWebExchange ensureRequestId(ServerWebExchange exchange) {
        String requestId = exchange.getRequest().getHeaders().getFirst(HeaderConstant.X_REQUESTED_ID);
        if (StringUtils.hasText(requestId)) {
            return exchange;
        }

        String generatedRequestId = UUID.randomUUID().toString();
        return exchange.mutate()
            .request(builder -> builder.header(HeaderConstant.X_REQUESTED_ID, generatedRequestId))
            .build();
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

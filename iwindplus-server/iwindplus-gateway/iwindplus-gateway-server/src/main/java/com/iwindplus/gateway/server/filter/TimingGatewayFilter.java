/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.gateway.server.filter;

import com.iwindplus.base.domain.constant.CommonConstant.HeaderConstant;
import com.iwindplus.base.util.ReactorUtil;
import com.iwindplus.gateway.server.domain.constant.GatewayConstant.FilterConstant;
import com.iwindplus.gateway.server.domain.constant.GatewayConstant.ServerWebExchangeContextConstant;
import com.iwindplus.gateway.server.util.GatewayUtil;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 全局耗时统计过滤器.
 *
 * @author zengdegui
 * @since 2026/01/28 22:12
 */
@Slf4j
@Component
public class TimingGatewayFilter implements Ordered, GlobalFilter {

    @Override
    public int getOrder() {
        return FilterConstant.FILTER_TIMING_GATEWAY_ORDER;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 统一生成并透传请求唯一标识，保证同一请求链路使用相同的采样 Key。
        ServerWebExchange requestExchange = ensureRequestId(exchange);

        long start = System.currentTimeMillis();
        // 设置请求开始时间
        ReactorUtil.setAttribute(requestExchange, ServerWebExchangeContextConstant.REQUEST_TIME, start);

        return chain.filter(requestExchange)
            .doFinally(signal -> {
                // 总耗时
                final long cost = System.currentTimeMillis() - start;
                log.info("[GatewayTotalTiming] cost={}ms", cost);

                GatewayUtil.clearRequestParams(requestExchange);
            });
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
}

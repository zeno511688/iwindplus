/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.gateway.server.filter;

import com.iwindplus.base.util.ReactorUtil;
import com.iwindplus.gateway.server.domain.constant.GatewayConstant.FilterConstant;
import com.iwindplus.gateway.server.domain.constant.GatewayConstant.ServerWebExchangeContextConstant;
import com.iwindplus.gateway.server.util.GatewayUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
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
        long start = System.currentTimeMillis();
        // 设置请求开始时间
        ReactorUtil.setAttribute(exchange, ServerWebExchangeContextConstant.REQUEST_TIME, start);

        return chain.filter(exchange)
            .doFinally(signal -> {
                // 总耗时
                long cost = System.currentTimeMillis() - start;
                log.info("[GatewayTotalTiming] cost={}ms", cost);

                GatewayUtil.clearRequestParams(exchange);
            });
    }

}

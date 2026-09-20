/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.gateway.interfaces.filter;

import cn.hutool.core.util.IdUtil;
import com.iwindplus.base.domain.constant.CommonConstant.HeaderConstant;
import com.iwindplus.base.util.ReactorUtil;
import com.iwindplus.gateway.infrastructure.constant.GatewayFilterConstant;
import com.iwindplus.gateway.infrastructure.constant.GatewayWebExchangeConstant;
import com.iwindplus.gateway.infrastructure.support.GatewayUtil;
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
        return GatewayFilterConstant.FILTER_TIMING_GATEWAY_ORDER;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        final long start = System.currentTimeMillis();
        // 统一生成并透传请求唯一标识，保证同一请求链路使用相同的采样 Key。
        final ServerWebExchange requestExchange = ensureRequestId(exchange);
        // 必须写入最终传递给下游的 exchange，避免 mutate().build() 后计时属性不一致。
        ReactorUtil.setAttribute(requestExchange, GatewayWebExchangeConstant.REQUEST_TIME, start);

        return chain.filter(requestExchange)
            .doFinally(signal -> {
                // 总耗时
                final long cost = System.currentTimeMillis() - start;
                log.info("[GatewayTotalTiming] execute cost={}ms", cost);

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

        String generatedRequestId = IdUtil.simpleUUID();
        return exchange.mutate()
            .request(builder -> builder.header(HeaderConstant.X_REQUESTED_ID, generatedRequestId))
            .build();
    }
}

/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */
package com.iwindplus.gateway.server.filter;

import com.iwindplus.base.util.ReactorUtil;
import com.iwindplus.gateway.server.domain.constant.GatewayConstant.FilterConstant;
import com.iwindplus.gateway.server.filter.base.BaseGatewayFilter;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 请求体过滤器.
 *
 * @author zengdegui
 * @since 2025/07/28 00:31
 */
@Slf4j
@Component
public class RequestBodyFilter extends BaseGatewayFilter {

    @Override
    public int getOrder() {
        return FilterConstant.FILTER_REQUEST_BODY_ORDER;
    }

    @Override
    protected Mono<Void> filterInternal(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactorUtil.readRequestBody(exchange, chain::filter)
            .doOnNext(collector -> Optional.ofNullable(collector.getRequestBody())
                .ifPresent(body -> log.info("收到 body长度={}", body.length()))
            ).then();
    }
}

/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */
package com.iwindplus.gateway.interfaces.filter;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.domain.constant.CommonConstant.HeaderConstant;
import com.iwindplus.base.domain.constant.CommonConstant.NumberConstant;
import com.iwindplus.base.util.HttpsUtil;
import com.iwindplus.base.util.JacksonUtil;
import com.iwindplus.base.util.PathMatchUtil;
import com.iwindplus.base.util.ReactorUtil;
import com.iwindplus.base.util.domain.dto.ReactorResponseDTO;
import com.iwindplus.gateway.infrastructure.client.dto.GatewayLogDTO;
import com.iwindplus.gateway.infrastructure.configuration.LogProperty;
import com.iwindplus.gateway.infrastructure.constant.GatewayFilterConstant;
import com.iwindplus.gateway.infrastructure.support.GatewayUtil;
import com.iwindplus.gateway.interfaces.filter.base.BaseGatewayFilter;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * 日志过滤器.
 *
 * @author zengdegui
 * @since 2020/4/15
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LogFilter extends BaseGatewayFilter {

    private final LogProperty property;

    @Override
    public int getOrder() {
        return GatewayFilterConstant.FILTER_LOG_ORDER;
    }

    @Override
    protected boolean shouldSkip(ServerWebExchange exchange) {
        if (GatewayUtil.shouldSkip(exchange, property.getEnabled())) {
            return true;
        }

        // 路径忽略
        final String path = exchange.getRequest().getPath().value();
        if (hasIgnoredApi(path)) {
            return true;
        }

        // 采样率
        String requestId = exchange.getRequest().getHeaders().getFirst(HeaderConstant.X_REQUESTED_ID);
        return !HttpsUtil.checkSampleRateInRange(property.getSampleRate(), requestId);
    }

    @Override
    protected Mono<Void> filterInternal(ServerWebExchange exchange, GatewayFilterChain chain) {
        final boolean needRespBody = Boolean.TRUE.equals(property.getEnabledResponseBody());

        // 冷流 + cache（避免重复构建）
        Mono<GatewayLogDTO> logDto = Mono.defer(() ->
                GatewayUtil.buildGatewayLog(exchange, property))
            .cache();

        Mono<Void> main;

        if (needRespBody) {
            main = ReactorUtil.readResponseBody(exchange, chain::filter)
                .flatMap(collector -> buildResponse(property, collector, logDto))
                .then();
        } else {
            main = chain.filter(exchange);
        }

        return main.then(
            logDto.publishOn(Schedulers.boundedElastic())
                .doOnNext(GatewayUtil::asyncPublishGatewayLog)
                .onErrorResume(e -> {
                    log.warn("async publish gateway log error={}", e.getMessage());
                    return Mono.empty();
                })
        ).then();
    }

    private boolean hasIgnoredApi(String requestPath) {
        List<String> ignored = property.getIgnoredApi();
        if (CollUtil.isNotEmpty(ignored)) {
            List<String> patterns = ignored.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
            return PathMatchUtil.match(patterns, requestPath);
        }
        return false;
    }

    /**
     * 构建响应日志（函数式写法）
     */
    private Mono<GatewayLogDTO> buildResponse(
        LogProperty cfg,
        ReactorResponseDTO collector,
        Mono<GatewayLogDTO> logDto) {

        return logDto.map(entity -> {

            entity.setResponseStatus(collector.getResponseStatus());

            if (Boolean.TRUE.equals(cfg.getEnabledResponseHeader())) {
                entity.setResponseHeaders(
                    JacksonUtil.toJsonStr(collector.getResponseHeaders())
                );
            }

            if (Boolean.TRUE.equals(cfg.getEnabledResponseBody())) {
                String body = collector.getResponseBody();
                if (CharSequenceUtil.isNotBlank(body)) {
                    entity.setResponseBody(
                        CharSequenceUtil.maxLength(
                            body,
                            cfg.getLimitResponseBody()
                                * NumberConstant.NUMBER_ONE_THOUSAND_TWENTY_FOUR
                        )
                    );
                }
            }

            return entity;
        });
    }
}
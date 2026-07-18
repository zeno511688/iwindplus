/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */
package com.iwindplus.gateway.server.filter;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.domain.constant.CommonConstant.NumberConstant;
import com.iwindplus.base.util.HttpsUtil;
import com.iwindplus.base.util.JacksonUtil;
import com.iwindplus.base.util.PathMatchUtil;
import com.iwindplus.base.util.ReactorUtil;
import com.iwindplus.base.util.domain.dto.ReactorResponseDTO;
import com.iwindplus.gateway.server.domain.constant.GatewayConstant.FilterConstant;
import com.iwindplus.gateway.server.domain.property.GatewayProperty;
import com.iwindplus.gateway.server.domain.property.GatewayProperty.LogConfig;
import com.iwindplus.gateway.server.filter.base.BaseGatewayFilter;
import com.iwindplus.gateway.server.util.GatewayUtil;
import com.iwindplus.log.domain.dto.GatewayLogDTO;
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

    private final GatewayProperty property;

    @Override
    public int getOrder() {
        return FilterConstant.FILTER_LOG_ORDER;
    }

    @Override
    protected boolean shouldSkip(ServerWebExchange exchange) {
        final LogConfig cfg = property.getLog();

        if (GatewayUtil.shouldSkip(exchange, cfg.getEnabled())) {
            return true;
        }

        // 路径忽略
        final String path = exchange.getRequest().getPath().value();
        if (hasIgnoredApi(path)) {
            return true;
        }

        // 采样率
        return !HttpsUtil.checkSampleRateInRange(cfg.getSampleRate());
    }

    @Override
    protected Mono<Void> filterInternal(ServerWebExchange exchange, GatewayFilterChain chain) {
        final LogConfig cfg = property.getLog();
        final boolean needRespBody = Boolean.TRUE.equals(cfg.getEnabledResponseBody());

        // 冷流 + cache（避免重复构建）
        Mono<GatewayLogDTO> logDto = Mono.defer(() ->
                GatewayUtil.buildGatewayLog(exchange, cfg))
            .cache();

        Mono<Void> main;

        if (needRespBody) {
            main = ReactorUtil.readResponseBody(exchange, chain::filter)
                .flatMap(collector -> buildResponse(cfg, collector, logDto))
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
        List<String> ignored = property.getLog().getIgnoredApi();
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
        LogConfig cfg,
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
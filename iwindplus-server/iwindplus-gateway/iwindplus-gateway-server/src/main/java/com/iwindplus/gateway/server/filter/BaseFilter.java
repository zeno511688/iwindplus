/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.gateway.server.filter;

import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.domain.constant.CommonConstant.ApiSignConstant;
import com.iwindplus.base.domain.constant.CommonConstant.HeaderConstant;
import com.iwindplus.base.util.ReactorUtil;
import com.iwindplus.gateway.server.domain.constant.GatewayConstant.FilterConstant;
import com.iwindplus.gateway.server.domain.property.GatewayProperty;
import com.iwindplus.gateway.server.domain.property.GatewayProperty.BaseConfig;
import com.iwindplus.gateway.server.filter.base.BaseGatewayFilter;
import com.iwindplus.gateway.server.util.GatewayUtil;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 基础过滤器.
 *
 * @author zengdegui
 * @since 2020/4/15
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BaseFilter extends BaseGatewayFilter {

    private final GatewayProperty property;

    @Override
    public int getOrder() {
        return FilterConstant.FILTER_BASE_ORDER;
    }

    @Override
    protected boolean shouldSkip(ServerWebExchange exchange) {
        return GatewayUtil.shouldSkip(
            exchange,
            property.getBase().getEnabled()
        );
    }

    @Override
    protected Mono<ServerWebExchange> before(ServerWebExchange exchange) {
        final BaseConfig cfg = property.getBase();

        final Set<String> headersToRemove = Set.of(
            HeaderConstant.X_USER_INFO,
            HeaderConstant.X_FORWARDED_FOR,
            HeaderConstant.TRACE_PARENT,
            HeaderConstant.TRACE_ID,
            HeaderConstant.REAL_IP,
            HeaderConstant.X_REQUESTED_WITH,
            HeaderConstant.X_FORWARDED_PREFIX,
            ApiSignConstant.X_PATH
        );

        // 1. header 清洗
        ServerWebExchange mutated = ReactorUtil.removeHeaders(exchange, headersToRemove);

        // 2. cookie → token
        if (Boolean.TRUE.equals(cfg.getEnabledAuthCookie())) {
            mutated = ReactorUtil.buildAuthorizationByCookie(mutated);
        }

        ServerHttpRequest request = mutated.getRequest();

        // 3. 计算
        String prefix = GatewayUtil.getApiPrefix(mutated);
        String path = processPath(request.getPath().value(), prefix, cfg.getEnabledRelativePath());
        String lang = GatewayUtil.getLang(request);
        String realIp = GatewayUtil.getRealIp(mutated);

        log.debug("{} path={}, lang={}, realIp={}, prefix={}",
            getClass().getSimpleName(), path, lang, realIp, prefix);

        // 4. 注入 header
        ServerHttpRequest newReq = request.mutate()
            .headers(h -> {
                h.set(ApiSignConstant.X_PATH, path);
                h.set(HttpHeaders.ACCEPT_LANGUAGE, lang);
                h.set(HeaderConstant.REAL_IP, realIp);
                h.set(HeaderConstant.X_FORWARDED_PREFIX, prefix);
            })
            .build();

        return Mono.just(mutated.mutate().request(newReq).build());
    }

    private String processPath(String path, String prefix, boolean enabledRelativePath) {
        if (CharSequenceUtil.isNotBlank(prefix)
            && enabledRelativePath
            && path.startsWith(prefix)
            && path.length() > prefix.length()) {
            return path.substring(prefix.length());
        }
        return path;
    }
}

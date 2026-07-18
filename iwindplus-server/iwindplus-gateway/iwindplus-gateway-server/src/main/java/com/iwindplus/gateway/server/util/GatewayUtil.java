/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.gateway.server.util;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.iwindplus.base.domain.constant.CommonConstant;
import com.iwindplus.base.domain.constant.CommonConstant.HeaderConstant;
import com.iwindplus.base.domain.constant.CommonConstant.NetWorkConstant;
import com.iwindplus.base.domain.constant.CommonConstant.NumberConstant;
import com.iwindplus.base.domain.constant.CommonConstant.SymbolConstant;
import com.iwindplus.base.domain.constant.CommonConstant.SystemConstant;
import com.iwindplus.base.domain.context.UserContextHolder;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.UserBaseVO;
import com.iwindplus.base.util.DatesUtil;
import com.iwindplus.base.util.HttpsUtil;
import com.iwindplus.base.util.JacksonUtil;
import com.iwindplus.base.util.ReactorUtil;
import com.iwindplus.base.util.domain.dto.ReactorRequestDTO;
import com.iwindplus.gateway.server.domain.constant.GatewayConstant.ServerWebExchangeContextConstant;
import com.iwindplus.gateway.server.domain.property.GatewayProperty.LogConfig;
import com.iwindplus.gateway.server.support.GatewayLogPublisher;
import com.iwindplus.log.domain.dto.GatewayLogDTO;
import com.iwindplus.log.domain.dto.GatewayLogDTO.GatewayLogDTOBuilder;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.cloud.gateway.support.ipresolver.XForwardedRemoteAddressResolver;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Gateway 工具类
 *
 * @author zengdegui
 * @since 2024/05/22 22:22
 */
@Slf4j
public class GatewayUtil {

    private GatewayUtil() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 判断是否白名单（跳过）.
     *
     * @param exchange 请求
     * @param enabled  是否启用
     * @return boolean
     */
    public static boolean shouldSkip(ServerWebExchange exchange, Boolean enabled) {
        if (Boolean.FALSE.equals(enabled)) {
            return true;
        }

        Boolean whited = ReactorUtil.getAttribute(
            exchange,
            ServerWebExchangeContextConstant.WHITED_FLAG
        );

        return Boolean.TRUE.equals(whited);
    }

    /**
     * 清理请求参数.
     *
     * @param exchange 请求
     */
    public static void clearRequestParams(ServerWebExchange exchange) {
        final Map<String, Object> attributes = exchange.getAttributes();
        attributes.remove(ReactorUtil.REQUEST_BODY);
        attributes.remove(ReactorUtil.RESPONSE_BODY);
        attributes.remove(ServerWebExchangeContextConstant.REQUEST_TIME);
        attributes.remove(ServerWebExchangeContextConstant.WHITED_FLAG);
        attributes.remove(ServerWebExchangeContextConstant.USER_INFO);
    }

    /**
     * 计时（统计从开始过滤器到当前过滤器的执行时间，当前执行时间=当前过滤器打印的时间-上一个过滤器打印的时间）.
     *
     * @param exchange   请求
     * @param filterName 过滤器名称
     */
    public static void logTiming(ServerWebExchange exchange, String filterName) {
        final long start = ReactorUtil.getAttribute(exchange, ServerWebExchangeContextConstant.REQUEST_TIME);
        if (Objects.isNull(start)) {
            return;
        }

        long cost = System.currentTimeMillis() - start;
        log.info("{} execute cost={} ms",
            filterName, cost);
    }

    /**
     * 获取API前缀（路由）.
     *
     * @param exchange 请求
     * @return String
     */
    public static String getApiPrefix(ServerWebExchange exchange) {
        String remaining = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_PREDICATE_MATCHED_PATH_ATTR);
        if (CharSequenceUtil.isBlank(remaining)) {
            return SymbolConstant.EMPTY_STR;
        }

        return remaining.replace(SymbolConstant.BASE_PATH, SymbolConstant.EMPTY_STR)
            .replace(SymbolConstant.SLASH_ASTERISK, SymbolConstant.EMPTY_STR);
    }

    /**
     * 获取请求相对路径.
     *
     * @param exchange 请求
     * @return String
     */
    public static String getRelativePath(ServerWebExchange exchange) {
        final ServerHttpRequest request = exchange.getRequest();
        String requestPath = request.getPath().value();
        String apiPrefix = request.getHeaders().getFirst(HeaderConstant.X_FORWARDED_PREFIX);
        if (CharSequenceUtil.isBlank(apiPrefix)) {
            apiPrefix = GatewayUtil.getApiPrefix(exchange);
        }

        return CharSequenceUtil.isNotBlank(apiPrefix)
            ? requestPath.substring(apiPrefix.length())
            : requestPath;
    }

    /**
     * 获取真实ip.
     *
     * @param exchange 请求
     * @return String
     */
    public static String getRealIp(ServerWebExchange exchange) {
        XForwardedRemoteAddressResolver xForwardedRemoteAddressResolver = XForwardedRemoteAddressResolver.maxTrustedIndex(1);
        InetSocketAddress inetSocketAddress = xForwardedRemoteAddressResolver.resolve(exchange);
        return Optional.ofNullable(inetSocketAddress).map(InetSocketAddress::getAddress).map(InetAddress::getHostAddress)
            .orElse(Optional.ofNullable(exchange.getRequest().getRemoteAddress())
                .map(InetSocketAddress::getHostString)
                .orElse(SystemConstant.UNKNOWN));
    }

    /**
     * 获取语言.
     *
     * @param req 请求
     * @return String
     */
    public static String getLang(ServerHttpRequest req) {
        return Optional.ofNullable(req.getHeaders().getFirst(HttpHeaders.ACCEPT_LANGUAGE))
            .orElse(HttpsUtil.buildDefaultLanguage());
    }

    /**
     * 构建网关日志信息.
     *
     * @param exchange 请求
     * @param cfg      日志配置
     * @return Mono<GatewayLogDTO>
     */
    public static Mono<GatewayLogDTO> buildGatewayLog(ServerWebExchange exchange, LogConfig cfg) {
        final UserBaseVO user = ReactorUtil.getAttribute(exchange, ServerWebExchangeContextConstant.USER_INFO);
        final long requestTime = ReactorUtil.getAttribute(exchange, ServerWebExchangeContextConstant.REQUEST_TIME);
        final ServerHttpRequest request = exchange.getRequest();
        final HttpHeaders headers = request.getHeaders();

        GatewayLogDTOBuilder<?, ?> builder = GatewayLogDTO.builder()
            .requestId(headers.getFirst(HeaderConstant.X_REQUESTED_ID))
            .bizTraceId(MDC.get(HeaderConstant.TRACE_ID))
            .ip(headers.getFirst(HeaderConstant.REAL_IP))
            .requestSchema(request.getURI().getScheme())
            .requestPath(request.getPath().value())
            .requestMethod(request.getMethod().name())
            .requestTime(DatesUtil.parseDate(requestTime, DatePattern.NORM_DATETIME_MS_PATTERN));

        GatewayUtil.buildTargetServer(exchange, builder);
        GatewayUtil.buildUserInfo(builder, user);
        GatewayUtil.buildUserAgent(builder, headers.getFirst(HttpHeaders.USER_AGENT));

        // 2. 真正需要 RequestBody 时才去“读一次”，并且一直在 Mono 里
        return ReactorUtil.getRequestBodyByAttr(exchange)
            .filter(data -> cfg.getEnabledRequestHeader()
                || cfg.getEnabledRequestBody()
                || cfg.getEnabledRequestParam())
            .map(data -> buildRequestData(cfg, builder, data))
            .switchIfEmpty(Mono.<GatewayLogDTO>fromCallable(() -> builder.build()));
    }

    /**
     * 异步发布日志.
     *
     * @param entity 日志信息
     * @return void
     */
    public static void asyncPublishGatewayLog(GatewayLogDTO entity) {
        try {
            final long beginMillis = DatesUtil.parse(entity.getRequestTime(), DatePattern.NORM_DATETIME_MS_PATTERN).getTime();
            final long endMillis = System.currentTimeMillis();
            entity.setResponseTime(DatesUtil.parseDate(endMillis, DatePattern.NORM_DATETIME_MS_PATTERN));
            entity.setExecuteTime(endMillis - beginMillis);
            SpringUtil.getBean(GatewayLogPublisher.class).publish(entity);
        } catch (Exception e) {
            log.error("Failed to publish gateway log event", e);
        }
    }

    /**
     * 异步发布错误日志（记录异常日志）.
     *
     * @param exchange  请求
     * @param cfg       配置
     * @param exception 异常
     * @param <T>       泛型
     * @return Mono<T>
     */
    public static <T> Mono<T> asyncPublishErrorLog(ServerWebExchange exchange, LogConfig cfg, BizException exception) {
        if (!cfg.getEnabledError()) {
            return Mono.error(exception);
        }

        return GatewayUtil.buildGatewayLog(exchange, cfg)
            .publishOn(Schedulers.boundedElastic())
            .doOnNext(dto -> {
                dto.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                dto.setResponseErrorCode(exception.getBizCode());
                dto.setResponseErrorMessage(exception.getBizMessage());
                asyncPublishGatewayLog(dto);
            }).then(Mono.error(exception));
    }

    /**
     * 获取请求body和参数.
     *
     * @param exchange 请求
     * @return Mono<Map < String, Object>>
     */
    public static Mono<Map<String, Object>> getRequestBodyAndParam(ServerWebExchange exchange) {
        return Mono.defer(() -> {
            ServerHttpRequest request = exchange.getRequest();

            Map<String, Object> params = new HashMap<>(16);
            params.putAll(request.getQueryParams().toSingleValueMap());

            return ReactorUtil.getRequestBodyByAttr(exchange)
                .flatMap(dto -> {
                    String body = dto.getRequestBody();
                    if (CharSequenceUtil.isBlank(body)) {
                        return Mono.empty();
                    }
                    return Mono.just(body);
                })
                .map(body -> HttpsUtil.getByStr(body, true))
                .doOnNext(params::putAll)
                .thenReturn(params);
        });
    }

    private static void buildTargetServer(ServerWebExchange exchange, GatewayLogDTOBuilder<?, ?> builder) {
        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        if (route == null) {
            builder.targetServer(SystemConstant.UNKNOWN).requestSchema(SystemConstant.UNKNOWN);
            return;
        }

        URI uri = route.getUri();
        // 1. 优先取 host
        String target = uri.getHost();
        // 2. host 为空时兜底解析
        if (CharSequenceUtil.isBlank(target)) {
            target = uri.toString()
                .replaceFirst("^.+://([^/:]+).*", "$1");

            if (uri.toString().contains(NetWorkConstant.WSS_PREFIX)) {
                builder.requestSchema(NetWorkConstant.WSS);
            } else if (uri.toString().contains(NetWorkConstant.WS_PREFIX)) {
                builder.requestSchema(NetWorkConstant.WS);
            }
        }

        builder.targetServer(target);
    }

    private static void buildUserInfo(GatewayLogDTOBuilder<?, ?> builder, UserBaseVO user) {
        if (null == user) {
            user = UserContextHolder.getDefaultUser();
        }

        builder.userId(user.getUserId())
            .orgId(user.getOrgId())
            .createdId(user.getUserId())
            .createdBy(user.getRealName())
            .modifiedBy(user.getRealName())
            .modifiedId(user.getUserId());
    }

    private static void buildUserAgent(GatewayLogDTOBuilder<?, ?> builder, String userAgentStr) {
        if (CharSequenceUtil.isBlank(userAgentStr)) {
            return;
        }

        UserAgent userAgent = UserAgentUtil.parse(userAgentStr);
        Optional.ofNullable(userAgent).ifPresent(agent ->
            builder.platformName(agent.getPlatform().getName())
                .osName(agent.getOs().getName())
                .browserName(agent.getBrowser().getName())
        );
    }

    private static GatewayLogDTO buildRequestData(LogConfig cfg, GatewayLogDTOBuilder<?, ?> builder, ReactorRequestDTO data) {
        if (cfg.getEnabledRequestHeader()) {
            Map<String, String> headerMap = data.getRequestHeaders();
            if (MapUtil.isNotEmpty(headerMap)) {
                builder.requestHeaders(JacksonUtil.toJsonStr(headerMap));
            }
        }
        if (cfg.getEnabledRequestBody()) {
            String body = data.getRequestBody();
            if (CharSequenceUtil.isNotBlank(body)) {
                int limit = cfg.getLimitRequestBody() * NumberConstant.NUMBER_ONE_THOUSAND_TWENTY_FOUR;
                builder.requestBody(CharSequenceUtil.maxLength(body, limit));
            }
        }
        if (cfg.getEnabledRequestParam()) {
            Map<String, String> paramMap = data.getQueryParams();
            if (MapUtil.isNotEmpty(paramMap)) {
                builder.requestParam(URLUtil.buildQuery(paramMap, StandardCharsets.UTF_8));
            }
        }
        return builder.build();
    }

}

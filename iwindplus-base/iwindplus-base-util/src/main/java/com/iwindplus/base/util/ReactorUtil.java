/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.PrimitiveArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.iwindplus.base.domain.constant.CommonConstant;
import com.iwindplus.base.domain.constant.CommonConstant.NumberConstant;
import com.iwindplus.base.domain.constant.CommonConstant.OauthConstant;
import com.iwindplus.base.domain.constant.CommonConstant.SymbolConstant;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.util.domain.dto.ReactorRequestDTO;
import com.iwindplus.base.util.domain.dto.ReactorResponseDTO;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactor工具类.
 *
 * @author zengdegui
 * @since 2025/10/20 23:00
 */
@Slf4j
public class ReactorUtil {

    private ReactorUtil() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 请求体（缓存用）.
     */
    public static final String REQUEST_BODY = "requestBody";

    /**
     * 响应体（缓存用）
     */
    public static final String RESPONSE_BODY = "responseBody";

    /**
     * 构建新的请求对象，并设置新的请求头参数.
     *
     * @param exchange ServerWebExchange
     * @return ServerWebExchange
     */
    public static ServerWebExchange buildAuthorizationByCookie(ServerWebExchange exchange) {
        return Optional.ofNullable(exchange.getRequest().getCookies().getFirst(OauthConstant.ACCESS_TOKEN))
            .map(cookie -> exchange.mutate()
                .request(exchange.getRequest().mutate()
                    .headers(headers -> headers.set(HttpHeaders.AUTHORIZATION, cookie.getValue()))
                    .build())
                .build())
            .orElse(exchange);
    }

    /**
     * 删除请求头中的指定参数.
     *
     * @param exchange    ServerWebExchange
     * @param headerNames 要删除的请求头名称集合
     * @return ServerWebExchange
     */
    public static ServerWebExchange removeHeaders(ServerWebExchange exchange, Collection<String> headerNames) {
        return Optional.ofNullable(headerNames)
            .filter(CollUtil::isNotEmpty)
            .map(names -> exchange.getRequest().mutate()
                .headers(headers -> names.forEach(headers::remove))
                .build())
            .map(mutatedRequest -> exchange.mutate().request(mutatedRequest).build())
            .orElse(exchange);
    }

    /**
     * 删除请求头中的指定参数（单个）.
     *
     * @param exchange   ServerWebExchange
     * @param headerName 要删除的请求头名称
     * @return 修改后的 ServerWebExchange
     */
    public static ServerWebExchange removeHeader(ServerWebExchange exchange, String headerName) {
        return removeHeaders(exchange, Set.of(headerName));
    }

    /**
     * 获取响应结果.
     *
     * @param exchange       请求
     * @param httpStatusCode 状态码
     * @param data           数据
     * @return Mono<Void>
     */
    public static Mono<Void> getMonoResponse(ServerWebExchange exchange, HttpStatusCode httpStatusCode, ResultVO<Object> data) {
        ServerHttpResponse response = exchange.getResponse();
        if (Objects.nonNull(httpStatusCode)) {
            response.setStatusCode(httpStatusCode);
        }
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        return Mono.defer(() -> {
            byte[] bytes = JacksonUtil.toJsonBytes(data);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);

            return response.writeWith(Mono.just(buffer));
        });
    }

    /**
     * 获取请求体.
     *
     * @param exchange 当前交换器
     * @return Mono<ReactorRequestDTO>
     */
    public static Mono<ReactorRequestDTO> getRequestBodyByAttr(ServerWebExchange exchange) {
        final ReactorRequestDTO cached = exchange.getAttribute(REQUEST_BODY);
        return Mono.justOrEmpty(cached);
    }

    /**
     * 读取并缓存请求体，随后执行任意 Function<T,Mono<T>>，最终返回 Mono<ReactorRequestDTO>.
     *
     * @param exchange 当前交换器
     * @param function 要对“新” exchange 执行的逻辑
     * @param <T>      function 返回的 Mono 的泛型，可以任意
     * @return 携带 body 的 Mono<ReactorRequestDTO>
     */
    public static <T> Mono<ReactorRequestDTO> readRequestBody(
        ServerWebExchange exchange,
        Function<ServerWebExchange, Mono<T>> function) {

        ServerHttpRequest request = exchange.getRequest();
        HttpHeaders headers = request.getHeaders();
        MediaType contentType = headers.getContentType();

        ReactorRequestDTO collector = new ReactorRequestDTO();
        collector.setRequestHeaders(headers.toSingleValueMap());
        collector.setQueryParams(request.getQueryParams().toSingleValueMap());

        if (!isReadableRequest(contentType)) {
            setAttribute(exchange, REQUEST_BODY, collector);
            return function.apply(exchange).thenReturn(collector);
        }

        return Mono.defer(() ->
                DataBufferUtils.join(request.getBody())
                    .flatMap(dataBuffer -> {
                            byte[] body;
                            try {
                                body = new byte[dataBuffer.readableByteCount()];
                                dataBuffer.read(body);
                            } finally {
                                DataBufferUtils.release(dataBuffer);
                            }

                            collector.setRequestBody(new String(body, StandardCharsets.UTF_8));

                            ServerHttpRequest newRequest =
                                ReactorUtil.buildNewServerHttpRequest(exchange, body);

                            ServerWebExchange newExchange = exchange.mutate()
                                .request(newRequest)
                                .build();

                            setAttribute(newExchange, REQUEST_BODY, collector);

                            return function.apply(newExchange).thenReturn(collector);
                        }
                    ))
            .doOnError(e -> {
                if (!(e instanceof BizException)) {
                    log.error("Failed to read request body.", e);
                }
            });
    }

    /**
     * 构建新请求（重新包装请求体）.
     *
     * @param exchange  请求
     * @param bodyBytes 请求体字节数组
     * @return ServerHttpRequest
     */
    public static ServerHttpRequest buildNewServerHttpRequest(ServerWebExchange exchange, byte[] bodyBytes) {
        DataBuffer buffer = exchange.getResponse()
            .bufferFactory()
            .wrap(bodyBytes);

        return new ServerHttpRequestDecorator(exchange.getRequest()) {
            @Override
            public Flux<DataBuffer> getBody() {
                return Flux.just(buffer);
            }
        };
    }

    /**
     * 获取响应体.
     *
     * @param exchange 当前交换器
     * @return Mono<ReactorResponseDTO>
     */
    public static Mono<ReactorResponseDTO> getResponseBodyByAttr(ServerWebExchange exchange) {
        final ReactorResponseDTO cached = exchange.getAttribute(RESPONSE_BODY);
        return Mono.justOrEmpty(cached);
    }

    /**
     * 读取并缓存响应体，随后执行任意 Function<T,Mono<T>>，最终返回 Mono<ReactorResponseDTO>.
     *
     * @param exchange 当前交换器
     * @param function 要对“新” exchange 执行的逻辑
     * @param <T>      function 返回的 Mono 的泛型
     * @return 携带响应体的 Mono<ReactorResponseDTO>
     */
    public static <T> Mono<ReactorResponseDTO> readResponseBody(ServerWebExchange exchange, Function<ServerWebExchange, Mono<T>> function) {
        // 提前分配一个容器，用来聚合下游写出的数据
        final ReactorResponseDTO collector = new ReactorResponseDTO();

        // 1. 构造可重复读的响应
        final ServerHttpResponseDecorator newResp = buildNewResponseDecorator(exchange, collector);
        final ServerWebExchange newExchange = exchange.mutate().response(newResp).build();
        // 2. 执行业务逻辑
        return function.apply(newExchange)
            .then(Mono.fromCallable(() -> {
                fillResponseMeta(newExchange, collector);
                setAttribute(newExchange, RESPONSE_BODY, collector);
                return collector;
            }))
            .doOnError(ex -> {
                if (ex instanceof BizException bizEx) {
                    throw bizEx;
                } else {
                    log.error("Failed to read response body", ex);
                }
            });
    }

    /**
     * 构建新响应（重新包装响应体）.
     *
     * @param exchange  当前交换器
     * @param collector 数据收集器
     * @return ServerHttpResponseDecorator
     */
    public static ServerHttpResponseDecorator buildNewResponseDecorator(
        ServerWebExchange exchange, ReactorResponseDTO collector) {
        final ServerHttpResponse original = exchange.getResponse();

        final StringBuilder bodyBuffer = new StringBuilder(512);
        return new ServerHttpResponseDecorator(original) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                if (!(body instanceof Flux<?> flux)) {
                    return super.writeWith(body);
                }

                return super.writeWith(
                    ((Flux<DataBuffer>) flux)
                        .map(buffer -> {
                            DataBuffer retained = DataBufferUtils.retain(buffer);
                            captureToString(retained, bodyBuffer);
                            return retained;
                        })
                        .doOnComplete(() ->
                            collector.setResponseBody(bodyBuffer.toString()))
                );
            }

            @Override
            public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
                return super.writeAndFlushWith(
                    Flux.from(body)
                        .map(inner ->
                            Flux.from(inner)
                                .map(buffer -> {
                                    DataBuffer retained =
                                        DataBufferUtils.retain(buffer);
                                    captureToString(retained, bodyBuffer);
                                    return retained;
                                })
                        )
                        .doOnComplete(() ->
                            collector.setResponseBody(bodyBuffer.toString()))
                );
            }
        };
    }

    /**
     * 截取数据.
     *
     * @param bytes 字节数组
     * @param limit 限制长度（可选）
     * @return String
     */
    public static String getLimitByBytes(byte[] bytes, Integer limit) {
        if (PrimitiveArrayUtil.isEmpty(bytes)) {
            return null;
        }
        String str = new String(bytes);
        if (Objects.nonNull(limit)) {
            return StrUtil.maxLength(str, limit * NumberConstant.NUMBER_ONE_THOUSAND_TWENTY_FOUR);
        }
        return str;
    }

    /**
     * 构建查询参数.
     *
     * @param request 请求
     * @return String
     */
    public static String buildQueryParam(ServerHttpRequest request) {
        Map<String, String> singleMap = request.getQueryParams().toSingleValueMap();
        return MapUtil.isEmpty(singleMap) ? SymbolConstant.EMPTY_STR : URLUtil.buildQuery(singleMap, StandardCharsets.UTF_8);
    }

    /**
     * 设置属性.
     *
     * @param exchange 请求
     * @param key      键
     * @param value    值
     */
    public static <T> void setAttribute(ServerWebExchange exchange, String key, T value) {
        exchange.getAttributes().put(key, value);
    }

    /**
     * 获取属性.
     *
     * @param exchange 请求
     * @param key      键
     * @return T
     */
    public static <T> T getAttribute(ServerWebExchange exchange, String key) {
        return exchange.getAttribute(key);
    }

    private static boolean isReadableRequest(MediaType contentType) {
        if (contentType == null) {
            return false;
        }
        return MediaType.APPLICATION_JSON.isCompatibleWith(contentType)
            || MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(contentType);
    }

    private static void captureToString(DataBuffer buffer, StringBuilder out) {
        int readable = buffer.readableByteCount();
        if (readable <= 0) {
            return;
        }

        int pos = buffer.readPosition();
        byte[] bytes = new byte[readable];

        for (int i = 0; i < readable; i++) {
            bytes[i] = buffer.getByte(pos + i);
        }

        out.append(new String(bytes, StandardCharsets.UTF_8));
    }

    private static void fillResponseMeta(ServerWebExchange exchange, ReactorResponseDTO collector) {
        HttpHeaders headers = new HttpHeaders();
        headers.putAll(exchange.getResponse().getHeaders());

        collector.setResponseHeaders(headers.toSingleValueMap());
        collector.setResponseStatus(
            exchange.getResponse().getStatusCode() != null
                ? exchange.getResponse().getStatusCode().value()
                : null
        );
    }
}

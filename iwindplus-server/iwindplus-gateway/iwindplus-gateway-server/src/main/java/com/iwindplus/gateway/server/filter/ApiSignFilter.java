/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.gateway.server.filter;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.iwindplus.base.domain.constant.CommonConstant.ApiSignConstant;
import com.iwindplus.base.domain.constant.CommonConstant.NumberConstant;
import com.iwindplus.base.domain.dto.MessageBaseDTO;
import com.iwindplus.base.domain.enums.BaseEnum;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.enums.OperateTypeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.BaseSignVO;
import com.iwindplus.base.redis.domain.constant.RedisConstant;
import com.iwindplus.base.redis.service.RedissonService;
import com.iwindplus.base.util.ApiSignUtil;
import com.iwindplus.base.util.PathMatchUtil;
import com.iwindplus.base.util.ReactorUtil;
import com.iwindplus.base.util.domain.dto.ApiSignVerifyDTO;
import com.iwindplus.gateway.server.client.MgtClient;
import com.iwindplus.gateway.server.domain.constant.GatewayConstant.FilterConstant;
import com.iwindplus.gateway.server.domain.property.GatewayProperty;
import com.iwindplus.gateway.server.filter.base.BaseGatewayFilter;
import com.iwindplus.gateway.server.util.GatewayUtil;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * API签名过滤器.
 *
 * @author zengdegui
 * @since 2020/4/15
 */
@Slf4j
@Component
public class ApiSignFilter extends BaseGatewayFilter {

    private static final List<String> DEFAULT_IGNORED_APIS =
        List.of(
            "/static/**",
            "/api-docs",
            "/doc.html",
            "/actuator/**",
            "/api/imWs/ws",
            "/oauth2/token",
            "/inner/**"
        );

    private final GatewayProperty property;
    private final RedissonService redissonService;
    private final MgtClient mgtClient;
    private final AsyncLoadingCache<String, BaseSignVO> cache;

    public ApiSignFilter(GatewayProperty property, RedissonService redissonService, MgtClient mgtClient) {
        this.property = property;
        this.redissonService = redissonService;
        this.mgtClient = mgtClient;
        this.cache = Caffeine.newBuilder()
            .maximumSize(property.getApiSign().getMaxSize())
            .expireAfterWrite(property.getApiSign().getCacheTimeout())
            .refreshAfterWrite(property.getApiSign().getCacheRefresh())
            .recordStats()
            .buildAsync((key, executor) -> loadApiSign(key).toFuture());
    }

    @Override
    public int getOrder() {
        return FilterConstant.FILTER_API_SIGN_ORDER;
    }

    @Override
    protected boolean shouldSkip(ServerWebExchange exchange) {
        if (GatewayUtil.shouldSkip(exchange, property.getApiSign().getEnabled())) {
            return true;
        }

        String path = exchange.getRequest().getPath().value();

        Set<String> patterns = new HashSet<>(DEFAULT_IGNORED_APIS);
        List<String> custom = property.getApiSign().getIgnoredApi();
        if (CollUtil.isNotEmpty(custom)) {
            patterns.addAll(custom);
        }

        return PathMatchUtil.match(patterns.stream().distinct().toList(), path);
    }

    @Override
    protected Mono<ServerWebExchange> before(ServerWebExchange exchange) {
        return checkApiSign(exchange)
            .then(Mono.defer(() -> {
                Set<String> headersToRemove = Set.of(
                    ApiSignConstant.X_ACCESS_KEY,
                    ApiSignConstant.X_TIMESTAMP,
                    ApiSignConstant.X_NONCE,
                    ApiSignConstant.X_PATH,
                    ApiSignConstant.X_METHOD,
                    ApiSignConstant.X_SIGN
                );

                ServerWebExchange mutated = ReactorUtil.removeHeaders(exchange, headersToRemove);

                return Mono.just(mutated);
            }));
    }

    private Mono<Void> checkApiSign(ServerWebExchange exchange) {
        return buildApiSign(exchange)
            .flatMap(dto -> getApiSign(dto.getAccessKey())
                .flatMap(apiSign -> {
                    if (apiSign == null) {
                        return GatewayUtil.asyncPublishErrorLog(exchange, property.getLog(), new BizException(BizCodeEnum.ACCESS_KEY_NOT_EXIST));
                    }
                    dto.setSecretKey(apiSign.getSecretKey());
                    dto.setTimeout(Duration.ofSeconds(apiSign.getTimeout()));

                    if (!ApiSignUtil.verifySign(dto)) {
                        return GatewayUtil.asyncPublishErrorLog(exchange, property.getLog(), new BizException(BizCodeEnum.INVALID_SIGN));
                    }

                    if (Boolean.TRUE.equals(property.getApiSign().getEnabledExecuteOnlyOnce())) {
                        // 防重放攻击
                        final HttpMethod method = exchange.getRequest().getMethod();
                        if (!HttpMethod.GET.equals(method)) {
                            String key = RedisConstant.REPEAT_SUBMIT_KEY_PREFIX + dto.getSign();
                            return redissonService.repeatSubmit()
                                .executeReactive(
                                    key,
                                    property.getApiSign().getOnlyCheckOnceTtl(),
                                    Mono::empty
                                );
                        }
                    }

                    return Mono.empty();
                }));
    }

    private Mono<ApiSignVerifyDTO> buildApiSign(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        HttpHeaders headers = request.getHeaders();
        String path = headers.getFirst(ApiSignConstant.X_PATH);
        String accessKey = headers.getFirst(ApiSignConstant.X_ACCESS_KEY);
        String timestamp = headers.getFirst(ApiSignConstant.X_TIMESTAMP);
        String nonce = headers.getFirst(ApiSignConstant.X_NONCE);
        String sign = headers.getFirst(ApiSignConstant.X_SIGN);

        if (CharSequenceUtil.isBlank(accessKey)) {
            return GatewayUtil.asyncPublishErrorLog(exchange, property.getLog(), new BizException(BizCodeEnum.ACCESS_KEY_NOT_EXIST));
        }
        if (CharSequenceUtil.isBlank(timestamp)) {
            return GatewayUtil.asyncPublishErrorLog(exchange, property.getLog(), new BizException(BizCodeEnum.TIMESTAMP_NOT_EXIST));
        }
        if (CharSequenceUtil.isBlank(nonce) || nonce.length() < NumberConstant.NUMBER_TEN) {
            return GatewayUtil.asyncPublishErrorLog(exchange, property.getLog(), new BizException(BizCodeEnum.INVALID_NONCE));
        }
        if (CharSequenceUtil.isBlank(sign)) {
            return GatewayUtil.asyncPublishErrorLog(exchange, property.getLog(), new BizException(BizCodeEnum.SIGN_NOT_EXIST));
        }

        return GatewayUtil.getRequestBodyAndParam(exchange)
            .flatMap(params -> {
                ApiSignVerifyDTO verifyDTO = ApiSignVerifyDTO.builder()
                    .accessKey(accessKey)
                    .timestamp(timestamp)
                    .nonce(nonce)
                    .sign(sign)
                    .method(request.getMethod().name())
                    .path(path)
                    .params(params)
                    .build();
                return Mono.just(verifyDTO);
            });
    }

    private Mono<BaseSignVO> getApiSign(String accessKey) {
        return Mono.fromFuture(() -> cache.get(accessKey))
            .flatMap(vo -> vo == null ? Mono.empty() : Mono.just(vo))
            .onErrorResume(e -> {
                // 异常降级：使用同步缓存
                BaseSignVO fallback = cache.synchronous().getIfPresent(accessKey);
                if (fallback != null) {
                    log.warn("获取 API签名配置失败，使用缓存降级, accessKey={}", accessKey, e);
                    return Mono.just(fallback);
                }
                log.warn("获取 API签名配置失败，返回空", e);
                return Mono.empty();
            });
    }

    private Mono<BaseSignVO> loadApiSign(String accessKey) {
        return mgtClient.getByAccessKey(accessKey)
            .doOnNext(vo -> log.info("API签名配置加载完成, accessKey={}", accessKey))
            .doOnError(ex -> {
                if (ex instanceof BizException bizEx) {
                    throw bizEx;
                } else {
                    log.error("API签名配置加载失败, accessKey={}", accessKey, ex);
                }
            });
    }

    /**
     * 刷新API签名配置.
     *
     * @param message 消息
     */
    public void refreshAppCert(MessageBaseDTO<List<BaseSignVO>> message) {
        if (message == null || message.getOperateType() == null
            || CollUtil.isEmpty(message.getData())) {
            return;
        }
        OperateTypeEnum operateType = BaseEnum.fromValue(message.getOperateType(), OperateTypeEnum.class);

        message.getData().forEach(vo -> {
            switch (operateType) {
                case ADD:
                case MODIFY:
                    cache.synchronous().put(vo.getAccessKey(), vo);
                    break;
                case DELETE:
                    cache.synchronous().invalidate(vo.getAccessKey());
                    break;
                default:
                    throw new BizException(BizCodeEnum.UNSUPPORTED_TYPE, message.getOperateType());
            }
        });
        log.info("API签名配置刷新完成，缓存条目数={}", cache.synchronous().estimatedSize());
    }

}
/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */
package com.iwindplus.gateway.interfaces.filter;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.iwindplus.base.domain.constant.CommonConstant.HeaderConstant;
import com.iwindplus.base.domain.constant.CommonConstant.SymbolConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.UserBaseVO;
import com.iwindplus.base.util.CryptoUtil;
import com.iwindplus.base.util.HttpsUtil;
import com.iwindplus.base.util.JacksonUtil;
import com.iwindplus.base.util.PathMatchUtil;
import com.iwindplus.base.util.ReactorUtil;
import com.iwindplus.base.web.domain.property.FilterProperty;
import com.iwindplus.gateway.infrastructure.client.AuthClient;
import com.iwindplus.gateway.infrastructure.client.MgtClient;
import com.iwindplus.gateway.infrastructure.client.vo.ResourceVO;
import com.iwindplus.gateway.infrastructure.configuration.property.AuthProperty;
import com.iwindplus.gateway.infrastructure.configuration.property.LogProperty;
import com.iwindplus.gateway.infrastructure.configuration.constant.GatewayFilterConstant;
import com.iwindplus.gateway.infrastructure.configuration.constant.GatewayWebExchangeConstant;
import com.iwindplus.gateway.infrastructure.configuration.enums.AuthTokenModeEnum;
import com.iwindplus.gateway.infrastructure.support.GatewayUtil;
import com.iwindplus.gateway.interfaces.filter.base.BaseGatewayFilter;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 认证过滤器.
 *
 * @author zengdegui
 * @since 2020/4/15
 */
@Slf4j
@Component
public class AuthFilter extends BaseGatewayFilter {

    private final AuthProperty authProperty;
    private final LogProperty logProperty;
    private final FilterProperty filterProperty;
    private final AuthClient authClient;
    private final MgtClient mgtClient;

    /**
     * token -> UserBaseVO（仅缓存远程校验结果）
     */
    private final AsyncLoadingCache<String, UserBaseVO> tokenCache;

    /**
     * 用户权限缓存 key = orgId:userId
     */
    private final AsyncLoadingCache<String, Set<String>> userPermissionCache;

    public AuthFilter(
        AuthProperty authProperty,
        LogProperty logProperty,
        FilterProperty filterProperty,
        AuthClient authClient,
        MgtClient mgtClient) {

        this.authProperty = authProperty;
        this.logProperty = logProperty;
        this.filterProperty = filterProperty;
        this.authClient = authClient;
        this.mgtClient = mgtClient;

        this.tokenCache = Caffeine.newBuilder()
            .maximumSize(authProperty.getTokenCacheMaxSize())
            .expireAfterWrite(authProperty.getTokenCacheTimeout())
            .recordStats()
            .buildAsync((key, executor) -> loadTokenRemote(key).toFuture());

        this.userPermissionCache = Caffeine.newBuilder()
            .maximumSize(authProperty.getPermissionCacheMaxSize())
            .expireAfterWrite(authProperty.getPermissionCacheTimeout())
            .recordStats()
            .buildAsync((key, executor) -> loadUserPermissions(key).toFuture());
    }

    @Override
    public int getOrder() {
        return GatewayFilterConstant.FILTER_AUTH_ORDER;
    }

    @Override
    protected boolean shouldSkip(ServerWebExchange exchange) {
        return GatewayUtil.shouldSkip(
            exchange,
            authProperty.getEnabled()
        );
    }

    @Override
    protected Mono<ServerWebExchange> before(ServerWebExchange exchange) {

        return checkAccessToken(exchange)
            .map(ctx -> {
                // 放入上下文
                ReactorUtil.setAttribute(ctx.exchange(),
                    GatewayWebExchangeConstant.USER_INFO,
                    ctx.user());

                return ctx.exchange();
            });
    }

    @Override
    protected Mono<Void> filterInternal(ServerWebExchange exchange,
        GatewayFilterChain chain) {

        UserBaseVO user = ReactorUtil.getAttribute(
            exchange, GatewayWebExchangeConstant.USER_INFO);

        return checkApiPermission(exchange, user)
            .then(chain.filter(exchange));
    }

    /**
     * token -> user + new exchange
     */
    private Mono<AuthContext> checkAccessToken(ServerWebExchange exchange) {
        return Mono.justOrEmpty(HttpsUtil.getAuthorization(exchange.getRequest()))
            .switchIfEmpty(Mono.error(new BizException(BizCodeEnum.TOKEN_NOT_EXIST)))
            .map(this::extractToken)
            .flatMap(token ->
                resolveUser(token, authProperty)
                    .map(user -> {
                        ServerWebExchange newExchange = buildExchange(exchange, user);
                        return new AuthContext(newExchange, user);
                    })
            );
    }

    private String extractToken(String auth) {
        if (CharSequenceUtil.isBlank(auth)) {
            return null;
        }
        return auth.startsWith(HeaderConstant.X_BEARER_TYPE)
            ? auth.substring(HeaderConstant.X_BEARER_TYPE.length())
            : auth;
    }

    private Mono<UserBaseVO> resolveUser(String token, AuthProperty property) {
        // OPAQUE模式：不透明令牌无法本地解析，强制走远程校验
        boolean useRemote = Boolean.TRUE.equals(property.getEnabledRemoteToken())
            || AuthTokenModeEnum.OPAQUE.equals(property.getTokenMode());

        // 本地解析（仅JWT模式）
        if (!useRemote) {
            return Mono.justOrEmpty(HttpsUtil.getUserInfo(token))
                .switchIfEmpty(Mono.error(
                    new BizException(BizCodeEnum.INVALID_ACCESS_TOKEN)));
        }

        // 远程校验 + 缓存（single-flight）
        return Mono.fromFuture(tokenCache.get(token))
            .switchIfEmpty(Mono.error(
                new BizException(BizCodeEnum.INVALID_ACCESS_TOKEN)));
    }

    private ServerWebExchange buildExchange(ServerWebExchange exchange,
        UserBaseVO user) {

        String json = JacksonUtil.toJsonStr(user);
        String encrypted = CryptoUtil.encrypt(json, filterProperty.getCrypto());

        ServerHttpRequest newReq = exchange.getRequest()
            .mutate()
            .header(HeaderConstant.X_USER_ID, user.getUserId().toString())
            .header(HeaderConstant.X_USER_INFO, encrypted)
            .build();

        return exchange.mutate().request(newReq).build();
    }

    private Mono<UserBaseVO> loadTokenRemote(String token) {
        return authClient.checkAccessToken(token)
            .doOnSuccess(u -> log.info("Token 校验完成"));
    }

    /**
     * 权限校验
     */
    private Mono<Void> checkApiPermission(ServerWebExchange exchange,
        UserBaseVO user) {

        if (Boolean.FALSE.equals(authProperty.getEnabledApiPermission())) {
            return Mono.empty();
        }

        String path = exchange.getRequest().getPath().value();

        // 忽略路径
        if (hasIgnoredWhitedFlag(path)) {
            return Mono.empty();
        }

        // 通用API
        List<String> generalApi = authProperty.getGeneralApi();
        if (CollUtil.isNotEmpty(generalApi)
            && PathMatchUtil.match(generalApi, path)) {
            return Mono.empty();
        }

        String userKey = buildUserKey(user.getOrgId(), user.getUserId());

        return Mono.fromFuture(userPermissionCache.get(userKey))
            .flatMap(perms ->
                perms.contains(path)
                    ? Mono.empty()
                    : buildErrorInfo(exchange)
            );
    }

    private Mono<Set<String>> loadUserPermissions(String key) {
        UserKey u = UserKey.parse(key);

        return mgtClient.listApiCheckedByUserId(u.orgId, u.userId)
            .map(this::buildPermissionSet)
            .doOnSuccess(v ->
                log.info("用户权限加载完成 orgId={} userId={} size={}",
                    u.orgId, u.userId, v.size()));
    }

    private Set<String> buildPermissionSet(List<ResourceVO> list) {
        Set<String> set = ConcurrentHashMap.newKeySet();
        for (ResourceVO r : list) {
            if (CollUtil.isNotEmpty(r.getApiUrls())) {
                for (String apiUrl : r.getApiUrls()) {
                    set.add(apiUrl);
                }
            }
        }
        return set;
    }

    private Mono<Void> buildErrorInfo(ServerWebExchange exchange) {
        return GatewayUtil.asyncPublishErrorLog(
            exchange,
            logProperty,
            new BizException(BizCodeEnum.NO_API_PERMISSION));
    }

    private boolean hasIgnoredWhitedFlag(String path) {
        List<String> ignored = authProperty.getIgnoredApi();
        return CollUtil.isNotEmpty(ignored)
            && PathMatchUtil.match(ignored, path);
    }

    private String buildUserKey(Long orgId, Long userId) {
        return orgId + SymbolConstant.COLON + userId;
    }

    /**
     * 强类型上下文（替代 Tuple2）
     */
    record AuthContext(ServerWebExchange exchange, UserBaseVO user) {

    }

    record UserKey(Long orgId, Long userId) {

        static UserKey parse(String key) {
            String[] arr = key.split(SymbolConstant.COLON, 2);
            return new UserKey(
                Long.valueOf(arr[0]),
                Long.valueOf(arr[1])
            );
        }
    }
}
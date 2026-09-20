/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.gateway.infrastructure.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.domain.vo.UserBaseVO;
import com.iwindplus.base.http.client.domain.enums.HttpClientTypeEnum;
import com.iwindplus.base.http.client.factory.HttpClientExecuteHandlerFactory;
import com.iwindplus.base.http.client.support.HttpClientExecuteHandler;
import com.iwindplus.gateway.infrastructure.configuration.property.ServerApiProperty;
import com.iwindplus.gateway.infrastructure.configuration.property.ServerApiProperty.AuthApiConfig;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * auth 客户端.
 *
 * @author zengdegui
 * @since 2025/08/24 16:18
 */
@Slf4j
@Component
public class AuthClient {

    private final ServerApiProperty serverApiProperty;
    private final HttpClientExecuteHandler httpClientExecuteHandler;

    /**
     * 构造函数.
     *
     * @param serverApiProperty 服务配置
     * @param factory           factory
     */
    public AuthClient(
        ServerApiProperty serverApiProperty,
        HttpClientExecuteHandlerFactory factory) {
        this.serverApiProperty = serverApiProperty;
        httpClientExecuteHandler = factory.getHandler(HttpClientTypeEnum.WEB_CLIENT);
    }

    /**
     * auth api配置.
     *
     * @return AuthApiConfig
     */
    private AuthApiConfig getCfg() {
        return serverApiProperty.getAuth();
    }

    /**
     * 获取用户信息.
     *
     * @param accessToken 访问token
     * @return Mono<UserBaseVO>
     */
    public Mono<UserBaseVO> checkAccessToken(String accessToken) {
        final String url = serverApiProperty.resolveUrl(this.getCfg().getAuthorizationCheckAccessTokenUrl());
        final Map<String, String> query = Map.of("accessToken", accessToken);
        return Mono.fromCompletionStage(
            httpClientExecuteHandler.getAsync(url, query, null, new TypeReference<ResultVO<UserBaseVO>>() {
            })).flatMap(ResultVO::unwrap);
    }
}
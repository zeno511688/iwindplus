/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.gateway.server.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwindplus.auth.domain.constant.AuthConstant;
import com.iwindplus.base.domain.constant.CommonConstant.NetWorkConstant;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.domain.vo.UserBaseVO;
import com.iwindplus.base.http.client.domain.enums.HttpClientTypeEnum;
import com.iwindplus.base.http.client.executor.HttpClientExecutor;
import com.iwindplus.base.http.client.factory.HttpClientExecutorStrategyFactory;
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

    public static final String AUTH_PREFIX = String.format("%s%s", NetWorkConstant.LB_PREFIX, AuthConstant.AUTH_SERVER_NAME);
    private final HttpClientExecutor httpClientExecutor;

    /**
     * 构造函数.
     *
     * @param factory factory
     */
    public AuthClient(HttpClientExecutorStrategyFactory factory) {
        httpClientExecutor = factory.getHttpClientExecutor(HttpClientTypeEnum.WEB_CLIENT);
    }

    /**
     * 获取用户信息.
     *
     * @param accessToken 访问token
     * @return Mono<UserBaseVO>
     */
    public Mono<UserBaseVO> checkAccessToken(String accessToken) {
        final String url = String.format("%s%s", AUTH_PREFIX, "/inner/authorization/checkAccessToken");
        final Map<String, String> query = Map.of("accessToken", accessToken);
        return Mono.fromCompletionStage(
            httpClientExecutor.getAsync(url, query, null, new TypeReference<ResultVO<UserBaseVO>>() {
            })).flatMap(ResultVO::unwrap);
    }
}
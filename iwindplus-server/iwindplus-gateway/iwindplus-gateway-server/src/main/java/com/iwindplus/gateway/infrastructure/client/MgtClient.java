/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.gateway.infrastructure.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwindplus.base.domain.dto.UserExtendFunctionValidDTO;
import com.iwindplus.base.domain.enums.AppCertTypeEnum;
import com.iwindplus.base.domain.vo.BaseSignVO;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.domain.vo.UserExtendFunctionValidVO;
import com.iwindplus.base.http.client.domain.enums.HttpClientTypeEnum;
import com.iwindplus.base.http.client.factory.HttpClientExecuteHandlerFactory;
import com.iwindplus.base.http.client.support.HttpClientExecuteHandler;
import com.iwindplus.gateway.infrastructure.client.vo.ResourceVO;
import com.iwindplus.gateway.infrastructure.client.vo.ServerApiVO;
import com.iwindplus.gateway.infrastructure.client.vo.ServerRouteDefinitionVO;
import com.iwindplus.gateway.infrastructure.configuration.ServerApiProperty;
import com.iwindplus.gateway.infrastructure.configuration.ServerApiProperty.MgtApiConfig;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * mgt客户端.
 *
 * @author zengdegui
 * @since 2025/08/24 16:18
 */
@Slf4j
@Component
public class MgtClient {

    private final ServerApiProperty serverApiProperty;
    private final HttpClientExecuteHandler httpClientExecuteHandler;

    /**
     * 构造函数.
     *
     * @param serverApiProperty 服务配置
     * @param factory           factory
     */
    public MgtClient(
        ServerApiProperty serverApiProperty,
        HttpClientExecuteHandlerFactory factory) {
        this.serverApiProperty = serverApiProperty;
        this.httpClientExecuteHandler = factory.getHandler(HttpClientTypeEnum.WEB_CLIENT);
    }

    /**
     * mgt api配置.
     *
     * @return MgtApiConfig
     */
    private MgtApiConfig getCfg() {
        return serverApiProperty.getMgt();
    }

    /**
     * 获取所有路由定义.
     *
     * @return Mono<List < ServerRouteDefinitionVO>>
     */
    public Mono<List<ServerRouteDefinitionVO>> listRouteDefinition() {
        final String url = serverApiProperty.resolveUrl(this.getCfg().getServerListRouteDefinitionUrl());
        return Mono.fromCompletionStage(
            httpClientExecuteHandler.getAsync(url, null, null, new TypeReference<ResultVO<List<ServerRouteDefinitionVO>>>() {
            })).flatMap(ResultVO::unwrap);
    }

    /**
     * 获取应用凭证api签名信息.
     *
     * @param accessKey accessKey
     * @return Mono<BaseSignVO>
     */
    public Mono<BaseSignVO> getByAccessKey(String accessKey) {
        final String url = serverApiProperty.resolveUrl(this.getCfg().getAppCertGetByAccessKeyUrl());
        final Map<String, ? extends Serializable> query = Map.of("accessKey", accessKey, "appCertType", AppCertTypeEnum.API_GATEWAY_SIGN_BLACKLIST);
        return Mono.fromCompletionStage(
            httpClientExecuteHandler.getAsync(url, query, null, new TypeReference<ResultVO<BaseSignVO>>() {
            })).flatMap(ResultVO::unwrap);
    }

    /**
     * 获取API白名单所有API.
     *
     * @return Mono<List < String>>
     */
    public Mono<List<String>> listApi() {
        final String url = serverApiProperty.resolveUrl(this.getCfg().getApiWhiteListListApiUrl());
        return Mono.fromCompletionStage(
            httpClientExecuteHandler.getAsync(url, null, null, new TypeReference<ResultVO<List<String>>>() {
            })).flatMap(ResultVO::unwrap);
    }

    /**
     * 获取IP黑名单所有IP.
     *
     * @return Mono<List < String>>
     */
    public Mono<List<String>> listIp() {
        final String url = serverApiProperty.resolveUrl(this.getCfg().getIpBlackListListIpUrl());
        return Mono.fromCompletionStage(
            httpClientExecuteHandler.getAsync(url, null, null, new TypeReference<ResultVO<List<String>>>() {
            })).flatMap(ResultVO::unwrap);
    }

    /**
     * 获取用户API权限.
     *
     * @param orgId  组织主键
     * @param userId 用户主键
     * @return Mono<List < ResourceBaseExtendVO>>
     */
    public Mono<List<ResourceVO>> listApiCheckedByUserId(Long orgId, Long userId) {
        final String url = serverApiProperty.resolveUrl(this.getCfg().getResourceListApiCheckedByUserIdUrl());
        final Map<String, Long> query = Map.of("orgId", orgId, "userId", userId);
        return Mono.fromCompletionStage(
            httpClientExecuteHandler.getAsync(url, query, null, new TypeReference<ResultVO<List<ResourceVO>>>() {
            })).flatMap(ResultVO::unwrap);
    }

    /**
     * 校验用户API权限.
     *
     * @param orgId         组织主键
     * @param userId        用户主键
     * @param requestMethod 请求方式
     * @param path          路径
     * @return Mono<Boolean>
     */
    public Mono<Boolean> checkApiByUserId(Long orgId, Long userId, String requestMethod, String path) {
        final String url = serverApiProperty.resolveUrl(this.getCfg().getResourceCheckApiByUserIdUrl());
        final Map<String, ? extends Serializable> query = Map.of("orgId", orgId, "userId", userId, "requestMethod", requestMethod, "path", path);
        return Mono.fromCompletionStage(
            httpClientExecuteHandler.getAsync(url, query, null, new TypeReference<ResultVO<Boolean>>() {
            })).flatMap(ResultVO::unwrap);
    }

    /**
     * 校验用户扩展功能（GA，邮箱，短信，yubikey）.
     *
     * @param entity 对象
     * @return Mono<UserExtendFunctionValidVO>
     */
    public Mono<UserExtendFunctionValidVO> checkExtendFunctionByUserId(UserExtendFunctionValidDTO entity) {
        final String url = serverApiProperty.resolveUrl(this.getCfg().getUserCheckExtendFunctionByUserIdUrl());
        return Mono.fromCompletionStage(
            httpClientExecuteHandler.postAsync(url, entity, null, new TypeReference<ResultVO<UserExtendFunctionValidVO>>() {
            })).flatMap(ResultVO::unwrap);
    }

    /**
     * 获取所有服务API.
     *
     * @return Mono<List < ServerApiBaseVO>>
     */
    public Mono<List<ServerApiVO>> listServerApi() {
        final String url = serverApiProperty.resolveUrl(this.getCfg().getServerApiListApiUrl());
        return Mono.fromCompletionStage(
            httpClientExecuteHandler.getAsync(url, null, null, new TypeReference<ResultVO<List<ServerApiVO>>>() {
            })).flatMap(ResultVO::unwrap);
    }
}
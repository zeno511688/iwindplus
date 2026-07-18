/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.gateway.server.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwindplus.base.domain.constant.CommonConstant.NetWorkConstant;
import com.iwindplus.base.domain.dto.UserExtendFunctionValidDTO;
import com.iwindplus.base.domain.vo.BaseSignVO;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.domain.vo.UserExtendFunctionValidVO;
import com.iwindplus.base.http.client.domain.enums.HttpClientTypeEnum;
import com.iwindplus.base.http.client.executor.HttpClientExecutor;
import com.iwindplus.base.http.client.factory.HttpClientExecutorStrategyFactory;
import com.iwindplus.mgt.domain.constant.MgtConstant;
import com.iwindplus.base.domain.enums.AppCertTypeEnum;
import com.iwindplus.mgt.domain.vo.power.ResourceBaseExtendVO;
import com.iwindplus.mgt.domain.vo.system.ServerApiBaseVO;
import com.iwindplus.mgt.domain.vo.system.ServerRouteDefinitionVO;
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

    public static final String MGT_PREFIX = String.format("%s%s", NetWorkConstant.LB_PREFIX, MgtConstant.MGT_SERVER_NAME);
    private final HttpClientExecutor httpClientExecutor;

    /**
     * 构造函数.
     *
     * @param factory HttpClientExecutorStrategyFactory
     */
    public MgtClient(HttpClientExecutorStrategyFactory factory) {
        httpClientExecutor = factory.getHttpClientExecutor(HttpClientTypeEnum.WEB_CLIENT);
    }

    /**
     * 获取所有路由定义.
     *
     * @return Mono<List < ServerRouteDefinitionVO>>
     */
    public Mono<List<ServerRouteDefinitionVO>> listRouteDefinition() {
        final String url = String.format("%s%s", MGT_PREFIX, "/inner/server/listRouteDefinition");
        return Mono.fromCompletionStage(
            httpClientExecutor.getAsync(url, null, null, new TypeReference<ResultVO<List<ServerRouteDefinitionVO>>>() {
            })).flatMap(ResultVO::unwrap);
    }

    /**
     * 根据accessKey获取api签名信息.
     *
     * @param accessKey accessKey
     * @return Mono<BaseSignVO>
     */
    public Mono<BaseSignVO> getByAccessKey(String accessKey) {
        final String url = String.format("%s%s", MGT_PREFIX, "/inner/appCert/getByAccessKey");
        final Map<String, ? extends Serializable> query = Map.of("accessKey", accessKey, "appCertType", AppCertTypeEnum.API_GATEWAY_SIGN_BLACKLIST);
        return Mono.fromCompletionStage(
            httpClientExecutor.getAsync(url, query, null, new TypeReference<ResultVO<BaseSignVO>>() {
            })).flatMap(ResultVO::unwrap);
    }

    /**
     * 获取所有API.
     *
     * @return Mono<List < String>>
     */
    public Mono<List<String>> listApi() {
        final String url = String.format("%s%s", MGT_PREFIX, "/inner/apiWhiteList/listApi");
        return Mono.fromCompletionStage(
            httpClientExecutor.getAsync(url, null, null, new TypeReference<ResultVO<List<String>>>() {
            })).flatMap(ResultVO::unwrap);
    }

    /**
     * 获取所有IP.
     *
     * @return Mono<List < String>>
     */
    public Mono<List<String>> listIp() {
        final String url = String.format("%s%s", MGT_PREFIX, "/inner/ipBlackList/listIp");
        return Mono.fromCompletionStage(
            httpClientExecutor.getAsync(url, null, null, new TypeReference<ResultVO<List<String>>>() {
            })).flatMap(ResultVO::unwrap);
    }

    /**
     * 获取用户API权限.
     *
     * @param orgId  组织主键
     * @param userId 用户主键
     * @return Mono<List < ResourceBaseExtendVO>>
     */
    public Mono<List<ResourceBaseExtendVO>> listApiCheckedByUserId(Long orgId, Long userId) {
        final String url = String.format("%s%s", MGT_PREFIX, "/inner/resource/listApiCheckedByUserId");
        final Map<String, Long> query = Map.of("orgId", orgId, "userId", userId);
        return Mono.fromCompletionStage(
            httpClientExecutor.getAsync(url, query, null, new TypeReference<ResultVO<List<ResourceBaseExtendVO>>>() {
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
        final String url = String.format("%s%s", MGT_PREFIX, "/inner/resource/checkApiByUserId");
        final Map<String, ? extends Serializable> query = Map.of("orgId", orgId, "userId", userId, "requestMethod", requestMethod, "path", path);
        return Mono.fromCompletionStage(
            httpClientExecutor.getAsync(url, query, null, new TypeReference<ResultVO<Boolean>>() {
            })).flatMap(ResultVO::unwrap);
    }

    /**
     * 校验用户扩展功能（GA，邮箱，短信，yubikey）.
     *
     * @param entity 对象
     * @return Mono<UserExtendFunctionValidVO>
     */
    public Mono<UserExtendFunctionValidVO> checkExtendFunctionByUserId(UserExtendFunctionValidDTO entity) {
        final String url = String.format("%s%s", MGT_PREFIX, "/inner/user/checkExtendFunctionByUserId");
        return Mono.fromCompletionStage(
            httpClientExecutor.postAsync(url, entity, null, new TypeReference<ResultVO<UserExtendFunctionValidVO>>() {
            })).flatMap(ResultVO::unwrap);
    }

    /**
     * 获取所有服务API.
     *
     * @return Mono<List < ServerApiBaseVO>>
     */
    public Mono<List<ServerApiBaseVO>> listServerApi() {
        final String url = String.format("%s%s", MGT_PREFIX, "/inner/serverApi/listApi");
        return Mono.fromCompletionStage(
            httpClientExecutor.getAsync(url, null, null, new TypeReference<ResultVO<List<ServerApiBaseVO>>>() {
            })).flatMap(ResultVO::unwrap);
    }
}
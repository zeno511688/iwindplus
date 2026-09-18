/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.auth.infrastructure.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwindplus.auth.infrastructure.configuration.ServerApiProperty;
import com.iwindplus.auth.infrastructure.model.dto.ClientDTO;
import com.iwindplus.auth.infrastructure.model.vo.ClientBaseVO;
import com.iwindplus.auth.infrastructure.model.vo.ClientVO;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.http.client.domain.enums.HttpClientTypeEnum;
import com.iwindplus.base.http.client.factory.HttpClientExecuteHandlerFactory;
import com.iwindplus.base.http.client.support.HttpClientExecuteHandler;
import io.swagger.v3.oas.annotations.Operation;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 登陆认证业务层客户端.
 *
 * @author zengdegui
 * @since 2024/06/11 20:38
 */
@Slf4j
@Component
public class ClientClient {

    private final ServerApiProperty serverApiProperty;
    private final HttpClientExecuteHandler httpClientExecuteHandler;

    /**
     * 构造函数.
     *
     * @param serverApiProperty 服务配置
     * @param factory           factory
     */
    public ClientClient(
        ServerApiProperty serverApiProperty,
        HttpClientExecuteHandlerFactory factory) {
        this.serverApiProperty = serverApiProperty;
        this.httpClientExecuteHandler = factory.getHandler(HttpClientTypeEnum.REST_CLIENT);
    }

    /**
     * 添加客户端.
     *
     * @param entity 对象
     * @return ResultVO<ClientBaseVO>
     */
    public ResultVO<ClientBaseVO> save(ClientDTO entity) {
        final ServerApiProperty.MgtApiConfig mgtApiConfig = this.serverApiProperty.getMgt();
        final String url = serverApiProperty.resolveUrl(mgtApiConfig.getClientSaveUrl());
        return httpClientExecuteHandler
            .post(
                url,
                entity,
                null,
                new TypeReference<>() {
                }
            );
    }

    /**
     * 详情.
     *
     * @param id 主键
     * @return ResultVO < ClientVO>
     */
    public ResultVO<ClientVO> getDetail(@RequestParam(value = "id") String id) {
        final ServerApiProperty.MgtApiConfig mgtApiConfig = this.serverApiProperty.getMgt();
        final String url = serverApiProperty.resolveUrl(mgtApiConfig.getClientGetDetailUrl());
        final Map<String, String> query = Map.of(
            "id", id
        );
        return httpClientExecuteHandler
            .get(
                url,
                query,
                null,
                new TypeReference<>() {
                }
            );
    }

    /**
     * 通过客户端id查询.
     *
     * @param clientId 客户端id
     * @return ResultVO<ClientVO>
     */
    @Operation(summary = "通过客户端id查询")
    public ResultVO<ClientVO> getByClientId(String clientId) {
        final ServerApiProperty.MgtApiConfig mgtApiConfig = this.serverApiProperty.getMgt();
        final String url = serverApiProperty.resolveUrl(mgtApiConfig.getClientGetByClientIdUrl());
        final Map<String, String> query = Map.of(
            "clientId", clientId
        );
        return httpClientExecuteHandler
            .get(
                url,
                query,
                null,
                new TypeReference<>() {
                }
            );
    }
}

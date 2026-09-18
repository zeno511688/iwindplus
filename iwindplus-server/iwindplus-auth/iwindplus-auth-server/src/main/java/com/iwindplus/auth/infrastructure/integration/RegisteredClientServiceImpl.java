/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.auth.infrastructure.integration;

import cn.hutool.core.bean.BeanUtil;
import com.iwindplus.auth.infrastructure.client.ClientClient;
import com.iwindplus.auth.infrastructure.model.dto.ClientDTO;
import com.iwindplus.auth.infrastructure.model.vo.ClientVO;
import com.iwindplus.base.domain.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Component;

/**
 * 客户端业务层接口实现类.
 *
 * @author zengdegui
 * @since 2024/07/14 17:12
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisteredClientServiceImpl implements RegisteredClientRepository {

    private final ClientClient clientClient;
    private final RegisteredClientConverter registeredClientConverter;

    @Override
    public void save(RegisteredClient registeredClient) {
        final ClientVO registeredClientVO = this.registeredClientConverter.convert(registeredClient);
        final ClientDTO entity = BeanUtil.copyProperties(registeredClientVO, ClientDTO.class);
        this.clientClient.save(entity);
    }

    @Override
    public RegisteredClient findById(String id) {
        ResultVO<ClientVO> result = this.clientClient.getDetail(id);
        return this.buildRegisteredClient(result);
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        ResultVO<ClientVO> result = this.clientClient.getByClientId(clientId);
        return this.buildRegisteredClient(result);
    }

    private RegisteredClient buildRegisteredClient(ResultVO<ClientVO> result) {
        result.errorThrow();
        final ClientVO data = result.getBizData();
        return this.registeredClientConverter.convert(data);
    }
}

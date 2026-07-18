/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.auth.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.iwindplus.auth.server.converter.RegisteredClientConverter;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.mgt.client.system.ClientClient;
import com.iwindplus.mgt.domain.dto.system.ClientDTO;
import com.iwindplus.mgt.domain.vo.system.ClientVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Service;

/**
 * 客户端业务层接口实现类.
 *
 * @author zengdegui
 * @since 2024/07/14 17:12
 */
@Service
@Slf4j
public class RegisteredClientServiceImpl implements RegisteredClientRepository {

    @Resource
    private ClientClient clientClient;

    @Resource
    private RegisteredClientConverter registeredClientConverter;

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

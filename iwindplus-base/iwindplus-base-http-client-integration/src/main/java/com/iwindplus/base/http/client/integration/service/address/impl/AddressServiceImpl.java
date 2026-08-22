/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.integration.service.address.impl;

import com.iwindplus.base.http.client.integration.domain.enums.AddressProviderEnum;
import com.iwindplus.base.http.client.integration.domain.vo.address.AddressVO;
import com.iwindplus.base.http.client.integration.factory.AddressExecuteHandlerStrategyFactory;
import com.iwindplus.base.http.client.integration.service.address.AddressService;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

/**
 * 地址服务实现类.
 *
 * @author zengdegui
 * @since 2025/08/20
 */
@Slf4j
public class AddressServiceImpl implements AddressService {

    private final AddressExecuteHandlerStrategyFactory addressStrategyFactory;

    public AddressServiceImpl(
        AddressExecuteHandlerStrategyFactory addressStrategyFactory) {
        this.addressStrategyFactory = addressStrategyFactory;
    }

    @Override
    public Optional<AddressVO> getAddress(String ip) {
        return this.addressStrategyFactory.queryAddress(ip);
    }

    @Override
    public Optional<AddressVO> getAddress(String ip, AddressProviderEnum provider) {
        return this.addressStrategyFactory.queryAddress(ip, provider);
    }

    @Override
    public List<AddressProviderEnum> getAvailableProviders() {
        return this.addressStrategyFactory.getAvailableProviders();
    }
}

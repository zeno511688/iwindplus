/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.address.support.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.address.support.AddressExecuteHandler;
import com.iwindplus.base.http.client.support.HttpClientExecuteHandler;
import com.iwindplus.base.http.client.factory.HttpClientExecuteHandlerFactory;
import com.iwindplus.base.address.domain.constant.AddressConstant;
import com.iwindplus.base.address.domain.dto.Ip138AddressDTO;
import com.iwindplus.base.address.domain.enums.AddressProviderEnum;
import com.iwindplus.base.address.domain.property.AddressProperty;
import com.iwindplus.base.address.domain.vo.AddressVO;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * IP138地址服务策略.
 *
 * @author zengdegui
 * @since 2026/08/21
 */
@Slf4j
public class Ip138AddressExecuteHandler implements AddressExecuteHandler {

    @Getter
    private final AddressProviderEnum provider = AddressProviderEnum.IP138;

    private final HttpClientExecuteHandlerFactory httpClientExecuteHandlerFactory;
    private final AddressProperty.ProviderConfig config;

    public Ip138AddressExecuteHandler(
        HttpClientExecuteHandlerFactory httpClientExecuteHandlerFactory,
        AddressProperty.ProviderConfig config) {
        this.httpClientExecuteHandlerFactory = httpClientExecuteHandlerFactory;
        this.config = config;
    }

    @Override
    public boolean isHealthy() {
        return this.config != null
            && Boolean.TRUE.equals(this.config.getEnabled())
            && CharSequenceUtil.isNotBlank(config.getApiKey());
    }

    @Override
    public Optional<AddressVO> getAddress(String ip) {
        try {
            return doGetAddress(ip);
        } catch (Exception e) {
            log.warn("IP138 address query failed [provider={}]: error={}", provider.getName(), e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<AddressVO> doGetAddress(String ip) {
        String secretKey = this.config.getSecretKey();

        // 构建请求头
        Map<String, String> headers = new HashMap<>(4);
        headers.put(AddressConstant.PARAM_TOKEN, secretKey);

        // 构建请求参数
        Map<String, String> queryParams = new HashMap<>(4);
        queryParams.put(AddressConstant.PARAM_IP, ip);

        HttpClientExecuteHandler executor = this.httpClientExecuteHandlerFactory.getDefaultHandler();
        Ip138AddressDTO response = executor.get(
            AddressConstant.Url.IP138_URL_STR,
            queryParams,
            headers,
            Ip138AddressDTO.class
        );

        if (response == null || response.getData() == null || response.getData().size() < AddressConstant.IP138_MIN_DATA_SIZE) {
            log.warn("IP138 address response is invalid: ip={}", ip);
            return Optional.empty();
        }

        final List<String> data = response.getData();

        final AddressVO vo = AddressVO.builder()
            .ip(ip)
            .nation(data.size() > 0 ? data.get(AddressConstant.IP138_NATION_INDEX) : null)
            .province(data.size() > 1 ? data.get(AddressConstant.IP138_PROVINCE_INDEX) : null)
            .city(data.size() > 2 ? data.get(AddressConstant.IP138_CITY_INDEX) : null)
            .build();
        return Optional.of(vo);
    }
}

/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.integration.support.adress.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.http.client.executor.HttpClientExecutor;
import com.iwindplus.base.http.client.factory.HttpClientExecutorStrategyFactory;
import com.iwindplus.base.http.client.integration.domain.constant.AddressConstant;
import com.iwindplus.base.http.client.integration.domain.dto.address.Ip138AddressDTO;
import com.iwindplus.base.http.client.integration.domain.enums.AddressProviderEnum;
import com.iwindplus.base.http.client.integration.domain.property.AddressProperty;
import com.iwindplus.base.http.client.integration.domain.vo.address.AddressVO;
import com.iwindplus.base.http.client.integration.support.adress.AddressExecuteHandler;
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

    private final HttpClientExecutorStrategyFactory httpClientExecutorStrategyFactory;
    private final AddressProperty.ProviderConfig config;

    public Ip138AddressExecuteHandler(
        HttpClientExecutorStrategyFactory httpClientExecutorStrategyFactory,
        AddressProperty.ProviderConfig config) {
        this.httpClientExecutorStrategyFactory = httpClientExecutorStrategyFactory;
        this.config = config;
    }

    @Override
    public boolean isHealthy() {
        return Boolean.TRUE.equals(config.getEnabled())
            && CharSequenceUtil.isNotBlank(config.getApiKey());
    }

    @Override
    public Optional<AddressVO> queryAddress(String ip) {
        try {
            return doQueryAddress(ip);
        } catch (Exception e) {
            log.warn("IP138 address query failed [provider={}]: error={}", provider.getName(), e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<AddressVO> doQueryAddress(String ip) {
        String apiKey = this.config.getApiKey();

        // 构建请求头
        Map<String, String> headers = new HashMap<>(4);
        headers.put(AddressConstant.PARAM_TOKEN, apiKey);

        // 构建请求参数
        Map<String, String> queryParams = new HashMap<>(4);
        queryParams.put(AddressConstant.PARAM_IP, ip);

        // 使用 HttpClientExecutor 调用第三方接口
        HttpClientExecutor executor = this.httpClientExecutorStrategyFactory.getDefaultHttpClientExecutor();
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

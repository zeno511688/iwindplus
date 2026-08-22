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
import com.iwindplus.base.http.client.integration.domain.dto.address.PconlineAddressDTO;
import com.iwindplus.base.http.client.integration.domain.enums.AddressProviderEnum;
import com.iwindplus.base.http.client.integration.domain.property.AddressProperty;
import com.iwindplus.base.http.client.integration.domain.vo.address.AddressVO;
import com.iwindplus.base.http.client.integration.support.adress.AddressExecuteHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 太平洋网络地址服务策略（不推荐使用）.
 *
 * @author zengdegui
 * @since 2026/08/21
 */
@Slf4j
public class PconlineAddressExecuteHandler implements AddressExecuteHandler {

    @Getter
    private final AddressProviderEnum provider = AddressProviderEnum.PCONLINE;

    private final HttpClientExecutorStrategyFactory httpClientExecutorStrategyFactory;
    private final AddressProperty.ProviderConfig config;

    public PconlineAddressExecuteHandler(
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
            log.warn("Pconline address query failed [provider={}]: error={}", provider.getName(), e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<AddressVO> doQueryAddress(String ip) {
        // 构建请求参数
        Map<String, String> queryParams = new HashMap<>(4);
        queryParams.put(AddressConstant.PARAM_IP, ip);
        queryParams.put(AddressConstant.PARAM_JSON, AddressConstant.VALUE_TRUE);

        // 使用 HttpClientExecutor 调用第三方接口
        HttpClientExecutor executor = this.httpClientExecutorStrategyFactory.getDefaultHttpClientExecutor();
        PconlineAddressDTO response = executor.get(
            AddressConstant.Url.PCONLINE_URL_STR,
            queryParams,
            null,
            PconlineAddressDTO.class
        );

        if (response == null) {
            log.warn("Pconline address response is invalid: ip={}", ip);
            return Optional.empty();
        }

        // 转换为统一VO
        AddressVO vo = AddressVO.builder()
                .ip(ip)
                .province(response.getPro())
                .city(response.getCity())
                .build();

        return Optional.of(vo);
    }
}

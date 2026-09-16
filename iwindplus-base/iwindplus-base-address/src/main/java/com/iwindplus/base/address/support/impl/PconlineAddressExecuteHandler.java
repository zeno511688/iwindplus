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
import com.iwindplus.base.address.domain.dto.PconlineAddressDTO;
import com.iwindplus.base.address.domain.enums.AddressProviderEnum;
import com.iwindplus.base.address.domain.property.AddressProperty;
import com.iwindplus.base.address.domain.vo.AddressVO;
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

    private final HttpClientExecuteHandlerFactory httpClientExecuteHandlerFactory;
    private final AddressProperty.ProviderConfig config;

    public PconlineAddressExecuteHandler(
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
            log.warn("Pconline address query failed [provider={}]: error={}", provider.getName(), e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<AddressVO> doGetAddress(String ip) {
        // 构建请求参数
        Map<String, String> queryParams = new HashMap<>(4);
        queryParams.put(AddressConstant.PARAM_IP, ip);
        queryParams.put(AddressConstant.PARAM_JSON, AddressConstant.VALUE_TRUE);

        HttpClientExecuteHandler executor = this.httpClientExecuteHandlerFactory.getDefaultHandler();
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

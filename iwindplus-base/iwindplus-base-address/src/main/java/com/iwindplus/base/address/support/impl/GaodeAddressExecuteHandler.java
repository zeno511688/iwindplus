/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.address.support.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.iwindplus.base.address.support.AddressExecuteHandler;
import com.iwindplus.base.domain.constant.CommonConstant.SymbolConstant;
import com.iwindplus.base.http.client.support.HttpClientExecuteHandler;
import com.iwindplus.base.http.client.factory.HttpClientExecuteHandlerFactory;
import com.iwindplus.base.address.domain.constant.AddressConstant;
import com.iwindplus.base.address.domain.dto.GaodeAddressDTO;
import com.iwindplus.base.address.domain.enums.AddressProviderEnum;
import com.iwindplus.base.address.domain.property.AddressProperty;
import com.iwindplus.base.address.domain.vo.AddressVO;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 高德地图地址服务策略.
 *
 * @author zengdegui
 * @since 2026/08/21
 */
@Slf4j
public class GaodeAddressExecuteHandler implements AddressExecuteHandler {

    @Getter
    private final AddressProviderEnum provider = AddressProviderEnum.GAODE;

    private final HttpClientExecuteHandlerFactory httpClientExecuteHandlerFactory;
    private final AddressProperty.ProviderConfig config;

    public GaodeAddressExecuteHandler(
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
            log.warn("Gaode address query failed [provider={}]: error={}", provider.getName(), e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<AddressVO> doGetAddress(String ip) {
        String key = this.config.getApiKey();

        // 构建请求参数
        Map<String, String> queryParams = new HashMap<>(4);
        queryParams.put(AddressConstant.PARAM_IP, ip);
        queryParams.put(AddressConstant.PARAM_KEY, key);

        // 如果配置了 SecretKey，需要计算签名
        if (StrUtil.isNotBlank(this.config.getSecretKey())) {
            String sig = calculateSignature(queryParams);
            queryParams.put(AddressConstant.PARAM_SIG, sig);
        }

        HttpClientExecuteHandler executor = this.httpClientExecuteHandlerFactory.getDefaultHandler();
        GaodeAddressDTO response = executor.get(
            AddressConstant.Url.GAODE_URL_STR,
            queryParams,
            null,
            GaodeAddressDTO.class
        );

        if (response == null) {
            log.warn("Gaode address response is null: ip={}", ip);
            return Optional.empty();
        }

        // 检查返回状态
        if (response.getStatus() != 1) {
            log.warn("Gaode address query failed: ip={}, status={}, info={}",
                ip, response.getStatus(), response.getInfo());
            return Optional.empty();
        }

        if (StrUtil.isBlank(response.getProvince())) {
            log.warn("Gaode address response province is blank: ip={}", ip);
            return Optional.empty();
        }

        // 转换为统一VO
        AddressVO vo = AddressVO.builder()
            .ip(ip)
            .province(response.getProvince())
            .city(response.getCity())
            .build();

        return Optional.of(vo);
    }

    /**
     * 计算高德签名.
     *
     * @param queryParams 查询参数
     * @return sig签名
     */
    private String calculateSignature(Map<String, String> queryParams) {
        try {
            TreeMap<String, String> sortedParams = new TreeMap<>(queryParams);
            StringBuilder queryBuilder = new StringBuilder();
            for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
                if (queryBuilder.length() > 0) {
                    queryBuilder.append(SymbolConstant.LOGICAL_AND);
                }
                queryBuilder.append(entry.getKey())
                    .append(SymbolConstant.EQUAL)
                    .append(entry.getValue());
            }
            // 高德签名算法：在参数字符串末尾拼接 SecretKey 后进行 MD5 加密
            queryBuilder.append(this.config.getSecretKey());
            return SecureUtil.md5(queryBuilder.toString());
        } catch (Exception e) {
            log.error("Failed to calculate gaode sig: error={}", e.getMessage());
            return CharSequenceUtil.EMPTY;
        }
    }
}

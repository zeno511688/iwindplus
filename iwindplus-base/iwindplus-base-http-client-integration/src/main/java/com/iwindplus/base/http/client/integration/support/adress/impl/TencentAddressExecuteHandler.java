/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.integration.support.adress.impl;

import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.iwindplus.base.domain.constant.CommonConstant.SymbolConstant;
import com.iwindplus.base.http.client.executor.HttpClientExecutor;
import com.iwindplus.base.http.client.factory.HttpClientExecutorStrategyFactory;
import com.iwindplus.base.http.client.integration.domain.constant.AddressConstant;
import com.iwindplus.base.http.client.integration.domain.dto.address.TencentAddressDTO;
import com.iwindplus.base.http.client.integration.domain.enums.AddressProviderEnum;
import com.iwindplus.base.http.client.integration.domain.property.AddressProperty;
import com.iwindplus.base.http.client.integration.domain.vo.address.AddressVO;
import com.iwindplus.base.http.client.integration.support.adress.AddressExecuteHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 腾讯地图地址服务策略.
 *
 * @author zengdegui
 * @since 2026/08/21
 */
@Slf4j
public class TencentAddressExecuteHandler implements AddressExecuteHandler {

    @Getter
    private final AddressProviderEnum provider = AddressProviderEnum.TENCENT;

    private final HttpClientExecutorStrategyFactory httpClientExecutorStrategyFactory;
    private final AddressProperty.ProviderConfig config;

    public TencentAddressExecuteHandler(
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
            log.warn("Tencent address failed [provider={}]: error={}", provider.getName(), e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<AddressVO> doQueryAddress(String ip) {
        String key = this.config.getApiKey();

        // 构建请求参数
        Map<String, String> queryParams = new HashMap<>(4);
        queryParams.put(AddressConstant.PARAM_IP, ip);
        queryParams.put(AddressConstant.PARAM_KEY, key);

        // 如果配置了 SecretKey，需要计算签名
        if (StrUtil.isNotBlank(this.config.getSecretKey())) {
            String sig = calculateSignature(AddressConstant.Url.TENCENT_URL_STR, queryParams);
            queryParams.put(AddressConstant.PARAM_SIG, sig);
        }

        // 使用 HttpClientExecutor 调用第三方接口
        HttpClientExecutor executor = this.httpClientExecutorStrategyFactory.getDefaultHttpClientExecutor();
        TencentAddressDTO response = executor.get(
            AddressConstant.Url.TENCENT_URL_STR,
            queryParams,
            null,
            TencentAddressDTO.class
        );

        if (response == null) {
            log.warn("Tencent address response is null: ip={}", ip);
            return Optional.empty();
        }

        // 检查返回状态
        if (response.getStatus() == null || response.getStatus() != 0) {
            log.warn("Tencent address query failed: ip={}, status={}, message={}",
                ip, response.getStatus(), response.getMessage());
            return Optional.empty();
        }

        if (response.getResult() == null || response.getResult().getAdInfo() == null) {
            log.warn("Tencent address response is invalid: ip={}", ip);
            return Optional.empty();
        }

        TencentAddressDTO.Result result = response.getResult();
        TencentAddressDTO.AdInfo adInfo = result.getAdInfo();

        // 转换为统一VO
        AddressVO.AddressVOBuilder builder = AddressVO.builder()
            .ip(ip)
            .province(adInfo.getProvince())
            .city(adInfo.getCity());

        // 添加经纬度
        if (result.getLocation() != null) {
            TencentAddressDTO.Location location = result.getLocation();
            builder.longitude(location.getLng())
                .latitude(location.getLat());
        }

        return Optional.of(builder.build());
    }

    /**
     * 计算腾讯地图签名.
     * <p>
     * 签名算法：MD5(请求路径 + "?" + 参数键值对拼接 + SecretKey)
     * 参数键值对需要按 key 字典序排序
     * 注意：请求参数必须是未进行任何编码（如 urlencode）的原始数据
     * </p>
     *
     * @param urlStr    请求URL
     * @param params    请求参数
     * @return 签名
     */
    private String calculateSignature(String urlStr, Map<String, String> params) {
        try {
            // 使用 Hutool 解析URL获取请求路径
            UrlBuilder urlBuilder = UrlBuilder.of(urlStr);
            String uriPath = urlBuilder.getPath().toString();

            // 按字典序排序参数
            TreeMap<String, String> sortedParams = new TreeMap<>(params);

            // 手动拼接查询字符串（不进行URL编码，使用原始数据）
            StringBuilder queryBuilder = new StringBuilder();
            for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
                if (queryBuilder.length() > 0) {
                    queryBuilder.append(SymbolConstant.LOGICAL_AND);
                }
                queryBuilder.append(entry.getKey())
                    .append(SymbolConstant.EQUAL)
                    .append(entry.getValue());
            }

            // 拼接字符串：uriPath + "?" + queryString + secretKey
            String plainString = uriPath + SymbolConstant.QUESTION_MARK
                + queryBuilder + this.config.getSecretKey();
            return SecureUtil.md5(plainString);
        } catch (Exception e) {
            log.error("Failed to calculate tencent sig: error={}", e.getMessage());
            return CharSequenceUtil.EMPTY;
        }
    }
}

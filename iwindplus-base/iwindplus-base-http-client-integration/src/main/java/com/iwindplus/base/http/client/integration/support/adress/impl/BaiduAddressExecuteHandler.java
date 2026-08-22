/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.integration.support.adress.impl;

import cn.hutool.core.net.URLEncodeUtil;
import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.iwindplus.base.domain.constant.CommonConstant.SymbolConstant;
import com.iwindplus.base.http.client.executor.HttpClientExecutor;
import com.iwindplus.base.http.client.factory.HttpClientExecutorStrategyFactory;
import com.iwindplus.base.http.client.integration.domain.constant.AddressConstant;
import com.iwindplus.base.http.client.integration.domain.dto.address.BaiduAddressDTO;
import com.iwindplus.base.http.client.integration.domain.dto.address.BaiduAddressDTO.AddressDetail;
import com.iwindplus.base.http.client.integration.domain.enums.AddressProviderEnum;
import com.iwindplus.base.http.client.integration.domain.property.AddressProperty;
import com.iwindplus.base.http.client.integration.domain.vo.address.AddressVO;
import com.iwindplus.base.http.client.integration.support.adress.AddressExecuteHandler;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 百度地图地址服务策略.
 *
 * @author zengdegui
 * @since 2026/08/21
 */
@Slf4j
public class BaiduAddressExecuteHandler implements AddressExecuteHandler {

    @Getter
    private final AddressProviderEnum provider = AddressProviderEnum.BAIDU;

    private final HttpClientExecutorStrategyFactory httpClientExecutorStrategyFactory;
    private final AddressProperty.ProviderConfig config;

    public BaiduAddressExecuteHandler(
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
            log.warn("Baidu address query failed [provider={}]: error={}", provider.getName(), e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<AddressVO> doQueryAddress(String ip) {
        String ak = this.config.getApiKey();

        // 构建请求参数
        Map<String, String> queryParams = new LinkedHashMap<>();
        queryParams.put(AddressConstant.PARAM_IP, ip);
        queryParams.put(AddressConstant.PARAM_AK, ak);

        // 计算sn签名（如果配置了sk）
        if (StrUtil.isNotBlank(this.config.getSecretKey())) {
            String sn = calculateSignature(AddressConstant.Url.BAIDU_URL_STR, queryParams);
            queryParams.put(AddressConstant.PARAM_SN, sn);
        }

        // 使用 HttpClientExecutor 调用第三方接口
        HttpClientExecutor executor = this.httpClientExecutorStrategyFactory.getDefaultHttpClientExecutor();
        BaiduAddressDTO response = executor.get(
            AddressConstant.Url.BAIDU_URL_STR,
            queryParams,
            null,
            BaiduAddressDTO.class
        );

        if (response == null) {
            log.warn("Baidu address response is null: ip={}", ip);
            return Optional.empty();
        }

        // 检查返回状态
        if (response.getStatus() == null || response.getStatus() != 0) {
            log.warn("Baidu address query failed: ip={}, status={}", ip, response.getStatus());
            return Optional.empty();
        }

        if (response.getContent() == null || response.getContent().getAddressDetail() == null) {
            log.warn("Baidu address response content is invalid: ip={}", ip);
            return Optional.empty();
        }

        // 转换为统一VO
        final AddressVO vo = getAddressVO(ip, response);
        return Optional.of(vo);
    }

    private AddressVO getAddressVO(String ip, BaiduAddressDTO response) {
        final AddressVO vo = AddressVO.builder()
            .ip(ip)
            .province(Optional.ofNullable(response)
                .map(BaiduAddressDTO::getContent)
                .map(BaiduAddressDTO.Content::getAddressDetail)
                .map(AddressDetail::getProvince)
                .orElse(null))
            .city(Optional.ofNullable(response)
                .map(BaiduAddressDTO::getContent)
                .map(BaiduAddressDTO.Content::getAddressDetail)
                .map(AddressDetail::getCity)
                .orElse(null))
            .longitude(Optional.ofNullable(response)
                .map(BaiduAddressDTO::getContent)
                .map(BaiduAddressDTO.Content::getPoint)
                .map(BaiduAddressDTO.Point::getX)
                .orElse(null))
            .latitude(Optional.ofNullable(response)
                .map(BaiduAddressDTO::getContent)
                .map(BaiduAddressDTO.Content::getPoint)
                .map(BaiduAddressDTO.Point::getY)
                .orElse(null))
            .build();
        return vo;
    }

    /**
     * 计算百度签名.
     *
     * @param url         URL
     * @param queryParams 查询参数
     * @return sn签名
     */
    private String calculateSignature(String url, Map<String, String> queryParams) {
        try {
            // 使用 Hutool 解析URL
            UrlBuilder urlBuilder = UrlBuilder.of(url);
            String uriPath = urlBuilder.getPath().toString();

            // 构建查询字符串
            UrlBuilder queryBuilder = UrlBuilder.of();
            queryParams.forEach(queryBuilder::addQuery);
            String queryString = queryBuilder.getQueryStr();

            // 拼接字符串：uriPath + "?" + queryString + sk
            String plainString = uriPath + SymbolConstant.QUESTION_MARK + queryString + this.config.getSecretKey();

            // URL编码和MD5计算
            String encodedString = URLEncodeUtil.encodeAll(plainString);
            return SecureUtil.md5(encodedString);
        } catch (Exception e) {
            log.error("Failed to calculate baidu sn: error={}", e.getMessage());
            return CharSequenceUtil.EMPTY;
        }
    }
}

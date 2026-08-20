/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.integration.service.impl;

import com.iwindplus.base.http.client.executor.HttpClientExecutor;
import com.iwindplus.base.http.client.factory.HttpClientExecutorStrategyFactory;
import com.iwindplus.base.http.client.integration.domain.constant.HttpClientIntegrationConstant.AddressConstant;
import com.iwindplus.base.http.client.integration.domain.dto.BaiduAddressDTO;
import com.iwindplus.base.http.client.integration.domain.dto.GaodeAddressDTO;
import com.iwindplus.base.http.client.integration.domain.dto.Ip138AddressDTO;
import com.iwindplus.base.http.client.integration.domain.dto.PconlineAddressDTO;
import com.iwindplus.base.http.client.integration.domain.dto.TencentAddressDTO;
import com.iwindplus.base.http.client.integration.domain.vo.AddressVO;
import com.iwindplus.base.http.client.integration.service.AddressService;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

/**
 * 地址服务实现类.
 *
 * @author zengdegui
 * @since 2025/08/20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final HttpClientExecutorStrategyFactory httpClientExecutorStrategyFactory;

    @Override
    public Optional<AddressVO> getAddressByPconline(String ip) {
        HttpClientExecutor executor = this.httpClientExecutorStrategyFactory.getDefaultHttpClientExecutor();
        Map<String, String> queryParams = new HashMap<>(4);
        queryParams.put(AddressConstant.PARAM_JSON,
            AddressConstant.VALUE_TRUE);
        queryParams.put(AddressConstant.PARAM_IP, ip);
        PconlineAddressDTO response = executor.get(
            AddressConstant.PCONLINE_URL_STR,
            queryParams,
            null,
            PconlineAddressDTO.class
        );
        return this.toAddress(response == null ? null : response.getPro(), response == null ? null : response.getCity());
    }

    @Override
    public Optional<AddressVO> getAddressByGaode(String ip, String appCode) {
        HttpClientExecutor executor = this.httpClientExecutorStrategyFactory.getDefaultHttpClientExecutor();
        Map<String, String> headers = new HashMap<>(4);
        headers.put(HttpHeaders.AUTHORIZATION, "APPCODE " + appCode);
        Map<String, String> queryParams = new HashMap<>(4);
        queryParams.put(AddressConstant.PARAM_IP, ip);
        GaodeAddressDTO response = executor.get(
            AddressConstant.GAODEYUNTU_URL_STR,
            queryParams,
            headers,
            GaodeAddressDTO.class
        );
        return this.toAddress(response == null ? null : response.getProvince(), response == null ? null : response.getCity());
    }

    @Override
    public Optional<AddressVO> getAddressByIp138(String ip, String token) {
        HttpClientExecutor executor = this.httpClientExecutorStrategyFactory.getDefaultHttpClientExecutor();
        Map<String, String> headers = new HashMap<>(4);
        headers.put(AddressConstant.PARAM_TOKEN, token);
        Map<String, String> queryParams = new HashMap<>(4);
        queryParams.put(AddressConstant.PARAM_IP, ip);
        Ip138AddressDTO response = executor.get(
            AddressConstant.IP138_URL_STR,
            queryParams,
            headers,
            Ip138AddressDTO.class
        );
        if (response == null || response.getData() == null
            || response.getData().size() < AddressConstant.IP138_MIN_DATA_SIZE) {
            return Optional.empty();
        }
        return this.toAddress(
            response.getData().get(AddressConstant.IP138_PROVINCE_INDEX),
            response.getData().get(AddressConstant.IP138_CITY_INDEX)
        );
    }

    @Override
    public Optional<AddressVO> getAddressByBaidu(String ip, String ak) {
        HttpClientExecutor executor = this.httpClientExecutorStrategyFactory.getDefaultHttpClientExecutor();
        Map<String, String> queryParams = new HashMap<>(4);
        queryParams.put(AddressConstant.PARAM_IP, ip);
        queryParams.put(AddressConstant.PARAM_AK, ak);
        BaiduAddressDTO response = executor.get(
            AddressConstant.BAIDU_URL_STR,
            queryParams,
            null,
            BaiduAddressDTO.class
        );
        BaiduAddressDTO.AddressDetail addressDetail = response == null || response.getContent() == null
            ? null : response.getContent().getAddressDetail();
        return this.toAddress(
            addressDetail == null ? null : addressDetail.getProvince(),
            addressDetail == null ? null : addressDetail.getCity()
        );
    }

    @Override
    public Optional<AddressVO> getAddressByTencent(String ip, String key) {
        HttpClientExecutor executor = this.httpClientExecutorStrategyFactory.getDefaultHttpClientExecutor();
        Map<String, String> queryParams = new HashMap<>(4);
        queryParams.put(AddressConstant.PARAM_IP, ip);
        queryParams.put(AddressConstant.PARAM_KEY, key);
        TencentAddressDTO response = executor.get(
            AddressConstant.TENCENT_URL_STR,
            queryParams,
            null,
            TencentAddressDTO.class
        );
        TencentAddressDTO.AdInfo adInfo = response == null || response.getResult() == null
            ? null : response.getResult().getAdInfo();
        return this.toAddress(
            adInfo == null ? null : adInfo.getProvince(),
            adInfo == null ? null : adInfo.getCity()
        );
    }

    /**
     * 将省份和城市转换为地址对象.
     *
     * @param province 省份
     * @param city 城市
     * @return 地址信息
     */
    private Optional<AddressVO> toAddress(String province, String city) {
        if (province == null || city == null) {
            return Optional.empty();
        }
        return Optional.of(AddressVO.builder().province(province).city(city).build());
    }

}

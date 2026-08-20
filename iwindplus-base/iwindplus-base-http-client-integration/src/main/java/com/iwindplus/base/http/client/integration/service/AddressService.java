/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.integration.service;

import com.iwindplus.base.http.client.integration.domain.vo.AddressVO;
import java.util.Optional;

/**
 * 地址服务接口.
 *
 * @author zengdegui
 * @since 2025/08/20
 */
public interface AddressService {

    /**
     * 根据IP获取地址信息（太平洋网络）.
     *
     * @param ip ip（必填）
     * @return Optional<AddressVO>
     */
    Optional<AddressVO> getAddressByPconline(String ip);

    /**
     * 根据IP获取地址信息（高德云图）.
     *
     * @param ip      ip（必填）
     * @param appCode 应用code（必填）
     * @return Optional<AddressVO>
     */
    Optional<AddressVO> getAddressByGaode(String ip, String appCode);

    /**
     * 根据IP获取地址信息（IP138）.
     *
     * @param ip    ip（必填）
     * @param token token（必填）
     * @return Optional<AddressVO>
     */
    Optional<AddressVO> getAddressByIp138(String ip, String token);

    /**
     * 根据IP获取地址信息（百度地图）.
     *
     * @param ip ip（必填）
     * @param ak 百度地图ak（必填）
     * @return Optional<AddressVO>
     */
    Optional<AddressVO> getAddressByBaidu(String ip, String ak);

    /**
     * 根据IP获取地址信息（腾讯地图）.
     *
     * @param ip  ip（必填）
     * @param key 腾讯地图key（必填）
     * @return Optional<AddressVO>
     */
    Optional<AddressVO> getAddressByTencent(String ip, String key);
}

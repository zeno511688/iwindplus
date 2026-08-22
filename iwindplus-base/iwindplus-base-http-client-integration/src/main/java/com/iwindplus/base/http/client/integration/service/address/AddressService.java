/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.integration.service.address;

import com.iwindplus.base.http.client.integration.domain.enums.AddressProviderEnum;
import com.iwindplus.base.http.client.integration.domain.vo.address.AddressVO;

import java.util.List;
import java.util.Optional;

/**
 * 地址服务接口.
 *
 * @author zengdegui
 * @since 2025/08/20
 */
public interface AddressService {

    /**
     * 根据IP获取地址信息（自动路由，支持故障转移）.
     * 按配置的优先级依次尝试各个提供商，直到成功为止.
     *
     * @param ip ip（必填）
     * @return Optional<AddressVO>
     */
    Optional<AddressVO> getAddress(String ip);

    /**
     * 根据IP获取地址信息（指定提供商）.
     *
     * @param ip       ip（必填）
     * @param provider 提供商（必填）
     * @return Optional<AddressVO>
     */
    Optional<AddressVO> getAddress(String ip, AddressProviderEnum provider);

    /**
     * 获取所有可用的地址服务提供商.
     *
     * @return 提供商列表
     */
    List<AddressProviderEnum> getAvailableProviders();
}

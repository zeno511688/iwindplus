/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.integration.support.adress;

import com.iwindplus.base.http.client.integration.domain.enums.AddressProviderEnum;
import com.iwindplus.base.http.client.integration.domain.vo.address.AddressVO;

import java.util.Optional;

/**
 * 地址服务执行策略接口.
 *
 * @author zengdegui
 * @since 2026/08/21
 */
public interface AddressExecuteHandler {

    /**
     * 获取提供商类型.
     *
     * @return 提供商枚举
     */
    AddressProviderEnum getProvider();

    /**
     * 根据IP查询地址信息.
     *
     * @param ip IP地址
     * @return 地址信息
     */
    Optional<AddressVO> queryAddress(String ip);

    /**
     * 健康检查.
     *
     * @return 是否健康
     */
    default boolean isHealthy() {
        return true;
    }

    /**
     * 获取优先级.
     *
     * @return 优先级（数字越小优先级越高）
     */
    default int getPriority() {
        return getProvider().getPriority();
    }
}

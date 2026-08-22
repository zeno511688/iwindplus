/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.integration.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 地址服务提供商枚举.
 *
 * @author zengdegui
 * @since 2026/08/21
 */
@Getter
@AllArgsConstructor
public enum AddressProviderEnum {

    /**
     * 百度地图.
     */
    BAIDU("baidu", "百度地图", 1),

    /**
     * 高德地图.
     */
    GAODE("gaode", "高德地图", 2),

    /**
     * 腾讯地图.
     */
    TENCENT("tencent", "腾讯地图", 3),

    /**
     * IP138.
     */
    IP138("ip138", "IP138", 4),

    /**
     * 太平洋网络（不推荐）.
     */
    PCONLINE("pconline", "太平洋网络", 99);

    /**
     * 提供商编码.
     */
    private final String code;

    /**
     * 提供商名称.
     */
    private final String name;

    /**
     * 优先级（数字越小优先级越高）.
     */
    private final int priority;

    /**
     * 根据编码获取枚举.
     *
     * @param code 编码
     * @return 枚举
     */
    public static AddressProviderEnum getByCode(String code) {
        for (AddressProviderEnum provider : values()) {
            if (provider.getCode().equals(code)) {
                return provider;
            }
        }
        return null;
    }
}

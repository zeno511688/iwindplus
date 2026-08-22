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
 * KYC服务提供商枚举.
 *
 * @author zengdegui
 * @since 2026/08/21
 */
@Getter
@AllArgsConstructor
public enum KycProviderEnum {

    /**
     * SumSub.
     */
    SUMSUB("sumsub", "SumSub", 1),
    ;

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
    public static KycProviderEnum getByCode(String code) {
        for (KycProviderEnum provider : values()) {
            if (provider.getCode().equals(code)) {
                return provider;
            }
        }
        return null;
    }
}

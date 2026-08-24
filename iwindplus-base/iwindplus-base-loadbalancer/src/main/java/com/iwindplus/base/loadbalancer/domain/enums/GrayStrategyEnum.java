/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.loadbalancer.domain.enums;

import com.iwindplus.base.domain.enums.BaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 灰度策略类型枚举.
 *
 * @author zengdegui
 * @since 2024/08/24
 */
@Getter
@RequiredArgsConstructor
public enum GrayStrategyEnum implements BaseEnum<String> {

    /**
     * 白名单策略.
     */
    WHITELIST("whitelist", "白名单策略"),

    /**
     * 百分比策略.
     */
    PERCENTAGE("percentage", "百分比策略");

    /**
     * 值.
     */
    private final String value;

    /**
     * 描述.
     */
    private final String desc;
}

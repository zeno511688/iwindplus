/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.rocket.domain.enums;

import com.iwindplus.base.domain.enums.BaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 选择器类型枚举定义.
 *
 * @author zengdegui
 * @since 2024/11/25 23:33
 */
@Getter
@RequiredArgsConstructor
public enum ConsumeModeEnum implements BaseEnum<String> {
    /**
     * Receive asynchronously delivered messages concurrently
     */
    CONCURRENTLY("CONCURRENTLY", "同时"),

    /**
     * Receive asynchronously delivered messages orderly. one queue, one thread
     */
    ORDERLY("ORDERLY", "顺序");

    /**
     * 值.
     */
    private final String value;

    /**
     * 描述.
     */
    private final String desc;

}

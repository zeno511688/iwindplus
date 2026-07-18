/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.redis.domain.enums;

import com.iwindplus.base.domain.enums.BaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 幂等结果处理模式枚举.
 *
 * @author zengdegui
 * @since 2026/04/29 21:48
 */
@Getter
@RequiredArgsConstructor
public enum IdempotentResultModeEnum implements BaseEnum<String> {

    /**
     * 返回历史结果
     */
    RETURN_CACHE("return_cache", "返回历史结果"),

    /**
     * 抛业务重复异常
     */
    THROW_ERROR("throw_error", "抛业务重复异常"),

    /**
     * 等待执行完成
     */
    WAIT("wait", "等待执行完成"),

    ;

    /**
     * 值
     */
    private final String value;

    /**
     * 描述
     */
    private final String desc;
}

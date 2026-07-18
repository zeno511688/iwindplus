/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 启用状态枚举.
 *
 * @author zengdegui
 * @since 2018/10/11
 */
@Getter
@RequiredArgsConstructor
public enum EnableStatusEnum implements BaseEnum<Integer> {

    /**
     * 禁用.
     */
    DISABLE(0, "禁用"),

    /**
     * 启用.
     */
    ENABLE(1, "启用"),

    /**
     * 锁定.
     */
    LOCKED(2, "锁定");

    /**
     * 值.
     */
    @EnumValue
    private final Integer value;

    /**
     * 描述.
     */
    private final String desc;

}

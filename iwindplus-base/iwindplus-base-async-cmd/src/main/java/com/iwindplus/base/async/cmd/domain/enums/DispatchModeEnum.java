/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.iwindplus.base.domain.enums.BaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 调度类型枚举定义.
 *
 * @author zengdegui
 * @since 2025/9/14
 */
@Getter
@RequiredArgsConstructor
public enum DispatchModeEnum implements BaseEnum<Integer> {
    /**
     * 异步.
     */
    ASYNC(0, "异步"),

    /**
     * 调度中心.
     */
    CENTER(1, "调度中心"),

    /**
     * 未知.
     */
    UNKNOWN(2, "未知"),
    ;

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

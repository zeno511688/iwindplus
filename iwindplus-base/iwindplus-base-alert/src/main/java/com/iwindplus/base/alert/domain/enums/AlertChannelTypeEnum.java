/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.alert.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.iwindplus.base.domain.enums.BaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 告警渠道类型枚举.
 *
 * @author zengdegui
 * @since 2026/01/17 22:25
 */
@Getter
@RequiredArgsConstructor
public enum AlertChannelTypeEnum implements BaseEnum<Integer> {

    /**
     * 飞书.
     */
    FEI_SHU(0, "飞书"),

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

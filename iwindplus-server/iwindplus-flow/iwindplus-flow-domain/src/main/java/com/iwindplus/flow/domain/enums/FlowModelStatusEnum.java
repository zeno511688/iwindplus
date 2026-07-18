/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.iwindplus.base.domain.enums.BaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 实例状态枚举定义.
 *
 * @author zengdegui
 * @since 2018/10/10
 */
@Getter
@RequiredArgsConstructor
public enum FlowModelStatusEnum implements BaseEnum<Integer> {
    /**
     * 待发布.
     */
    TO_BE_PUBLISHED(0, "待发布"),

    /**
     * 已发布.
     */
    PUBLISHED(1, "已发布"),

    /**
     * 已停用.
     */
    DISABLED(2, "已停用"),

    /**
     * 历史版本.
     */
    HISTORY(3, "历史版本"),
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

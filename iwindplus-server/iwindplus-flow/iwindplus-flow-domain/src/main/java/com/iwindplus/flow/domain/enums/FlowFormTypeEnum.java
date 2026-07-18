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
 * 流程表单类型枚举定义.
 *
 * @author zengdegui
 * @since 2018/10/10
 */
@Getter
@RequiredArgsConstructor
public enum FlowFormTypeEnum implements BaseEnum<Integer> {
    /**
     * 无表单.
     */
    NONE(0, "无表单"),

    /**
     * 表单.
     */
    FORM(1, "表单"),

    /**
     * 自定义.
     */
    CUSTOM(2, "自定义"),

    /**
     * 固定格式.
     */
    FIXED(3, "固定格式"),

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

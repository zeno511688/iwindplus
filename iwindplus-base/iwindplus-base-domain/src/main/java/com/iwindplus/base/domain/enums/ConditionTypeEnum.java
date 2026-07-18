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
 * 条件类型枚举定义.
 *
 * @author zengdegui
 * @since 2018/10/10
 */
@Getter
@RequiredArgsConstructor
public enum ConditionTypeEnum implements BaseEnum<String> {

    /**
     * 等于.
     */
    EQ("eq", "等于"),

    /**
     * 大于等于.
     */
    GE("ge", "大于等于"),

    /**
     * 大于.
     */
    GT("gt", "大于"),

    /**
     * 小于等于.
     */
    LE("le", "小于等于"),

    /**
     * 包含.
     */
    LIKE("like", "包含"),

    /**
     * 小于.
     */
    LT("lt", "小于"),

    /**
     * 不等于.
     */
    NE("ne", "不等于"),

    /**
     * 不包含.
     */
    NOT_LIKE("not_like", "不包含");

    /**
     * 值.
     */
    @EnumValue
    private final String value;

    /**
     * 描述.
     */
    private final String desc;
}

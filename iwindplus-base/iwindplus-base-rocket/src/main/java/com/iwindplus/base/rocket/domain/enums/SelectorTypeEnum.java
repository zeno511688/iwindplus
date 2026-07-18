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
import org.apache.rocketmq.common.filter.ExpressionType;

/**
 * 选择器类型枚举定义.
 *
 * @author zengdegui
 * @since 2024/11/25 23:33
 */
@Getter
@RequiredArgsConstructor
public enum SelectorTypeEnum implements BaseEnum<String> {

    /**
     * @see ExpressionType#TAG
     */
    TAG("TAG", "标签"),

    /**
     * @see ExpressionType#SQL92
     */
    SQL92("SQL92", "SQL92");

    /**
     * 值.
     */
    private final String value;

    /**
     * 描述.
     */
    private final String desc;

}

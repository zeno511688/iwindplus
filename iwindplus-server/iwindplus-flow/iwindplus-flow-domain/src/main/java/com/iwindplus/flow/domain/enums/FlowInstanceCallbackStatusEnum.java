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
 * 流程实例回调状态枚举.
 *
 * @author zengdegui
 * @since 2021/1/31
 */
@Getter
@RequiredArgsConstructor
public enum FlowInstanceCallbackStatusEnum implements BaseEnum<Integer> {
    /**
     * 待处理.
     */
    PENDING(0, "待处理"),

    /**
     * 完成.
     */
    COMPLETE(1, "完成"),

    /**
     * 失败.
     */
    FAILED(2, "失败"),

    /**
     * 丢弃.
     */
    DISCARD(3, "丢弃"),

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

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
 * 流程实例状态枚举.
 *
 * @author zengdegui
 * @since 2021/1/31
 */
@Getter
@RequiredArgsConstructor
public enum FlowInstanceQueryTypeEnum implements BaseEnum<Integer> {

    /**
     * 我的发起.
     */
    MY_INITIATED(0, "我的发起"),

    /**
     * 我的已办.
     */
    MY_DONE(1, "审批中"),

    /**
     * 抄送我的.
     */
    MY_CC(2, "抄送我的"),

    /**
     * 所有.
     */
    ALL(3, "所有"),

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

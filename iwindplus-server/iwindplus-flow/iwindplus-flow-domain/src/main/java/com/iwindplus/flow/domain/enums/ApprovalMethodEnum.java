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
 * 审批方式枚举定义.
 *
 * @author zengdegui
 * @since 2018/10/10
 */
@Getter
@RequiredArgsConstructor
public enum ApprovalMethodEnum implements BaseEnum<Integer> {
    /**
     * 按顺序依次审批.
     */
    SEQ_SIGN(0, "按顺序依次审批"),

    /**
     * 会签（可同时审批，所以人必须审批通过).
     */
    COUNTER_SIGN(1, "会签（可同时审批，所以人必须审批通过）"),

    /**
     * 或签（有一人审批通过即可）.
     */
    OR_SIGN(2, "或签（有一人审批通过即可）"),

    /**
     * 票签（类似投票，达到比例即通过）.
     */
    TICKET_SIGN(3, "票签（类似投票，达到比例即通过）"),

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

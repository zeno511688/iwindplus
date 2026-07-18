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
 * 任务状态枚举定义.
 *
 * @author zengdegui
 * @since 2018/10/10
 */
@Getter
@RequiredArgsConstructor
public enum FlowTaskStatusEnum implements BaseEnum<Integer> {
    /**
     * 审批中.
     */
    APPROVAL(0, "审批中"),

    /**
     * 已审核.
     */
    AUDITED(1, "已审核"),

    /**
     * 已驳回.
     */
    REJECTED(2, "已驳回"),

    /**
     * 已撤销.
     */
    REVOKED(3, "已撤销"),

    /**
     * 已终止.
     */
    TERMINATED(4, "已终止"),
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

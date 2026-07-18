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
 * 流程任务事件类型枚举.
 *
 * @author zengdegui
 * @since 2026/05/20 23:36
 */
@Getter
@RequiredArgsConstructor
public enum FlowTaskEventTypeEnum implements BaseEnum<Integer> {

    /**
     * 任务已创建（有新的待办）.
     */
    TASK_CREATED(0, "任务已创建"),

    /**
     * 任务审批通过.
     */
    TASK_APPROVED(1, "任务审批通过"),

    /**
     * 任务驳回（终止流程）.
     */
    TASK_REJECTED(2, "任务驳回"),

    /**
     * 任务退回到指定节点.
     */
    TASK_REJECTED_TO_NODE(3, "任务退回到指定节点"),

    /**
     * 任务已转交.
     */
    TASK_TRANSFERRED(4, "任务已转交"),

    /**
     * 任务已委托.
     */
    TASK_DELEGATED(5, "任务已委托"),

    /**
     * 任务已跳转.
     */
    TASK_JUMPED(6, "任务已跳转");

    /**
     * 枚举值.
     */
    @EnumValue
    private final Integer value;

    /**
     * 描述.
     */
    private final String desc;
}
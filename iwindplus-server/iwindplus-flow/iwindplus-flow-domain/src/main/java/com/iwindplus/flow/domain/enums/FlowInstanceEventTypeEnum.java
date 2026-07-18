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
 * 流程实例事件类型枚举.
 *
 * @author zengdegui
 * @since 2026/05/20 23:36
 */
@Getter
@RequiredArgsConstructor
public enum FlowInstanceEventTypeEnum implements BaseEnum<Integer> {

    /**
     * 流程启动.
     */
    INSTANCE_STARTED(0, "流程启动"),

    /**
     * 流程完成.
     */
    INSTANCE_FINISHED(1, "流程完成"),

    /**
     * 流程驳回.
     */
    INSTANCE_REJECTED(2, "流程驳回"),

    /**
     * 流程撤销.
     */
    INSTANCE_REVOKED(3, "流程撤销"),

    /**
     * 流程终止.
     */
    INSTANCE_TERMINATED(4, "流程终止");

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
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
 * 流程节点类型枚举定义.
 *
 * @author zengdegui
 * @since 2018/10/10
 */
@Getter
@RequiredArgsConstructor
public enum FlowNodeTypeEnum implements BaseEnum<Integer> {
    /**
     * 开始节点
     */
    START(0, "开始节点"),

    /**
     * 结束节点.
     */
    END(1, "结束节点"),

    /**
     * 审批节点
     */
    APPROVAL_NODE(2, "审批节点"),

    /**
     * 条件节点.
     */
    CONDITION_NODE(3, "条件节点"),

    /**
     * 抄送节点.
     */
    CC_NODE(4, "抄送节点"),

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

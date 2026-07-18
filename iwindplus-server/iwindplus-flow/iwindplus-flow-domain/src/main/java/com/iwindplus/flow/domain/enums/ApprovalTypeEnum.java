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
public enum ApprovalTypeEnum implements BaseEnum<Integer> {
    /**
     * 自动通过.
     */
    AUTO_PASS(0, "自动通过"),

    /**
     * 自动拒绝.
     */
    AUTO_REJECT(1, "自动拒绝"),

    /**
     * 人工审批.
     */
    MANUAL_APPROVAL(2, "人工审批"),

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

/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.dtx.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.iwindplus.base.domain.enums.BaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 分支事务状态枚举.
 *
 * @author zengdegui
 * @since 2026/02/05 22:18
 */
@Getter
@RequiredArgsConstructor
public enum BranchTxStatusEnum implements BaseEnum<Integer> {

    /**
     * Try尝试中.
     */
    TRYING(0, "Try尝试中"),

    /**
     * Try成功.
     */
    TRY_SUCCESS(1, "Try成功"),

    /**
     * Try失败.
     */
    TRY_FAIL(2, "Try失败"),

    /**
     * Confirm处理中.
     */
    CONFIRMING(3, "Confirm处理中"),

    /**
     * Confirm成功.
     */
    CONFIRM_SUCCESS(4, "Confirm成功"),

    /**
     * Confirm失败.
     */
    CONFIRM_FAIL(5, "Confirm失败"),

    /**
     * Cancel处理中.
     */
    CANCELING(6, "Cancel处理中"),

    /**
     * Cancel成功.
     */
    CANCEL_SUCCESS(7, "Cancel成功"),

    /**
     * Cancel失败.
     */
    CANCEL_FAIL(8, "Cancel失败");;

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

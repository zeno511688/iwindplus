/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.dtx.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.iwindplus.base.domain.enums.BaseEnum;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 全局事务状态枚举.
 *
 * @author zengdegui
 * @since 2026/02/05 22:13
 */
@Getter
@RequiredArgsConstructor
public enum GlobalTxStatusEnum implements BaseEnum<Integer> {

    /**
     * Try尝试中.
     */
    TRYING(0, "Try尝试中"),

    /**
     * Confirm处理中.
     */
    CONFIRMING(1, "Confirm处理中"),

    /**
     * Confirm成功.
     */
    CONFIRM_SUCCESS(2, "Confirm成功"),

    /**
     * Confirm失败.
     */
    CONFIRM_FAIL(3, "Confirm失败"),

    /**
     * Cancel处理中.
     */
    CANCELING(4, "Cancel处理中"),

    /**
     * Cancel成功.
     */
    CANCEL_SUCCESS(5, "Cancel成功"),

    /**
     * Cancel失败.
     */
    CANCEL_FAIL(6, "Cancel失败"),

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

    /**
     * 获取需要重试的
     *
     * @return List<GlobalTxStatusEnum>
     */
    public static List<GlobalTxStatusEnum> getRetryStatus() {
        return List.of(GlobalTxStatusEnum.CONFIRMING, GlobalTxStatusEnum.CONFIRM_FAIL,
            GlobalTxStatusEnum.CANCELING, GlobalTxStatusEnum.CANCEL_FAIL);
    }
}

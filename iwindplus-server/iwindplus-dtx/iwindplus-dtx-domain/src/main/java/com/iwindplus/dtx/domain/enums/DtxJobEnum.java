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
 * 分布式事务job枚举.
 *
 * @author zengdegui
 * @since 2018/12/27
 */
@Getter
@RequiredArgsConstructor
public enum DtxJobEnum implements BaseEnum<Integer> {

    /**
     * 超时任务（用于定时任务扫描 TRYING 状态超时事务，并自动触发 cancel）.
     */
    TIMEOUT_JOB(0, "超时任务"),

    /**
     * 重试任务：（Confirm 重试，Cancel 重试）.
     */
    RETRY_JOB(1, "重试任务"),

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

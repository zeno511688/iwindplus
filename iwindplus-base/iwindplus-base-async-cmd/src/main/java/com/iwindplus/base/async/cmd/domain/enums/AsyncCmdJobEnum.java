/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.iwindplus.base.domain.enums.BaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 异步命令job枚举.
 *
 * @author zengdegui
 * @since 2018/12/27
 */
@Getter
@RequiredArgsConstructor
public enum AsyncCmdJobEnum implements BaseEnum<Integer> {
    /**
     * 重置任务：各种系统崩溃，执行超时状态，执行时间重置.
     */
    RESET_JOB(0, "重置任务"),

    /**
     * 重试任务：所有任务执行失败后，由该任务重试执行.
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

/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.export.task.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.iwindplus.base.domain.enums.BaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 导出任务job枚举.
 *
 * @author zengdegui
 * @since 2026/08/28
 */
@Getter
@RequiredArgsConstructor
public enum ExportTaskJobEnum implements BaseEnum<Integer> {
    /**
     * 重试任务：所有未完成任务按调度时间重新执行.
     */
    RETRY_JOB(0, "重试任务"),

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

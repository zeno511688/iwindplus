/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.document.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.iwindplus.base.domain.enums.BaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 文档任务job枚举.
 *
 * @author zengdegui
 * @since 2026/08/28
 */
@Getter
@RequiredArgsConstructor
public enum DocumentTaskJobEnum implements BaseEnum<Integer> {
    /**
     * 导出任务.
     */
    EXPORT_EXCEL_JOB(0, "导出excel任务"),

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

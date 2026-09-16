/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.ocr.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.iwindplus.base.domain.enums.BaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * ocr类型枚举定义.
 *
 * @author zengdegui
 * @since 2026/9/1
 */
@Getter
@RequiredArgsConstructor
public enum OcrTypeEnum implements BaseEnum<Integer> {
    /**
     * 印刷文字.
     */
    PRINT_WORD(0, "印刷文字"),

    /**
     * 翔云.
     */
    XIANGYUN(1, "翔云"),

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

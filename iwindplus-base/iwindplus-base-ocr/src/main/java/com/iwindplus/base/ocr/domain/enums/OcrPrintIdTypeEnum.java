/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.ocr.domain.enums;

import com.iwindplus.base.domain.enums.BaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 印刷文字ocr证件类型枚举.
 *
 * @author zengdegui
 * @since 2018/10/11
 */
@Getter
@RequiredArgsConstructor
public enum OcrPrintIdTypeEnum implements BaseEnum<String> {
    /**
     * 二代身份证正面.
     */
    SECOND_ID_CARD_FRONT("face", "二代身份证正面"),

    /**
     * 二代身份证背面.
     */
    SECOND_ID_CARD_BACK("back", "二代身份证背面"),
    ;

    /**
     * 值.
     */
    private final String value;

    /**
     * 描述.
     */
    private final String desc;
}

/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.iwindplus.base.domain.enums.BaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * .
 *
 * @author zengdegui
 * @since 2026/01/19 23:22
 */
@Getter
@RequiredArgsConstructor
public enum HttpBodyTypeEnum implements BaseEnum<String> {

    /**
     * application/json
     */
    JSON("json", "JSON 格式"),

    /**
     * application/x-www-form-urlencoded
     */
    FORM("form", "表单格式"),

    /**
     * multipart/form-data
     */
    MULTIPART("multipart", "文件上传格式"),

    /**
     * 无请求体（GET / DELETE）
     */
    NONE("none", "无请求体");

    /**
     * 值.
     */
    @EnumValue
    private final String value;

    /**
     * 描述.
     */
    private final String desc;
}

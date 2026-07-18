/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 请求方式枚举定义.
 *
 * @author zengdegui
 * @since 2018/10/10
 */
@Getter
@RequiredArgsConstructor
public enum RequestMethodEnum implements BaseEnum<String> {

    /**
     * GET.
     */
    GET("GET", "GET"),

    /**
     * POST.
     */
    POST("POST", "POST"),

    /**
     * PUT.
     */
    PUT("PUT", "PUT"),

    /**
     * PATCH.
     */
    PATCH("PATCH", "PATCH"),

    /**
     * DELETE.
     */
    DELETE("DELETE", "DELETE");

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
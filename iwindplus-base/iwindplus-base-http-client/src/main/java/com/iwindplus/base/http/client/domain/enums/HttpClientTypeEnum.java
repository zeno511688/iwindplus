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
 * http 客户端类型.
 *
 * @author zengdegui
 * @since 2026/01/17 22:25
 */
@Getter
@RequiredArgsConstructor
public enum HttpClientTypeEnum implements BaseEnum<Integer> {

    /**
     * httpClient.
     */
    HTTP_CLIENT(0, "httpClient"),

    /**
     * restClient.
     */
    REST_CLIENT(1, "restClient"),

    /**
     * okHttp.
     */
    OK_HTTP(2, "okHttp"),

    /**
     * webClient.
     */
    WEB_CLIENT(3, "webClient"),

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

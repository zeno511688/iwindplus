/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.gateway.infrastructure.configuration.enums;

import com.iwindplus.base.domain.enums.BaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Token模式枚举定义.
 *
 * @author zengdegui
 * @since 2018/10/10
 */
@Getter
@RequiredArgsConstructor
public enum AuthTokenModeEnum implements BaseEnum<String> {

    /**
     * JWT.
     */
    JWT("JWT", "JWT"),

    /**
     * 不透明令牌（只生成id）
     */
    OPAQUE("OPAQUE", "不透明令牌（只生成id）"),
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

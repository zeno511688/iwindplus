/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.loadbalancer.domain.enums;

import com.iwindplus.base.domain.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 版本类型枚举.
 *
 * @author zengdegui
 * @since 2024/08/24
 */
@Getter
@RequiredArgsConstructor
public enum VersionTypeEnum implements BaseEnum<String> {

    /**
     * 灰度版本.
     */
    GRAY("gray", "灰度版本"),

    /**
     * 稳定版本.
     */
    STABLE("stable", "稳定版本");

    /**
     * 值.
     */
    private final String value;

    /**
     * 描述.
     */
    private final String desc;
}

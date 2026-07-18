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
 * 平台类型类型枚举定义.
 *
 * @author zengdegui
 * @since 2018/10/10
 */
@Getter
@RequiredArgsConstructor
public enum PlatformTypeEnum implements BaseEnum<Integer> {

    /**
     * 后台管理端.
     */
    MGT(0, "后台管理端"),

    /**
     * web端.
     */
    WEB(1, "web端"),

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
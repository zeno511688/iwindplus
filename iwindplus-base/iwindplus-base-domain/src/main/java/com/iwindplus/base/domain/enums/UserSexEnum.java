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
 * 用户性别枚举.
 *
 * @author zengdegui
 * @since 2018/10/10
 */
@Getter
@RequiredArgsConstructor
public enum UserSexEnum implements BaseEnum<Integer> {

    /**
     * 未知.
     */
    UNKNOWN(0, "未知"),

    /**
     * 男.
     */
    MALE(1, "男"),

    /**
     * 女.
     */
    FEMALE(2, "女");

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

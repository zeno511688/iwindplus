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
 * 短信类型枚举定义.
 *
 * @author zengdegui
 * @since 2024/11/17 11:16
 */

@Getter
@RequiredArgsConstructor
public enum SmsTypeEnum implements BaseEnum<Integer> {
    /**
     * 阿里云.
     */
    ALIYUN(0, "阿里云"),

    /**
     * 七牛云.
     */
    QINIU(1, "七牛云"),

    /**
     * 麦讯通.
     */
    MXTONG(2, "麦讯通"),

    /**
     * 凌凯.
     */
    LINGKAI(3, "凌凯"),

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

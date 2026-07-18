/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.im.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.iwindplus.base.domain.enums.BaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 发送状态枚举.
 *
 * @author zengdegui
 * @since 2021/7/8
 */
@Getter
@RequiredArgsConstructor
public enum SendStatusEnum implements BaseEnum<Integer> {
    /**
     * 发送失败
     */
    FAILED(-1, "发送失败"),

    /**
     * 待发送
     */
    TO_BE_SENT(0, "待发送"),

    /**
     * 发送成功.
     */
    SUCCESS(1, "发送成功"),

    /**
     * 发送中
     */
    SENDING(2, "发送中"),
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

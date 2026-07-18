/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.im.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.iwindplus.base.domain.enums.BaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 消息状态枚举.
 *
 * @author zengdegui
 * @since 2021/1/31
 */
@Getter
@RequiredArgsConstructor
public enum MsgStatusEnum implements BaseEnum<Integer> {
    /**
     * 未读.
     */
    UN_READ(0, "未读"),

    /**
     * 已读.
     */
    READ(1, "已读"),

    /**
     * 已回收.
     */
    RECYCLED(2, "已回收"),

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

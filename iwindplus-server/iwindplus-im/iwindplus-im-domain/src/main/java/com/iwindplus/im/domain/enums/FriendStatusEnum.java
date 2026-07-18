/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.im.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.iwindplus.base.domain.enums.BaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 好友状态枚举.
 *
 * @author zengdegui
 * @since 2021/1/31
 */
@Getter
@RequiredArgsConstructor
public enum FriendStatusEnum implements BaseEnum<Integer> {
    /**
     * 待确认.
     */
    UN_CONFIRMED(0, "待确认"),

    /**
     * 已通过.
     */
    PASSED(1, "已通过"),

    /**
     * 已拒绝.
     */
    REJECTED(2, "已拒绝");

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

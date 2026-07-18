/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.im.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.iwindplus.base.domain.enums.BaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 聊天群加入类型枚举.
 *
 * @author zengdegui
 * @since 2021/7/8
 */
@Getter
@RequiredArgsConstructor
public enum JoinTypeEnum implements BaseEnum<Integer> {
    /**
     * 邀请.
     */
    INVITE(0, "邀请"),

    /**
     * 扫码
     */
    SCAN_QRCODE(1, "扫码"),

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

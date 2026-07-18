/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.alert.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.iwindplus.base.domain.enums.BaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 告警消息类型枚举.
 *
 * @author zengdegui
 * @since 2026/01/17 22:25
 */
@Getter
@RequiredArgsConstructor
public enum AlertMessageTypeEnum implements BaseEnum<Integer> {

    /**
     * 企业应用消息.
     */
    APP(0, "企业应用消息"),

    /**
     * Webhook消息.
     */
    WEBHOOK(1, "Webhook消息"),
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

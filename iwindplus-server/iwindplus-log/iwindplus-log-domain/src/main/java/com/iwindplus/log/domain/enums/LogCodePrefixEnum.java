/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.log.domain.enums;

import com.iwindplus.base.domain.enums.BaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 编码前缀枚举.
 *
 * @author zengdegui
 * @since 2018/10/10
 */
@Getter
@RequiredArgsConstructor
public enum LogCodePrefixEnum implements BaseEnum<String> {
    /**
     * 邮箱前缀.
     */
    MAIL_PREFIX("mail_", "邮箱前缀"),

    /**
     * 短信前缀.
     */
    SMS_PREFIX("sms_", "短信前缀"),
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

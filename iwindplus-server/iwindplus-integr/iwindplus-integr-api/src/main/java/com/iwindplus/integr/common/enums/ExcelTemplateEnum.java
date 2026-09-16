/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.integr.common.enums;

import com.iwindplus.base.domain.enums.BaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * excel模板枚举.
 *
 * @author zengdegui
 * @since 2021/7/8
 */
@Getter
@RequiredArgsConstructor
public enum ExcelTemplateEnum implements BaseEnum<String> {

    /**
     * 对象存储模板.
     */
    OSS_CONFIG_TEMPLATE("static/excel/oss_tpl.xlsx", "对象存储模板"),

    /**
     * 短信模板.
     */
    SMS_CONFIG_TEMPLATE("static/excel/sms_tpl.xlsx", "短信模板"),

    /**
     * 邮箱模板.
     */
    MAIL_CONFIG_TEMPLATE("static/excel/mail_tpl.xlsx", "邮箱模板"),

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

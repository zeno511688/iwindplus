/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.setup.domain.enums;

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
     * 对象存储配置模板.
     */
    OSS_CONFIG_TEMPLATE("static/excel/oss_config.xlsx", "对象存储配置模板"),

    /**
     * 短信配置模板.
     */
    SMS_CONFIG_TEMPLATE("static/excel/sms_config.xlsx", "短信配置模板"),

    /**
     * 邮箱配置模板.
     */
    MAIL_CONFIG_TEMPLATE("static/excel/mail_config.xlsx", "邮箱配置模板"),

    /**
     * 微信小程序配置模板.
     */
    WECHAT_CONFIG_MA_TEMPLATE("static/excel/wechat_config_ma.xlsx", "微信小程序配置模板"),

    /**
     * 微信公众号配置模板.
     */
    WECHAT_CONFIG_MP_TEMPLATE("static/excel/wechat_config_mp.xlsx", "微信公众号配置模板"),

    /**
     * 视频点播配置模板.
     */
    VOD_CONFIG_TEMPLATE("static/excel/vod_config.xlsx", "视频点播配置模板"),
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

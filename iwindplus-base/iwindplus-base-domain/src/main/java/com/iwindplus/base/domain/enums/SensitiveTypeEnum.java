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
 * 脱敏类型.
 *
 * @author zengdegui
 * @since 2024/11/26 23:55
 */
@Getter
@RequiredArgsConstructor
public enum SensitiveTypeEnum implements BaseEnum<String> {

    /**
     * 自定义.
     */
    CUSTOM("custom", "自定义"),

    /**
     * 用户主键.
     */
    USER_ID("userId", "用户主键"),

    /**
     * 中文名.
     */
    CHINESE_NAME("chineseName", "中文名"),

    /**
     * 身份证号.
     */
    ID_CARD("idCard", "身份证号"),

    /**
     * 座机号.
     */
    FIXED_PHONE("fixedPhone", "座机号"),

    /**
     * 手机号.
     */
    MOBILE_PHONE("mobilePhone", "手机号"),

    /**
     * 地址.
     */
    ADDRESS("address", "地址"),

    /**
     * 电子邮件.
     */
    EMAIL("email", "电子邮件"),

    /**
     * 密码.
     */
    PASSWORD("password", "密码"),

    /**
     * 车牌.
     */
    CAR_LICENSE("carLicense", "车牌"),

    /**
     * 银行卡.
     */
    BANK_CARD("bankCard", "银行卡"),

    /**
     * ipv4.
     */
    IPV4("ipv4", "ipv4"),

    /**
     * ipv6.
     */
    IPV6("ipv6", "ipv6"),

    /**
     * 只显示第一个.
     */
    FIRST_MASK("firstMask", "只显示第一个"),

    /**
     * 清空为空串.
     */
    CLEAR_TO_EMPTY("clearToEmpty", "清空为空串"),

    /**
     * 清空为null.
     */
    CLEAR_TO_NULL("clearToNull", "清空为null");

    /**
     * 值.
     */
    @EnumValue
    private final String value;

    /**
     * 描述.
     */
    private final String desc;

}

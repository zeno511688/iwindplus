package com.iwindplus.log.domain.enums;

import com.iwindplus.base.domain.exception.CommonException;
import lombok.Getter;

/**
 * 业务编码枚举.
 *
 * @author zengdegui
 * @since 2024/04/30 14:47
 */
@Getter
public enum LogCodeEnum implements CommonException {
    /**
     * 验证码错误.
     */
    CAPTCHA_ERROR("captcha_error", "验证码错误"),

    /**
     * 验证码过期.
     */
    CAPTCHA_EXPIRED("captcha_expired", "验证码过期"),

    /**
     * 验证码未过期.
     */
    CAPTCHA_NOT_EXPIRED("captcha_not_expired", "验证码未过期"),

    /**
     * 验证码只能使用一次.
     */
    CAPTCHA_CAN_USE_ONCE("captcha_can_user_once", "验证码只能使用一次"),

    /**
     * 验证码每天发送次数不超过{0}次.
     */
    CAPTCHA_LIMIT_DAY("captcha_limit_day", "验证码每天发送次数不超过{0}次"),

    /**
     * 验证码每小时发送次数不超过{0}次.
     */
    CAPTCHA_LIMIT_HOUR("captcha_limit_hour", "验证码每小时发送次数不超过{0}次"),

    /**
     * 验证码每分钟发送次数不超过{0}次.
     */
    CAPTCHA_LIMIT_MINUTE("CAPTCHA_LIMIT_MINUTE", "验证码每分钟发送次数不超过{0}次");

    /**
     * 业务编码.
     */
    private final String bizCode;

    /**
     * 业务信息.
     */
    private final String bizMessage;

    /**
     * 构造方法.
     *
     * @param bizCode    业务编码
     * @param bizMessage 业务信息
     */
    LogCodeEnum(final String bizCode, final String bizMessage) {
        this.bizCode = bizCode;
        this.bizMessage = bizMessage;
    }
}

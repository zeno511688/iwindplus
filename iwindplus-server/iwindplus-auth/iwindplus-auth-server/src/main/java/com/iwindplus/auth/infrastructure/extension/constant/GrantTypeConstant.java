/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.auth.infrastructure.extension.constant;

import com.iwindplus.base.domain.constant.CommonConstant;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

/**
 * .
 *
 * @author zengdegui
 * @since 2026/09/15 01:23
 */
public final class GrantTypeConstant {

    private GrantTypeConstant() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 授权类型(密码: password).
     */
    public static final AuthorizationGrantType PASSWORD = new AuthorizationGrantType("password");

    /**
     * 授权类型(短信验证码: sms_code).
     */
    public static final AuthorizationGrantType SMS_CODE = new AuthorizationGrantType("sms_code");

    /**
     * 授权类型授权类型(绑定授权: bind_code).
     */
    public static final AuthorizationGrantType BIND_CODE = new AuthorizationGrantType("bind_code");

    /**
     * 授权类型(邮箱验证码: mail_code).
     */
    public static final AuthorizationGrantType MAIL_CODE = new AuthorizationGrantType("mail_code");

    /**
     * 手机号.
     */
    public static final String MOBILE = "mobile";

    /**
     * 验证码.
     */
    public static final String CAPTCHA = "captcha";

    /**
     * 邮箱.
     */
    public static final String MAIL = "mail";

    /**
     * 编码.
     */
    public static final String CODE = "code";

    /**
     * 图形验证码key.
     */
    public static final String GRAPHIC_CAPTCHA_KEY = "graphicCaptchaKey";

    /**
     * 图形验证码.
     */
    public static final String GRAPHIC_CAPTCHA = "graphicCaptcha";
}

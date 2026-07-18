/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.auth.domain.constant;

import com.iwindplus.base.domain.constant.CommonConstant;
import com.iwindplus.base.domain.constant.CommonConstant.DbConstant;
import com.iwindplus.base.domain.constant.CommonConstant.OauthConstant;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

/**
 * 常数.
 *
 * @author zengdegui
 * @since 2024/04/30 15:09
 */
public class AuthConstant {

    private AuthConstant() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * RING缓存相关常数 .
     */
    public static final int RING_BUFFER_SIZE = 1 << 14;

    /**
     * 认证服务名.
     */
    public static final String AUTH_SERVER_NAME = "iwindplus-auth";

    /**
     * 认证服务客户端扫描包名.
     */
    public static final String AUTH_CLIENT_SCAN_BASE_PACKAGE = "com.iwindplus.auth.client";

    /**
     * 登录地址.
     */
    public static final String LOGIN_URL = "/oauth2/login";

    /**
     * 登录提交地址.
     */
    public static final String LOGIN_SUBMIT_URL = "/authentication/login";

    /**
     * 退出地址.
     */
    public static final String LOGOUT_URL = "/oauth2/logout";

    /**
     * 授权码地址.
     */
    public static final String AUTHORIZE_URL = "/oauth2/authorize";

    /**
     * 自定义授权地址.
     */
    public static final String CONSENT_URL = "/oauth2/consent";

    /**
     * 设备码验证地址.
     */
    public static final String DEVICE_VERIFICATION_URL = "/oauth2/device_verification";

    /**
     * jwk秘钥
     */
    public static final String JWK_SET_KEY = "jwtSetKey";

    /**
     * 认证方法.
     */
    public static final String AUTHENTICATION_METHOD = "authentication_method";

    /**
     * 凭证.
     */
    public static final String CREDENTIALS = "credentials";

    /**
     * 客户端密钥过期时间.
     */
    public static final String CLIENT_SECRET_EXPIRES_AT = "client_secret_expires_at";

    /**
     * 认证参数名.
     */
    public static final String OAUTH2_PARAMETER_NAME_ID = DbConstant.ID;

    /**
     * 认证类型
     */
    public static final String AUTHORIZATION_TYPE = OauthConstant.AUTHORIZATION_TYPE;

    /**
     * 缓存超时时间.
     */
    public static final Long TIMEOUT = 600L;

    /**
     * 授权确认缓存key前缀.
     */
    public static final String CONSENT_KEY_PREFIX = "consent";

    /**
     * 授权类型(密码: password)请求相关常数.
     */
    public static class GrantTypePasswordConstant {

        private GrantTypePasswordConstant() {
            throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
        }

        /**
         * 授权类型(密码: password).
         */
        public static final AuthorizationGrantType PASSWORD = new AuthorizationGrantType("password");
    }

    /**
     * 授权类型(短信验证码: sms_code)请求相关常数.
     */
    public static class GrantTypeSmsCodeConstant {

        private GrantTypeSmsCodeConstant() {
            throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
        }

        /**
         * 授权类型(短信验证码: sms_code).
         */
        public static final AuthorizationGrantType SMS_CODE = new AuthorizationGrantType("sms_code");

        /**
         * 配置编码.
         */
        public static final String CODE = "code";

        /**
         * 手机号.
         */
        public static final String MOBILE = "mobile";

        /**
         * 验证码.
         */
        public static final String CAPTCHA = "captcha";
    }

    /**
     * 授权类型(邮箱验证码: mail_code)请求相关常数.
     */
    public static class GrantTypeMailCodeConstant {

        private GrantTypeMailCodeConstant() {
            throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
        }

        /**
         * 授权类型(邮箱验证码: mail_code).
         */
        public static final AuthorizationGrantType MAIL_CODE = new AuthorizationGrantType("mail_code");

        /**
         * 编码.
         */
        public static final String CODE = "code";

        /**
         * 邮箱.
         */
        public static final String MAIL = "mail";

        /**
         * 验证码.
         */
        public static final String CAPTCHA = "captcha";
    }

    /**
     * 授权类型(绑定授权: bind_code)请求相关常数.
     */
    public static class GrantTypeBindCodeConstant {

        private GrantTypeBindCodeConstant() {
            throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
        }

        /**
         * 授权类型授权类型(绑定授权: bind_code).
         */
        public static final AuthorizationGrantType BIND_CODE = new AuthorizationGrantType("bind_code");

        /**
         * 编码.
         */
        public static final String CODE = "code";
    }

}

/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.auth.domain.enums;

import com.iwindplus.base.domain.exception.CommonException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 业务编码枚举.
 *
 * @author zengdegui
 * @since 2020/6/13
 */
@Getter
@RequiredArgsConstructor
public enum AuthCodeEnum implements CommonException {
    /**
     * 用户登录异常.
     */
    USER_LOGIN_ABNORMAL("user_login_abnormal", "用户登录异常"),

    /**
     * 授权类型不能为空.
     */
    GRANT_TYPE_EMPTY("grant_type_empty", "授权类型不能为空"),

    /**
     * 用户授权申请被拒绝.
     */
    AUTHORIZATION_DENIED("authorization_denied", "用户授权申请被拒绝"),

    /**
     * 不支持的授权类型.
     */
    UNSUPPORTED_GRANT_TYPE( "unsupported_grant_type", "不支持的授权类型"),

    /**
     * 未授权的客户端.
     */
    UNAUTHORIZED_CLIENT( "unauthorized_client", "未授权的客户端"),

    /**
     * 无效的客户端.
     */
    INVALID_CLIENT("invalid_client", "无效的客户端"),

    /**
     * 客户端不存在.
     */
    CLIENT_NOT_EXIST("client_not_exist", "客户端不存在"),

    /**
     * 客户端密码不能为空.
     */
    CLIENT_PASSWORD_EMPTY("client_password_empty", "客户端密码不能为空"),

    /**
     * 客户端密码错误.
     */
    CLIENT_PASSWORD_ERROR("client_password_error", "客户端密码错误"),

    /**
     * 客户端密钥已过期.
     */
    CLIENT_SECRET_EXPIRED("client_secret_expired", "客户端密钥已过期"),

    /**
     * 无效的请求.
     */
    INVALID_REQUEST("invalid_request", "无效的请求"),

    /**
     * 无效的授权.
     */
    INVALID_GRANT( "invalid_grant", "无效的授权"),

    /**
     * 无效的scope.
     */
    INVALID_SCOPE("invalid_scope", "无效的scope"),

    /**
     * 客户端禁用.
     */
    CLIENT_DISABLE("client_disable", "客户端禁用"),

    /**
     * 用户身份校验失败.
     */
    IDENTITY_VERIFICATION_FAILED( "identity_verification_failed", "用户身份校验失败");

    /**
     * 业务编码.
     */
    private final String bizCode;

    /**
     * 业务信息.
     */
    private final String bizMessage;
}

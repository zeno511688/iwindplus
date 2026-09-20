/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.auth.infrastructure.extension;

import com.iwindplus.auth.infrastructure.extension.constant.GrantTypeConstant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken;

/**
 * 密码授权模式身份验证令牌(包含用户名和密码等)
 *
 * @author zengdegui
 * @since 2024/05/22
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class PasswordAuthenticationToken extends OAuth2AuthorizationGrantAuthenticationToken {

    /**
     * 用户名.
     */
    private final String username;

    /**
     * 密码.
     */
    private final String password;

    /**
     * 令牌申请访问范围.
     */
    private final Set<String> scopes;

    /**
     * 图形验证码key.
     */
    private final String captchaKey;

    /**
     * 图形验证码.
     */
    private final String captcha;

    /**
     * 密码模式身份验证令牌.
     *
     * @param clientPrincipal      客户端信息
     * @param additionalParameters 自定义额外参数
     * @param scopes               令牌申请访问范围
     * @param username             用户名
     * @param password             密码
     * @param captchaKey           图形验证码key
     * @param captcha              图形验证码
     */
    public PasswordAuthenticationToken(
        Authentication clientPrincipal,
        Map<String, Object> additionalParameters,
        Set<String> scopes,
        String username,
        String password,
        String captchaKey,
        String captcha) {
        super(GrantTypeConstant.PASSWORD, clientPrincipal, additionalParameters);
        this.scopes = Collections.unmodifiableSet(null != scopes ? new HashSet<>(scopes) : Collections.emptySet());
        this.username = username;
        this.password = password;
        this.captchaKey = captchaKey;
        this.captcha = captcha;
    }
}

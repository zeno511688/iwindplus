/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.auth.server.extension;

import com.iwindplus.auth.domain.constant.AuthConstant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken;

/**
 * 邮箱验证码身份验证令牌
 *
 * @author zengdegui
 * @since 2024/05/22
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class MailCodeAuthenticationToken extends OAuth2AuthorizationGrantAuthenticationToken {

    /**
     * 编码.
     */
    private final String code;

    /**
     * 邮箱.
     */
    public final String mail;

    /**
     * 验证码.
     */
    public final String captcha;

    /**
     * 令牌申请访问范围.
     */
    private final Set<String> scopes;

    /**
     * 邮箱模式身份验证令牌.
     *
     * @param clientPrincipal      客户端信息
     * @param scopes               令牌申请访问范围
     * @param additionalParameters 自定义额外参数
     * @param code                 编码
     * @param mail                 邮箱
     * @param captcha              验证码
     */
    protected MailCodeAuthenticationToken(
        Authentication clientPrincipal,
        Map<String, Object> additionalParameters,
        Set<String> scopes,
        String code,
        String mail,
        String captcha) {
        super(AuthConstant.GrantTypeMailCodeConstant.MAIL_CODE, clientPrincipal, additionalParameters);
        this.scopes = Collections.unmodifiableSet(null != scopes ? new HashSet<>(scopes) : Collections.emptySet());
        this.code = code;
        this.mail = mail;
        this.captcha = captcha;
    }
}

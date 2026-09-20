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
 * 短信验证码身份验证令牌
 *
 * @author zengdegui
 * @since 2024/05/22
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class SmsCodeAuthenticationToken extends OAuth2AuthorizationGrantAuthenticationToken {

    /**
     * 手机.
     */
    public final String mobile;

    /**
     * 验证码.
     */
    public final String captcha;

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
    private final String graphicCaptcha;

    /**
     * 短信模式身份验证令牌.
     *
     * @param clientPrincipal      客户端信息
     * @param scopes               令牌申请访问范围
     * @param additionalParameters 自定义额外参数
     * @param mobile               手机
     * @param captcha              短信验证码
     * @param captchaKey           图形验证码key
     * @param graphicCaptcha       图形验证码
     */
    protected SmsCodeAuthenticationToken(
        Authentication clientPrincipal,
        Map<String, Object> additionalParameters,
        Set<String> scopes,
        String mobile,
        String captcha,
        String captchaKey,
        String graphicCaptcha) {
        super(GrantTypeConstant.SMS_CODE, clientPrincipal, additionalParameters);
        this.scopes = Collections.unmodifiableSet(null != scopes ? new HashSet<>(scopes) : Collections.emptySet());
        this.mobile = mobile;
        this.captcha = captcha;
        this.captchaKey = captchaKey;
        this.graphicCaptcha = graphicCaptcha;
    }
}

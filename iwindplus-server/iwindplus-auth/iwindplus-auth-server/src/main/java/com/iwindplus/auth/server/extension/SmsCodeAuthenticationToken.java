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
 * 短信验证码身份验证令牌
 *
 * @author zengdegui
 * @since 2024/05/22
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class SmsCodeAuthenticationToken extends OAuth2AuthorizationGrantAuthenticationToken {

    /**
     * 编码.
     */
    private final String code;

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
     * 短信模式身份验证令牌.
     *
     * @param clientPrincipal      客户端信息
     * @param scopes               令牌申请访问范围
     * @param additionalParameters 自定义额外参数
     * @param code                 编码
     * @param mobile               手机
     * @param captcha              验证码
     */
    protected SmsCodeAuthenticationToken(
        Authentication clientPrincipal,
        Map<String, Object> additionalParameters,
        Set<String> scopes,
        String code,
        String mobile,
        String captcha) {
        super(AuthConstant.GrantTypeSmsCodeConstant.SMS_CODE, clientPrincipal, additionalParameters);
        this.scopes = Collections.unmodifiableSet(null != scopes ? new HashSet<>(scopes) : Collections.emptySet());
        this.code = code;
        this.mobile = mobile;
        this.captcha = captcha;
    }
}

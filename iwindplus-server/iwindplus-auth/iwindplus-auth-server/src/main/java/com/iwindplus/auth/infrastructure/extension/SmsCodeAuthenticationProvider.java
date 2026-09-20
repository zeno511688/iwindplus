/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.auth.infrastructure.extension;

import cn.hutool.core.lang.Assert;
import com.iwindplus.auth.infrastructure.client.LoginAuthClient;
import com.iwindplus.auth.infrastructure.extension.constant.GrantTypeConstant;
import com.iwindplus.auth.infrastructure.persistence.LoginAttemptService;
import java.util.Objects;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;

/**
 * 短信验证码认证授权提供者
 *
 * @author zengdegui
 * @since 2024/05/22
 */
@Slf4j
public record SmsCodeAuthenticationProvider(
    OAuth2AuthorizationService authorizationService,
    OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator,
    LoginAuthClient loginAuthClient,
    LoginAttemptService loginAttemptService) implements AuthenticationProvider {

    /**
     * 构造方法.
     *
     * @param authorizationService the authorization service
     * @param tokenGenerator       the token generator
     * @param loginAuthClient      loginAuthClient
     * @param loginAttemptService  loginAttemptService
     */
    public SmsCodeAuthenticationProvider {
        Assert.notNull(authorizationService, "authorizationService cannot be null");
        Assert.notNull(tokenGenerator, "tokenGenerator cannot be null");
        Assert.notNull(loginAuthClient, "loginAuthClient cannot be null");
        Assert.notNull(loginAttemptService, "loginAttemptService cannot be null");
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        SmsCodeAuthenticationToken smsCodeAuthenticationToken = (SmsCodeAuthenticationToken) authentication;

        OAuth2ClientAuthenticationToken clientPrincipal = PasswordAuthenticationProvider
            .validateClient(smsCodeAuthenticationToken, GrantTypeConstant.SMS_CODE);
        RegisteredClient registeredClient = clientPrincipal.getRegisteredClient();

        // 验证申请访问范围(Scope)
        Set<String> authorizedScopes = registeredClient.getScopes();
        Set<String> requestedScopes = smsCodeAuthenticationToken.getScopes();
        authorizedScopes = PasswordAuthenticationProvider.getScopes(registeredClient, authorizedScopes, requestedScopes);

        String mobile = smsCodeAuthenticationToken.getMobile();
        String captcha = smsCodeAuthenticationToken.getCaptcha();
        Assert.notNull(mobile, "mobile cannot be null");
        Assert.notNull(captcha, "captcha cannot be null");

        // 登录前安全检查：检查账号锁定状态
        PasswordAuthenticationProvider.checkAccountLocked(this.loginAttemptService, mobile);

        // 根据手机号获取信息
        UserDetails userDetails = null;
        try {
            this.loginAuthClient.validateCaptchaByMobile(mobile, captcha);
            userDetails = this.loginAuthClient.loadUserByMobile(mobile);
        } catch (Exception ex) {
            PasswordAuthenticationProvider.convertException(ex);
        }
        if (Objects.isNull(userDetails)) {
            PasswordAuthenticationProvider.handleCodeAuthFailure(this.loginAttemptService, mobile);
        }

        // 登录成功，清除尝试记录
        this.loginAttemptService.recordSuccess(mobile);

        return PasswordAuthenticationProvider.buildTokenResponse(clientPrincipal, registeredClient, authorizedScopes,
            requestedScopes, userDetails, GrantTypeConstant.SMS_CODE, smsCodeAuthenticationToken, tokenGenerator,
            authorizationService);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return SmsCodeAuthenticationToken.class.isAssignableFrom(authentication);
    }
}

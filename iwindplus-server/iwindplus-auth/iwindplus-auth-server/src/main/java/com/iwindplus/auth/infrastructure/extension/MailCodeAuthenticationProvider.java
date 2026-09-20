/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.auth.infrastructure.extension;

import cn.hutool.core.lang.Assert;
import com.iwindplus.auth.common.enums.AuthCodeEnum;
import com.iwindplus.auth.infrastructure.client.LoginAuthClient;
import com.iwindplus.auth.infrastructure.model.dto.OauthUserDTO;
import com.iwindplus.auth.infrastructure.exception.CustomOauth2AuthenticationException;
import com.iwindplus.auth.infrastructure.extension.constant.GrantTypeConstant;
import com.iwindplus.auth.infrastructure.persistence.LoginAttemptService;
import com.iwindplus.auth.infrastructure.support.Oauth2Util;
import java.util.Objects;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;

/**
 * 邮箱验证码认证授权提供者.
 *
 * @author zengdegui
 * @since 2024/05/22
 */
@Slf4j
public record MailCodeAuthenticationProvider(
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
    public MailCodeAuthenticationProvider {
        Assert.notNull(authorizationService, "authorizationService cannot be null");
        Assert.notNull(tokenGenerator, "tokenGenerator cannot be null");
        Assert.notNull(loginAuthClient, "loginAuthClient cannot be null");
        Assert.notNull(loginAttemptService, "loginAttemptService cannot be null");
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        MailCodeAuthenticationToken mailCodeAuthenticationToken = (MailCodeAuthenticationToken) authentication;

        OAuth2ClientAuthenticationToken clientPrincipal = Oauth2Util
            .getAuthenticatedClientElseThrowInvalidClient(mailCodeAuthenticationToken);
        RegisteredClient registeredClient = clientPrincipal.getRegisteredClient();
        if (Objects.isNull(registeredClient)) {
            throw new CustomOauth2AuthenticationException(AuthCodeEnum.INVALID_CLIENT);
        }

        // 验证客户端是否支持授权类型(grant_type=mail_code)
        if (!registeredClient.getAuthorizationGrantTypes().contains(GrantTypeConstant.MAIL_CODE)) {
            throw new CustomOauth2AuthenticationException(AuthCodeEnum.INVALID_GRANT);
        }

        // 验证申请访问范围(Scope)
        Set<String> authorizedScopes = registeredClient.getScopes();
        Set<String> requestedScopes = mailCodeAuthenticationToken.getScopes();
        authorizedScopes = PasswordAuthenticationProvider.getScopes(registeredClient, authorizedScopes, requestedScopes);

        String mail = mailCodeAuthenticationToken.getMail();
        String captcha = mailCodeAuthenticationToken.getCaptcha();
        Assert.notNull(mail, "mail cannot be null");
        Assert.notNull(captcha, "captcha cannot be null");

        // 登录前安全检查：检查账号锁定状态和图形验证码
        PasswordAuthenticationProvider.checkLoginSecurity(
            this.loginAttemptService, mail,
            mailCodeAuthenticationToken.getCaptchaKey(),
            mailCodeAuthenticationToken.getGraphicCaptcha());

        // 根据邮箱获取信息
        UserDetails userDetails = null;
        try {
            this.loginAuthClient.validateCaptchaByMail(mail, captcha);
            userDetails = this.loginAuthClient.loadUserByMail(mail);
        } catch (Exception ex) {
            PasswordAuthenticationProvider.convertException(ex);
        }
        if (Objects.isNull(userDetails)) {
            PasswordAuthenticationProvider.handleAuthFailure(this.loginAttemptService, mail);
        }

        // 登录成功，清除尝试记录
        this.loginAttemptService.recordSuccess(mail);

        OauthUserDTO userInfo = (OauthUserDTO) userDetails;
        String id = PasswordAuthenticationProvider.buildKey(userInfo.getUserId());

        Authentication usernamePasswordAuthentication = new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword());
        // 访问令牌(Access Token) 构造器
        DefaultOAuth2TokenContext.Builder tokenContextBuilder = DefaultOAuth2TokenContext.builder()
            .registeredClient(registeredClient)
            .principal(usernamePasswordAuthentication)
            .authorizationServerContext(AuthorizationServerContextHolder.getContext())
            .authorizedScopes(authorizedScopes)
            .authorizationGrantType(GrantTypeConstant.MAIL_CODE)
            .authorizationGrant(mailCodeAuthenticationToken);
        OAuth2Authorization.Builder authorizationBuilder = PasswordAuthenticationProvider.buildAuthorizationBuilder(registeredClient, id,
            authorizedScopes, userDetails.getUsername(), GrantTypeConstant.MAIL_CODE, usernamePasswordAuthentication);
        return PasswordAuthenticationProvider.buildAuthenticationToken(clientPrincipal, registeredClient, requestedScopes, tokenContextBuilder,
            tokenGenerator, authorizationBuilder, authorizationService, id);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return MailCodeAuthenticationToken.class.isAssignableFrom(authentication);
    }
}

/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.auth.server.extension;

import cn.hutool.core.lang.Assert;
import com.iwindplus.auth.domain.constant.AuthConstant;
import com.iwindplus.auth.domain.dto.OauthUserDTO;
import com.iwindplus.auth.domain.enums.AuthCodeEnum;
import com.iwindplus.auth.domain.exception.CustomOauth2AuthenticationException;
import com.iwindplus.auth.server.service.SmsCodeDetailsService;
import com.iwindplus.auth.server.util.Oauth2Util;
import java.util.Objects;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
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
    SmsCodeDetailsService smsCodeDetailsService) implements AuthenticationProvider {

    /**
     * 构造方法.
     *
     * @param authorizationService  the authorization service
     * @param tokenGenerator        the token generator
     * @param smsCodeDetailsService smsCodeDetailsService
     */
    public SmsCodeAuthenticationProvider {
        Assert.notNull(authorizationService, "authorizationService cannot be null");
        Assert.notNull(tokenGenerator, "tokenGenerator cannot be null");
        Assert.notNull(smsCodeDetailsService, "smsCodeDetailsService cannot be null");
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        SmsCodeAuthenticationToken smsCodeAuthenticationToken = (SmsCodeAuthenticationToken) authentication;

        OAuth2ClientAuthenticationToken clientPrincipal = Oauth2Util
            .getAuthenticatedClientElseThrowInvalidClient(smsCodeAuthenticationToken);
        RegisteredClient registeredClient = clientPrincipal.getRegisteredClient();
        if (Objects.isNull(registeredClient)) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);
        }

        // 验证客户端是否支持授权类型(grant_type=sms_code)
        if (!registeredClient.getAuthorizationGrantTypes().contains(AuthConstant.GrantTypeSmsCodeConstant.SMS_CODE)) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT);
        }

        // 验证申请访问范围(Scope)
        Set<String> authorizedScopes = registeredClient.getScopes();
        Set<String> requestedScopes = smsCodeAuthenticationToken.getScopes();
        authorizedScopes = PasswordAuthenticationProvider.getScopes(registeredClient, authorizedScopes, requestedScopes);

        String code = smsCodeAuthenticationToken.getCode();
        String mobile = smsCodeAuthenticationToken.getMobile();
        String captcha = smsCodeAuthenticationToken.getCaptcha();
        Assert.notNull(code, "code cannot be null");
        Assert.notNull(mobile, "mobile cannot be null");
        Assert.notNull(captcha, "captcha cannot be null");
        // 根据手机号获取信息
        UserDetails userDetails = null;
        try {
            this.smsCodeDetailsService.validate(code, mobile, captcha);
            userDetails = this.smsCodeDetailsService.loadUserByMobile(mobile);
        } catch (Exception ex) {
            PasswordAuthenticationProvider.convertException(ex);
        }
        if (Objects.isNull(userDetails)) {
            throw new CustomOauth2AuthenticationException(AuthCodeEnum.IDENTITY_VERIFICATION_FAILED);
        }

        OauthUserDTO userInfo = (OauthUserDTO) userDetails;
        String id = PasswordAuthenticationProvider.buildKey(userInfo.getUserId());

        Authentication usernamePasswordAuthentication = new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword());
        // 访问令牌(Access Token) 构造器
        DefaultOAuth2TokenContext.Builder tokenContextBuilder = DefaultOAuth2TokenContext.builder()
            .registeredClient(registeredClient)
            .principal(usernamePasswordAuthentication)
            .authorizationServerContext(AuthorizationServerContextHolder.getContext())
            .authorizedScopes(authorizedScopes)
            .authorizationGrantType(AuthConstant.GrantTypeSmsCodeConstant.SMS_CODE)
            .authorizationGrant(smsCodeAuthenticationToken);
        OAuth2Authorization.Builder authorizationBuilder = PasswordAuthenticationProvider.buildAuthorizationBuilder(registeredClient, id,
            authorizedScopes, userDetails.getUsername(), AuthConstant.GrantTypeSmsCodeConstant.SMS_CODE, usernamePasswordAuthentication);
        return PasswordAuthenticationProvider.buildAuthenticationToken(clientPrincipal, registeredClient, requestedScopes, tokenContextBuilder,
            tokenGenerator, authorizationBuilder, authorizationService, id);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return SmsCodeAuthenticationToken.class.isAssignableFrom(authentication);
    }
}

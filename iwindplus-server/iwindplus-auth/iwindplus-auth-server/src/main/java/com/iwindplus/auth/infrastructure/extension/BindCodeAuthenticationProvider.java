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
import com.iwindplus.auth.infrastructure.client.dto.OauthUserDTO;
import com.iwindplus.auth.infrastructure.exception.CustomOauth2AuthenticationException;
import com.iwindplus.auth.infrastructure.extension.constant.GrantTypeConstant;
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
 * 绑定授权模式提供者
 * <p>
 * 处理基于编码身份验证
 *
 * @author zengdegui
 * @since 2024/05/22
 */
@Slf4j
public record BindCodeAuthenticationProvider(
    OAuth2AuthorizationService authorizationService,
    OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator,
    LoginAuthClient loginAuthClient) implements AuthenticationProvider {

    /**
     * 构造方法.
     *
     * @param authorizationService   the authorization service
     * @param tokenGenerator         the token generator
     * @param loginAuthClient loginAuthClient
     */
    public BindCodeAuthenticationProvider {
        Assert.notNull(authorizationService, "authorizationService cannot be null");
        Assert.notNull(tokenGenerator, "tokenGenerator cannot be null");
        Assert.notNull(loginAuthClient, "loginAuthClient cannot be null");
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        BindCodeAuthenticationToken bindCodeAuthenticationToken = (BindCodeAuthenticationToken) authentication;

        OAuth2ClientAuthenticationToken clientPrincipal = Oauth2Util
            .getAuthenticatedClientElseThrowInvalidClient(bindCodeAuthenticationToken);
        RegisteredClient registeredClient = clientPrincipal.getRegisteredClient();
        if (Objects.isNull(registeredClient)) {
            throw new CustomOauth2AuthenticationException(AuthCodeEnum.INVALID_CLIENT);
        }

        // 验证客户端是否支持授权类型(grant_type=bind_code)
        if (!registeredClient.getAuthorizationGrantTypes().contains(GrantTypeConstant.BIND_CODE)) {
            throw new CustomOauth2AuthenticationException(AuthCodeEnum.INVALID_GRANT);
        }

        // 验证申请访问范围(Scope)
        Set<String> authorizedScopes = registeredClient.getScopes();
        Set<String> requestedScopes = bindCodeAuthenticationToken.getScopes();
        authorizedScopes = PasswordAuthenticationProvider.getScopes(registeredClient, authorizedScopes, requestedScopes);

        String code = bindCodeAuthenticationToken.getCode();
        Assert.notNull(code, "code cannot be null");
        // 根据绑定编码获取信息
        UserDetails userDetails = null;
        try {
            userDetails = this.loginAuthClient.loadUserByCode(code);
        } catch (Exception ex) {
            PasswordAuthenticationProvider.convertException(ex);
        }
        if (Objects.isNull(userDetails)) {
            throw new CustomOauth2AuthenticationException(AuthCodeEnum.IDENTITY_VERIFICATION_FAILED);
        }

        OauthUserDTO userInfo = (OauthUserDTO) userDetails;
        String id = PasswordAuthenticationProvider.buildKey(userInfo.getUserId());

        Authentication usernamePasswordAuthentication = new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword());
        DefaultOAuth2TokenContext.Builder tokenContextBuilder = DefaultOAuth2TokenContext.builder()
            .registeredClient(registeredClient)
            .principal(usernamePasswordAuthentication)
            .authorizationServerContext(AuthorizationServerContextHolder.getContext())
            .authorizedScopes(authorizedScopes)
            .authorizationGrantType(GrantTypeConstant.SMS_CODE)
            .authorizationGrant(bindCodeAuthenticationToken);
        OAuth2Authorization.Builder authorizationBuilder = PasswordAuthenticationProvider.buildAuthorizationBuilder(registeredClient, id,
            authorizedScopes, userDetails.getUsername(), GrantTypeConstant.SMS_CODE, usernamePasswordAuthentication);
        return PasswordAuthenticationProvider.buildAuthenticationToken(clientPrincipal, registeredClient, requestedScopes, tokenContextBuilder,
            tokenGenerator, authorizationBuilder, authorizationService, id);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return BindCodeAuthenticationToken.class.isAssignableFrom(authentication);
    }
}

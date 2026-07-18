/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.auth.server.extension;

import com.iwindplus.auth.domain.enums.AuthCodeEnum;
import com.iwindplus.auth.domain.exception.CustomOauth2AuthenticationException;
import com.iwindplus.auth.server.config.property.AuthProperty;
import com.iwindplus.auth.server.util.Oauth2Util;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import java.security.Principal;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization.Token;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext.Builder;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.util.Assert;

/**
 * 刷新token认证证授权提供者.
 *
 * @author zengdegui
 * @since 2025/03/28 23:15
 */
@Slf4j
public record RefreshTokenAuthenticationProvider(
    OAuth2AuthorizationService authorizationService,
    OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator,
    AuthProperty authProperty) implements AuthenticationProvider {

    /**
     * 构造方法.
     *
     * @param authorizationService authorizationService
     * @param tokenGenerator       tokenGenerator
     */
    public RefreshTokenAuthenticationProvider {
        Assert.notNull(authorizationService, "authorizationService cannot be null");
        Assert.notNull(tokenGenerator, "tokenGenerator cannot be null");
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        RefreshTokenAuthenticationToken refreshTokenAuthentication = (RefreshTokenAuthenticationToken) authentication;

        OAuth2ClientAuthenticationToken clientPrincipal = Oauth2Util
            .getAuthenticatedClientElseThrowInvalidClient(refreshTokenAuthentication);
        RegisteredClient registeredClient = clientPrincipal.getRegisteredClient();
        if (Objects.isNull(registeredClient)) {
            throw new CustomOauth2AuthenticationException(AuthCodeEnum.INVALID_CLIENT);
        }
        OAuth2Authorization authorization = this.authorizationService.findByToken(refreshTokenAuthentication.getRefreshToken(),
            OAuth2TokenType.REFRESH_TOKEN);
        if (null == authorization) {
            throw new CustomOauth2AuthenticationException(BizCodeEnum.INVALID_REFRESH_TOKEN);
        }
        if (!registeredClient.getId().equals(authorization.getRegisteredClientId())) {
            throw new CustomOauth2AuthenticationException(AuthCodeEnum.INVALID_GRANT);
        }
        if (!registeredClient.getAuthorizationGrantTypes().contains(AuthorizationGrantType.REFRESH_TOKEN)) {
            throw new CustomOauth2AuthenticationException(AuthCodeEnum.UNAUTHORIZED_CLIENT);
        }
        Token<OAuth2RefreshToken> refreshTokenAuthorization = authorization.getRefreshToken();
        if (!refreshTokenAuthorization.isActive()) {
            throw new CustomOauth2AuthenticationException(AuthCodeEnum.INVALID_GRANT);
        }
        // 判断访问token是否过期
        if (!authorization.getAccessToken().isExpired()) {
            if (Boolean.TRUE.equals(this.authProperty.getEnabledTokenExpiredValid())) {
                throw new CustomOauth2AuthenticationException(BizCodeEnum.ACCESS_TOKEN_NOT_EXPIRED);
            }

            // 如果访问token未过期，返回查询结果
            OAuth2AccessToken accessToken = authorization.getAccessToken().getToken();
            return new OAuth2AccessTokenAuthenticationToken(
                registeredClient,
                clientPrincipal,
                accessToken,
                refreshTokenAuthorization.getToken(),
                Collections.emptyMap()
            );
        }
        // 验证申请访问范围(Scope)
        Set<String> scopes = refreshTokenAuthentication.getScopes();
        Set<String> authorizedScopes = authorization.getAuthorizedScopes();
        if (!authorizedScopes.containsAll(scopes)) {
            throw new CustomOauth2AuthenticationException(AuthCodeEnum.INVALID_SCOPE);
        }
        if (scopes.isEmpty()) {
            scopes = authorizedScopes;
        }
        Builder tokenContextBuilder = DefaultOAuth2TokenContext.builder()
            .registeredClient(registeredClient)
            .principal(authorization.getAttribute(Principal.class.getName()))
            .authorizationServerContext(AuthorizationServerContextHolder.getContext())
            .authorization(authorization)
            .authorizedScopes(scopes)
            .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
            .authorizationGrant(refreshTokenAuthentication);

        // 删除旧的token
        this.authorizationService.remove(authorization);

        OAuth2Authorization.Builder authorizationBuilder = OAuth2Authorization.from(authorization);
        final OAuth2AccessTokenAuthenticationToken accessTokenAuthenticationToken = this.buildAuthenticationToken(clientPrincipal,
            registeredClient, refreshTokenAuthorization, authorizedScopes,
            tokenContextBuilder, authorizationBuilder);
        return accessTokenAuthenticationToken;
    }

    private OAuth2AccessTokenAuthenticationToken buildAuthenticationToken(OAuth2ClientAuthenticationToken clientPrincipal,
        RegisteredClient registeredClient, Token<OAuth2RefreshToken> refreshTokenAuthorization, Set<String> authorizedScopes,
        Builder tokenContextBuilder, OAuth2Authorization.Builder authorizationBuilder) {
        // 生成OidcIdToken
        OidcIdToken idToken = PasswordAuthenticationProvider.getOidcIdToken(tokenGenerator, authorizedScopes, tokenContextBuilder,
            authorizationBuilder);
        OAuth2AccessToken accessToken = PasswordAuthenticationProvider.getNewAccessToken(tokenGenerator, tokenContextBuilder, authorizationBuilder);
        OAuth2RefreshToken refreshToken = refreshTokenAuthorization.getToken();
        if (!registeredClient.getTokenSettings().isReuseRefreshTokens()) {
            refreshToken = PasswordAuthenticationProvider.getNewRefreshToken(tokenGenerator, tokenContextBuilder, authorizationBuilder);
        }
        // 持久化令牌发放记录到数据库
        this.authorizationService.save(authorizationBuilder.build());
        Map<String, Object> additionalParameters =
            null != idToken ? Collections.singletonMap(OidcParameterNames.ID_TOKEN, idToken.getTokenValue()) : Collections.emptyMap();
        return new OAuth2AccessTokenAuthenticationToken(registeredClient, clientPrincipal, accessToken, refreshToken, additionalParameters);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return RefreshTokenAuthenticationToken.class.isAssignableFrom(authentication);
    }
}

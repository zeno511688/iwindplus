/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.auth.server.extension;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.iwindplus.auth.domain.constant.AuthConstant;
import com.iwindplus.auth.domain.dto.OauthUserDTO;
import com.iwindplus.auth.domain.enums.AuthCodeEnum;
import com.iwindplus.auth.domain.exception.CustomOauth2AuthenticationException;
import com.iwindplus.auth.server.service.SysUserDetailsService;
import com.iwindplus.auth.server.util.Oauth2Util;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import java.security.Principal;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClaimAccessor;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization.Token;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;

/**
 * 密码模式身份验证提供者
 * <p>
 * 处理基于用户名和密码的身份验证
 *
 * @author zengdegui
 * @since 2024/05/22
 */
@Slf4j
public record PasswordAuthenticationProvider(
    OAuth2AuthorizationService authorizationService,
    OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator,
    SysUserDetailsService sysUserDetailsService,
    PasswordEncoder passwordEncoder) implements AuthenticationProvider {

    private static final OAuth2TokenType ID_TOKEN_TOKEN_TYPE = new OAuth2TokenType(OidcParameterNames.ID_TOKEN);

    /**
     * 构造方法.
     *
     * @param authorizationService  the authorization service
     * @param tokenGenerator        the token generator
     * @param sysUserDetailsService sysUserDetailsService
     * @param passwordEncoder       passwordEncoder
     */
    public PasswordAuthenticationProvider {
        Assert.notNull(authorizationService, "authorizationService cannot be null");
        Assert.notNull(tokenGenerator, "tokenGenerator cannot be null");
        Assert.notNull(sysUserDetailsService, "sysUserDetailsService cannot be null");
        Assert.notNull(passwordEncoder, "passwordEncoder cannot be null");
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        PasswordAuthenticationToken passwordAuthenticationToken = (PasswordAuthenticationToken) authentication;

        OAuth2ClientAuthenticationToken clientPrincipal = Oauth2Util
            .getAuthenticatedClientElseThrowInvalidClient(passwordAuthenticationToken);
        RegisteredClient registeredClient = clientPrincipal.getRegisteredClient();
        if (Objects.isNull(registeredClient)) {
            throw new CustomOauth2AuthenticationException(AuthCodeEnum.INVALID_CLIENT);
        }

        // 验证客户端是否支持授权类型(grant_type=password)
        if (!registeredClient.getAuthorizationGrantTypes().contains(AuthConstant.GrantTypePasswordConstant.PASSWORD)) {
            throw new CustomOauth2AuthenticationException(AuthCodeEnum.INVALID_GRANT);
        }

        // 验证申请访问范围(Scope)
        Set<String> authorizedScopes = registeredClient.getScopes();
        Set<String> requestedScopes = passwordAuthenticationToken.getScopes();
        authorizedScopes = PasswordAuthenticationProvider.getScopes(registeredClient, authorizedScopes, requestedScopes);

        // 生成用户名密码身份验证令牌
        String username = passwordAuthenticationToken.getUsername();
        String password = passwordAuthenticationToken.getPassword();
        Assert.notNull(username, "username cannot be null");
        Assert.notNull(password, "password cannot be null");

        // 根据邮箱获取信息
        UserDetails userDetails = null;
        try {
            userDetails = this.sysUserDetailsService.loadUserByUsername(username);
        } catch (Exception ex) {
            PasswordAuthenticationProvider.convertException(ex);
        }
        if (Objects.isNull(userDetails)) {
            throw new CustomOauth2AuthenticationException(AuthCodeEnum.IDENTITY_VERIFICATION_FAILED);
        }
        boolean matches = this.passwordEncoder.matches(password, userDetails.getPassword());
        if (Boolean.FALSE.equals(matches)) {
            throw new CustomOauth2AuthenticationException(BizCodeEnum.PASSWORD_ERROR);
        }

        OauthUserDTO userInfo = (OauthUserDTO) userDetails;
        String id = PasswordAuthenticationProvider.buildKey(userInfo.getUserId());

        Authentication usernamePasswordAuthentication = new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword());
        DefaultOAuth2TokenContext.Builder tokenContextBuilder = DefaultOAuth2TokenContext.builder()
            .registeredClient(registeredClient)
            .principal(usernamePasswordAuthentication)
            .authorizationServerContext(AuthorizationServerContextHolder.getContext())
            .authorizedScopes(authorizedScopes)
            .authorizationGrantType(AuthConstant.GrantTypePasswordConstant.PASSWORD)
            .authorizationGrant(passwordAuthenticationToken);

        OAuth2Authorization.Builder authorizationBuilder = PasswordAuthenticationProvider.buildAuthorizationBuilder(registeredClient, id,
            authorizedScopes, userDetails.getUsername(), AuthConstant.GrantTypePasswordConstant.PASSWORD, usernamePasswordAuthentication);
        return PasswordAuthenticationProvider.buildAuthenticationToken(clientPrincipal, registeredClient, requestedScopes, tokenContextBuilder,
            tokenGenerator, authorizationBuilder, authorizationService, id);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return PasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    /**
     * 构造AuthenticationToken
     *
     * @param clientPrincipal      clientPrincipal
     * @param registeredClient     registeredClient
     * @param requestedScopes      requestedScopes
     * @param tokenContextBuilder  tokenContextBuilder
     * @param tokenGenerator       tokenGenerator
     * @param authorizationBuilder authorizationBuilder
     * @param authorizationService authorizationService
     * @param id                   id
     * @return OAuth2AccessTokenAuthenticationToken
     */
    public static OAuth2AccessTokenAuthenticationToken buildAuthenticationToken(
        OAuth2ClientAuthenticationToken clientPrincipal,
        RegisteredClient registeredClient,
        Set<String> requestedScopes,
        DefaultOAuth2TokenContext.Builder tokenContextBuilder,
        OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator,
        OAuth2Authorization.Builder authorizationBuilder,
        OAuth2AuthorizationService authorizationService,
        String id) {
        OAuth2AccessToken accessToken;
        OAuth2RefreshToken refreshToken;

        // 生成OidcIdToken
        OidcIdToken idToken = PasswordAuthenticationProvider.getOidcIdToken(tokenGenerator, requestedScopes, tokenContextBuilder,
            authorizationBuilder);
        OAuth2Authorization authorization = authorizationService.findById(id);
        if (Objects.isNull(authorization)) {
            accessToken = PasswordAuthenticationProvider.getNewAccessToken(tokenGenerator, tokenContextBuilder, authorizationBuilder);
            refreshToken = PasswordAuthenticationProvider.getNewRefreshToken(tokenGenerator, tokenContextBuilder, authorizationBuilder);
            // 持久化令牌发放记录到数据库
            authorizationService.save(authorizationBuilder.build());
        } else {
            accessToken = Optional.ofNullable(authorization.getAccessToken()).map(Token::getToken).orElse(null);
            refreshToken = Optional.ofNullable(authorization.getRefreshToken()).map(Token::getToken).orElse(null);
            // 判断刷新token是否过期，刷新token过期访问token必然过期，则删除
            if (authorization.getRefreshToken().isExpired()) {
                authorizationService.remove(authorization);
                accessToken = PasswordAuthenticationProvider.getNewAccessToken(tokenGenerator, tokenContextBuilder, authorizationBuilder);
                refreshToken = PasswordAuthenticationProvider.getNewRefreshToken(tokenGenerator, tokenContextBuilder, authorizationBuilder);
                // 持久化令牌发放记录到数据库
                final OAuth2Authorization build = authorizationBuilder.accessToken(accessToken).refreshToken(refreshToken).build();
                authorizationService.save(build);
            } else {
                // 判断访问token是否过期
                if (authorization.getAccessToken().isExpired()) {
                    authorizationService.remove(authorization);
                    accessToken = PasswordAuthenticationProvider.getNewAccessToken(tokenGenerator, tokenContextBuilder, authorizationBuilder);
                    // 持久化令牌发放记录到数据库
                    final OAuth2Authorization build = authorizationBuilder.accessToken(accessToken).refreshToken(refreshToken).build();
                    authorizationService.save(build);
                }
            }
        }
        Map<String, Object> additionalParameters =
            null != idToken ? Collections.singletonMap(OidcParameterNames.ID_TOKEN, idToken.getTokenValue()) : Collections.emptyMap();
        return new OAuth2AccessTokenAuthenticationToken(registeredClient, clientPrincipal, accessToken, refreshToken, additionalParameters);
    }

    /**
     * 构造 OAuth2Authorization.Builder.
     *
     * @param registeredClient       registeredClient
     * @param id                     id
     * @param authorizedScopes       authorizedScopes
     * @param principalName          principalName
     * @param authorizationGrantType authorizationGrantType
     * @param authentication         authentication
     * @return OAuth2Authorization.Builder
     */
    public static OAuth2Authorization.Builder buildAuthorizationBuilder(
        RegisteredClient registeredClient,
        String id,
        Set<String> authorizedScopes,
        String principalName,
        AuthorizationGrantType authorizationGrantType,
        Authentication authentication) {
        return OAuth2Authorization.withRegisteredClient(registeredClient)
            .id(id)
            .principalName(principalName)
            .authorizationGrantType(authorizationGrantType)
            .authorizedScopes(authorizedScopes)
            .attribute(Principal.class.getName(), authentication);
    }

    /**
     * 验证申请访问范围(Scope).
     *
     * @param registeredClient registeredClient
     * @param authorizedScopes authorizedScopes
     * @param requestedScopes  requestedScopes
     * @return Set<String>
     */
    public static Set<String> getScopes(RegisteredClient registeredClient, Set<String> authorizedScopes, Set<String> requestedScopes) {
        if (CollUtil.isNotEmpty(requestedScopes)) {
            Set<String> unauthorizedScopes = requestedScopes.stream()
                .filter(Objects::nonNull)
                .filter(requestedScope -> !registeredClient.getScopes().contains(requestedScope))
                .collect(Collectors.toSet());
            if (CollUtil.isNotEmpty(unauthorizedScopes)) {
                throw new CustomOauth2AuthenticationException(AuthCodeEnum.INVALID_SCOPE);
            }
            authorizedScopes = new LinkedHashSet<>(requestedScopes);
        }
        return authorizedScopes;
    }

    /**
     * 生成访问token.
     *
     * @param tokenGenerator       tokenGenerator
     * @param tokenContextBuilder  tokenContextBuilder
     * @param authorizationBuilder authorizationBuilder
     * @return OAuth2AccessToken
     */
    public static OAuth2AccessToken getNewAccessToken(
        OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator,
        DefaultOAuth2TokenContext.Builder tokenContextBuilder,
        OAuth2Authorization.Builder authorizationBuilder) {
        OAuth2TokenContext tokenContext = tokenContextBuilder.tokenType(OAuth2TokenType.ACCESS_TOKEN).build();
        OAuth2Token token = tokenGenerator.generate(tokenContext);
        if (null == token) {
            OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR,
                "The token generator failed to generate the access token.", Oauth2Util.ERROR_URI);
            throw new CustomOauth2AuthenticationException(error.getErrorCode(), error.getDescription());
        }
        OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, token.getTokenValue(),
            token.getIssuedAt(), token.getExpiresAt(), tokenContext.getAuthorizedScopes());
        OAuth2TokenFormat accessTokenFormat = tokenContext.getRegisteredClient()
            .getTokenSettings()
            .getAccessTokenFormat();
        authorizationBuilder.token(accessToken, metadata -> {
            if (token instanceof ClaimAccessor claimAccessor) {
                metadata.put(Token.CLAIMS_METADATA_NAME, claimAccessor.getClaims());
            }
            metadata.put(Token.INVALIDATED_METADATA_NAME, false);
            metadata.put(OAuth2TokenFormat.class.getName(), accessTokenFormat.getValue());
        });
        return accessToken;
    }

    /**
     * 生成刷新token.
     *
     * @param tokenGenerator       tokenGenerator
     * @param tokenContextBuilder  tokenContextBuilder
     * @param authorizationBuilder authorizationBuilder
     * @return OAuth2RefreshToken
     */
    public static OAuth2RefreshToken getNewRefreshToken(
        OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator,
        DefaultOAuth2TokenContext.Builder tokenContextBuilder,
        OAuth2Authorization.Builder authorizationBuilder) {
        OAuth2TokenContext tokenContext = tokenContextBuilder.tokenType(OAuth2TokenType.REFRESH_TOKEN).build();
        OAuth2Token token = tokenGenerator.generate(tokenContext);
        if (token instanceof OAuth2RefreshToken refreshToken) {
            authorizationBuilder.refreshToken(refreshToken);
        } else {
            OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR,
                "The token generator failed to generate the refresh token.", Oauth2Util.ERROR_URI);
            throw new CustomOauth2AuthenticationException(error.getErrorCode(), error.getDescription());
        }
        return refreshToken;
    }

    /**
     * 生成OidcIdToken.
     *
     * @param tokenGenerator       tokenGenerator
     * @param requestedScopes      requestedScopes
     * @param tokenContextBuilder  tokenContextBuilder
     * @param authorizationBuilder authorizationBuilder
     * @return OidcIdToken
     */
    public static OidcIdToken getOidcIdToken(
        OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator,
        Set<String> requestedScopes,
        DefaultOAuth2TokenContext.Builder tokenContextBuilder,
        OAuth2Authorization.Builder authorizationBuilder) {
        OAuth2TokenContext tokenContext;
        OidcIdToken idToken;
        if (requestedScopes.contains(OidcScopes.OPENID)) {
            tokenContext = tokenContextBuilder
                .tokenType(ID_TOKEN_TOKEN_TYPE)
                .authorization(authorizationBuilder.build())
                .build();
            OAuth2Token generatedIdToken = tokenGenerator.generate(tokenContext);
            if (!(generatedIdToken instanceof Jwt)) {
                OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR,
                    "The token generator failed to generate the ID token.", Oauth2Util.ERROR_URI);
                throw new CustomOauth2AuthenticationException(error.getErrorCode(), error.getDescription());
            }
            idToken = new OidcIdToken(generatedIdToken.getTokenValue(), generatedIdToken.getIssuedAt(),
                generatedIdToken.getExpiresAt(), ((Jwt) generatedIdToken).getClaims());
            authorizationBuilder.token(idToken, metadata ->
                metadata.put(Token.CLAIMS_METADATA_NAME, idToken.getClaims()));
        } else {
            idToken = null;
        }
        return idToken;
    }

    /**
     * 异常转化.
     *
     * @param ex
     */
    public static void convertException(Exception ex) {
        if (ex instanceof BizException obj) {
            throw new CustomOauth2AuthenticationException(obj);
        }
        throw new CustomOauth2AuthenticationException(AuthCodeEnum.IDENTITY_VERIFICATION_FAILED);
    }

    /**
     * 构建key.
     *
     * @param userId 用户主键
     * @return String
     */
    public static String buildKey(Long userId) {
        return userId.toString();
    }
}

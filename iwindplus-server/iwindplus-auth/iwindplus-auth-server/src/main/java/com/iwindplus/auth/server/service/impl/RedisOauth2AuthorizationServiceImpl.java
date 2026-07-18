/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2011-2020, All rights reserved.
 */

package com.iwindplus.auth.server.service.impl;

import com.iwindplus.auth.domain.constant.AuthConstant;
import com.iwindplus.base.util.CryptoUtil;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2DeviceCode;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2UserCode;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * 基于redis的授权管理服务.
 *
 * @author zengdegui
 * @since 2024-9-27
 */
@Service
@RequiredArgsConstructor
public class RedisOauth2AuthorizationServiceImpl implements OAuth2AuthorizationService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void save(OAuth2Authorization authorization) {
        Assert.notNull(authorization, "authorization cannot be null");
        this.buildRedisTemplate();
        final String idKey = RedisOauth2AuthorizationServiceImpl.buildAuthorizationKey(AuthConstant.OAUTH2_PARAMETER_NAME_ID, authorization.getId());
        this.redisTemplate.opsForValue().set(idKey, authorization);
        if (this.isState(authorization)) {
            String state = authorization.getAttribute(OAuth2ParameterNames.STATE);
            String isStateKey = RedisOauth2AuthorizationServiceImpl.buildAuthorizationKey(OAuth2ParameterNames.STATE, state);
            this.redisTemplate.opsForValue().set(isStateKey, authorization, AuthConstant.TIMEOUT, TimeUnit.SECONDS);
        }
        if (this.isAuthorizationCode(authorization)) {
            OAuth2Authorization.Token<OAuth2AuthorizationCode> authorizationCode = authorization.getToken(OAuth2AuthorizationCode.class);
            String tokenValue = authorizationCode.getToken().getTokenValue();
            String isAuthorizationCodeKey = RedisOauth2AuthorizationServiceImpl.buildAuthorizationKey(OAuth2ParameterNames.CODE, tokenValue);
            Instant expiresAt = authorizationCode.getToken().getExpiresAt();
            final long timeout = Duration.between(Instant.now(), expiresAt).getSeconds();
            this.redisTemplate.opsForValue().set(isAuthorizationCodeKey, authorization, timeout, TimeUnit.SECONDS);
        }
        if (this.isAccessToken(authorization)) {
            OAuth2Authorization.Token<OAuth2AccessToken> accessToken = authorization.getToken(OAuth2AccessToken.class);
            String tokenValue = accessToken.getToken().getTokenValue();
            String isAccessTokenKey = RedisOauth2AuthorizationServiceImpl.buildAuthorizationKey(OAuth2ParameterNames.ACCESS_TOKEN, tokenValue);
            Instant expiresAt = accessToken.getToken().getExpiresAt();
            final long timeout = Duration.between(Instant.now(), expiresAt).getSeconds();
            this.redisTemplate.opsForValue().set(isAccessTokenKey, authorization, timeout, TimeUnit.SECONDS);
        }
        if (this.isRefreshToken(authorization)) {
            OAuth2Authorization.Token<OAuth2RefreshToken> refreshToken = authorization.getToken(OAuth2RefreshToken.class);
            String tokenValue = refreshToken.getToken().getTokenValue();
            String isRefreshTokenKey = RedisOauth2AuthorizationServiceImpl.buildAuthorizationKey(OAuth2ParameterNames.REFRESH_TOKEN, tokenValue);
            Instant expiresAt = refreshToken.getToken().getExpiresAt();
            final long timeout = Duration.between(Instant.now(), expiresAt).getSeconds();
            this.redisTemplate.opsForValue().set(isRefreshTokenKey, authorization, timeout, TimeUnit.SECONDS);
        }
        if (this.isIdToken(authorization)) {
            OAuth2Authorization.Token<OidcIdToken> idToken = authorization.getToken(OidcIdToken.class);
            String tokenValue = idToken.getToken().getTokenValue();
            String isIdTokenKey = RedisOauth2AuthorizationServiceImpl.buildAuthorizationKey(OidcParameterNames.ID_TOKEN, tokenValue);
            Instant expiresAt = idToken.getToken().getExpiresAt();
            final long timeout = Duration.between(Instant.now(), expiresAt).getSeconds();
            this.redisTemplate.opsForValue().set(isIdTokenKey, authorization, timeout, TimeUnit.SECONDS);
        }
        if (this.isDeviceCode(authorization)) {
            OAuth2Authorization.Token<OAuth2DeviceCode> deviceCode = authorization.getToken(OAuth2DeviceCode.class);
            String tokenValue = deviceCode.getToken().getTokenValue();
            String isDeviceCodeKey = RedisOauth2AuthorizationServiceImpl.buildAuthorizationKey(OAuth2ParameterNames.DEVICE_CODE, tokenValue);
            Instant expiresAt = deviceCode.getToken().getExpiresAt();
            final long timeout = Duration.between(Instant.now(), expiresAt).getSeconds();
            this.redisTemplate.opsForValue().set(isDeviceCodeKey, authorization, timeout, TimeUnit.SECONDS);
        }
        if (this.isUserCode(authorization)) {
            OAuth2Authorization.Token<OAuth2UserCode> userCode = authorization.getToken(OAuth2UserCode.class);
            String tokenValue = userCode.getToken().getTokenValue();
            String isUserCodeKey = RedisOauth2AuthorizationServiceImpl.buildAuthorizationKey(OAuth2ParameterNames.USER_CODE, tokenValue);
            Instant expiresAt = userCode.getToken().getExpiresAt();
            final long timeout = Duration.between(Instant.now(), expiresAt).getSeconds();
            this.redisTemplate.opsForValue().set(isUserCodeKey, authorization, timeout, TimeUnit.SECONDS);
        }
    }

    @Override
    public void remove(OAuth2Authorization authorization) {
        List<String> keys = new ArrayList<>(10);
        String idKey = RedisOauth2AuthorizationServiceImpl.buildAuthorizationKey(AuthConstant.OAUTH2_PARAMETER_NAME_ID, authorization.getId());
        keys.add(idKey);
        if (this.isState(authorization)) {
            String state = authorization.getAttribute(OAuth2ParameterNames.STATE);
            String isStateKey = RedisOauth2AuthorizationServiceImpl.buildAuthorizationKey(OAuth2ParameterNames.STATE, state);
            keys.add(isStateKey);
        }
        if (this.isAuthorizationCode(authorization)) {
            OAuth2Authorization.Token<OAuth2AuthorizationCode> authorizationCode = authorization.getToken(OAuth2AuthorizationCode.class);
            String tokenValue = authorizationCode.getToken().getTokenValue();
            String isAuthorizationCodeKey = RedisOauth2AuthorizationServiceImpl.buildAuthorizationKey(OAuth2ParameterNames.CODE, tokenValue);
            keys.add(isAuthorizationCodeKey);
        }
        if (this.isAccessToken(authorization)) {
            OAuth2Authorization.Token<OAuth2AccessToken> accessToken = authorization.getToken(OAuth2AccessToken.class);
            String tokenValue = accessToken.getToken().getTokenValue();
            String isAccessTokenKey = RedisOauth2AuthorizationServiceImpl.buildAuthorizationKey(OAuth2ParameterNames.ACCESS_TOKEN, tokenValue);
            keys.add(isAccessTokenKey);
        }
        if (this.isRefreshToken(authorization)) {
            OAuth2Authorization.Token<OAuth2RefreshToken> refreshToken = authorization.getToken(OAuth2RefreshToken.class);
            String tokenValue = refreshToken.getToken().getTokenValue();
            String isRefreshTokenKey = RedisOauth2AuthorizationServiceImpl.buildAuthorizationKey(OAuth2ParameterNames.REFRESH_TOKEN, tokenValue);
            keys.add(isRefreshTokenKey);
        }
        if (this.isIdToken(authorization)) {
            OAuth2Authorization.Token<OidcIdToken> idToken = authorization.getToken(OidcIdToken.class);
            String tokenValue = idToken.getToken().getTokenValue();
            String isIdTokenKey = RedisOauth2AuthorizationServiceImpl.buildAuthorizationKey(OidcParameterNames.ID_TOKEN, tokenValue);
            keys.add(isIdTokenKey);
        }
        if (this.isDeviceCode(authorization)) {
            OAuth2Authorization.Token<OAuth2DeviceCode> deviceCode = authorization.getToken(OAuth2DeviceCode.class);
            String tokenValue = deviceCode.getToken().getTokenValue();
            String isDeviceCodeKey = RedisOauth2AuthorizationServiceImpl.buildAuthorizationKey(OAuth2ParameterNames.DEVICE_CODE, tokenValue);
            keys.add(isDeviceCodeKey);
        }
        if (this.isUserCode(authorization)) {
            OAuth2Authorization.Token<OAuth2UserCode> userCode = authorization.getToken(OAuth2UserCode.class);
            String tokenValue = userCode.getToken().getTokenValue();
            String isUserCodeKey = RedisOauth2AuthorizationServiceImpl.buildAuthorizationKey(OAuth2ParameterNames.USER_CODE, tokenValue);
            keys.add(isUserCodeKey);
        }
        this.redisTemplate.delete(keys);
    }

    @Override
    public OAuth2Authorization findById(String id) {
        this.buildRedisTemplate();
        final String idKey = RedisOauth2AuthorizationServiceImpl.buildAuthorizationKey(AuthConstant.OAUTH2_PARAMETER_NAME_ID, id);
        return (OAuth2Authorization) Optional.ofNullable(this.redisTemplate.opsForValue().get(idKey)).orElse(null);
    }

    @Override
    public OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType) {
        Assert.hasText(token, "token cannot be empty");
        Assert.notNull(tokenType, "tokenType cannot be empty");
        this.buildRedisTemplate();
        return (OAuth2Authorization) this.redisTemplate.opsForValue()
            .get(RedisOauth2AuthorizationServiceImpl.buildAuthorizationKey(tokenType.getValue(), token));
    }

    private void buildRedisTemplate() {
        this.redisTemplate.setKeySerializer(RedisSerializer.string());
        this.redisTemplate.setValueSerializer(RedisSerializer.java());
    }

    private boolean isState(OAuth2Authorization authorization) {
        return Objects.nonNull(authorization.getAttribute(OAuth2ParameterNames.STATE));
    }

    private boolean isAuthorizationCode(OAuth2Authorization authorization) {
        OAuth2Authorization.Token<OAuth2AuthorizationCode> authorizationCode =
            authorization.getToken(OAuth2AuthorizationCode.class);
        return Objects.nonNull(authorizationCode);
    }

    private boolean isAccessToken(OAuth2Authorization authorization) {
        OAuth2Authorization.Token<OAuth2AccessToken> accessToken =
            authorization.getToken(OAuth2AccessToken.class);
        return Objects.nonNull(accessToken) && Objects.nonNull(accessToken.getToken().getTokenType());
    }

    private boolean isRefreshToken(OAuth2Authorization authorization) {
        OAuth2Authorization.Token<OAuth2RefreshToken> refreshToken =
            authorization.getToken(OAuth2RefreshToken.class);
        return Objects.nonNull(refreshToken) && Objects.nonNull(refreshToken.getToken().getTokenValue());
    }

    private boolean isIdToken(OAuth2Authorization authorization) {
        OAuth2Authorization.Token<OidcIdToken> idToken =
            authorization.getToken(OidcIdToken.class);
        return Objects.nonNull(idToken) && Objects.nonNull(idToken.getToken().getTokenValue());
    }

    private boolean isDeviceCode(OAuth2Authorization authorization) {
        OAuth2Authorization.Token<OAuth2DeviceCode> deviceCode =
            authorization.getToken(OAuth2DeviceCode.class);
        return Objects.nonNull(deviceCode) && Objects.nonNull(deviceCode.getToken().getTokenValue());
    }

    private boolean isUserCode(OAuth2Authorization authorization) {
        OAuth2Authorization.Token<OAuth2UserCode> userCode =
            authorization.getToken(OAuth2UserCode.class);
        return Objects.nonNull(userCode) && Objects.nonNull(userCode.getToken().getTokenValue());
    }

    /**
     * md5加密key.
     *
     * @param type  类型
     * @param value 值
     * @return String
     */
    public static String buildAuthorizationKey(String type, String value) {
        value = CryptoUtil.encryptBySm3(value);
        return String.format("%s::%s::%s", AuthConstant.AUTHORIZATION_TYPE, type, value);
    }
}
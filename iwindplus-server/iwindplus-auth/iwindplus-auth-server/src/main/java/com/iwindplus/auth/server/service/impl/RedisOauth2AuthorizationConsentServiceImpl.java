/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.auth.server.service.impl;

import com.iwindplus.auth.domain.constant.AuthConstant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsent;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * 基于redis的授权确认服务实现.
 *
 * @author zengdegui
 * @since 2024-9-27
 */
@Service
@RequiredArgsConstructor
public class RedisOauth2AuthorizationConsentServiceImpl implements OAuth2AuthorizationConsentService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void save(OAuth2AuthorizationConsent authorizationConsent) {
        Assert.notNull(authorizationConsent, "authorizationConsent cannot be null");
        this.buildRedisTemplate();
        String registeredClientId = authorizationConsent.getRegisteredClientId();
        String principalName = authorizationConsent.getPrincipalName();
        String key = this.buildAuthorizationKey(registeredClientId, principalName);
        this.redisTemplate.opsForValue().set(key, authorizationConsent);
    }

    @Override
    public void remove(OAuth2AuthorizationConsent authorizationConsent) {
        Assert.notNull(authorizationConsent, "authorizationConsent cannot be null");

        String registeredClientId = authorizationConsent.getRegisteredClientId();
        String principalName = authorizationConsent.getPrincipalName();
        String key = this.buildAuthorizationKey(registeredClientId, principalName);
        this.redisTemplate.delete(key);
    }

    @Override
    public OAuth2AuthorizationConsent findById(String registeredClientId, String principalName) {
        Assert.hasText(registeredClientId, "registeredClientId cannot be empty");
        Assert.hasText(principalName, "principalName cannot be empty");
        this.buildRedisTemplate();
        String key = this.buildAuthorizationKey(registeredClientId, principalName);
        return (OAuth2AuthorizationConsent) Optional.ofNullable(this.redisTemplate.opsForValue().get(key)).orElse(null);
    }

    private void buildRedisTemplate() {
        this.redisTemplate.setKeySerializer(RedisSerializer.string());
        this.redisTemplate.setValueSerializer(RedisSerializer.java());
    }

    /**
     * key.
     *
     * @param registeredClientId 注册客户端id
     * @param principalName      身份信息
     * @return String
     */
    private String buildAuthorizationKey(String registeredClientId, String principalName) {
        return String.format("%s::%s_%s", AuthConstant.CONSENT_KEY_PREFIX, registeredClientId, principalName);
    }
}

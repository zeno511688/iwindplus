/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.auth.infrastructure.persistence;

import com.iwindplus.auth.common.constant.AuthConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsent;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 基于redis的授权确认服务实现.
 *
 * @author zengdegui
 * @since 2024-9-27
 */
@Slf4j
@Component
public class RedisOauth2AuthorizationConsentServiceImpl implements OAuth2AuthorizationConsentService {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisOauth2AuthorizationConsentServiceImpl(
        @Qualifier("oauth2RedisTemplate") RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void save(OAuth2AuthorizationConsent authorizationConsent) {
        Assert.notNull(authorizationConsent, "authorizationConsent cannot be null");
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
        String key = this.buildAuthorizationKey(registeredClientId, principalName);
        return (OAuth2AuthorizationConsent) this.redisTemplate.opsForValue().get(key);
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

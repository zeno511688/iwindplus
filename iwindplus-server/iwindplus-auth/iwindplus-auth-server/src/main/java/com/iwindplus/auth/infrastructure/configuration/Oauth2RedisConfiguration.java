/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.auth.infrastructure.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * OAuth2专用Redis配置.
 * <p>
 * OAuth2授权对象使用JDK序列化，与全局GzipRedisSerializer隔离，互不影响.
 *
 * @author zengdegui
 * @since 2026-09-20
 */
@Slf4j
@Configuration
public class Oauth2RedisConfiguration {

    /**
     * 创建OAuth2专用的RedisTemplate.
     * <p>
     * 使用JDK序列化存储OAuth2Authorization等复杂对象，
     * 复用全局RedisTemplate的ConnectionFactory.
     *
     * @param sharedRedisTemplate 全局共享的RedisTemplate
     * @return OAuth2专用的RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> oauth2RedisTemplate(
        RedisTemplate<String, Object> sharedRedisTemplate) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(sharedRedisTemplate.getConnectionFactory());
        template.setKeySerializer(RedisSerializer.string());
        template.setValueSerializer(RedisSerializer.java());
        template.setHashKeySerializer(RedisSerializer.string());
        template.setHashValueSerializer(RedisSerializer.java());
        template.afterPropertiesSet();
        log.info("OAuth2 RedisTemplate initialized with JDK serialization");
        return template;
    }
}

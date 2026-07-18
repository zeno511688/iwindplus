/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.shiro.cache;

import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * redis实现shiro缓存管理器.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ShiroRedisCacheManager implements CacheManager {
    @SuppressWarnings("rawtypes")
    private final ConcurrentMap<String, Cache> caches = new ConcurrentHashMap<>(16);

    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 前缀.
     */
    private String keyPrefix;

    /**
     * 失效时间.
     */
    private Duration timeout;

    @SuppressWarnings("unchecked")
    @Override
    public <K, V> Cache<K, V> getCache(String name) throws CacheException {
        Cache<K, V> cache = this.caches.get(name);
        if (Objects.isNull(cache)) {
            cache = new ShiroCache<>(name, (RedisTemplate<K, V>) this.redisTemplate, this.keyPrefix, this.timeout);
            this.caches.put(name, cache);
        }
        return cache;
    }
}

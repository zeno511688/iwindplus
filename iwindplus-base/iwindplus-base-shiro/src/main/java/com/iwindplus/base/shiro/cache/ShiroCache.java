/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.shiro.cache;

import cn.hutool.core.collection.CollUtil;
import com.iwindplus.base.domain.constant.CommonConstant;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * redis实现shiro缓存.
 *
 * @param <K> 泛型
 * @param <V> 泛型
 * @author zengdegui
 * @since 2018/9/1
 */
@Slf4j
public class ShiroCache<K, V> implements Cache<K, V> {

    private RedisTemplate<K, V> redisTemplate;

    /**
     * 名称.
     */
    private String name;

    /**
     * 前缀.
     */
    private String keyPrefix;

    /**
     * 失效时间.
     */
    private Duration timeout;

    public ShiroCache(String name, RedisTemplate<K, V> redisTemplate, String keyPrefix, Duration timeout) {
        super();
        this.name = name;
        this.redisTemplate = redisTemplate;
        this.keyPrefix = keyPrefix;
        this.timeout = timeout;
    }

    @Override
    public void clear() throws CacheException {
        try {
            this.redisTemplate.delete(this.keys());
        } catch (Exception ex) {
            log.error("清除缓存出错", ex);
        }
    }

    @Override
    public V get(K key) throws CacheException {
        if (Objects.isNull(key)) {
            return null;
        }
        try {
            return this.redisTemplate.opsForValue().get(this.getRedisCacheKey(key));
        } catch (Exception ex) {
            log.error("获取缓存出错", ex);
        }
        return null;
    }

    @Override
    public Set<K> keys() {
        try {
            return this.redisTemplate.keys(this.getRedisCacheKey("*"));
        } catch (Exception ex) {
            log.error("获取缓存key出错", ex);
        }
        return Collections.emptySet();
    }

    @Override
    public V put(K key, V value) throws CacheException {
        if (Objects.isNull(key) || Objects.isNull(value)) {
            return null;
        }
        try {
            this.redisTemplate.opsForValue().set(this.getRedisCacheKey(key), value, this.timeout);
        } catch (Exception ex) {
            log.error("缓存出错", ex);
        }
        return value;
    }

    @Override
    public V remove(K key) throws CacheException {
        if (Objects.isNull(key)) {
            return null;
        }
        V old = this.get(key);
        try {
            this.redisTemplate.delete(this.getRedisCacheKey(key));
        } catch (Exception ex) {
            log.error("删除缓存出错", ex);
        }
        return old;
    }

    @Override
    public int size() {
        try {
            return this.keys().size();
        } catch (Exception ex) {
            log.error("获取缓存个数出错", ex);
        }
        return CommonConstant.NumberConstant.NUMBER_ZERO;
    }

    @Override
    public Collection<V> values() {
        List<V> list = new ArrayList<>(10);
        try {
            Set<K> set = this.keys();
            if (CollUtil.isNotEmpty(set)) {
                final List<V> values = this.redisTemplate.opsForValue().multiGet(set);
                if (CollUtil.isNotEmpty(values)) {
                    values.stream().filter(Objects::nonNull).forEach(list::add);
                }
            }
        } catch (Exception ex) {
            log.error("获取缓存出错", ex);
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    private K getRedisCacheKey(Object key) {
        Object redisKey = this.getStringRedisKey(key);
        StringBuilder sb = new StringBuilder();
        sb.append(this.keyPrefix).append(this.name).append(CommonConstant.SymbolConstant.COLON).append(redisKey);
        return (K) sb.toString();
    }

    private String getStringRedisKey(Object key) {
        String redisKey;
        if (key instanceof PrincipalCollection obj) {
            redisKey = this.getRedisKeyFromPrincipalCollection(obj);
        } else {
            redisKey = key.toString();
        }
        return redisKey;
    }

    private String getRedisKeyFromPrincipalCollection(PrincipalCollection key) {
        List<String> realmNames = this.getRealmNames(key);
        Collections.sort(realmNames);
        return this.joinRealmNames(realmNames);
    }

    private List<String> getRealmNames(PrincipalCollection key) {
        List<String> realmArr = new ArrayList<>(10);
        Set<String> realmNames = key.getRealmNames();
        realmNames.forEach(realmArr::add);
        return realmArr;
    }

    private String joinRealmNames(List<String> realmArr) {
        StringBuilder redisKeyBuilder = new StringBuilder();
        realmArr.forEach(redisKeyBuilder::append);
        return redisKeyBuilder.toString();
    }

    @Override
    public String toString() {
        return "ShiroCacheDTO{" + "redisTemplate=" + redisTemplate + ", name='" + name + '\'' + ", keyPrefix='" + keyPrefix + '\'' + ", timeout="
            + timeout + '}';
    }
}

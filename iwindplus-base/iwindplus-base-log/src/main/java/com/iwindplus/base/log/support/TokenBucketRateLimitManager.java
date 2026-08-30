/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.log.support;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.iwindplus.base.log.domain.dto.TokenBucketRateLimiterDTO;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * 基于令牌桶的限流管理器.
 *
 * <p>特性：
 * <ul>
 *   <li>使用令牌桶算法，更简单可靠</li>
 *   <li>支持静默期（超过限流后进入静默期）</li>
 *   <li>使用 Caffeine 缓存，自动过期</li>
 *   <li>线程安全，无竞态条件</li>
 * </ul>
 *
 * @author zengdegui
 * @since 2026/08/26
 */
@Slf4j
public class TokenBucketRateLimitManager {

    /**
     * 限流器缓存.
     */
    private final Cache<String, RateLimitEntry> cache;

    /**
     * 桶容量（最大令牌数）.
     */
    private final long capacity;

    /**
     * 令牌生成速率（每秒生成的令牌数）.
     */
    private final long rate;

    /**
     * 静默期时长（毫秒）.
     */
    private final long silenceMillis;

    /**
     * 构造函数.
     *
     * @param capacity     桶容量（最大令牌数）
     * @param rate         令牌生成速率（每秒生成的令牌数）
     * @param silenceMillis 静默期时长（毫秒）
     * @param cacheSize    缓存大小
     */
    public TokenBucketRateLimitManager(long capacity, long rate, long silenceMillis, int cacheSize) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must > 0");
        }
        if (rate <= 0) {
            throw new IllegalArgumentException("rate must > 0");
        }
        if (silenceMillis < 0) {
            throw new IllegalArgumentException("silenceMillis must >= 0");
        }
        if (cacheSize <= 0) {
            throw new IllegalArgumentException("cacheSize must > 0");
        }

        this.capacity = capacity;
        this.rate = rate;
        this.silenceMillis = silenceMillis;

        this.cache = Caffeine.newBuilder()
            .maximumSize(cacheSize)
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build();

        log.info("TokenBucketRateLimitManager initialized: capacity={}, rate={}/s, silenceMillis={}, cacheSize={}",
            capacity, rate, silenceMillis, cacheSize);
    }

    /**
     * 检查是否被限流.
     *
     * @param key 限流键
     * @return true-被限流，false-未限流
     */
    public boolean isRateLimited(String key) {
        RateLimitEntry entry = cache.get(key, k -> new RateLimitEntry(capacity, rate));

        // 检查是否在静默期
        if (entry.isInSilencePeriod()) {
            return true;
        }

        // 尝试获取令牌
        boolean acquired = entry.tryAcquire();

        if (!acquired) {
            // 获取失败，进入静默期
            entry.enterSilencePeriod(silenceMillis);
            log.debug("Rate limit triggered for key: {}, entering silence period for {}ms", key, silenceMillis);
        }

        return !acquired;
    }

    /**
     * 获取缓存大小.
     *
     * @return 缓存大小
     */
    public long getCacheSize() {
        return cache.estimatedSize();
    }

    /**
     * 清空缓存.
     */
    public void clear() {
        cache.invalidateAll();
        log.info("Rate limit cache cleared");
    }

    /**
     * 限流条目（包含令牌桶和静默期控制）.
     */
    private static class RateLimitEntry {

        private final TokenBucketRateLimiterDTO rateLimiter;
        private volatile long silenceEndTime = 0;

        RateLimitEntry(long capacity, long rate) {
            this.rateLimiter = new TokenBucketRateLimiterDTO(capacity, rate);
        }

        boolean tryAcquire() {
            return rateLimiter.tryAcquire();
        }

        boolean isInSilencePeriod() {
            return System.currentTimeMillis() < silenceEndTime;
        }

        void enterSilencePeriod(long silenceMillis) {
            this.silenceEndTime = System.currentTimeMillis() + silenceMillis;
        }
    }
}

/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.log.ratelimit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.iwindplus.base.log.domain.dto.PreciseRateLimitEntryDTO;
import java.time.Duration;

/**
 * 精确滑动窗口限流管理器
 * <p>
 * 负责： - key 维度限流 - 静默期控制 - 自动过期清理
 *
 * @author zengdegui
 * @since 2025/11/23 21:15
 */
public class PreciseRateLimitManager {

    /**
     * 缓存
     */
    private final Cache<String, PreciseRateLimitEntryDTO> cache;

    /**
     * 滑动窗口的桶数
     */
    private final Integer bucketCount;

    /**
     * 窗口大小（毫秒）
     */
    private final Long windowMillis;

    /**
     * 静默期（毫秒）
     */
    private final Long silenceMillis;

    /**
     * 最大请求数
     */
    private final Integer maxRequests;

    /**
     * 构造函数.
     *
     * @param bucketCount    桶数量
     * @param windowSeconds  窗口大小（秒）
     * @param silenceSeconds 静默期（秒）
     * @param maxRequests    最大请求数
     * @param maxSize        最大缓存大小
     */
    public PreciseRateLimitManager(
        Integer bucketCount,
        Long windowSeconds,
        Long silenceSeconds,
        Integer maxRequests,
        Integer maxSize) {

        if (bucketCount <= 0) {
            throw new IllegalArgumentException("bucketCount must > 0");
        }

        this.bucketCount = bucketCount;
        this.windowMillis = windowSeconds * 1000;
        this.silenceMillis = silenceSeconds * 1000;
        this.maxRequests = maxRequests;

        this.cache = Caffeine.newBuilder()
            .maximumSize(maxSize)
            .expireAfterAccess(Duration.ofMinutes(10))
            .build();
    }

    /**
     * 判断是否被限流.
     *
     * @param key 键
     * @return boolean
     */
    public boolean isRateLimited(String key) {
        long now = System.currentTimeMillis();

        PreciseRateLimitEntryDTO entry = cache.get(
            key,
            k -> new PreciseRateLimitEntryDTO(bucketCount, windowMillis)
        );

        if (entry.isSilenced(now)) {
            return true;
        }

        int total = entry.increment(now);
        if (total > maxRequests) {
            entry.trySetSilence(now, silenceMillis);
            return true;
        }

        return false;
    }

    /**
     * 获取缓存大小.
     *
     * @return long
     */
    public long getCacheSize() {
        return cache.estimatedSize();
    }
}
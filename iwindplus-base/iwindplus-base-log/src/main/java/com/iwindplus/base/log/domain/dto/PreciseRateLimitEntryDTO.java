/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.log.domain.dto;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Data;

/**
 * 单个 key 的限流条目 包含： - 精确滑动窗口计数器 - 静默期控制.
 *
 * @author zengdegui
 * @since 2025/11/23 21:15
 */
@Data
public class PreciseRateLimitEntryDTO implements Serializable {

    /**
     * 精确滑动窗口计数器.
     */
    private final SlidingWindowRateLimiterDTO limiter;

    /**
     * 静默期控制.
     */
    private final AtomicLong silenceEnd = new AtomicLong(0);

    /**
     * 构造函数.
     *
     * @param bucketCount  滑动窗口大小
     * @param windowMillis 滑动窗口时间间隔
     */
    public PreciseRateLimitEntryDTO(int bucketCount, long windowMillis) {
        this.limiter = new SlidingWindowRateLimiterDTO(bucketCount, windowMillis);
    }

    /**
     * 是否被静默期控制.
     *
     * @param now 当前时间
     * @return 是否被静默期控制
     */
    public boolean isSilenced(long now) {
        return now < silenceEnd.get();
    }

    /**
     * 设置静默期.
     *
     * @param now           当前时间
     * @param silenceMillis 静默期时间间隔
     * @return 是否设置成功
     */
    public boolean trySetSilence(long now, long silenceMillis) {
        long newEnd = now + silenceMillis;

        while (true) {
            long current = silenceEnd.get();
            if (current > newEnd) {
                return false;
            }
            if (silenceEnd.compareAndSet(current, newEnd)) {
                return true;
            }
        }
    }

    /**
     * 尝试增加计数.
     *
     * @param now 当前时间
     * @return 增加后的计数值
     */
    public int increment(long now) {
        return limiter.incrementAndGet(now);
    }
}
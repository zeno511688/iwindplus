/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.log.domain.dto;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import lombok.Data;

/**
 * 真正精确滑动时间窗口限流器
 * <p>
 * 算法： - 将窗口拆分为多个 bucket - 每个 bucket 使用 LongAdder 计数 - 通过时间戳判断 bucket 是否过期 - 统计有效 bucket 总和
 *
 * @author zengdegui
 * @since 2025/11/23 21:15
 */
@Data
public class SlidingWindowRateLimiterDTO implements Serializable {

    /**
     * 桶数量
     */
    private final Integer bucketCount;

    /**
     * 滑动窗口大小.
     */
    private final Long windowMillis;

    /**
     * 每个 bucket 的大小.
     */
    private final Long bucketMillis;

    /**
     * 时间桶
     */
    private final Bucket[] buckets;

    /**
     * 构造函数
     *
     * @param bucketCount  桶数量
     * @param windowMillis 滑动窗口大小
     */
    public SlidingWindowRateLimiterDTO(int bucketCount, long windowMillis) {
        if (bucketCount <= 0) {
            throw new IllegalArgumentException("bucketCount must > 0");
        }
        if (windowMillis <= 0) {
            throw new IllegalArgumentException("windowMillis must > 0");
        }

        this.bucketCount = bucketCount;
        this.windowMillis = windowMillis;
        this.bucketMillis = windowMillis / bucketCount;

        this.buckets = new Bucket[bucketCount];
        for (int i = 0; i < bucketCount; i++) {
            buckets[i] = new Bucket();
        }
    }

    /**
     * 递增并返回当前滑动窗口总请求数
     */
    public int incrementAndGet(long now) {
        long bucketIndex = (now / bucketMillis) % bucketCount;
        Bucket bucket = buckets[(int) bucketIndex];

        long bucketStart = now - (now % bucketMillis);

        bucket.resetIfExpired(bucketStart);

        bucket.increment();

        return sumValidBuckets(now);
    }

    /**
     * 统计当前窗口内有效 bucket 总和
     */
    private int sumValidBuckets(long now) {
        long minTime = now - windowMillis;
        int total = 0;

        for (Bucket bucket : buckets) {
            long start = bucket.startTime.get();
            if (start >= minTime) {
                total += bucket.count.intValue();
            }
        }

        return total;
    }

    /**
     * 单个时间桶
     */
    private static class Bucket {

        /**
         * 桶内计数器
         */
        private final LongAdder count = new LongAdder();

        /**
         * 桶开始时间
         */
        private final AtomicLong startTime = new AtomicLong(0);

        /**
         * 递增
         */
        void increment() {
            count.increment();
        }

        /**
         * 重置
         */
        void resetIfExpired(long newStart) {
            long oldStart = startTime.get();

            if (oldStart != newStart) {
                if (startTime.compareAndSet(oldStart, newStart)) {
                    count.reset();
                }
            }
        }
    }
}
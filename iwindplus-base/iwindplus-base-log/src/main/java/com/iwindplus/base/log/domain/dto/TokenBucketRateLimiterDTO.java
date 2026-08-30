/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.log.domain.dto;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 令牌桶限流器.
 *
 * <p>算法原理：
 * <ul>
 *   <li>系统以恒定速率向桶中添加令牌</li>
 *   <li>每次请求需要从桶中获取一个令牌</li>
 *   <li>如果桶中没有令牌，请求被拒绝</li>
 *   <li>桶有最大容量，令牌数不会超过容量</li>
 * </ul>
 *
 * <p>优点：
 * <ul>
 *   <li>允许突发流量（更灵活）</li>
 *   <li>实现简单，性能高</li>
 *   <li>无并发竞态问题</li>
 *   <li>业界广泛使用</li>
 * </ul>
 *
 * @author zengdegui
 * @since 2026/08/26
 */
public class TokenBucketRateLimiterDTO {

    /**
     * 桶容量（最大令牌数）.
     */
    private final Long capacity;

    /**
     * 令牌生成速率（每秒生成的令牌数）.
     */
    private final Long rate;

    /**
     * 当前令牌数量.
     */
    private final AtomicLong tokens;

    /**
     * 上次更新时间（毫秒）.
     */
    private final AtomicLong lastRefillTime;

    /**
     * 构造函数.
     *
     * @param capacity 桶容量（最大令牌数）
     * @param rate     令牌生成速率（每秒生成的令牌数）
     */
    public TokenBucketRateLimiterDTO(long capacity, long rate) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must > 0");
        }
        if (rate <= 0) {
            throw new IllegalArgumentException("rate must > 0");
        }

        this.capacity = capacity;
        this.rate = rate;
        // 初始填满桶
        this.tokens = new AtomicLong(capacity);
        this.lastRefillTime = new AtomicLong(System.currentTimeMillis());
    }

    /**
     * 尝试获取令牌.
     *
     * @return true-获取成功，false-获取失败（被限流）
     */
    public boolean tryAcquire() {
        return tryAcquire(1);
    }

    /**
     * 尝试获取指定数量的令牌.
     *
     * @param requested 请求的令牌数量
     * @return true-获取成功，false-获取失败（被限流）
     */
    public boolean tryAcquire(long requested) {
        if (requested <= 0) {
            throw new IllegalArgumentException("requested must > 0");
        }

        // 先补充令牌
        refill();

        // CAS 循环尝试获取令牌
        while (true) {
            long current = tokens.get();

            // 令牌不足，拒绝请求
            if (current < requested) {
                return false;
            }

            // 尝试减少令牌
            long newTokens = current - requested;
            if (tokens.compareAndSet(current, newTokens)) {
                return true;
            }
        }
    }

    /**
     * 补充令牌（基于时间差计算）.
     */
    private void refill() {
        long now = System.currentTimeMillis();
        long lastTime = lastRefillTime.get();

        // 计算时间差（毫秒）
        long elapsed = now - lastTime;

        // 时间差太小，不需要补充
        if (elapsed <= 0) {
            return;
        }

        // 计算应该补充的令牌数
        // elapsed 毫秒 / 1000 = 秒数
        // 秒数 * rate = 应该补充的令牌数
        long tokensToAdd = (elapsed * rate) / 1000;

        // 补充的令牌数为 0，不需要补充
        if (tokensToAdd <= 0) {
            return;
        }

        // CAS 更新上次补充时间
        if (!lastRefillTime.compareAndSet(lastTime, now)) {
            // 其他线程已经更新了时间，放弃本次补充
            return;
        }

        // CAS 循环补充令牌
        while (true) {
            long current = tokens.get();

            // 计算新的令牌数（不超过容量）
            long newTokens = Math.min(current + tokensToAdd, capacity);

            // 尝试更新令牌数
            if (tokens.compareAndSet(current, newTokens)) {
                return;
            }
        }
    }

    /**
     * 获取当前令牌数量.
     *
     * @return 当前令牌数量
     */
    public long getAvailableTokens() {
        refill();
        return tokens.get();
    }

    /**
     * 重置令牌桶（填满令牌）.
     */
    public void reset() {
        tokens.set(capacity);
        lastRefillTime.set(System.currentTimeMillis());
    }
}

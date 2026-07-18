/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.redis.domain.annotation;

import com.iwindplus.base.redis.support.RedisKeyResolver;
import com.iwindplus.base.redis.support.impl.UserRedisKeyResolver;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.temporal.ChronoUnit;
import org.redisson.api.RateType;

/**
 * redis限流注解.
 *
 * @author zengdegui
 * @since 2020/6/13
 */
@Inherited
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RedisRateLimiter {

    /**
     * 是否开启（可选，默认：true）.
     *
     * @return boolean
     */
    boolean enabled() default true;

    /**
     * 是否按路径限流（可选，默认：false）.
     *
     * @return boolean
     */
    boolean enabledLimitPath() default false;

    /**
     * 名称（可选）.
     *
     * @return String
     */
    String[] names() default {};

    /**
     * keys（可选，支持使用spEl表达式）.
     *
     * @return String[]
     */
    String[] keys() default {};

    /**
     * 限流类型，默认 OVERALL（全局限流）.
     *
     * @return RateType
     */
    RateType rateType() default RateType.OVERALL;

    /**
     * 限流次数，每个时间窗口允许请求数量（可选，默认：1000）.
     *
     * @return long
     */
    long rate() default 1000;

    /**
     * 限流速率，时间窗口大小（可选，默认：1）
     *
     * @return long
     */
    long rateInterval() default 1;

    /**
     * 限流速率时间单位（可选，默认：秒(s)）
     *
     * @return TimeUnit
     */
    ChronoUnit rateIntervalUnit() default ChronoUnit.SECONDS;

    /**
     * 使用的Key解析器（可选，默认：UserRedisKeyResolver）.
     *
     * @return Class<? extends RedisKeyResolver>
     */
    Class<? extends RedisKeyResolver> keyResolver() default UserRedisKeyResolver.class;
}

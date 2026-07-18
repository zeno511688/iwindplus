/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.redis.domain.annotation;

import com.iwindplus.base.redis.domain.enums.RedisLockTypeEnum;
import com.iwindplus.base.redis.support.RedisKeyResolver;
import com.iwindplus.base.redis.support.impl.UserRedisKeyResolver;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * redis分布式锁注解.
 *
 * @author zengdegui
 * @since 2020/6/13
 */
@Inherited
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RedisLock {

    /**
     * 是否开启（可选，默认：true）.
     *
     * @return boolean
     */
    boolean enabled() default true;

    /**
     * 名称（可选）.
     *
     * @return String
     */
    String[] names() default {};

    /**
     * 分布式锁的key（可选，支持使用spEl表达式）.
     *
     * @return String[]
     */
    String[] keys() default {};

    /**
     * 租约时间（可选，默认：-1） 如果当前线程成功获取到锁，那么锁将被持有的时间长度。这个时间过后，锁会自动释放，默认为 -1(代表不指定，如果指定则看门狗(watchdog)不会自动续约).
     *
     * @return long
     */
    long leaseTime() default -1;

    /**
     * 等待获取锁的最长时间（可选，默认：1）
     *
     * @return long
     */
    long waitTime() default 1;

    /**
     * 时间单位（可选，默认：秒(s)）
     *
     * @return TimeUnit
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 分布式锁类型（可选，默认：LOCK） 目前支持的类型请查看{@link RedisLockTypeEnum}
     *
     * @return RedissonLockType
     */
    RedisLockTypeEnum lockType() default RedisLockTypeEnum.LOCK;

    /**
     * 使用的Key解析器（可选，默认：UserRedisKeyResolver）.
     *
     * @return Class<? extends RedisKeyResolver>
     */
    Class<? extends RedisKeyResolver> keyResolver() default UserRedisKeyResolver.class;
}

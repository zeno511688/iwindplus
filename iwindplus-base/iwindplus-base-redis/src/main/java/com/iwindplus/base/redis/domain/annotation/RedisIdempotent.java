/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.redis.domain.annotation;

import com.iwindplus.base.redis.domain.enums.IdempotentResultModeEnum;
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
 * Redis 幂等注解.
 *
 * @author zengdegui
 * @since 2020/6/13
 */
@Inherited
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RedisIdempotent {

    /**
     * 是否启用.
     *
     * @return boolean
     */
    boolean enabled() default true;

    /**
     * 是否需要请求ID.
     *
     * @return boolean
     */
    boolean requireRequestId() default false;

    /**
     * 幂等处理中过期时间（默认：30s）.
     *
     * @return long
     */
    long processingTtl() default 30;

    /**
     * 幂等成功过期时间（默认：600s）.
     *
     * @return long
     */
    long successTtl() default 600;

    /**
     * 时间单位（默认：秒(s)）.
     *
     * @return TimeUnit
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 业务前缀（可选）.
     *
     * @return String
     */
    String[] names() default {};

    /**
     * key 解析字段（可选，支持参数名 / SpEL / 自定义 resolver）
     *
     * @return String[]
     */
    String[] keys() default {};

    /**
     * 使用的Key解析器（可选，默认：UserRedisKeyResolver）.
     *
     * @return Class<? extends RedisKeyResolver>
     */
    Class<? extends RedisKeyResolver> keyResolver() default UserRedisKeyResolver.class;

    /**
     * 幂等结果处理模式（可选，默认：RETURN_CACHE）.
     *
     * @return IdempotentResultModeEnum
     */
    IdempotentResultModeEnum resultMode() default IdempotentResultModeEnum.RETURN_CACHE;
}

/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.redis.support;

import org.aspectj.lang.JoinPoint;
import org.springframework.cache.interceptor.KeyGenerator;

/**
 * Redis Key解析器接口.
 *
 * @author zengdegui
 * @since 2020/6/13
 */
public interface RedisKeyResolver {

    /**
     * 解析一个 Key.
     *
     * @param joinPoint    AOP切面
     * @param keyGenerator key生成器
     * @param keys         keys
     * @return String
     */
    String resolver(JoinPoint joinPoint, KeyGenerator keyGenerator, String[] keys);
}

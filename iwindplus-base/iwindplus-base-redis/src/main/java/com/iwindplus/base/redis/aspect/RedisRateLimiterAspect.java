/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.redis.aspect;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.iwindplus.base.redis.domain.annotation.RedisRateLimiter;
import com.iwindplus.base.redis.domain.constant.RedisConstant;
import com.iwindplus.base.redis.domain.property.RedisProperty;
import com.iwindplus.base.redis.domain.property.RedisProperty.RateLimiterConfig;
import com.iwindplus.base.redis.service.RedissonService;
import com.iwindplus.base.redis.support.RedisKeyResolver;
import com.iwindplus.base.util.CryptoUtil;
import com.iwindplus.base.util.HttpsUtil;
import jakarta.annotation.Resource;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RateType;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.core.annotation.Order;

/**
 * 限流切面.
 *
 * @author zengdegui
 * @since 2024/07/06 12:18
 */
@Slf4j
@Aspect
@Order(1)
public class RedisRateLimiterAspect {

    @Resource
    private RedissonService redissonService;

    @Resource
    private KeyGenerator keyGenerator;

    @Resource
    private RedisProperty property;

    /**
     * 切点.
     */
    @Pointcut("@annotation(com.iwindplus.base.redis.domain.annotation.RedisRateLimiter)")
    public void pointCutMethod() {
    }

    /**
     * Before 切面
     *
     * @param joinPoint joinPoint
     */
    @Before("pointCutMethod()")
    public void beforePointCut(JoinPoint joinPoint) {
        final RateLimiterConfig cfg = this.property.getRateLimiter();

        final Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        final RedisRateLimiter annotation = method.getAnnotation(RedisRateLimiter.class);
        if (Boolean.FALSE.equals(cfg.getEnabled())
            || Boolean.FALSE.equals(annotation.enabled())) {
            return;
        }

        final RateType rateType = annotation.rateType();
        final long rate = annotation.rate();
        final long rateInterval = annotation.rateInterval();
        final ChronoUnit rateIntervalUnit = annotation.rateIntervalUnit();
        final RedisKeyResolver keyResolver = SpringUtil.getBean(annotation.keyResolver());
        final String path = HttpsUtil.getPath();
        final String fallback = joinPoint.getSignature().toShortString();
        final String[] names = ArrayUtil.isNotEmpty(annotation.names())
            ? annotation.names()
            : CharSequenceUtil.isNotBlank(path)
                ? new String[]{CryptoUtil.encryptBySm3(path)} : new String[]{fallback};

        final String name = redissonService.baseOperation().getRedisKey(RedisConstant.RATE_LIMITER_KEY, names,
            keyResolver, joinPoint, this.keyGenerator, annotation.keys());

        this.redissonService.rateLimiter().execute(name, rateType, rate, Duration.of(rateInterval, rateIntervalUnit), null);
    }

}
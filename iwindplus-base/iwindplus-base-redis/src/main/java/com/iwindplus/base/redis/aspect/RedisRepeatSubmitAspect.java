/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.redis.aspect;

import cn.hutool.extra.spring.SpringUtil;
import com.iwindplus.base.redis.domain.annotation.RedisRepeatSubmit;
import com.iwindplus.base.redis.domain.constant.RedisConstant;
import com.iwindplus.base.redis.domain.property.RedisProperty;
import com.iwindplus.base.redis.domain.property.RedisProperty.RepeatSubmitConfig;
import com.iwindplus.base.redis.service.RedissonService;
import com.iwindplus.base.redis.support.RedisKeyResolver;
import jakarta.annotation.Resource;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.core.annotation.Order;

/**
 * 防重复提交切面.
 *
 * @author zengdegui
 * @since 2024/07/06 12:18
 */
@Slf4j
@Aspect
@Order(2)
public class RedisRepeatSubmitAspect {

    @Resource
    private KeyGenerator keyGenerator;

    @Resource
    private RedissonService redissonService;

    @Resource
    private RedisProperty property;

    /**
     * 切点.
     */
    @Pointcut("@annotation(com.iwindplus.base.redis.domain.annotation.RedisRepeatSubmit)")
    public void pointCutMethod() {
    }

    /**
     * 环绕通知.
     *
     * @param joinPoint
     * @return Object
     * @throws Throwable
     */
    @Around("pointCutMethod()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        final RepeatSubmitConfig cfg = this.property.getRepeatSubmit();

        final Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        final RedisRepeatSubmit annotation = method.getAnnotation(RedisRepeatSubmit.class);
        if (Boolean.FALSE.equals(cfg.getEnabled())
            || Boolean.FALSE.equals(annotation.enabled())) {
            return joinPoint.proceed();
        }

        final RedisKeyResolver keyResolver = SpringUtil.getBean(annotation.keyResolver());

        final String key = redissonService.baseOperation().getRedisKey(RedisConstant.REPEAT_SUBMIT_KEY_PREFIX, annotation.names(),
            keyResolver, joinPoint, this.keyGenerator, annotation.keys());
        final Duration ttl = getDuration(annotation.ttl(), annotation.timeUnit());

        return this.redissonService.repeatSubmit().execute(key, ttl, joinPoint::proceed);
    }

    private Duration getDuration(long ttl, TimeUnit timeUnit) {
        return Duration.of(
            ttl,
            Optional.ofNullable(timeUnit)
                .orElse(TimeUnit.SECONDS)
                .toChronoUnit()
        );
    }
}

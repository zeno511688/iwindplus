/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.redis.aspect;

import cn.hutool.extra.spring.SpringUtil;
import com.iwindplus.base.redis.domain.annotation.RedisLock;
import com.iwindplus.base.redis.domain.constant.RedisConstant;
import com.iwindplus.base.redis.domain.enums.RedisLockTypeEnum;
import com.iwindplus.base.redis.domain.property.RedisProperty;
import com.iwindplus.base.redis.domain.property.RedisProperty.LockConfig;
import com.iwindplus.base.redis.service.RedissonService;
import com.iwindplus.base.redis.support.RedisKeyResolver;
import jakarta.annotation.Resource;
import java.lang.reflect.Method;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.core.annotation.Order;

/**
 * 分布式锁切面.
 *
 * @author zengdegui
 * @since 2024/07/06 12:18
 */
@Slf4j
@Aspect
@Order(4)
public class RedisLockAspect {

    @Resource
    private KeyGenerator keyGenerator;

    @Resource
    private RedissonService redissonService;

    @Resource
    private RedisProperty property;

    /**
     * 切点.
     */
    @Pointcut("@annotation(com.iwindplus.base.redis.domain.annotation.RedisLock)")
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
        final LockConfig cfg = this.property.getLock();

        final Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        final RedisLock annotation = method.getAnnotation(RedisLock.class);
        if (Boolean.FALSE.equals(cfg.getEnabled())
            || Boolean.FALSE.equals(annotation.enabled())) {
            return joinPoint.proceed();
        }

        final RedisKeyResolver keyResolver = SpringUtil.getBean(annotation.keyResolver());

        final RedisLockTypeEnum lockType = Optional.ofNullable(annotation.lockType()).orElse(cfg.getLockType());
        final String key = redissonService.baseOperation().getRedisKey(RedisConstant.LOCK_KEY_PREFIX, annotation.names(),
            keyResolver, joinPoint, this.keyGenerator, annotation.keys());

        return this.redissonService.lock().execute(lockType, key, annotation.waitTime()
            , annotation.leaseTime(), annotation.timeUnit(), joinPoint::proceed);
    }
}

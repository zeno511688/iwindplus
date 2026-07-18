/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.redis.aspect;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwindplus.base.domain.constant.CommonConstant.HeaderConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.redis.domain.annotation.RedisIdempotent;
import com.iwindplus.base.redis.domain.constant.RedisConstant;
import com.iwindplus.base.redis.domain.enums.IdempotentResultModeEnum;
import com.iwindplus.base.redis.domain.property.RedisProperty;
import com.iwindplus.base.redis.domain.property.RedisProperty.IdempotentConfig;
import com.iwindplus.base.redis.service.RedissonService;
import com.iwindplus.base.redis.support.RedisKeyResolver;
import com.iwindplus.base.util.HttpsUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.Map;
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
import org.springframework.util.ConcurrentReferenceHashMap;

/**
 * Redis 幂等切面.
 *
 * @author zengdegui
 * @since 2026/02/03 22:31
 */
@Slf4j
@Aspect
@Order(3)
public class RedisIdempotentAspect {

    @Resource
    private KeyGenerator keyGenerator;

    @Resource
    private RedissonService redissonService;

    @Resource
    private RedisProperty property;

    @Resource
    private ObjectMapper objectMapper;

    private static final Map<Type, JavaType> TYPE_CACHE =
        new ConcurrentReferenceHashMap<>(16, ConcurrentReferenceHashMap.ReferenceType.WEAK);

    /**
     * 切点.
     */
    @Pointcut("@annotation(com.iwindplus.base.redis.domain.annotation.RedisIdempotent)")
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
        final IdempotentConfig cfg = property.getIdempotent();

        final Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        final RedisIdempotent annotation = method.getAnnotation(RedisIdempotent.class);

        if (Boolean.FALSE.equals(cfg.getEnabled())
            || Boolean.FALSE.equals(annotation.enabled())) {
            return joinPoint.proceed();
        }

        final HttpServletRequest request = HttpsUtil.getHttpServletRequest();
        String requestId = null;
        if (request != null) {
            requestId = request.getHeader(HeaderConstant.X_REQUESTED_ID);
        }

        if (annotation.requireRequestId() && CharSequenceUtil.isBlank(requestId)) {
            throw new BizException(BizCodeEnum.REQUEST_ID_NOT_EMPTY);
        }

        // 构建 reqKey
        String reqKey = null;
        if (CharSequenceUtil.isNotBlank(requestId)) {
            reqKey = RedisConstant.IDEMPOTENT_REQ_KEY_PREFIX + requestId;
        }

        final RedisKeyResolver keyResolver = SpringUtil.getBean(annotation.keyResolver());

        // 构建 bizKey
        String bizKey = redissonService.baseOperation().getRedisKey(
            RedisConstant.IDEMPOTENT_BIZ_KEY_PREFIX,
            annotation.names(),
            keyResolver,
            joinPoint,
            this.keyGenerator,
            annotation.keys()
        );

        final Duration processingTtl = getDuration(annotation.processingTtl(), annotation.timeUnit());
        final Duration successTtl = getDuration(annotation.successTtl(), annotation.timeUnit());

        final JavaType javaType = resolveReturnType(method);
        final IdempotentResultModeEnum resultMode = annotation.resultMode();

        return this.redissonService.idempotent().execute(reqKey, bizKey, processingTtl,
            successTtl, javaType, resultMode, joinPoint::proceed);
    }

    private Duration getDuration(long ttl, TimeUnit timeUnit) {
        return Duration.of(
            ttl,
            Optional.ofNullable(timeUnit)
                .orElse(TimeUnit.SECONDS)
                .toChronoUnit()
        );
    }

    private JavaType resolveReturnType(Method method) {
        // computeIfAbsent 线程安全，且只在第一次计算
        Type type = method.getGenericReturnType();
        return TYPE_CACHE.computeIfAbsent(type, objectMapper::constructType);
    }
}

/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.feign.aspect;

import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.feign.domain.annotation.IgnoreFeignFallback;
import com.iwindplus.base.feign.domain.property.FeignProperty;
import com.iwindplus.base.feign.domain.property.FeignProperty.FeignFallbackConfig;
import feign.FeignException;
import feign.RetryableException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import jakarta.annotation.Resource;
import java.lang.reflect.Method;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import reactor.core.publisher.Mono;

/**
 * 全局Feign异常降级AOP .
 *
 * @author zengdegui
 * @since 2026/04/11 00:28
 */
@Slf4j
@Aspect
public class FeignGlobalFallbackAspect {

    @Resource
    private FeignProperty property;

    /**
     * 切点.
     */
    @Pointcut("@within(org.springframework.cloud.openfeign.FeignClient)")
    public void pointCutMethod() {
    }

    /**
     * 环绕通知.
     *
     * @param joinPoint 切点
     * @return Object
     * @throws Throwable 异常
     */
    @Around("pointCutMethod()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        final FeignFallbackConfig cfg = this.property.getFallback();
        if (Boolean.FALSE.equals(cfg.getEnabled())) {
            return joinPoint.proceed();
        }

        final MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Class<?> targetClass = joinPoint.getTarget().getClass();
        Method method = methodSignature.getMethod();

        // 方法级忽略（优先级最高）
        if (method.isAnnotationPresent(IgnoreFeignFallback.class)) {
            return joinPoint.proceed();
        }

        // 类级忽略
        if (targetClass.isAnnotationPresent(IgnoreFeignFallback.class)) {
            return joinPoint.proceed();
        }

        try {
            return joinPoint.proceed();
        } catch (Throwable ex) {
            log.warn("Feign调用异常，method={}, args={}, ex={}",
                method.getName(),
                Arrays.toString(joinPoint.getArgs()),
                ex.toString()
            );

            if (ex instanceof BizException bizEx) {
                return handleReadOrWrite(method, bizEx);
            }

            BizException bizException = buildBizException(ex);

            return handleReadOrWrite(method, bizException);
        }
    }

    private Object handleReadOrWrite(Method method, BizException e) {
        if (isWriteOperation(method)) {
            return handleWrite(method, e);
        }

        return handleRead(method, e);
    }

    private boolean isWriteOperation(Method method) {
        if (method.isAnnotationPresent(GetMapping.class)) {
            return false;
        }

        if (method.isAnnotationPresent(PostMapping.class)
            || method.isAnnotationPresent(PutMapping.class)
            || method.isAnnotationPresent(DeleteMapping.class)) {
            return true;
        }

        RequestMapping mapping = method.getAnnotation(RequestMapping.class);
        if (mapping != null && mapping.method().length > 0) {
            for (RequestMethod m : mapping.method()) {
                if (m == RequestMethod.GET) {
                    return false;
                }
                if (m == RequestMethod.POST
                    || m == RequestMethod.PUT
                    || m == RequestMethod.DELETE) {
                    return true;
                }
            }
        }

        return true;
    }

    private Object handleRead(Method method, BizException e) {
        if (Mono.class.isAssignableFrom(method.getReturnType())) {
            return Mono.just(ResultVO.error(e));
        }

        return ResultVO.error(e);
    }

    private Object handleWrite(Method method, BizException e) {
        if (Mono.class.isAssignableFrom(method.getReturnType())) {
            return Mono.error(e);
        }

        return ResultVO.error(e);
    }

    private BizException buildBizException(Throwable ex) {
        if (ex instanceof CallNotPermittedException
            || ex instanceof RetryableException) {
            return new BizException(BizCodeEnum.SERVICE_UNAVAILABLE);
        }

        if (ex instanceof FeignException) {
            return new BizException(BizCodeEnum.RPC_ERROR);
        }

        return new BizException(BizCodeEnum.EXECUTE_ERROR);
    }
}

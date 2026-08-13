/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.xxl.job.aspect;

import com.iwindplus.base.monitor.support.ObservationExecutor;
import com.xxl.job.core.handler.annotation.XxlJob;
import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * xxl-job执行拦截.
 *
 * @author zengdegui
 * @since 2024/4/11
 */
@Slf4j
@Aspect
public record XxlJobObservationAspect(ObservationExecutor observationExecutor) {

    private static final String OBSERVATION_NAME = "xxl.job.execute";
    private static final String JOB_NAME = "job.name";

    /**
     * 环绕通知.
     *
     * @param joinPoint 切点
     * @return Object
     * @throws Throwable 异常
     */
    @Around("@annotation(com.xxl.job.core.handler.annotation.XxlJob)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        final MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        final Method method = methodSignature.getMethod();

        final XxlJob xxlJob = method.getAnnotation(XxlJob.class);
        final String jobName = xxlJob.value();

        return observationExecutor.execute(
            OBSERVATION_NAME,
            observation -> {
                observation
                    .lowCardinalityKeyValue(JOB_NAME, jobName);

                return joinPoint.proceed();
            }
        );
    }

}

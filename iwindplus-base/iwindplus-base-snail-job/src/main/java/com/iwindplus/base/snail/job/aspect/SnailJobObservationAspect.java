/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.snail.job.aspect;

import com.aizuda.snailjob.client.job.core.annotation.JobExecutor;
import com.iwindplus.base.monitor.support.ObservationExecutor;
import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * snail-job执行拦截.
 *
 * @author zengdegui
 * @since 2024/4/11
 */
@Slf4j
@Aspect
public record SnailJobObservationAspect(ObservationExecutor observationExecutor) {

    private static final String OBSERVATION_NAME = "snail.job.execute";
    private static final String JOB_NAME = "job.name";

    /**
     * 环绕通知.
     *
     * @param joinPoint 切点
     * @return Object
     */
    @Around("@annotation(com.aizuda.snailjob.client.job.core.annotation.JobExecutor)")
    public Object around(ProceedingJoinPoint joinPoint) {
        final MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        final Method method = methodSignature.getMethod();

        final JobExecutor job = method.getAnnotation(JobExecutor.class);
        final String jobName = job.name();

        return observationExecutor.execute(
            OBSERVATION_NAME,
            observation -> {
                observation
                    .lowCardinalityKeyValue(JOB_NAME, jobName);

                try {
                    return joinPoint.proceed();
                } catch (Throwable e) {
                    log.error("Throwable", e);
                }
                return null;
            }
        );
    }

}

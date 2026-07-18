/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.mybatis.aspect;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 增删改事物切面.
 *
 * @author zengdegui
 * @since 2025/10/11 22:07
 */
@Slf4j
@Aspect
@Order(Ordered.LOWEST_PRECEDENCE - 10)
public class MybatisTransactionAspect {

    @Resource
    private TransactionTemplate transactionTemplate;

    /**
     * 切点.
     */
    @Pointcut("execution(* *..mapper..*Mapper.*insert*(..)) || " +
        "execution(* *..mapper..*Mapper.*save*(..)) || " +
        "execution(* *..mapper..*Mapper.*add*(..)) || " +
        "execution(* *..mapper..*Mapper.*create*(..)) || " +
        "execution(* *..mapper..*Mapper.*update*(..)) || " +
        "execution(* *..mapper..*Mapper.*edit*(..)) || " +
        "execution(* *..mapper..*Mapper.*modify*(..)) || " +
        "execution(* *..mapper..*Mapper.*merge*(..)) || " +
        "execution(* *..mapper..*Mapper.*delete*(..)) || " +
        "execution(* *..mapper..*Mapper.*remove*(..)) || " +
        "execution(* *..repository..*Repository.*insert*(..)) || " +
        "execution(* *..repository..*Repository.*save*(..)) || " +
        "execution(* *..repository..*Repository.*add*(..)) || " +
        "execution(* *..repository..*Repository.*create*(..)) || " +
        "execution(* *..repository..*Repository.*update*(..)) || " +
        "execution(* *..repository..*Repository.*edit*(..)) || " +
        "execution(* *..repository..*Repository.*modify*(..)) || " +
        "execution(* *..repository..*Repository.*merge*(..)) || " +
        "execution(* *..repository..*Repository.*delete*(..)) || " +
        "execution(* *..repository..*Repository.*remove*(..))"
    )
    public void pointCutMethod() {
    }

    /**
     * 环绕通知.
     *
     * @param pjp 切点
     * @return Object
     * @throws Throwable 异常
     */
    @Around("pointCutMethod()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        // 根据配置决定是否强制新事务
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            log.debug("Transaction already active, proceeding without new transaction for method={}.{}",
                pjp.getTarget().getClass().getSimpleName(),
                pjp.getSignature().getName());

            return pjp.proceed();
        }

        return transactionTemplate.execute(status -> {
            try {
                return pjp.proceed();
            } catch (Throwable ex) {
                log.warn("Tx rollback on {}.{} -> {}",
                    pjp.getTarget().getClass().getSimpleName(),
                    pjp.getSignature().getName(),
                    ex.toString());
                throw ex instanceof RuntimeException rex ? rex : new RuntimeException(ex);
            }
        });
    }
}

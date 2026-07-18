/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.dtx.tcc.aspect;

import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.dtx.tcc.domain.annotation.TccGlobalTx;
import com.iwindplus.base.domain.context.TccContextHolder;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.dtx.client.TccGlobalTxClient;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * tcc全局事务切面.
 *
 * @author zengdegui
 * @since 2026/02/06 21:31
 */
@Slf4j
@Aspect
public class TccGlobalTransactionalAspect {

    @Resource
    private TccGlobalTxClient tccGlobalTxClient;

    /**
     * 环绕通知.
     *
     * @param pjp         切点
     * @param tccGlobalTx 注解
     * @return Object
     */
    @Around("@annotation(tccGlobalTx)")
    public Object around(ProceedingJoinPoint pjp, TccGlobalTx tccGlobalTx) throws Throwable {
        String xid = this.doBegin(tccGlobalTx);
        if (CharSequenceUtil.isBlank(xid)) {
            return null;
        }

        // 绑定 ThreadLocal
        TccContextHolder.setXid(xid);
        try {
            Object result = pjp.proceed();
            // 提交
            this.doConfirm(xid);
            return result;
        } catch (Throwable ex) {
            log.error("TCC global transaction failed, initiating cancel phase. xid={}", xid, ex);
            this.doCancel(xid);

            throw ex;
        } finally {
            TccContextHolder.remove();
        }
    }

    private String doBegin(TccGlobalTx tccGlobalTx) {
        final ResultVO<String> response = this.tccGlobalTxClient.begin(
            tccGlobalTx.bizType(), tccGlobalTx.timeoutSeconds());
        response.errorThrow();
        return response.getBizData();
    }

    private void doConfirm(String xid) {
        final ResultVO<Boolean> response = this.tccGlobalTxClient.confirm(xid);
        response.errorThrow();
    }

    private void doCancel(String xid) {
        final ResultVO<Boolean> response = this.tccGlobalTxClient.cancel(xid);
        response.errorThrow();
    }
}

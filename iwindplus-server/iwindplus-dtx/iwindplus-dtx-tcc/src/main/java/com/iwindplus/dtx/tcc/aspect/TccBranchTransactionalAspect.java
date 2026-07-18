/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.dtx.tcc.aspect;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.iwindplus.dtx.tcc.domain.annotation.TccBranchTx;
import com.iwindplus.base.domain.constant.CommonConstant.NetWorkConstant;
import com.iwindplus.base.domain.context.TccContextHolder;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.util.JacksonUtil;
import com.iwindplus.dtx.client.TccBranchTxClient;
import com.iwindplus.dtx.domain.dto.TccBranchTxDTO;
import jakarta.annotation.Resource;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.util.Assert;

/**
 * tcc分支事务切面.
 *
 * @author zengdegui
 * @since 2026/02/06 21:47
 */
@Slf4j
@Aspect
public class TccBranchTransactionalAspect {

    @Resource
    private TccBranchTxClient tccBranchTxClient;

    /**
     * 环绕通知.
     *
     * @param pjp         切点
     * @param tccBranchTx 注解
     * @return Object
     */
    @Around("@annotation(tccBranchTx)")
    public Object around(ProceedingJoinPoint pjp, TccBranchTx tccBranchTx) throws Throwable {
        String xid = TccContextHolder.getXid();
        Assert.notNull(xid, "xid is null");

        final long branchId = IdUtil.getSnowflakeNextId();

        TccBranchTxDTO branch = TccBranchTxDTO.builder()
            .xid(xid)
            .branchId(branchId)
            .contextPath(CharSequenceUtil.isBlank(tccBranchTx.contextPath())
                ? NetWorkConstant.LB_PREFIX + SpringUtil.getApplicationName()
                : tccBranchTx.contextPath())
            .confirmUrl(tccBranchTx.confirmUrl())
            .cancelUrl(tccBranchTx.cancelUrl())
            .payload(this.buildPayload(pjp))
            .build();
        final ResultVO<Long> response = this.tccBranchTxClient.register(branch);
        response.errorThrow();
        final Long id = response.getBizData();
        if (Objects.isNull(id)) {
            return null;
        }

        try {
            Object result = pjp.proceed();

            // TRYING → TRY_SUCCESS
            doTrySuccess(id);

            return result;
        } catch (Throwable ex) {
            log.error("TCC branch transaction failed, initiating tryFail phase. xid={}", xid, ex);
            // TRYING → TRY_FAIL
            doTryFail(id);

            throw ex;
        }
    }

    private void doTrySuccess(Long id) {
        final ResultVO<Boolean> response = this.tccBranchTxClient.trySuccess(id);
        response.errorThrow();
    }

    private void doTryFail(Long id) {
        final ResultVO<Boolean> response = this.tccBranchTxClient.tryFail(id);
        response.errorThrow();
    }

    private String buildPayload(ProceedingJoinPoint pjp) {
        Object[] args = pjp.getArgs();

        if (args == null || args.length == 0) {
            return null;
        }

        // 如果只有一个参数，直接序列化
        if (args.length == 1) {
            return JacksonUtil.toJsonStr(args[0]);
        }

        // 多参数情况
        Map<String, Object> paramMap = new HashMap<>(16);

        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Parameter[] parameters = signature.getMethod().getParameters();

        for (int i = 0; i < parameters.length; i++) {
            paramMap.put(parameters[i].getName(), args[i]);
        }

        return JacksonUtil.toJsonStr(paramMap);
    }
}

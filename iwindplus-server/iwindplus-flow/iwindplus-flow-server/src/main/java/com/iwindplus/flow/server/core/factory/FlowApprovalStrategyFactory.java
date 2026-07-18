/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server.core.factory;

import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.flow.domain.enums.ApprovalMethodEnum;
import com.iwindplus.flow.server.core.strategy.ApprovalHandler;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.util.function.SingletonSupplier;

/**
 * 流程审批策略工厂.
 *
 * @author zengdegui
 * @since 2026/05/22 20:11
 */
@Slf4j
@Component
public class FlowApprovalStrategyFactory {

    private final Supplier<Map<ApprovalMethodEnum, ApprovalHandler>> strategyMapSupplier;

    /**
     * 构造函数.
     *
     * @param executorProvider 执行器提供者
     */
    public FlowApprovalStrategyFactory(ObjectProvider<ApprovalHandler> executorProvider) {

        this.strategyMapSupplier = SingletonSupplier.of(() -> {

            final Map<ApprovalMethodEnum, ApprovalHandler>
                strategyMap = executorProvider
                .orderedStream()
                .collect(Collectors.toMap(
                    ApprovalHandler::getType,
                    Function.identity(),
                    (existing, replacement) -> replacement
                ));

            log.info("Loaded {} strategies={}",
                ApprovalHandler.class.getSimpleName(),
                strategyMap.keySet()
            );

            return strategyMap;
        });
    }

    /**
     * 获取执行管理器.
     *
     * @param approvalMethod 审批方式
     * @return ApprovalHandler
     */
    public ApprovalHandler getApprovalHandler(ApprovalMethodEnum approvalMethod) {
        ApprovalHandler strategy = getStrategyMap().get(approvalMethod);
        if (strategy == null) {
            log.error("ApprovalHandler Invalid strategy={}", approvalMethod);
            throw new BizException(BizCodeEnum.INVALID_STRATEGY);
        }

        return strategy;
    }

    /**
     * 获取策略缓存.
     *
     * @return Map<ApprovalMethodEnum, ApprovalHandler>
     */
    private Map<ApprovalMethodEnum, ApprovalHandler> getStrategyMap() {
        return strategyMapSupplier.get();
    }

}

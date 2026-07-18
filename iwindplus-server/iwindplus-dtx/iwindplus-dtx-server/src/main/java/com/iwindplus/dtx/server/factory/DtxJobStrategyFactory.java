/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.dtx.server.factory;

import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.dtx.domain.enums.DtxJobEnum;
import com.iwindplus.dtx.server.support.DtxJobHandler;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.util.function.SingletonSupplier;

/**
 * 分布式事务job操作策略工厂.
 *
 * @author zengdegui
 * @since 2025/11/29 23:50
 */
@Slf4j
@Component
public class DtxJobStrategyFactory {

    private final Supplier<Map<DtxJobEnum, DtxJobHandler>> strategyMapSupplier;

    /**
     * 构造函数.
     *
     * @param executorProvider 执行器提供者
     */
    public DtxJobStrategyFactory(ObjectProvider<DtxJobHandler> executorProvider) {

        this.strategyMapSupplier = SingletonSupplier.of(() -> {

            final Map<DtxJobEnum, DtxJobHandler>
                strategyMap = executorProvider
                .orderedStream()
                .collect(Collectors.toMap(
                    DtxJobHandler::support,
                    Function.identity(),
                    (existing, replacement) -> replacement
                ));

            log.info("Loaded {} strategies={}",
                DtxJobHandler.class.getSimpleName(),
                strategyMap.keySet()
            );

            return strategyMap;
        });
    }

    /**
     * 获取job管理器.
     *
     * @param param 参数
     * @return DtxJobHandler
     */
    public DtxJobHandler getJobHandler(DtxJobEnum param) {
        DtxJobHandler strategy = getStrategyMap().get(param);
        if (strategy == null) {
            log.error("DtxJobHandler Invalid strategy={}", param);
            throw new BizException(BizCodeEnum.INVALID_STRATEGY);
        }

        return strategy;
    }

    /**
     * 获取策略缓存.
     *
     * @return Map<DtxJobEnum, DtxJobHandler>
     */
    private Map<DtxJobEnum, DtxJobHandler> getStrategyMap() {
        return strategyMapSupplier.get();
    }
}

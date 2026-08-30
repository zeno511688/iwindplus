/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.dtx.server.factory;

import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.dtx.domain.enums.DtxTaskJobEnum;
import com.iwindplus.dtx.server.support.DtxTaskJobHandler;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;
import org.springframework.util.function.SingletonSupplier;

/**
 * 分布式事务任务job操作策略工厂.
 *
 * @author zengdegui
 * @since 2025/11/29 23:50
 */
@Slf4j
@Component
public class DtxTaskJobStrategyFactory implements SmartInitializingSingleton {

    private final Supplier<Map<DtxTaskJobEnum, DtxTaskJobHandler>> strategyMapSupplier;

    /**
     * 构造函数.
     *
     * @param executorProvider 执行器提供者
     */
    public DtxTaskJobStrategyFactory(ObjectProvider<DtxTaskJobHandler> executorProvider) {
        this.strategyMapSupplier = SingletonSupplier.of(() -> {
            final Map<DtxTaskJobEnum, DtxTaskJobHandler>
                strategyMap = executorProvider
                .orderedStream()
                .collect(Collectors.toMap(
                    DtxTaskJobHandler::support,
                    Function.identity(),
                    (existing, replacement) -> replacement
                ));

            log.info("Loaded {} strategies={}",
                DtxTaskJobHandler.class.getSimpleName(),
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
    public DtxTaskJobHandler getJobHandler(DtxTaskJobEnum param) {
        DtxTaskJobHandler strategy = getStrategyMap().get(param);
        if (strategy == null) {
            log.error("DtxTaskJobHandler Invalid strategy={}", param);
            throw new BizException(BizCodeEnum.INVALID_STRATEGY);
        }

        return strategy;
    }

    /**
     * 获取策略缓存.
     *
     * @return Map<DtxTaskJobEnum, DtxTaskJobHandler>
     */
    private Map<DtxTaskJobEnum, DtxTaskJobHandler> getStrategyMap() {
        return strategyMapSupplier.get();
    }

    @Override
    public void afterSingletonsInstantiated() {
        getStrategyMap();
    }
}

/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.disruptor.factory;

import com.iwindplus.base.disruptor.support.DisruptorEventHandler;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.util.function.SingletonSupplier;

/**
 * Disruptor调度操作工厂.
 *
 * @author zengdegui
 * @since 2025/11/29 23:50
 */
@Slf4j
public class DisruptorEventHandlerStrategyFactory {

    private final Supplier<Map<String, DisruptorEventHandler<?>>> strategyMapSupplier;

    /**
     * 构造函数.
     *
     * @param executorProvider 执行器提供者
     */
    public DisruptorEventHandlerStrategyFactory(ObjectProvider<DisruptorEventHandler<?>> executorProvider) {

        this.strategyMapSupplier = SingletonSupplier.of(() -> {

            final Map<String, DisruptorEventHandler<?>>
                strategyMap = executorProvider
                .orderedStream()
                .collect(Collectors.toMap(
                    DisruptorEventHandler::getName,
                    Function.identity(),
                    (existing, replacement) -> replacement
                ));

            log.info("Loaded {} strategies={}",
                DisruptorEventHandler.class.getSimpleName(),
                strategyMap.keySet()
            );

            return strategyMap;
        });
    }

    /**
     * 获取执行管理器.
     *
     * @param name 名称
     * @return DisruptorEventHandler<?>
     */
    public DisruptorEventHandler<?> getDisruptorEventHandler(String name) {
        DisruptorEventHandler<?> strategy = getStrategyMap().get(name);
        if (strategy == null) {
            log.error("DisruptorEventHandler<?> Invalid strategy={}", name);
            throw new BizException(BizCodeEnum.INVALID_STRATEGY);
        }

        return strategy;
    }

    /**
     * 获取策略缓存.
     *
     * @return Map<String, DisruptorEventHandler < ?>>
     */
    public Map<String, DisruptorEventHandler<?>> getStrategyMap() {
        return strategyMapSupplier.get();
    }
}

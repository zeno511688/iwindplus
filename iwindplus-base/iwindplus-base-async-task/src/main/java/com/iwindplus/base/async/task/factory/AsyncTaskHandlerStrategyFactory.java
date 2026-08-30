/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.task.factory;

import com.iwindplus.base.async.task.support.AsyncTaskHandler;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.util.function.SingletonSupplier;

/**
 * 异步任务处理器策略工厂.
 *
 * @author zengdegui
 * @since 2025/11/29 23:50
 */
@Slf4j
public class AsyncTaskHandlerStrategyFactory implements SmartInitializingSingleton {

    private final Supplier<Map<String, AsyncTaskHandler>> strategyMapSupplier;

    /**
     * 构造函数.
     *
     * @param executorProvider 执行器提供者
     */
    public AsyncTaskHandlerStrategyFactory(ObjectProvider<AsyncTaskHandler> executorProvider) {

        this.strategyMapSupplier = SingletonSupplier.of(() -> {

            final Map<String, AsyncTaskHandler>
                strategyMap = executorProvider
                .orderedStream()
                .collect(Collectors.toMap(
                    AsyncTaskHandler::getExecuteName,
                    Function.identity(),
                    (existing, replacement) -> replacement
                ));

            log.info("Loaded {} strategies={}",
                AsyncTaskHandler.class.getSimpleName(),
                strategyMap.keySet()
            );

            return strategyMap;
        });
    }

    /**
     * 获取执行管理器.
     *
     * @param executeName 执行器名称
     * @return AsyncTaskHandler
     */
    public AsyncTaskHandler getTaskHandler(String executeName) {
        AsyncTaskHandler strategy = getStrategyMap().get(executeName);
        if (strategy == null) {
            log.error("AsyncTaskHandler Invalid strategy={}", executeName);
            throw new BizException(BizCodeEnum.INVALID_STRATEGY);
        }

        return strategy;
    }

    /**
     * 获取策略缓存.
     *
     * @return Map<String, AsyncTaskHandler>
     */
    private Map<String, AsyncTaskHandler> getStrategyMap() {
        return strategyMapSupplier.get();
    }

    @Override
    public void afterSingletonsInstantiated() {
        getStrategyMap();
    }
}

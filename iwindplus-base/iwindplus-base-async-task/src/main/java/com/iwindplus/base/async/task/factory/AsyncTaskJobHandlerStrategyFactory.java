/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.task.factory;

import com.iwindplus.base.async.task.domain.enums.AsyncTaskJobEnum;
import com.iwindplus.base.async.task.support.AsyncTaskJobHandler;
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
 * 异步任务job操作工厂.
 *
 * @author zengdegui
 * @since 2025/11/29 23:50
 */
@Slf4j
public class AsyncTaskJobHandlerStrategyFactory implements SmartInitializingSingleton {

    private final Supplier<Map<AsyncTaskJobEnum, AsyncTaskJobHandler>> strategyMapSupplier;

    /**
     * 构造函数.
     *
     * @param executorProvider 执行器提供者
     */
    public AsyncTaskJobHandlerStrategyFactory(ObjectProvider<AsyncTaskJobHandler> executorProvider) {

        this.strategyMapSupplier = SingletonSupplier.of(() -> {

            final Map<AsyncTaskJobEnum, AsyncTaskJobHandler>
                strategyMap = executorProvider
                .orderedStream()
                .collect(Collectors.toMap(
                    AsyncTaskJobHandler::support,
                    Function.identity(),
                    (existing, replacement) -> replacement
                ));

            log.info("Loaded {} strategies={}",
                AsyncTaskJobHandler.class.getSimpleName(),
                strategyMap.keySet()
            );

            return strategyMap;
        });
    }

    /**
     * 获取job管理器.
     *
     * @param param 参数
     * @return AsyncTaskJobHandler
     */
    public AsyncTaskJobHandler getJobHandler(AsyncTaskJobEnum param) {
        AsyncTaskJobHandler strategy = getStrategyMap().get(param);
        if (strategy == null) {
            log.error("AsyncTaskJobHandler Invalid strategy={}", param);
            throw new BizException(BizCodeEnum.INVALID_STRATEGY);
        }

        return strategy;
    }

    /**
     * 获取策略缓存.
     *
     * @return Map<AsyncTaskJobEnum, AsyncTaskJobHandler>
     */
    private Map<AsyncTaskJobEnum, AsyncTaskJobHandler> getStrategyMap() {
        return strategyMapSupplier.get();
    }

    @Override
    public void afterSingletonsInstantiated() {
        getStrategyMap();
    }
}

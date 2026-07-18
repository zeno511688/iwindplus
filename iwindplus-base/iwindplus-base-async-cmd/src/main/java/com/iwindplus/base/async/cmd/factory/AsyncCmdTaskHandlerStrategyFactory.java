/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.factory;

import com.iwindplus.base.async.cmd.support.AsyncCmdTaskHandler;
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
 * 异步命令任务助手操作工厂.
 *
 * @author zengdegui
 * @since 2025/11/29 23:50
 */
@Slf4j
public class AsyncCmdTaskHandlerStrategyFactory {

    private final Supplier<Map<String, AsyncCmdTaskHandler>> strategyMapSupplier;

    /**
     * 构造函数.
     *
     * @param executorProvider 执行器提供者
     */
    public AsyncCmdTaskHandlerStrategyFactory(ObjectProvider<AsyncCmdTaskHandler> executorProvider) {

        this.strategyMapSupplier = SingletonSupplier.of(() -> {

            final Map<String, AsyncCmdTaskHandler>
                strategyMap = executorProvider
                .orderedStream()
                .collect(Collectors.toMap(
                    AsyncCmdTaskHandler::getExecuteName,
                    Function.identity(),
                    (existing, replacement) -> replacement
                ));

            log.info("Loaded {} strategies={}",
                AsyncCmdTaskHandler.class.getSimpleName(),
                strategyMap.keySet()
            );

            return strategyMap;
        });
    }

    /**
     * 获取执行管理器.
     *
     * @param executeName 执行器名称
     * @return AsyncCmdTaskHandler
     */
    public AsyncCmdTaskHandler getTaskHandler(String executeName) {
        AsyncCmdTaskHandler strategy = getStrategyMap().get(executeName);
        if (strategy == null) {
            log.error("AsyncCmdTaskHandler Invalid strategy={}", executeName);
            throw new BizException(BizCodeEnum.INVALID_STRATEGY);
        }

        return strategy;
    }

    /**
     * 获取策略缓存.
     *
     * @return Map<String, AsyncCmdTaskHandler>
     */
    private Map<String, AsyncCmdTaskHandler> getStrategyMap() {
        return strategyMapSupplier.get();
    }
}

/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.factory;

import com.iwindplus.base.async.cmd.domain.enums.DispatchModeEnum;
import com.iwindplus.base.async.cmd.support.AsyncCmdDispatchHandler;
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
 * 异步命令调度操作工厂.
 *
 * @author zengdegui
 * @since 2025/11/29 23:50
 */
@Slf4j
public class AsyncCmdDispatchHandlerStrategyFactory {

    private final Supplier<Map<DispatchModeEnum, AsyncCmdDispatchHandler>> strategyMapSupplier;

    /**
     * 构造函数.
     *
     * @param executorProvider 执行器提供者
     */
    public AsyncCmdDispatchHandlerStrategyFactory(ObjectProvider<AsyncCmdDispatchHandler> executorProvider) {

        this.strategyMapSupplier = SingletonSupplier.of(() -> {

            final Map<DispatchModeEnum, AsyncCmdDispatchHandler>
                strategyMap = executorProvider
                .orderedStream()
                .collect(Collectors.toMap(
                    AsyncCmdDispatchHandler::support,
                    Function.identity(),
                    (existing, replacement) -> replacement
                ));

            log.info("Loaded {} strategies={}",
                AsyncCmdDispatchHandler.class.getSimpleName(),
                strategyMap.keySet()
            );

            return strategyMap;
        });
    }

    /**
     * 获取调度管理器.
     *
     * @param param 参数
     * @return AsyncCmdDispatchHandler
     */
    public AsyncCmdDispatchHandler getDispatchHandler(DispatchModeEnum param) {
        AsyncCmdDispatchHandler strategy = getStrategyMap().get(param);
        if (strategy == null) {
            log.error("AsyncCmdDispatchHandler Invalid strategy={}", param);
            throw new BizException(BizCodeEnum.INVALID_STRATEGY);
        }

        return strategy;
    }

    /**
     * 获取策略缓存.
     *
     * @return Map<DispatchModeEnum, AsyncCmdDispatchHandler>
     */
    private Map<DispatchModeEnum, AsyncCmdDispatchHandler> getStrategyMap() {
        return strategyMapSupplier.get();
    }
}

/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.factory;

import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdJobEnum;
import com.iwindplus.base.async.cmd.support.AsyncCmdJobHandler;
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
 * 异步命令job操作工厂.
 *
 * @author zengdegui
 * @since 2025/11/29 23:50
 */
@Slf4j
public class AsyncCmdJobHandlerStrategyFactory {

    private final Supplier<Map<AsyncCmdJobEnum, AsyncCmdJobHandler>> strategyMapSupplier;

    /**
     * 构造函数.
     *
     * @param executorProvider 执行器提供者
     */
    public AsyncCmdJobHandlerStrategyFactory(ObjectProvider<AsyncCmdJobHandler> executorProvider) {

        this.strategyMapSupplier = SingletonSupplier.of(() -> {

            final Map<AsyncCmdJobEnum, AsyncCmdJobHandler>
                strategyMap = executorProvider
                .orderedStream()
                .collect(Collectors.toMap(
                    AsyncCmdJobHandler::support,
                    Function.identity(),
                    (existing, replacement) -> replacement
                ));

            log.info("Loaded {} strategies={}",
                AsyncCmdJobHandler.class.getSimpleName(),
                strategyMap.keySet()
            );

            return strategyMap;
        });
    }

    /**
     * 获取job管理器.
     *
     * @param param 参数
     * @return AsyncCmdJobHandler
     */
    public AsyncCmdJobHandler getJobHandler(AsyncCmdJobEnum param) {
        AsyncCmdJobHandler strategy = getStrategyMap().get(param);
        if (strategy == null) {
            log.error("AsyncCmdJobHandler Invalid strategy={}", param);
            throw new BizException(BizCodeEnum.INVALID_STRATEGY);
        }

        return strategy;
    }

    /**
     * 获取策略缓存.
     *
     * @return Map<AsyncCmdJobEnum, AsyncCmdJobHandler>
     */
    private Map<AsyncCmdJobEnum, AsyncCmdJobHandler> getStrategyMap() {
        return strategyMapSupplier.get();
    }
}

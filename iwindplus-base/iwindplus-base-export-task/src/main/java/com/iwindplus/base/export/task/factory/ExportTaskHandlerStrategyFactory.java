/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.export.task.factory;

import com.iwindplus.base.export.task.support.ExportTaskHandler;
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
 * 导出任务处理器工厂.
 *
 * @author zengdegui
 * @since 2026/08/27
 */
@Slf4j
public class ExportTaskHandlerStrategyFactory implements SmartInitializingSingleton {

    private final Supplier<Map<String, ExportTaskHandler>> handlerMapSupplier;

    /**
     * 构造函数.
     *
     * @param handlerProvider 处理器提供者
     */
    public ExportTaskHandlerStrategyFactory(ObjectProvider<ExportTaskHandler> handlerProvider) {
        this.handlerMapSupplier = SingletonSupplier.of(() -> {
            final Map<String, ExportTaskHandler> handlerMap = handlerProvider
                .orderedStream()
                .collect(Collectors.toMap(
                    ExportTaskHandler::getExecuteName,
                    Function.identity(),
                    (existing, replacement) -> replacement
                ));

            log.info("Loaded {} handlers={}",
                ExportTaskHandler.class.getSimpleName(),
                handlerMap.keySet());

            return handlerMap;
        });
    }

    /**
     * 根据执行器名称获取处理器.
     *
     * @param executeName 执行器名称
     * @return 导出任务处理器
     */
    public ExportTaskHandler getTaskHandler(String executeName) {
        ExportTaskHandler handler = getHandlerMap().get(executeName);
        if (handler == null) {
            log.error("ExportTaskHandler Invalid executeName={}", executeName);
            throw new BizException(BizCodeEnum.INVALID_STRATEGY);
        }
        return handler;
    }

    /**
     * 获取处理器缓存.
     *
     * @return Map<String, ExportTaskHandler>
     */
    private Map<String, ExportTaskHandler> getHandlerMap() {
        return handlerMapSupplier.get();
    }

    @Override
    public void afterSingletonsInstantiated() {
        getHandlerMap();
    }
}

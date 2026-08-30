/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.export.task.factory;

import com.iwindplus.base.export.task.domain.enums.ExportTaskJobEnum;
import com.iwindplus.base.export.task.support.ExportTaskJobHandler;
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
 * 导出任务job处理器策略工厂.
 *
 * @author zengdegui
 * @since 2026/08/28
 */
@Slf4j
public class ExportTaskJobHandlerStrategyFactory implements SmartInitializingSingleton {

    private final Supplier<Map<ExportTaskJobEnum, ExportTaskJobHandler>> strategyMapSupplier;

    /**
     * 构造函数.
     *
     * @param handlerProvider 处理器提供者
     */
    public ExportTaskJobHandlerStrategyFactory(ObjectProvider<ExportTaskJobHandler> handlerProvider) {
        this.strategyMapSupplier = SingletonSupplier.of(() -> {
            final Map<ExportTaskJobEnum, ExportTaskJobHandler> strategyMap = handlerProvider
                .orderedStream()
                .collect(Collectors.toMap(
                    ExportTaskJobHandler::support,
                    Function.identity(),
                    (existing, replacement) -> replacement
                ));

            log.info("Loaded {} strategies={}",
                ExportTaskJobHandler.class.getSimpleName(),
                strategyMap.keySet());

            return strategyMap;
        });
    }

    /**
     * 获取job处理器.
     *
     * @param param 参数
     * @return DocumentTaskJobHandler
     */
    public ExportTaskJobHandler getJobHandler(ExportTaskJobEnum param) {
        ExportTaskJobHandler strategy = getStrategyMap().get(param);
        if (strategy == null) {
            log.error("DocumentTaskJobHandler Invalid strategy={}", param);
            throw new BizException(BizCodeEnum.INVALID_STRATEGY);
        }
        return strategy;
    }

    /**
     * 获取策略缓存.
     *
     * @return Map<DocumentTaskJobEnum, DocumentTaskJobHandler>
     */
    private Map<ExportTaskJobEnum, ExportTaskJobHandler> getStrategyMap() {
        return strategyMapSupplier.get();
    }

    @Override
    public void afterSingletonsInstantiated() {
        getStrategyMap();
    }
}

/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.binlog.domain.binlog.strategy;

import com.iwindplus.base.domain.enums.DbActionTypeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.binlog.domain.binlog.model.BinlogActionProcessDTO;
import com.iwindplus.binlog.domain.binlog.enums.BinlogConsumerCodeEnum;
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
 * binlog 操作策略工厂.
 *
 * @param <T> 参数
 * @param <R> 结果
 * @author zengdegui
 * @since 2025/11/29 23:50
 */
@Slf4j
@Component
public class BinlogActionHandlerFactory<T, R> implements SmartInitializingSingleton {

    private final Supplier<Map<DbActionTypeEnum, BinlogActionHandler<T, R>>> strategyMapSupplier;

    /**
     * 构造函数.
     *
     * @param executorProvider 执行器提供者
     */
    public BinlogActionHandlerFactory(ObjectProvider<BinlogActionHandler<T, R>> executorProvider) {
        this.strategyMapSupplier = SingletonSupplier.of(() -> {

            final Map<DbActionTypeEnum, BinlogActionHandler<T, R>>
                strategyMap = executorProvider
                .orderedStream()
                .collect(Collectors.toMap(
                    BinlogActionHandler::support,
                    Function.identity(),
                    (existing, replacement) -> replacement
                ));

            log.info("Loaded {} strategies={}",
                BinlogActionHandler.class.getSimpleName(),
                strategyMap.keySet()
            );

            return strategyMap;
        });
    }

    /**
     * 执行业务逻辑.
     *
     * @param entity 对象
     */
    public R execute(BinlogActionProcessDTO<T> entity) {
        BinlogActionHandler<T, R> strategy = getStrategyMap().get(entity.getActionType());
        if (strategy == null) {
            throw new BizException(BinlogConsumerCodeEnum.INVALID_ACTION_STRATEGY);
        }

        return strategy.execute(entity);
    }

    /**
     * 获取策略缓存.
     *
     * @return Map<DbActionTypeEnum, BinlogActionStrategy < T, R>>
     */
    private Map<DbActionTypeEnum, BinlogActionHandler<T, R>> getStrategyMap() {
        return strategyMapSupplier.get();
    }

    @Override
    public void afterSingletonsInstantiated() {
        getStrategyMap();
    }
}

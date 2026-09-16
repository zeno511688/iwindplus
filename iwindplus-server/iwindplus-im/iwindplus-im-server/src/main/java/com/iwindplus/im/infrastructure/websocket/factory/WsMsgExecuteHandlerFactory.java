/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.im.infrastructure.websocket.factory;

import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.im.api.dto.WsSendMsgDTO;
import com.iwindplus.im.common.enums.CommandEnum;
import com.iwindplus.im.common.enums.ImCodeEnum;
import com.iwindplus.im.infrastructure.websocket.strategy.WsMsgExecuteHandler;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;
import org.springframework.util.function.SingletonSupplier;
import org.tio.core.ChannelContext;

/**
 * websocket 消息策略工厂.
 *
 * @author zengdegui
 * @since 2025/09/21 20:21
 */
@Slf4j
@Component
public class WsMsgExecuteHandlerFactory implements SmartInitializingSingleton {

    private final Supplier<Map<CommandEnum, WsMsgExecuteHandler>> strategyMapSupplier;

    /**
     * 构造函数.
     *
     * @param executorProvider 执行器提供者
     */
    public WsMsgExecuteHandlerFactory(ObjectProvider<WsMsgExecuteHandler> executorProvider) {

        this.strategyMapSupplier = SingletonSupplier.of(() -> {

            final Map<CommandEnum, WsMsgExecuteHandler>
                strategyMap = executorProvider
                .orderedStream()
                .collect(Collectors.toMap(
                    WsMsgExecuteHandler::support,
                    Function.identity(),
                    (existing, replacement) -> replacement
                ));

            log.info("Loaded {} strategies={}",
                WsMsgExecuteHandler.class.getSimpleName(),
                strategyMap.keySet()
            );

            return strategyMap;
        });
    }

    /**
     * 发送消息.
     *
     * @param entity 消息实体
     * @param ctx    通道上下文
     */
    public void send(WsSendMsgDTO entity, ChannelContext ctx) {
        WsMsgExecuteHandler strategy = getStrategyMap().get(entity.getCommand());
        if (strategy == null) {
            throw new BizException(ImCodeEnum.INVALID_MSG_COMMAND);
        }

        strategy.send(entity, ctx);
    }

    /**
     * 获取策略缓存.
     *
     * @return Map<CommandEnum, WsMsgStrategy>
     */
    private Map<CommandEnum, WsMsgExecuteHandler> getStrategyMap() {
        return strategyMapSupplier.get();
    }

    @Override
    public void afterSingletonsInstantiated() {
        getStrategyMap();
    }
}

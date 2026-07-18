/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.im.server.factory;

import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.im.domain.dto.WsSendMsgDTO;
import com.iwindplus.im.domain.enums.CommandEnum;
import com.iwindplus.im.domain.enums.ImCodeEnum;
import com.iwindplus.im.server.strategy.WsMsgStrategy;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
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
public class WsMsgStrategyFactory {

    private final Supplier<Map<CommandEnum, WsMsgStrategy>> strategyMapSupplier;

    /**
     * 构造函数.
     *
     * @param executorProvider 执行器提供者
     */
    public WsMsgStrategyFactory(ObjectProvider<WsMsgStrategy> executorProvider) {

        this.strategyMapSupplier = SingletonSupplier.of(() -> {

            final Map<CommandEnum, WsMsgStrategy>
                strategyMap = executorProvider
                .orderedStream()
                .collect(Collectors.toMap(
                    WsMsgStrategy::support,
                    Function.identity(),
                    (existing, replacement) -> replacement
                ));

            log.info("Loaded {} strategies={}",
                WsMsgStrategy.class.getSimpleName(),
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
        WsMsgStrategy strategy = getStrategyMap().get(entity.getCommand());
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
    private Map<CommandEnum, WsMsgStrategy> getStrategyMap() {
        return strategyMapSupplier.get();
    }
}

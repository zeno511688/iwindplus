/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.integration.factory;

import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.http.client.integration.domain.dto.kyc.sumsub.SumSubWebhookDTO;
import com.iwindplus.base.http.client.integration.listener.SumSubWebhookListenerProcessor;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.util.function.SingletonSupplier;

/**
 * SumSub Webhook处理器策略工厂.
 * <p>
 * 基于@SumSubWebhookListener注解实现策略模式。
 * 业务方只需在方法上添加@SumSubWebhookListener注解，
 * 即可自动被加载到策略工厂中。
 * </p>
 *
 * @author zengdegui
 * @since 2026/08/21
 */
@Slf4j
public class SumSubWebhookHandlerStrategyFactory implements SmartInitializingSingleton {

    private final Supplier<Map<String, Consumer<SumSubWebhookDTO>>> strategyMapSupplier;

    /**
     * 构造函数.
     *
     * @param listenerProcessor 注解处理器
     */
    public SumSubWebhookHandlerStrategyFactory(SumSubWebhookListenerProcessor listenerProcessor) {
        this.strategyMapSupplier = SingletonSupplier.of(() -> {
            final Map<String, Consumer<SumSubWebhookDTO>> strategyMap = listenerProcessor.getHandlers();

            log.info("Loaded {} strategies (annotation: {})",
                SumSubWebhookHandlerStrategyFactory.class.getSimpleName(),
                strategyMap.size()
            );

            return strategyMap;
        });
    }

    /**
     * 获取Webhook处理器.
     *
     * @param webhookType Webhook事件类型
     * @return Consumer<SumSubWebhookDTO>
     */
    public Consumer<SumSubWebhookDTO> getHandler(String webhookType) {
        Consumer<SumSubWebhookDTO> handler = getStrategyMap().get(webhookType);
        if (handler == null) {
            log.error("SumSubWebhookHandler Invalid strategy={}", webhookType);
            throw new BizException(BizCodeEnum.INVALID_STRATEGY);
        }
        return handler;
    }

    /**
     * 获取策略缓存.
     *
     * @return Map<String, Consumer<SumSubWebhookDTO>>
     */
    private Map<String, Consumer<SumSubWebhookDTO>> getStrategyMap() {
        return strategyMapSupplier.get();
    }

    @Override
    public void afterSingletonsInstantiated() {
        getStrategyMap();
    }
}

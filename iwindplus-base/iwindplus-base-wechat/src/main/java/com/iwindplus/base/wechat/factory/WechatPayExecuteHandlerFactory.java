/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.wechat.factory;

import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.wechat.domain.property.WechatProperty;
import com.iwindplus.base.wechat.support.WechatPayExecuteHandler;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.util.function.SingletonSupplier;

/**
 * 微信支付策略工厂. 负责管理和路由支付策略，支持多配置.
 *
 * <p>策略结构：{@code Map<String, WechatPayExecuteHandler>}，key 为配置编码。</p>
 *
 * @author zengdegui
 * @since 2026/9/1
 */
@Slf4j
public class WechatPayExecuteHandlerFactory implements SmartInitializingSingleton {

    private final WechatProperty property;
    private final Supplier<Map<String, WechatPayExecuteHandler>> strategyMapSupplier;

    /**
     * 构造函数.
     *
     * @param property        属性配置
     * @param executorHandlers 策略列表
     */
    public WechatPayExecuteHandlerFactory(WechatProperty property, List<WechatPayExecuteHandler> executorHandlers) {
        this.property = property;
        this.strategyMapSupplier = SingletonSupplier.of(() -> {

            final Map<String, WechatPayExecuteHandler> strategyMap = executorHandlers
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                    WechatPayExecuteHandler::getCode,
                    Function.identity(),
                    (existing, replacement) -> replacement,
                    LinkedHashMap::new
                ));

            log.info("Loaded {} strategies={}",
                WechatPayExecuteHandler.class.getSimpleName(),
                strategyMap.keySet()
            );

            return strategyMap;
        });
    }

    /**
     * 根据配置编码获取支付策略.
     *
     * @param code 配置编码
     * @return 支付策略
     */
    public WechatPayExecuteHandler getHandler(String code) {
        WechatPayExecuteHandler handler = getStrategyMap().get(code);
        if (handler == null) {
            log.error("WechatPayExecuteHandler invalid code={}", code);
            throw new BizException(BizCodeEnum.INVALID_STRATEGY);
        }
        return handler;
    }

    /**
     * 获取默认支付策略（优先级最高的可用策略）.
     *
     * @return 默认支付策略
     */
    public WechatPayExecuteHandler getDefaultHandler() {
        return getStrategyMap().values().stream()
            .filter(WechatPayExecuteHandler::isHealthy)
            .min(Comparator.comparingInt(WechatPayExecuteHandler::getPriority))
            .orElse(null);
    }

    /**
     * 获取策略缓存.
     *
     * @return Map<String, WechatPayExecuteHandler>
     */
    private Map<String, WechatPayExecuteHandler> getStrategyMap() {
        return strategyMapSupplier.get();
    }

    @Override
    public void afterSingletonsInstantiated() {
        getStrategyMap();
    }
}

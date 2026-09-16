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
import com.iwindplus.base.wechat.support.WechatMpExecuteHandler;
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
 * 微信公众号策略工厂. 负责管理和路由公众号策略，支持多配置.
 *
 * <p>策略结构：{@code Map<String, WechatMpExecuteHandler>}，key 为配置编码。</p>
 *
 * @author zengdegui
 * @since 2026/9/1
 */
@Slf4j
public class WechatMpExecuteHandlerFactory implements SmartInitializingSingleton {

    private final WechatProperty property;
    private final Supplier<Map<String, WechatMpExecuteHandler>> strategyMapSupplier;

    /**
     * 构造函数.
     *
     * @param property         属性配置
     * @param executorHandlers 策略列表
     */
    public WechatMpExecuteHandlerFactory(WechatProperty property, List<WechatMpExecuteHandler> executorHandlers) {
        this.property = property;
        this.strategyMapSupplier = SingletonSupplier.of(() -> {

            final Map<String, WechatMpExecuteHandler> strategyMap = executorHandlers
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                    WechatMpExecuteHandler::getCode,
                    Function.identity(),
                    (existing, replacement) -> replacement,
                    LinkedHashMap::new
                ));

            log.info("Loaded {} strategies={}",
                WechatMpExecuteHandler.class.getSimpleName(),
                strategyMap.keySet()
            );

            return strategyMap;
        });
    }

    /**
     * 根据配置编码获取公众号策略.
     *
     * @param code 配置编码
     * @return 公众号策略
     */
    public WechatMpExecuteHandler getHandler(String code) {
        WechatMpExecuteHandler handler = getStrategyMap().get(code);
        if (handler == null) {
            log.error("WechatMpExecuteHandler invalid code={}", code);
            throw new BizException(BizCodeEnum.INVALID_STRATEGY);
        }
        return handler;
    }

    /**
     * 获取默认公众号策略（优先级最高的可用策略）.
     *
     * @return 默认公众号策略
     */
    public WechatMpExecuteHandler getDefaultHandler() {
        return getStrategyMap().values().stream()
            .filter(WechatMpExecuteHandler::isHealthy)
            .min(Comparator.comparingInt(WechatMpExecuteHandler::getPriority))
            .orElse(null);
    }

    /**
     * 获取策略缓存.
     *
     * @return Map<String, WechatMpExecuteHandler>
     */
    private Map<String, WechatMpExecuteHandler> getStrategyMap() {
        return strategyMapSupplier.get();
    }

    @Override
    public void afterSingletonsInstantiated() {
        getStrategyMap();
    }
}

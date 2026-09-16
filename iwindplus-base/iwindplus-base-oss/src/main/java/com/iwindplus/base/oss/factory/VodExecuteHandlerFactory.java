/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.oss.factory;

import cn.hutool.core.collection.CollUtil;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.enums.VodTypeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.oss.domain.property.VodProperty;
import com.iwindplus.base.oss.support.VodExecuteHandler;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.util.function.SingletonSupplier;

/**
 * Vod策略工厂. 负责管理和路由Vod策略，支持多配置和动态路由.
 *
 * <p>策略结构：{@code Map<VodTypeEnum, Map<String, VodExecuteHandler>>}，
 * 第一维为提供商类型，第二维为配置编码，同一提供商可存在多个配置。</p>
 *
 * <p>注意：Vod 不支持自动故障转移，存储和查询必须使用同一配置，
 * 否则会导致存储和查询不匹配。</p>
 *
 * @author zengdegui
 * @since 2026/9/6
 */
@Slf4j
public class VodExecuteHandlerFactory implements SmartInitializingSingleton {

    private final VodProperty property;
    private final Supplier<Map<VodTypeEnum, Map<String, VodExecuteHandler>>> strategyMapSupplier;

    /**
     * 构造函数.
     *
     * @param property         属性配置
     * @param executorProvider 执行器提供者
     */
    public VodExecuteHandlerFactory(VodProperty property, ObjectProvider<VodExecuteHandler> executorProvider) {
        this.property = property;
        this.strategyMapSupplier = SingletonSupplier.of(() -> {

            final Map<VodTypeEnum, Map<String, VodExecuteHandler>>
                strategyMap = executorProvider
                .orderedStream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                    VodExecuteHandler::getProvider,
                    LinkedHashMap::new,
                    Collectors.toMap(
                        VodExecuteHandler::getCode,
                        Function.identity(),
                        (existing, replacement) -> replacement,
                        LinkedHashMap::new
                    )
                ));

            log.info("Loaded {} strategies={}",
                VodExecuteHandler.class.getSimpleName(),
                strategyMap.keySet()
            );

            return strategyMap;
        });
    }

    /**
     * 根据Vod类型获取Vod策略（该提供商下优先级最高的可用策略）.
     *
     * @param type Vod类型
     * @return Vod策略
     */
    public VodExecuteHandler getHandler(VodTypeEnum type) {
        Map<String, VodExecuteHandler> handlers = getStrategyMap().get(type);
        if (handlers == null || handlers.isEmpty()) {
            log.error("VodExecuteHandler invalid strategy={}", type);
            throw new BizException(BizCodeEnum.INVALID_STRATEGY);
        }
        return handlers.values().stream()
            .filter(VodExecuteHandler::isHealthy)
            .min(Comparator.comparingInt(VodExecuteHandler::getPriority))
            .orElseThrow(() -> {
                log.error("VodExecuteHandler no available strategy={}", type);
                return new BizException(BizCodeEnum.INVALID_STRATEGY);
            });
    }

    /**
     * 根据Vod类型和配置编码获取Vod策略.
     *
     * <p>存储和查询必须使用同一配置编码，否则会导致存储和查询不匹配。</p>
     *
     * @param type Vod类型
     * @param code 配置编码
     * @return Vod策略
     */
    public VodExecuteHandler getHandler(VodTypeEnum type, String code) {
        Map<String, VodExecuteHandler> handlers = getStrategyMap().get(type);
        if (handlers == null) {
            log.error("VodExecuteHandler invalid strategy={}", type);
            throw new BizException(BizCodeEnum.INVALID_STRATEGY);
        }
        VodExecuteHandler handler = handlers.get(code);
        if (handler == null) {
            log.error("VodExecuteHandler invalid code={}", code);
            throw new BizException(BizCodeEnum.INVALID_STRATEGY);
        }
        return handler;
    }

    /**
     * 获取默认Vod策略.
     *
     * @return 默认Vod策略
     */
    public VodExecuteHandler getDefaultHandler() {
        String defaultCode = this.property.getDefaultCode();
        if (defaultCode == null || defaultCode.isBlank()) {
            log.warn("VodExecuteHandler default-code not configured");
            return null;
        }
        return getStrategyMap().values().stream()
            .map(handlers -> handlers.get(defaultCode))
            .filter(Objects::nonNull)
            .filter(VodExecuteHandler::isHealthy)
            .findFirst()
            .orElseGet(() -> {
                log.error("VodExecuteHandler default-code invalid code={}", defaultCode);
                return null;
            });
    }

    /**
     * 根据Vod类型和配置编码获取Vod配置.
     *
     * @param type Vod类型
     * @param code 配置编码
     * @return Vod配置
     */
    public VodProperty.BaseConfig getConfig(VodTypeEnum type, String code) {
        final List<? extends VodProperty.BaseConfig> configs = switch (type) {
            case ALIYUN -> this.property.getAliyun();
            default -> null;
        };

        if (CollUtil.isEmpty(configs)) {
            return null;
        }
        return configs.stream()
            .filter(config -> code.equals(config.getCode()))
            .findFirst()
            .orElse(null);
    }

    /**
     * 获取策略缓存.
     *
     * @return Map<VodTypeEnum, Map < String, VodExecuteHandler>>
     */
    private Map<VodTypeEnum, Map<String, VodExecuteHandler>> getStrategyMap() {
        return strategyMapSupplier.get();
    }

    @Override
    public void afterSingletonsInstantiated() {
        getStrategyMap();
    }
}

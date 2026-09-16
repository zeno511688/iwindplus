/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.oss.factory;

import cn.hutool.core.collection.CollUtil;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.enums.OssTypeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.oss.domain.property.OssProperty;
import com.iwindplus.base.oss.support.OssExecuteHandler;
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
 * OSS策略工厂. 负责管理和路由OSS策略，支持多配置和动态路由.
 *
 * <p>策略结构：{@code Map<OssTypeEnum, Map<String, OssExecuteHandler>>}，
 * 第一维为提供商类型，第二维为配置编码，同一提供商可存在多个配置。</p>
 *
 * <p>注意：OSS 不支持自动故障转移，存储和查询必须使用同一配置，
 * 否则会导致存储和查询不匹配。</p>
 *
 * @author zengdegui
 * @since 2026/9/6
 */
@Slf4j
public class OssExecuteHandlerFactory implements SmartInitializingSingleton {

    private final OssProperty property;
    private final Supplier<Map<OssTypeEnum, Map<String, OssExecuteHandler>>> strategyMapSupplier;

    /**
     * 构造函数.
     *
     * @param property         属性配置
     * @param executorHandlers 执行器列表
     */
    public OssExecuteHandlerFactory(OssProperty property, List<OssExecuteHandler> executorHandlers) {
        this.property = property;
        this.strategyMapSupplier = SingletonSupplier.of(() -> {

            final Map<OssTypeEnum, Map<String, OssExecuteHandler>>
                strategyMap = executorHandlers
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                    OssExecuteHandler::getProvider,
                    LinkedHashMap::new,
                    Collectors.toMap(
                        OssExecuteHandler::getCode,
                        Function.identity(),
                        (existing, replacement) -> replacement,
                        LinkedHashMap::new
                    )
                ));

            log.info("Loaded {} strategies={}",
                OssExecuteHandler.class.getSimpleName(),
                strategyMap.keySet()
            );

            return strategyMap;
        });
    }

    /**
     * 根据OSS类型获取OSS策略（该提供商下优先级最高的可用策略）.
     *
     * @param type OSS类型
     * @return OSS策略
     */
    public OssExecuteHandler getHandler(OssTypeEnum type) {
        Map<String, OssExecuteHandler> handlers = getStrategyMap().get(type);
        if (handlers == null || handlers.isEmpty()) {
            log.error("OssExecuteHandler invalid strategy={}", type);
            throw new BizException(BizCodeEnum.INVALID_STRATEGY);
        }
        return handlers.values().stream()
            .filter(OssExecuteHandler::isHealthy)
            .min(Comparator.comparingInt(OssExecuteHandler::getPriority))
            .orElseThrow(() -> {
                log.error("OssExecuteHandler no available strategy={}", type);
                return new BizException(BizCodeEnum.INVALID_STRATEGY);
            });
    }

    /**
     * 根据OSS类型和配置编码获取OSS策略.
     *
     * <p>存储和查询必须使用同一配置编码，否则会导致存储和查询不匹配。</p>
     *
     * @param type OSS类型
     * @param code 配置编码
     * @return OSS策略
     */
    public OssExecuteHandler getHandler(OssTypeEnum type, String code) {
        Map<String, OssExecuteHandler> handlers = getStrategyMap().get(type);
        if (handlers == null) {
            log.error("OssExecuteHandler invalid strategy={}", type);
            throw new BizException(BizCodeEnum.INVALID_STRATEGY);
        }
        OssExecuteHandler handler = handlers.get(code);
        if (handler == null) {
            log.error("OssExecuteHandler invalid code={}", code);
            throw new BizException(BizCodeEnum.INVALID_STRATEGY);
        }
        return handler;
    }

    /**
     * 获取默认OSS策略.
     *
     * @return 默认OSS策略
     */
    public OssExecuteHandler getDefaultHandler() {
        String defaultCode = this.property.getDefaultCode();
        if (defaultCode == null || defaultCode.isBlank()) {
            log.warn("OssExecuteHandler default-code not configured");
            return null;
        }
        return getStrategyMap().values().stream()
            .map(handlers -> handlers.get(defaultCode))
            .filter(Objects::nonNull)
            .filter(OssExecuteHandler::isHealthy)
            .findFirst()
            .orElseGet(() -> {
                log.error("OssExecuteHandler default-code invalid code={}", defaultCode);
                return null;
            });
    }

    /**
     * 根据OSS类型和配置编码获取OSS配置.
     *
     * @param type OSS类型
     * @param code 配置编码
     * @return OSS配置
     */
    public OssProperty.BaseConfig getConfig(OssTypeEnum type, String code) {
        final List<? extends OssProperty.BaseConfig> configs = switch (type) {
            case ALIYUN -> this.property.getAliyun();
            case QINIU -> this.property.getQiniu();
            case MINIO -> this.property.getMinio();
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
     * @return Map<OssTypeEnum, Map < String, OssExecuteHandler>>
     */
    private Map<OssTypeEnum, Map<String, OssExecuteHandler>> getStrategyMap() {
        return strategyMapSupplier.get();
    }

    @Override
    public void afterSingletonsInstantiated() {
        getStrategyMap();
    }
}

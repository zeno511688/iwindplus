/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.ocr.factory;

import cn.hutool.core.collection.CollUtil;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.ocr.domain.enums.OcrTypeEnum;
import com.iwindplus.base.ocr.domain.property.OcrProperty;
import com.iwindplus.base.ocr.support.OcrExecuteHandler;
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
 * OCR处理器策略工厂. 负责管理和路由OCR策略，支持多配置和动态路由.
 *
 * <p>策略结构：{@code Map<OcrTypeEnum, Map<String, OcrExecuteHandler>>}，
 * 第一维为OCR类型，第二维为配置编码，同一类型可存在多个配置。</p>
 *
 * @author zengdegui
 * @since 2026/9/1
 */
@Slf4j
public class OcrExecuteHandlerFactory implements SmartInitializingSingleton {

    private final OcrProperty property;
    private final Supplier<Map<OcrTypeEnum, Map<String, OcrExecuteHandler>>> strategyMapSupplier;

    /**
     * 构造函数.
     *
     * @param property        属性配置
     * @param handlerHandlers 处理器列表
     */
    public OcrExecuteHandlerFactory(
        OcrProperty property,
        List<OcrExecuteHandler> handlerHandlers) {

        this.property = property;

        this.strategyMapSupplier = SingletonSupplier.of(() -> {

            final Map<OcrTypeEnum, Map<String, OcrExecuteHandler>>
                strategyMap = handlerHandlers
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                    OcrExecuteHandler::getType,
                    LinkedHashMap::new,
                    Collectors.toMap(
                        OcrExecuteHandler::getCode,
                        Function.identity(),
                        (existing, replacement) -> replacement,
                        LinkedHashMap::new
                    )
                ));

            log.info("Loaded {} strategies={}",
                OcrExecuteHandler.class.getSimpleName(),
                strategyMap.keySet()
            );

            return strategyMap;
        });
    }

    /**
     * 根据OCR类型获取处理器（该类型下优先级最高的可用策略）.
     *
     * @param type OCR类型
     * @return OCR处理器
     */
    public OcrExecuteHandler getHandler(OcrTypeEnum type) {
        Map<String, OcrExecuteHandler> handlers = getStrategyMap().get(type);
        if (handlers == null || handlers.isEmpty()) {
            log.error("OcrExecuteHandler invalid strategy={}", type);
            throw new BizException(BizCodeEnum.INVALID_STRATEGY);
        }
        return handlers.values().stream()
            .filter(OcrExecuteHandler::isHealthy)
            .min(Comparator.comparingInt(OcrExecuteHandler::getPriority))
            .orElseThrow(() -> {
                log.error("OcrExecuteHandler no available strategy={}", type);
                return new BizException(BizCodeEnum.INVALID_STRATEGY);
            });
    }

    /**
     * 根据OCR类型和配置编码获取处理器.
     *
     * @param type OCR类型
     * @param code 配置编码
     * @return OCR处理器
     */
    public OcrExecuteHandler getHandler(OcrTypeEnum type, String code) {
        Map<String, OcrExecuteHandler> handlers = getStrategyMap().get(type);
        if (handlers == null) {
            log.error("OcrExecuteHandler invalid strategy={}", type);
            throw new BizException(BizCodeEnum.INVALID_STRATEGY);
        }
        OcrExecuteHandler handler = handlers.get(code);
        if (handler == null) {
            log.error("OcrExecuteHandler invalid code={}", code);
            throw new BizException(BizCodeEnum.INVALID_STRATEGY);
        }
        return handler;
    }

    /**
     * 获取默认OCR处理器.
     *
     * @return 默认OCR处理器
     */
    public OcrExecuteHandler getDefaultHandler() {
        String defaultCode = this.property.getDefaultCode();
        if (defaultCode == null || defaultCode.isBlank()) {
            log.warn("OcrExecuteHandler default-code not configured");
            return null;
        }
        return getStrategyMap().values().stream()
            .map(handlers -> handlers.get(defaultCode))
            .filter(Objects::nonNull)
            .findFirst()
            .orElseGet(() -> {
                log.error("OcrExecuteHandler default-code invalid code={}", defaultCode);
                return null;
            });
    }

    /**
     * 根据OCR类型和配置编码获取OCR配置.
     *
     * @param type OCR类型
     * @param code 配置编码
     * @return OCR配置
     */
    public OcrProperty.BaseConfig getConfig(OcrTypeEnum type, String code) {
        final List<? extends OcrProperty.BaseConfig> configs = switch (type) {
            case PRINT_WORD -> this.property.getPrintWord();
            case XIANGYUN -> this.property.getXiangyun();
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
     * @return Map<OcrTypeEnum, Map < String, OcrExecuteHandler>>
     */
    private Map<OcrTypeEnum, Map<String, OcrExecuteHandler>> getStrategyMap() {
        return strategyMapSupplier.get();
    }

    @Override
    public void afterSingletonsInstantiated() {
        getStrategyMap();
    }
}

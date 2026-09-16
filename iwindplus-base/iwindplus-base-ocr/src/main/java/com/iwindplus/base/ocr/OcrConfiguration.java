/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.ocr;

import com.iwindplus.base.ocr.domain.enums.OcrTypeEnum;
import com.iwindplus.base.ocr.domain.property.OcrProperty;
import com.iwindplus.base.ocr.domain.property.OcrProperty.BaseConfig;
import com.iwindplus.base.ocr.factory.OcrExecuteHandlerFactory;
import com.iwindplus.base.ocr.support.OcrExecuteHandler;
import com.iwindplus.base.ocr.support.impl.PrintWordOcrExecuteHandler;
import com.iwindplus.base.ocr.support.impl.XiangyunOcrExecuteHandler;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OCR配置.
 *
 * <p>printWord/xiangyun 各自独立支持多配置，每个配置对应一个运行时策略实例，通过配置编码区分。</p>
 *
 * @author zengdegui
 * @since 2019/8/13
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(OcrProperty.class)
@ConditionalOnProperty(prefix = "ocr", name = "enabled", havingValue = "true", matchIfMissing = true)
public class OcrConfiguration {

    /**
     * 创建 OCR 策略工厂.
     *
     * @param multipartProperties 文件上传配置
     * @param property            属性配置
     * @return OCR策略工厂
     */
    @Bean
    public OcrExecuteHandlerFactory ocrExecuteHandlerFactory(
        MultipartProperties multipartProperties,
        OcrProperty property) {
        List<OcrExecuteHandler> handlers = new ArrayList<>(10);
        handlers.addAll(
            this.buildHandlers(property.getPrintWord(), OcrTypeEnum.PRINT_WORD,
                config -> new PrintWordOcrExecuteHandler(multipartProperties, config))
        );
        handlers.addAll(
            this.buildHandlers(property.getXiangyun(), OcrTypeEnum.XIANGYUN,
                config -> new XiangyunOcrExecuteHandler(multipartProperties, config))
        );
        final OcrExecuteHandlerFactory factory = new OcrExecuteHandlerFactory(property, handlers);
        log.info("OcrExecuteHandlerFactory={}", factory);
        return factory;
    }

    /**
     * 根据配置列表构建策略实例列表（过滤未启用配置并按编码去重）.
     *
     * @param configs        配置列表
     * @param provider       服务商
     * @param handlerFactory 策略实例工厂
     * @param <C>            配置类型
     * @return 策略实例列表
     */
    private <C extends BaseConfig> List<OcrExecuteHandler> buildHandlers(
        List<C> configs,
        OcrTypeEnum provider,
        Function<C, OcrExecuteHandler> handlerFactory) {
        List<OcrExecuteHandler> handlers = new ArrayList<>(10);
        Set<String> codes = new HashSet<>(16);
        configs.stream()
            .filter(config -> Boolean.TRUE.equals(config.getEnabled()))
            .filter(config -> codes.add(config.getCode()))
            .forEach(config -> {
                log.info("Initializing {} OCR strategy [code={}]", provider, config.getCode());
                handlers.add(handlerFactory.apply(config));
            });
        return handlers;
    }
}

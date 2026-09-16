/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.mail;

import com.iwindplus.base.mail.domain.property.MailProperty;
import com.iwindplus.base.mail.domain.property.MailProperty.BaseConfig;
import com.iwindplus.base.mail.factory.MailExecuteHandlerFactory;
import com.iwindplus.base.mail.support.MailExecuteHandler;
import com.iwindplus.base.mail.support.impl.SpringMailExecuteHandler;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 邮件操作配置.
 *
 * @author zengdegui
 * @since 2020/12/6
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "mail", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(MailProperty.class)
public class MailConfiguration {

    /**
     * 邮件策略工厂.
     *
     * @param property 属性配置
     * @return 策略工厂
     */
    @Bean
    public MailExecuteHandlerFactory mailHandlerFactory(MailProperty property) {
        List<MailExecuteHandler> handlers = new ArrayList<>(10);
        handlers.addAll(
            this.buildHandlers(property.getConfigs(), SpringMailExecuteHandler::new)
        );
        MailExecuteHandlerFactory factory = new MailExecuteHandlerFactory(property, handlers);
        log.info("MailExecuteHandlerFactory={}", factory);
        return factory;
    }

    /**
     * 根据配置列表构建策略实例列表（过滤未启用配置并按编码去重）.
     *
     * @param configs        配置列表
     * @param handlerFactory 策略实例工厂
     * @param <C>            配置类型
     * @return 策略实例列表
     */
    private <C extends BaseConfig> List<MailExecuteHandler> buildHandlers(
        List<C> configs,
        Function<C, MailExecuteHandler> handlerFactory) {
        List<MailExecuteHandler> handlers = new ArrayList<>(10);
        Set<String> codes = new HashSet<>(16);
        configs.stream()
            .filter(config -> Boolean.TRUE.equals(config.getEnabled()))
            .filter(config -> codes.add(config.getCode()))
            .forEach(config -> {
                log.info("Initializing MAIL strategy [code={}]", config.getCode());
                handlers.add(handlerFactory.apply(config));
            });
        return handlers;
    }
}

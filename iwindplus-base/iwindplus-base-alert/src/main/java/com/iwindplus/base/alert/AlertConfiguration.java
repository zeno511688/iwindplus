/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.alert;

import com.iwindplus.base.alert.domain.property.AlertProperty;
import com.iwindplus.base.alert.factory.AlertExecuteHandlerFactory;
import com.iwindplus.base.alert.support.AlertExecuteHandler;
import com.iwindplus.base.alert.support.impl.FeishuAlertExecuteHandler;
import com.iwindplus.base.http.client.factory.HttpClientExecuteHandlerFactory;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 告警配置（对接第三方消息系统）.
 *
 * @author zengdegui
 * @since 2025/11/23 21:15
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(AlertProperty.class)
@ConditionalOnProperty(prefix = "alert", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AlertConfiguration {

    /**
     * 创建 AlertExecuteHandlerFactory.
     *
     * @param property                        属性配置
     * @param httpClientExecuteHandlerFactory HTTP客户端执行器工厂
     * @return AlertExecuteHandlerFactory
     */
    @Bean
    public AlertExecuteHandlerFactory alertExecuteHandlerFactory(
        AlertProperty property,
        HttpClientExecuteHandlerFactory httpClientExecuteHandlerFactory) {
        final List<AlertExecuteHandler> executorHandlers =
            this.feishuAlertExecuteHandlers(property, httpClientExecuteHandlerFactory);
        final AlertExecuteHandlerFactory factory = new AlertExecuteHandlerFactory(property, executorHandlers);
        log.info("AlertExecuteHandlerFactory={}", factory);
        return factory;
    }

    /**
     * 创建飞书告警执行器（支持多配置）.
     *
     * @param property                        属性配置
     * @param httpClientExecuteHandlerFactory HTTP客户端执行器工厂
     * @return 飞书告警执行器列表
     */
    private List<AlertExecuteHandler> feishuAlertExecuteHandlers(
        AlertProperty property,
        HttpClientExecuteHandlerFactory httpClientExecuteHandlerFactory) {
        List<AlertExecuteHandler> handlers = new ArrayList<>(10);
        Set<String> codes = new HashSet<>(16);
        property.getFeishu().stream()
            .filter(config -> Boolean.TRUE.equals(config.getEnabled()))
            .filter(config -> codes.add(config.getCode()))
            .forEach(config -> {
                log.info("Initializing Alert feishu strategy [code={}]", config.getCode());
                handlers.add(new FeishuAlertExecuteHandler(config, httpClientExecuteHandlerFactory));
            });
        return handlers;
    }
}

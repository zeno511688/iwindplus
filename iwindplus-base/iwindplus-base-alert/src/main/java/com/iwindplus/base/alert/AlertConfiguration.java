/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.alert;

import com.iwindplus.base.alert.domain.property.AlertProperty;
import com.iwindplus.base.alert.executor.AlertExecutor;
import com.iwindplus.base.alert.executor.impl.FeishuAlertExecutor;
import com.iwindplus.base.alert.factory.AlertExecutorStrategyFactory;
import com.iwindplus.base.http.client.factory.HttpClientExecutorStrategyFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
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

    @Resource
    private AlertProperty property;

    @Resource
    private HttpClientExecutorStrategyFactory httpClientExecutorStrategyFactory;

    /**
     * 创建 AlertExecutorStrategyFactory.
     *
     * @param executorProvider 执行器提供者
     * @return AlertExecutorStrategyFactory
     */
    @Bean
    public AlertExecutorStrategyFactory alertExecutorStrategyFactory(ObjectProvider<AlertExecutor> executorProvider) {
        final AlertExecutorStrategyFactory alertExecutorStrategyFactory = new AlertExecutorStrategyFactory(property, executorProvider);
        log.info("AlertExecutorStrategyFactory={}", alertExecutorStrategyFactory);
        return alertExecutorStrategyFactory;
    }

    /**
     * 创建 FeishuAlertExecutor.
     *
     * @return FeishuAlertExecutor
     */
    @Bean
    @ConditionalOnProperty(prefix = "alert.feishu", name = "enabled", havingValue = "true", matchIfMissing = true)
    public AlertExecutor feishuAlertExecutor() {
        final FeishuAlertExecutor feishuAlertExecutor = new FeishuAlertExecutor(property, httpClientExecutorStrategyFactory);
        log.info("FeishuAlertExecutor={}", feishuAlertExecutor);
        return feishuAlertExecutor;
    }
}

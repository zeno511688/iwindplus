/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.log;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.iwindplus.base.alert.factory.AlertExecutorStrategyFactory;
import com.iwindplus.base.log.domain.property.AlertLogProperty;
import com.iwindplus.base.log.service.AlertLogAppender;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * 告警日志配置.
 *
 * @author zengdegui
 * @since 2025/11/23 21:15
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(AlertLogProperty.class)
@RequiredArgsConstructor
public class AlertLogConfiguration {

    /**
     * 告警日志启用属性
     */
    private static final String ALERT_LOG_ENABLED = "alert.log.enabled";

    /**
     * Appender 名称
     */
    private static final String APPENDER_NAME = "ALERT_LOG_APPENDER";

    private final AlertLogProperty property;
    private final AlertExecutorStrategyFactory alertExecutorStrategyFactory;
    private final Environment environment;

    private AlertLogAppender appender;

    /**
     * 注册 Appender
     */
    @PostConstruct
    public void registerAppender() {
        try {
            LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
            Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);

            // 创建并启动
            this.appender = new AlertLogAppender(
                property,
                alertExecutorStrategyFactory,
                environment
            );
            appender.setContext(context);
            appender.setName(APPENDER_NAME);
            appender.start();

            rootLogger.addAppender(appender);
            log.info("AlertLogAppender registered and started successfully");

        } catch (Exception e) {
            log.error("Failed to register AlertLogAppender", e);
        }
    }

    /**
     * 注销 Appender
     */
    @PreDestroy
    public void unregisterAppender() {
        if (appender != null) {
            try {
                LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
                Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);
                rootLogger.detachAppender(appender);
                appender.stop();
                log.info("AlertLogAppender unregistered");
            } catch (Exception e) {
                log.error("Error unregistering AlertLogAppender", e);
            }
        }
    }
}
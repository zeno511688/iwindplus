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
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

/**
 * 告警日志配置. 自动注册 AlertLogAppender 到 Logback，无需 XML 配置.
 * 支持动态刷新配置，无需重启服务即可启用/禁用告警日志.
 *
 * @author zengdegui
 * @since 2025/11/23 21:15
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(AlertLogProperty.class)
@RequiredArgsConstructor
@RefreshScope
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

            // 根据配置决定是否启动
            if (Boolean.TRUE.equals(property.getEnabled())) {
                appender.start();
                rootLogger.addAppender(appender);
                log.info("AlertLogAppender registered and started successfully");
            } else {
                log.info("AlertLogAppender created but not started (disabled)");
            }

        } catch (Exception e) {
            log.error("Failed to register AlertLogAppender", e);
        }
    }

    /**
     * 监听配置变更事件，动态启用/禁用告警日志.
     *
     * @param event 环境变更事件
     */
    @EventListener(EnvironmentChangeEvent.class)
    public void onEnvironmentChange(EnvironmentChangeEvent event) {
        Set<String> changedKeys = event.getKeys();
        if (changedKeys.contains(ALERT_LOG_ENABLED)) {
            Boolean newEnabled = property.getEnabled();
            log.info("Alert log enabled changed to: {}", newEnabled);

            if (appender != null) {
                LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
                Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);

                if (Boolean.TRUE.equals(newEnabled)) {
                    if (!appender.isStarted()) {
                        appender.start();
                        rootLogger.addAppender(appender);
                        log.info("AlertLogAppender started dynamically");
                    }
                } else {
                    if (appender.isStarted()) {
                        rootLogger.detachAppender(appender);
                        appender.stop();
                        log.info("AlertLogAppender stopped dynamically");
                    }
                }
            }
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
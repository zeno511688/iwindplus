/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.logging.alert;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.iwindplus.base.alert.factory.AlertExecuteHandlerFactory;
import com.iwindplus.base.logging.alert.domain.property.LoggingAlertProperty;
import com.iwindplus.base.logging.alert.support.LoggingAlertAppender;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * 日志告警配置.
 *
 * @author zengdegui
 * @since 2025/11/23 21:15
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(LoggingAlertProperty.class)
@RequiredArgsConstructor
public class LoggingAlertConfiguration {

    /**
     * Appender 名称
     */
    private static final String APPENDER_NAME = "loggingAlertAppender";

    private final LoggingAlertProperty property;
    private final AlertExecuteHandlerFactory alertExecuteHandlerFactory;
    private final Environment environment;

    private LoggingAlertAppender appender;

    /**
     * 注册 Appender
     */
    @PostConstruct
    public void registerAppender() {
        LoggerContext context = getLoggerContext();
        if (context == null) {
            return;
        }

        Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);
        detachExistingAppender(rootLogger);

        LoggingAlertAppender newAppender = null;
        try {
            newAppender = new LoggingAlertAppender(
                property,
                alertExecuteHandlerFactory,
                environment
            );
            newAppender.setContext(context);
            newAppender.setName(APPENDER_NAME);
            newAppender.start();
            rootLogger.addAppender(newAppender);
            this.appender = newAppender;
            log.info("LoggingAlertAppender registered and started successfully");
        } catch (Exception exception) {
            if (newAppender != null) {
                newAppender.stop();
            }
            log.error("Failed to register LoggingAlertAppender", exception);
        }
    }

    /**
     * 注销 Appender.
     */
    @PreDestroy
    public void unregisterAppender() {
        LoggingAlertAppender registeredAppender = this.appender;
        if (registeredAppender == null) {
            return;
        }

        try {
            LoggerContext context = getLoggerContext();
            if (context != null) {
                Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);
                rootLogger.detachAppender(registeredAppender);
            }
            registeredAppender.stop();
            this.appender = null;
            log.info("LoggingAlertAppender unregistered");
        } catch (Exception exception) {
            log.error("Error unregistering LoggingAlertAppender", exception);
        }
    }

    /**
     * 获取 Logback 日志上下文.
     *
     * @return Logback 上下文；当前日志实现不是 Logback 时返回 null
     */
    private LoggerContext getLoggerContext() {
        Object loggerFactory = LoggerFactory.getILoggerFactory();
        if (loggerFactory instanceof LoggerContext loggerContext) {
            return loggerContext;
        }

        log.warn("LoggingAlertAppender is disabled because the current logger factory is not Logback: {}",
            loggerFactory.getClass().getName());
        return null;
    }

    /**
     * 移除同名的已有 Appender，避免重复注册.
     *
     * @param rootLogger Logback 根日志记录器
     */
    private void detachExistingAppender(Logger rootLogger) {
        Appender<ILoggingEvent> existingAppender = rootLogger.getAppender(APPENDER_NAME);
        if (existingAppender != null) {
            rootLogger.detachAppender(existingAppender);
            existingAppender.stop();
            log.info("Existing LoggingAlertAppender detached before registration");
        }
    }
}
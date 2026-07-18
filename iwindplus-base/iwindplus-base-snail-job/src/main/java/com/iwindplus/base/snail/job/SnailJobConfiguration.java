/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.snail.job;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import com.aizuda.snailjob.client.common.appender.SnailLogbackAppender;
import com.aizuda.snailjob.client.common.event.SnailClientStartingEvent;
import com.aizuda.snailjob.client.starter.EnableSnailJob;
import com.iwindplus.base.snail.job.domain.property.SnailJobProperty;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

/**
 * snail-job配置.
 *
 * @author zengdegui
 * @since 2025/04/17 23:58
 */
@Slf4j
@AutoConfiguration
@EnableSnailJob
@EnableConfigurationProperties(SnailJobProperty.class)
@ConditionalOnProperty(prefix = "snail-job", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SnailJobConfiguration {

    @Resource
    private SnailJobProperty property;

    private static final List<Runnable> CLOSE_ACTIONS = new CopyOnWriteArrayList<>();

    /**
     * 优雅关闭钩子.
     *
     * @return Runnable
     */
    @Bean(destroyMethod = "run")
    public Runnable snailJobShutdownHook() {
        return () -> CLOSE_ACTIONS.forEach(Runnable::run);
    }

    /**
     * Logback配置类
     */
    @Configuration
    @ConditionalOnClass({LoggerContext.class, SnailLogbackAppender.class})
    @ConditionalOnProperty(prefix = "snail-job.logback", name = "enabled", matchIfMissing = true)
    public class LogbackConfiguration {

        private static final String APPENDER_NAME = "SNAIL_LOG_APPENDER";

        @Async
        @EventListener(SnailClientStartingEvent.class)
        public void onStarting(@SuppressWarnings("unused") SnailClientStartingEvent event) {
            LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

            // 重复注册保护
            if (context.exists(APPENDER_NAME) != null) {
                return;
            }

            SnailLogbackAppender<ILoggingEvent> appender = new SnailLogbackAppender<>();
            appender.setName(APPENDER_NAME);
            appender.setContext(context);

            Level level = Level.toLevel(property.getLogback().getLevel(), Level.ERROR);
            appender.addFilter(new ThresholdFilter(level));
            appender.start();

            Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);
            rootLogger.addAppender(appender);

            log.info("SnailJob Logback appender started, level={}", level);

            // 注册优雅关闭钩子
            CLOSE_ACTIONS.add(() -> {
                rootLogger.detachAppender(appender);
                appender.stop();
            });
        }

        /**
         * 级别过滤器.
         */
        private static class ThresholdFilter extends Filter<ILoggingEvent> {

            private final Level level;

            ThresholdFilter(Level level) {
                this.level = level;
            }

            @Override
            public FilterReply decide(ILoggingEvent event) {
                return event.getLevel().isGreaterOrEqual(level)
                    ? FilterReply.ACCEPT
                    : FilterReply.DENY;
            }
        }
    }

}

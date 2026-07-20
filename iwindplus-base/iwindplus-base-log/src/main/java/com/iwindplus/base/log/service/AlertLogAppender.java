/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.log.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.AppenderBase;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import com.iwindplus.base.alert.domain.dto.AlertWebhookRequestDTO;
import com.iwindplus.base.alert.factory.AlertExecutorStrategyFactory;
import com.iwindplus.base.domain.constant.CommonConstant.HeaderConstant;
import com.iwindplus.base.domain.constant.CommonConstant.SymbolConstant;
import com.iwindplus.base.log.domain.property.AlertLogProperty;
import com.iwindplus.base.log.domain.property.AlertLogProperty.WebhookCfg;
import com.iwindplus.base.log.ratelimit.PreciseRateLimitManager;
import com.iwindplus.base.util.HttpsUtil;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.MDC;
import org.springframework.core.env.Environment;

/**
 * 告警日志追加器.
 *
 * @author zengdegui
 * @since 2025/11/23 21:15
 */
public class AlertLogAppender extends AppenderBase<ILoggingEvent> {

    private static final DateTimeFormatter DATE_FORMATTER =
        DateTimeFormatter.ofPattern(DatePattern.NORM_DATETIME_PATTERN)
            .withZone(ZoneId.systemDefault());

    private final PreciseRateLimitManager rateLimitManager;
    private final AlertLogProperty property;
    private final AlertExecutorStrategyFactory alertExecutorStrategyFactory;
    private final String cachedProfile;
    private final String cachedAppName;
    private final List<Pattern> compiledExcludePatterns;

    public AlertLogAppender(AlertLogProperty property,
        AlertExecutorStrategyFactory alertExecutorStrategyFactory,
        Environment environment) {
        this.property = property;
        this.alertExecutorStrategyFactory = alertExecutorStrategyFactory;

        String[] profiles = environment.getActiveProfiles();
        this.cachedProfile = profiles.length > 0 ? String.join(",", profiles) : "default";
        this.cachedAppName = environment.getProperty("spring.application.name", "unknown");

        this.rateLimitManager = new PreciseRateLimitManager(
            property.getRateLimit().getBucketCount(),
            property.getRateLimit().getWindowSeconds(),
            property.getRateLimit().getSilenceSeconds(),
            property.getRateLimit().getMaxRequests(),
            property.getRateLimit().getCacheSize()
        );

        final List<String> excludePatterns = property.getExcludePatterns();
        this.compiledExcludePatterns = CollUtil.isEmpty(excludePatterns)
            ? Collections.emptyList() : excludePatterns.stream()
            .map(Pattern::compile)
            .collect(Collectors.toList());

    }

    @Override
    protected void append(ILoggingEvent event) {
        if (!isStarted()) {
            return;
        }

        if (!event.getLevel().equals(Level.ERROR)) {
            return;
        }

        // 采样检查（避免后续计算）
        if (!HttpsUtil.checkSampleRateInRange(property.getSampleRate())) {
            return;
        }

        // 过滤检查
        if (!matchesFilter(event.getFormattedMessage())) {
            return;
        }

        String key = buildRateLimitKey(event);
        if (rateLimitManager.isRateLimited(key)) {
            return;
        }

        sendAlert(event);
    }

    private String buildRateLimitKey(ILoggingEvent event) {
        return event.getLoggerName() + "|" + event.getFormattedMessage();
    }

    private boolean matchesFilter(String message) {
        if (CollUtil.isEmpty(compiledExcludePatterns)) {
            return true;
        }
        for (Pattern p : compiledExcludePatterns) {
            if (p.matcher(message).find()) {
                return false;
            }
        }
        return true;
    }

    private void sendAlert(ILoggingEvent event) {
        WebhookCfg webhook = property.getWebhook();
        if (webhook == null) {
            return;
        }

        final String content = buildAlertMessage(event);

        try {
            final AlertWebhookRequestDTO entity = AlertWebhookRequestDTO
                .builder()
                .webhookUrl(webhook.getUrl())
                .secret(webhook.getSecret())
                .content(content)
                .build();
            this.alertExecutorStrategyFactory
                .getAlertExecutor(webhook.getChannelType())
                .sendWebhookMsg(entity);
        } catch (Exception e) {
            addError("Failed to send alert", e);
        }
    }

    private String buildAlertMessage(ILoggingEvent event) {
        StringBuilder sb = new StringBuilder();
        sb.append("🚨 System Alert\n")
            .append("Env：").append(cachedProfile).append('\n')
            .append("AppName：").append(cachedAppName).append('\n')
            .append("Owner：").append(
                CollUtil.isNotEmpty(property.getOwners())
                    ? property.getOwners().stream().collect(Collectors.joining(SymbolConstant.COMMA))
                    : "N/A"
            ).append('\n')
            .append("Time：").append(DATE_FORMATTER.format(event.getInstant())).append('\n')
            .append("Level: ").append(event.getLevel()).append("\n")
            .append("TraceId: ").append(MDC.get(HeaderConstant.TRACE_ID)).append("\n")
            .append("Logger: ").append(event.getLoggerName()).append("\n")
            .append("Thread: ").append(event.getThreadName()).append("\n")
            .append("Message: ").append(event.getFormattedMessage()).append("\n");

        String stack = extractStackTrace(event);
        if (stack != null) {
            sb.append("Exception: \n")
                .append(stack).append("\n```");
        }

        return sb.toString();
    }

    private String extractStackTrace(ILoggingEvent event) {
        IThrowableProxy proxy = event.getThrowableProxy();
        if (proxy == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder(1024);
        appendThrowableProxy(sb, proxy, "");

        String result = sb.toString();
        Integer maxStackLength = property.getStack().getMaxLength();
        // 修改：maxStackLength > 0 时才截取，<= 0（包括0和-1）表示不截取
        if (maxStackLength > 0 && result.length() > maxStackLength) {
            result = result.substring(0, maxStackLength) +
                "\n... (stack truncated, original length: " + result.length() + " chars, limit: " + maxStackLength + ")";
        }
        return result;
    }

    private void appendThrowableProxy(StringBuilder sb, IThrowableProxy proxy, String indent) {
        if (proxy == null) {
            return;
        }

        sb.append(indent).append(proxy.getClassName());
        if (proxy.getMessage() != null) {
            sb.append(": ").append(proxy.getMessage());
        }
        sb.append('\n');

        StackTraceElementProxy[] elements = proxy.getStackTraceElementProxyArray();
        int common = proxy.getCommonFrames();

        Integer maxStackFrames = property.getStack().getMaxFrames();
        // maxStackFrames > 0 时才限制，<= 0（包括0和-1）表示不限制
        int display;
        if (maxStackFrames > 0) {
            display = Math.min(elements.length - common, maxStackFrames);
        } else {
            display = elements.length - common;
        }

        for (int i = 0; i < display; i++) {
            sb.append(indent).append("    at ").append(elements[i].getSTEAsString()).append('\n');
        }

        // 只有限制生效且还有更多行时，才显示 "... more"
        if (maxStackFrames > 0 && elements.length - common > display) {
            sb.append(indent).append("    ... ")
                .append(elements.length - common - display).append(" more\n");
        }

        IThrowableProxy[] suppressed = proxy.getSuppressed();
        if (suppressed != null) {
            for (IThrowableProxy s : suppressed) {
                sb.append(indent).append("Suppressed: ");
                appendThrowableProxy(sb, s, indent + "    ");
            }
        }

        IThrowableProxy cause = proxy.getCause();
        if (cause != null) {
            sb.append(indent).append("Caused by: ");
            appendThrowableProxy(sb, cause, indent);
        }
    }
}
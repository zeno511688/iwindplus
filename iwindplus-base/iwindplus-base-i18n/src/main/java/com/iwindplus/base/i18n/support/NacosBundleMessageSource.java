/*
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.i18n.support;

import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.iwindplus.base.domain.constant.CommonConstant.SymbolConstant;
import com.iwindplus.base.i18n.domain.constant.I18nConstant;
import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.support.AbstractResourceBasedMessageSource;

/**
 * nacos国际化消息源.
 *
 * @author zengdegui
 * @since 2019/9/3
 */
@Slf4j
@EqualsAndHashCode(callSuper = true)
public class NacosBundleMessageSource extends AbstractResourceBasedMessageSource implements DisposableBean {

    private static final String DEFAULT_GROUP = I18nConstant.I18N_GROUP;
    private static final String DEFAULT_FILE_SUFFIX = I18nConstant.FILE_SUFFIX;
    private static final int DEFAULT_NACOS_TIMEOUT_MS = 3000;
    private static final Duration DEFAULT_CACHE_DURATION = Duration.ofMinutes(10);
    private static final int DEFAULT_MAX_CACHE_SIZE = 100;

    private final DtpExecutor i18nTaskExecutor;
    private final ConfigService configService;
    private final String group;
    private final Duration cacheDuration;
    private final int maxCacheSize;

    private final Cache<String, Properties> fileCache;
    private final Map<String, Listener> listenerMap = new ConcurrentHashMap<>(16);

    @Builder
    public NacosBundleMessageSource(ConfigService configService,
        DtpExecutor i18nTaskExecutor, String group, Duration cacheDuration, Integer maxCacheSize) {
        this.configService = Objects.requireNonNull(configService, "ConfigService cannot be null");
        this.group = CharSequenceUtil.blankToDefault(group, DEFAULT_GROUP);
        this.cacheDuration = getCacheDuration(cacheDuration);
        this.maxCacheSize = Optional.ofNullable(maxCacheSize).orElse(DEFAULT_MAX_CACHE_SIZE);
        this.i18nTaskExecutor = i18nTaskExecutor;
        this.fileCache = getFileCache(this.cacheDuration);

        log.info("NacosBundleMessageSource initialized: cacheDuration={}, maxCacheSize={}", this.cacheDuration, this.maxCacheSize);
    }

    private Cache<String, Properties> getFileCache(Duration cacheDuration) {
        final Caffeine<String, Properties> fileCacheBuilder = Caffeine.newBuilder()
            .maximumSize(this.maxCacheSize)
            .recordStats()
            .removalListener(this::onCacheEviction);
        if (!cacheDuration.isZero()) {
            fileCacheBuilder.expireAfterWrite(this.cacheDuration);
        }
        return fileCacheBuilder.build();
    }

    private Duration getCacheDuration(Duration cacheDuration) {
        return Optional.ofNullable(cacheDuration)
            .map(duration -> {
                if (duration.isNegative()) {
                    log.warn("Negative cache duration {} treated as never expire", duration);
                    return Duration.ZERO;
                }
                return duration;
            })
            .orElse(DEFAULT_CACHE_DURATION);
    }

    @Override
    protected String resolveCodeWithoutArguments(String code, Locale locale) {
        if (CharSequenceUtil.isBlank(code)) {
            return null;
        }
        return findMessage(code, locale);
    }

    @Override
    protected MessageFormat resolveCode(String code, Locale locale) {
        String msg = resolveCodeWithoutArguments(code, locale);
        return CharSequenceUtil.isNotBlank(msg) ? new MessageFormat(msg, locale) : null;
    }

    @Override
    public void destroy() {
        log.info("Destroying NacosBundleMessageSource, removing {} listeners", listenerMap.size());

        listenerMap.forEach((dataId, listener) -> {
            try {
                configService.removeListener(dataId, group, listener);
            } catch (Exception e) {
                log.warn("Failed to remove Naocs listener: dataId={}", dataId, e);
            }
        });
        listenerMap.clear();

        Optional.ofNullable(fileCache).ifPresent(Cache::invalidateAll);

        log.info("NacosBundleMessageSource destroyed");
    }

    private String findMessage(String code, Locale locale) {
        if (CharSequenceUtil.isBlank(code) || locale == null) {
            log.warn("Invalid parameters: code={}, locale={}", code, locale);
            return null;
        }

        return getBasenameSet().stream()
            .flatMap(basename -> calculateAllFilenames(basename, locale).stream())
            .distinct()
            .map(this::loadPropertiesFromNacos)
            .filter(props -> props != null && props.containsKey(code))
            .findFirst()
            .map(props -> {
                String message = props.getProperty(code);
                log.trace("Found message '{}' in locale {}", code, locale);
                return message;
            })
            .orElseGet(() -> {
                log.trace("No message found for code '{}' and locale {}", code, locale);
                return null;
            });
    }

    private Properties loadPropertiesFromNacos(String filename) {
        String dataId = filename + DEFAULT_FILE_SUFFIX;

        return fileCache.get(dataId, k -> {
            log.trace("Loading from Nacos: dataId={}, group={}", dataId, group);

            return loadProperties(dataId);
        });
    }

    private Properties loadProperties(String dataId) {
        try {
            String configInfo = configService.getConfig(dataId, group, DEFAULT_NACOS_TIMEOUT_MS);
            if (CharSequenceUtil.isBlank(configInfo)) {
                log.trace("Config not found: dataId={}", dataId);
                return new Properties();
            }

            // 监听
            registerListenerIfNeeded(dataId);

            Properties props = parseConfigInfo(configInfo);
            log.trace("Loaded {} properties from Nacos={}", props.size(), dataId);
            return props;
        } catch (NacosException e) {
            if (e.getErrCode() == NacosException.RESOURCE_NOT_FOUND) {
                log.trace("Config not found in Nacos: dataId={}", dataId);
                return new Properties();
            }
            log.error("Nacos error loading dataId={}, code={}", dataId, e.getErrCode(), e);
            return new Properties();
        } catch (IOException e) {
            log.error("Failed to parse properties: dataId={}, {}", dataId, e.getMessage());
            return new Properties();
        }
    }

    private void registerListenerIfNeeded(String dataId) {
        if (listenerMap.containsKey(dataId)) {
            return;
        }

        try {
            Listener listener = new NacosI18nListener(dataId, this);
            configService.addListener(dataId, group, listener);
            listenerMap.put(dataId, listener);
            log.info("Registered Nacos listener: dataId={}, group={}", dataId, group);
        } catch (NacosException e) {
            log.error("Failed to register listener: dataId={}, {}", dataId, e.getMessage());
        }
    }

    private String extractPureBaseName(String originalBaseName) {
        if (CharSequenceUtil.isBlank(originalBaseName)) {
            return CharSequenceUtil.EMPTY;
        }

        String baseName = originalBaseName.trim();

        int colonIndex = baseName.indexOf(SymbolConstant.COLON);
        if (colonIndex > 0) {
            baseName = baseName.substring(colonIndex + 1);
        }

        // 移除路径，只保留文件名
        int lastSlash = Math.max(
            baseName.lastIndexOf(SymbolConstant.SLASH),
            baseName.lastIndexOf(SymbolConstant.BACK_SLASH)
        );
        if (lastSlash >= 0) {
            baseName = baseName.substring(lastSlash + 1);
        }

        return baseName;
    }

    private List<String> calculateAllFilenames(String basename, Locale locale) {
        String pureBaseName = extractPureBaseName(basename);
        if (CharSequenceUtil.isBlank(pureBaseName)) {
            return Collections.emptyList();
        }

        List<String> variants = new ArrayList<>(4);

        // 精确匹配优先（从最具体到最通用）
        StringBuilder buffer = new StringBuilder(pureBaseName);
        variants.add(buffer.toString());

        if (CharSequenceUtil.isNotBlank(locale.getLanguage())) {
            buffer.append(SymbolConstant.UNDERLINE).append(locale.getLanguage());
            variants.add(buffer.toString());

            if (CharSequenceUtil.isNotBlank(locale.getCountry())) {
                buffer.append(SymbolConstant.UNDERLINE).append(locale.getCountry());
                variants.add(buffer.toString());

                if (CharSequenceUtil.isNotBlank(locale.getVariant())) {
                    buffer.append(SymbolConstant.UNDERLINE).append(locale.getVariant());
                    variants.add(buffer.toString());
                }
            }
        }

        // 反转：最高优先级在前
        Collections.reverse(variants);
        return variants;
    }

    private void onCacheEviction(String key, Properties value, RemovalCause cause) {
        if (cause.wasEvicted()) {
            log.debug("Cache evicted: key={}, cause={}", key, cause);
        }
    }

    @Slf4j
    private record NacosI18nListener(String dataId, NacosBundleMessageSource source) implements Listener {

        @Override
        public Executor getExecutor() {
            return source.i18nTaskExecutor;
        }

        @Override
        public void receiveConfigInfo(String configInfo) {
            if (CharSequenceUtil.isBlank(configInfo)) {
                log.warn("Received empty config, skip refresh={}", dataId);
                return;
            }

            try {
                Properties props = parseConfigInfo(configInfo);

                // 精确刷新单个文件缓存
                source.fileCache.put(dataId, props);

                log.info("Refreshed cache for file={}", dataId);
            } catch (IOException e) {
                log.error("Failed to parse config={}", dataId, e);
            }
        }
    }

    private static Properties parseConfigInfo(String configInfo) throws IOException {
        Properties props = new Properties();
        props.load(new StringReader(configInfo));
        return props;
    }
}
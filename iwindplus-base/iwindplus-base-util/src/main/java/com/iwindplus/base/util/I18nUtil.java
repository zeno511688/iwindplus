/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util;

import com.iwindplus.base.domain.constant.CommonConstant;
import com.iwindplus.base.domain.constant.CommonConstant.SymbolConstant;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

/**
 * 国际化工具类.
 *
 * @author zengdegui
 * @since 2023/08/12 17:26
 */
@Slf4j
public class I18nUtil {

    /**
     * 缓存：baseName#locale -> ResourceBundle.
     */
    private static final ConcurrentHashMap<String, ResourceBundle> CACHE = new ConcurrentHashMap<>(16);

    /**
     * 当前实例的基础名.
     */
    private final String baseName;

    /**
     * 当前实例的语言.
     */
    private final Locale locale;

    private I18nUtil() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 构造方法（私有）.
     *
     * @param baseName 文件基础名
     * @param locale   语言
     */
    private I18nUtil(String baseName, Locale locale) {
        this.baseName = baseName;
        this.locale = locale;
    }

    /**
     * 获取 ResourceBundle（线程安全，永久缓存）.
     *
     * @return ResourceBundle
     */
    private ResourceBundle getBundle() {
        String key = baseName + SymbolConstant.WELL_NO + (locale == null ? Locale.getDefault() : locale);
        return CACHE.computeIfAbsent(key,
            k -> ResourceBundle.getBundle(baseName, locale == null ? Locale.getDefault() : locale));
    }

    /**
     * 实例化（默认 basename=messages，默认 locale）.
     *
     * @return I18nUtil
     */
    public static I18nUtil getInstance() {
        return getInstance("i18n/messages", null);
    }

    /**
     * 实例化.
     *
     * @param baseName 文件基础名
     * @param locale   语言
     * @return I18nUtil
     */
    public static I18nUtil getInstance(String baseName, Locale locale) {
        return new I18nUtil(baseName, locale);
    }

    /**
     * 获取值.
     *
     * @param key 键
     * @return String
     */
    public String getString(String key) {
        try {
            return getBundle().getString(key);
        } catch (MissingResourceException e) {
            log.warn("No message found under code={} for locale={}", key, locale);
            return key;
        }
    }

    /**
     * 格式化.
     *
     * @param key  键
     * @param args 值
     * @return String
     */
    public String getFormattedString(String key, Object... args) {
        return MessageFormat.format(this.getString(key), args);
    }
}
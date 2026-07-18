/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ReflectUtil;
import com.iwindplus.base.domain.constant.CommonConstant;
import com.iwindplus.base.domain.constant.CommonConstant.SymbolConstant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.cglib.core.Converter;
import org.springframework.util.Assert;

/**
 * 对象复制工具类.
 *
 * @author zengdegui
 * @since 2021/9/7
 */
@Slf4j
public class BeanCopierUtil {

    /**
     * BeanCopier的缓存.
     */
    private static final ConcurrentHashMap<String, BeanCopier> BEAN_COPIER_CACHE = new ConcurrentHashMap<>(16);

    private BeanCopierUtil() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 对象复制.
     *
     * @param source 源对象
     * @param target 目标类
     * @param <K>    源对象泛型
     * @param <T>    目标类泛型
     * @return T
     */
    public static <K, T> T copyProperties(K source, Supplier<T> target) {
        return copyProperties(source, target, null);
    }

    /**
     * 对象复制.
     *
     * @param source 源对象
     * @param target 目标类
     * @param <K>    源对象泛型
     * @param <T>    目标类泛型
     * @return T
     */
    public static <K, T> T copyProperties(K source, Class<T> target) {
        return copyProperties(source, target, null);
    }

    /**
     * 对象复制.
     *
     * @param source    源对象
     * @param target    目标类
     * @param converter 转换器
     * @param <K>       源对象泛型
     * @param <T>       目标类泛型
     * @return T
     */
    public static <K, T> T copyProperties(K source, Supplier<T> target, Converter converter) {
        if (source == null) {
            return null;
        }
        final T res = target.get();
        if (res == null) {
            return null;
        }
        final BeanCopier copier = getBeanCopier(source.getClass(), res.getClass(), Objects.nonNull(converter));
        copier.copy(source, res, converter);
        return res;
    }

    /**
     * 对象复制.
     *
     * @param source    源对象
     * @param target    目标类
     * @param converter 转换器
     * @param <K>       源对象泛型
     * @param <T>       目标类泛型
     * @return T
     */
    public static <K, T> T copyProperties(K source, Class<T> target, Converter converter) {
        if (source == null) {
            return null;
        }
        T res = ReflectUtil.newInstanceIfPossible(target);
        if (Objects.nonNull(res)) {
            final BeanCopier copier = getBeanCopier(source.getClass(), target, Objects.nonNull(converter));
            copier.copy(source, res, converter);
        }
        return res;
    }

    /**
     * 对象集合复制.
     *
     * @param sources 源对象集合
     * @param target  目标类
     * @param <K>     源对象泛型
     * @param <T>     目标类泛型
     * @return List<T>
     */
    public static <K, T> List<T> copyToList(List<K> sources, Supplier<T> target) {
        if (CollUtil.isEmpty(sources)) {
            return new ArrayList<>();
        }
        return copyToList(sources, target, null);
    }

    /**
     * 对象集合复制.
     *
     * @param sources 源对象集合
     * @param target  目标类
     * @param <K>     源对象泛型
     * @param <T>     目标类泛型
     * @return List<T>
     */
    public static <K, T> List<T> copyToList(List<K> sources, Class<T> target) {
        if (CollUtil.isEmpty(sources)) {
            return new ArrayList<>();
        }
        return copyToList(sources, target, null);
    }

    /**
     * 对象集合复制.
     *
     * @param sources   源对象集合
     * @param target    目标类
     * @param converter 转换器
     * @param <K>       源对象泛型
     * @param <T>       目标类泛型
     * @return List<T>
     */
    public static <K, T> List<T> copyToList(List<K> sources, Supplier<T> target, Converter converter) {
        List<T> list = new ArrayList<>(sources.size());
        sources.forEach(source -> list.add(copyProperties(source, target, converter)));
        return list;
    }

    /**
     * 对象集合复制.
     *
     * @param sources   源对象集合
     * @param target    目标类
     * @param converter 转换器
     * @param <K>       源对象泛型
     * @param <T>       目标类泛型
     * @return List<T>
     */
    public static <K, T> List<T> copyToList(List<K> sources, Class<T> target, Converter converter) {
        List<T> list = new ArrayList<>(sources.size());
        sources.forEach(source -> list.add(copyProperties(source, target, converter)));
        return list;
    }

    private static <S, T> BeanCopier getBeanCopier(Class<S> source, Class<T> target, boolean useConverter) {
        Assert.notNull(source, "Source must not be null");
        Assert.notNull(target, "Target must not be null");
        String cacheKey = new StringBuilder(source.getName())
            .append(SymbolConstant.UNDERLINE).append(target.getName()).append(SymbolConstant.UNDERLINE).append(useConverter ? 1 : 0).toString();
        return BEAN_COPIER_CACHE.computeIfAbsent(cacheKey, k ->
            BeanCopier.create(source, target, useConverter));
    }
}

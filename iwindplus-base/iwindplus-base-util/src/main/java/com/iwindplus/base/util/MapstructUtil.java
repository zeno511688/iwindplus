/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.iwindplus.base.domain.constant.CommonConstant;
import io.github.linpeilie.Converter;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Mapstruct工具类（需要结合注解）.
 *
 * @author zengdegui
 * @since 2024/07/06 12:39
 */
public class MapstructUtil {

    private MapstructUtil() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * Mapstruct转换器.
     */
    private static final Converter CONVERTER = SpringUtil.getBean(Converter.class);

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
        if (ObjectUtil.isEmpty(source)) {
            return null;
        }
        if (ObjectUtil.isEmpty(target)) {
            return null;
        }
        final T res = target.get();
        return CONVERTER.convert(source, res);
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
        if (ObjectUtil.isEmpty(source)) {
            return null;
        }
        if (ObjectUtil.isEmpty(target)) {
            return null;
        }
        return CONVERTER.convert(source, target);
    }

    /**
     * 对象集合复制.
     *
     * @param source 源对象
     * @param target 目标类
     * @param <T>    目标类泛型
     * @return T
     */
    public static <T> T copyProperties(Map<String, Object> source, Class<T> target) {
        if (ObjectUtil.isEmpty(source)) {
            return null;
        }
        if (ObjectUtil.isEmpty(target)) {
            return null;
        }
        return CONVERTER.convert(source, target);
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
        if (ObjectUtil.isEmpty(sources)) {
            return null;
        }
        if (ObjectUtil.isEmpty(target)) {
            return null;
        }
        return CONVERTER.convert(sources, target);
    }
}

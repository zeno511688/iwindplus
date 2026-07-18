/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.domain.enums;

import java.util.Arrays;
import java.util.Objects;

/**
 * 通用枚举接口.
 *
 * @param <V> 枚举值
 * @author zengdegui
 * @since 2020/6/13
 */
public interface BaseEnum<V> {

    /**
     * 获取枚举的值.
     *
     * @return 返回枚举值
     */
    V getValue();

    /**
     * 获取枚举的描述.
     *
     * @return 返回枚举描述
     */
    String getDesc();

    /**
     * 根据名称查找枚举.
     *
     * @param name  名称
     * @param clazz 枚举类型
     * @param <V>   泛型值
     * @param <E>   泛型枚举类型
     * @return E
     */
    static <V, E extends Enum<E> & BaseEnum<V>> E fromName(String name, Class<E> clazz) {
        return Arrays.stream(clazz.getEnumConstants())
            .filter(e -> e.name().equals(name))
            .findFirst()
            .orElse(null);
    }

    /**
     * 根据值查找枚举.
     *
     * @param value 值
     * @param clazz 枚举类型
     * @param <V>   泛型值
     * @param <E>   泛型枚举类型
     * @return E
     */
    static <V, E extends Enum<E> & BaseEnum<V>> E fromValue(V value, Class<E> clazz) {
        return Arrays.stream(clazz.getEnumConstants())
            .filter(e -> Objects.equals(e.getValue(), value))
            .findFirst()
            .orElse(null);
    }

    /**
     * 根据描述查找枚举.
     *
     * @param desc  描述
     * @param clazz 枚举类型
     * @param <V>   泛型值
     * @param <E>   泛型枚举类型
     * @return E
     */
    static <V, E extends Enum<E> & BaseEnum<V>> E fromDesc(String desc, Class<E> clazz) {
        return Arrays.stream(clazz.getEnumConstants())
            .filter(e -> Objects.equals(e.getDesc(), desc))
            .findFirst()
            .orElse(null);
    }

}

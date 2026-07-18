/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util;

import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.domain.constant.CommonConstant;
import com.iwindplus.base.domain.constant.CommonConstant.SymbolConstant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * FlattenUtil扁平化对象工具类.
 *
 * @author zengdegui
 * @since 2024/07/06 12:39
 */
public class FlattenUtil {

    private static final String EMPTY = SymbolConstant.EMPTY_STR;
    private static final String DOT = SymbolConstant.POINT;
    private static final String LEFT = SymbolConstant.LEFT_SQUARE_BRACKET;
    private static final String RIGHT = SymbolConstant.RIGHT_SQUARE_BRACKET;

    private FlattenUtil() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 递归扁平化嵌套的 Map 或 List.
     *
     * @param value
     * @return Map<String, Object>
     */
    public static Map<String, Object> flatten(Object value) {
        return flatten(value, EMPTY, new HashMap<>(16));
    }

    /**
     * 递归扁平化嵌套的 Map 或 List.
     *
     * @param value  数据
     * @param prefix 前缀
     * @param out    扁平化后的 Map
     * @return Map<String, Object>
     */
    public static Map<String, Object> flatten(Object value, String prefix, Map<String, Object> out) {
        if (value == null || SymbolConstant.EMPTY_STR.equals(value)) {
            return out;
        }

        if (value instanceof Map<?, ?> map) {
            map.forEach((k, v) -> flatten(v, join(prefix, String.valueOf(k)), out));
        } else if (value instanceof List<?> list) {
            IntStream.range(0, list.size())
                .forEach(i -> flatten(list.get(i), join(prefix, LEFT + i + RIGHT), out));
        } else {
            out.put(prefix, value);
        }
        return out;
    }

    private static String join(String prefix, String segment) {
        return CharSequenceUtil.isEmpty(prefix) ? segment : prefix + DOT + segment;
    }

}

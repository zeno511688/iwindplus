/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util;

import com.iwindplus.base.domain.constant.CommonConstant;
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;

/**
 * 校验对象属性是否全部为空工具类.
 *
 * @author zengdegui
 * @since 2021/11/23
 */
@Slf4j
public class ObjectEmptyCheckUtil {

    private ObjectEmptyCheckUtil() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 验证字段是否全部为空.
     *
     * @param obj 对象
     * @return boolean
     */
    public static boolean isDeepEmpty(Object obj) {
        return isDeepEmpty(obj, new IdentityHashMap<>());
    }

    private static boolean isDeepEmpty(Object o, IdentityHashMap<Object, Object> seen) {
        if (o == null || null != seen.put(o, true)) {
            return true;
        }
        if (o instanceof String s) {
            return s.isEmpty();
        }
        if (o instanceof Optional<?> op) {
            return op.isEmpty();
        }
        if (o instanceof Collection<?> c) {
            return c.stream().allMatch(e -> isDeepEmpty(e, seen));
        }
        if (o instanceof Map<?, ?> m) {
            return m.values().stream().allMatch(v -> isDeepEmpty(v, seen));
        }
        if (o.getClass().isArray()) {
            return IntStream.range(0, Array.getLength(o))
                .allMatch(i -> isDeepEmpty(Array.get(o, i), seen));
        }
        if (o.getClass().isPrimitive() || o.getClass().getName().startsWith("java.")) {
            return false;
        }
        if (o.getClass().isRecord()) {
            return Arrays.stream(o.getClass().getRecordComponents())
                .allMatch(rc -> {
                    try {
                        return isDeepEmpty(rc.getAccessor().invoke(o), seen);
                    } catch (Exception ex) {
                        log.error(ExceptionConstant.ILLEGAL_ACCESS_EXCEPTION, ex);
                        return true;
                    }
                });
        }
        return Arrays.stream(o.getClass().getDeclaredFields())
            .filter(f -> !Modifier.isStatic(f.getModifiers()))
            .allMatch(f -> {
                try {
                    f.setAccessible(true);
                    return isDeepEmpty(f.get(o), seen);
                } catch (Exception ex) {
                    log.error(ExceptionConstant.ILLEGAL_ACCESS_EXCEPTION, ex);
                    return true;
                }
            });
    }
}
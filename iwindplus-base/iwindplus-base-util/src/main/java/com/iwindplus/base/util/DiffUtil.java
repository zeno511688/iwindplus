/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.iwindplus.base.domain.constant.CommonConstant;
import com.iwindplus.base.domain.constant.CommonConstant.NumberConstant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;

/**
 * 对比差异工具.
 *
 * @author zengdegui
 * @since 2025/08/08 23:06
 */
@Slf4j
public class DiffUtil {

    private static final Cache<Object, Object> SORT_CACHE =
        CacheBuilder.newBuilder()
            .maximumSize(NumberConstant.NUMBER_TWO_THOUSAND_FORTY_EIGHT)
            .softValues()
            .recordStats()
            .build();

    private static final ThreadLocal<IdentityHashMap<Object, Object>> MEMO =
        ThreadLocal.withInitial(IdentityHashMap::new);

    private DiffUtil() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 对比两个任意对象（Map / List / POJO）
     *
     * @param left  左侧对象
     * @param right 右侧对象
     * @return List<String>
     */
    public static List<String> compare(Object left, Object right) {
        Map<String, Object> leftMap = toMap(left);
        Map<String, Object> rightMap = toMap(right);
        return compare(leftMap, rightMap);
    }

    /**
     * 对比两个 Map.
     *
     * @param leftMap  左侧 Map
     * @param rightMap 右侧 Map
     * @return List<String>
     */
    public static List<String> compare(Map<String, Object> leftMap, Map<String, Object> rightMap) {
        try {
            if (ObjectUtil.isEmpty(leftMap) && ObjectUtil.isEmpty(rightMap)) {
                return Collections.emptyList();
            }
            Map<String, Object> left = leftMap == null ? Collections.emptyMap() : leftMap;
            Map<String, Object> right = rightMap == null ? Collections.emptyMap() : rightMap;

            Set<String> common = new HashSet<>(left.keySet());
            common.retainAll(right.keySet());

            List<String> diffs = new ArrayList<>(common.size());
            for (String key : common) {
                Object leftVal = left.get(key);
                Object rightVal = right.get(key);

                // 4. null 与 "" 视为相同，均算“空”
                boolean leftEmpty = isEmptyOrBlank(leftVal);
                boolean rightEmpty = isEmptyOrBlank(rightVal);

                if (leftEmpty && rightEmpty) {
                    continue;
                }
                if (leftEmpty || rightEmpty) {
                    diffs.add(key);
                    continue;
                }

                // 5. 递归排序后比较
                Object leftSorted = sorted(leftVal);
                Object rightSorted = sorted(rightVal);
                if (!Objects.equals(leftSorted, rightSorted)) {
                    diffs.add(key);
                }
            }

            diffs.sort(String::compareTo);
            return diffs;
        } finally {
            MEMO.remove();
        }
    }

    private static Map<String, Object> toMap(Object obj) {
        if (obj == null) {
            return Collections.emptyMap();
        }
        if (obj instanceof Map map) {
            return map;
        }
        if (obj instanceof List<?> list) {
            return listToMap(list);
        }

        return JacksonUtil.parseObject(obj.toString(), new TypeReference<>() {
        });
    }

    private static Map<String, Object> listToMap(List<?> list) {
        return IntStream.range(0, list.size())
            .boxed()
            .collect(Collectors.toMap(
                String::valueOf,
                list::get,
                (a, b) -> a,
                LinkedHashMap::new));
    }

    private static Object sorted(Object obj) {
        if (obj == null) {
            return null;
        }
        IdentityHashMap<Object, Object> memo = MEMO.get();
        Object hit = memo.get(obj);
        if (hit != null) {
            return hit;
        }

        Object ans;
        try {
            ans = SORT_CACHE.get(obj, () -> doSorted(obj));
        } catch (Exception e) {
            ans = doSorted(obj);
        }
        memo.put(obj, ans);
        return ans;
    }

    private static Object doSorted(Object obj) {
        if (obj instanceof List<?> list) {
            return sortedList(list);
        }

        if (obj instanceof Map map) {
            return sortedMap(map);
        }
        return obj;
    }

    private static List<Object> sortedList(List<?> list) {
        return list.stream()
            .filter(Objects::nonNull)
            .map(DiffUtil::sorted)
            .sorted(DiffUtil::compareComparable)
            .collect(Collectors.toList());
    }

    private static Map<String, Object> sortedMap(Map<String, Object> map) {
        return map.entrySet().stream()
            .filter(e -> CharSequenceUtil.isNotBlank(e.getKey()))
            .sorted(Map.Entry.comparingByKey())
            .collect(Collectors.toMap(e -> e.getKey(),
                e -> sorted(e.getValue()),
                (a, b) -> a, LinkedHashMap::new));
    }

    private static int compareComparable(Object o1, Object o2) {
        if (o1 instanceof Comparable c1
            && o2 instanceof Comparable c2
            && o1.getClass().equals(o2.getClass())) {
            return c1.compareTo(c2);
        }
        return o1.toString().compareTo(o2.toString());
    }

    private static boolean isEmptyOrBlank(Object val) {
        if (val == null) {
            return true;
        }
        if (val instanceof String s) {
            return CharSequenceUtil.isBlank(s);
        }
        return false;
    }

}
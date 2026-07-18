/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.boot.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.iwindplus.base.util.DiffUtil;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * 对比差异测试.
 *
 * @author zengdegui
 * @since 2025/08/09 20:10
 */
public class DiffUtilTest {

    @Test
    public void testComplexComparison() {
        // 创建复杂的嵌套对象
        Map<String, Object> left = new HashMap<>();
        left.put("name", "Alice");
        left.put("age", 25);
        left.put("hobbies", Arrays.asList("reading", "traveling"));
        left.put("address", Map.of("city", "New York", "zip", "10001"));
        left.put("friends", Arrays.asList(
            Map.of("name", "Bob", "age", 30),
            Map.of("name", "Charlie", "age", 35)
        ));

        Map<String, Object> right = new HashMap<>();
        right.put("name", "Alice");
        right.put("age", 26);
        right.put("hobbies", Arrays.asList("traveling", "reading"));
        right.put("address", Map.of("zip", "10001", "city", "New York"));
        right.put("friends", Arrays.asList(
            Map.of("name", "Charlie", "age", 35),
            Map.of("name", "Bob", "age", 30)
        ));

        // 调用 compare 方法
        List<String> differences = DiffUtil.compare(left, right);

        // 验证结果
        assertNotNull(differences);
        assertEquals(1, differences.size());

        // 验证每个差异
        String diff = differences.get(0);
        assertEquals("age", diff);
    }

    @Test
    public void testListComparison() {
        // 创建包含列表的简单对象
        Map<String, Object> left = new HashMap<>();
        left.put("hobbies", Arrays.asList("reading", "traveling"));

        Map<String, Object> right = new HashMap<>();
        right.put("hobbies", Arrays.asList("traveling", "reading"));

        // 调用 compare 方法
        List<String> differences = DiffUtil.compare(left, right);

        // 验证结果
        assertNotNull(differences);
        assertTrue(differences.isEmpty());
    }

    @Test
    public void testMapComparison() {
        // 创建包含嵌套 Map 的简单对象
        Map<String, Object> left = new HashMap<>();
        left.put("address", Map.of("city", "New York", "zip", "10001"));

        Map<String, Object> right = new HashMap<>();
        right.put("address", Map.of("zip", "10001", "city", "New York"));

        // 调用 compare 方法
        List<String> differences = DiffUtil.compare(left, right);

        // 验证结果
        assertNotNull(differences);
        assertTrue(differences.isEmpty());
    }

    @Test
    public void testNullAndEmptyMaps() {
        // 测试 null 和空 Map
        List<String> diffs1 = DiffUtil.compare(null, null);
        assertNotNull(diffs1);
        assertTrue(diffs1.isEmpty());

        List<String> diffs2 = DiffUtil.compare(new HashMap<>(), new HashMap<>());
        assertNotNull(diffs2);
        assertTrue(diffs2.isEmpty());

        List<String> diffs3 = DiffUtil.compare(null, new HashMap<>());
        assertNotNull(diffs3);
        assertTrue(diffs3.isEmpty());

        List<String> diffs4 = DiffUtil.compare(new HashMap<>(), null);
        assertNotNull(diffs4);
        assertTrue(diffs4.isEmpty());
    }

}

/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.iwindplus.base.domain.constant.CommonConstant;
import com.iwindplus.base.domain.constant.CommonConstant.NumberConstant;
import com.iwindplus.base.domain.constant.CommonConstant.SymbolConstant;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 高性能全路径匹配工具（静态工具类形式，支持模糊匹配，带缓存）.
 *
 * <p>通配符规则：
 *
 * <ul>
 *   <li>{@code *}：匹配一个路径段，但不能跨越 {@code /}
 *   <li>{@code **}：匹配零个或多个路径段，可以跨越 {@code /}
 * </ul>
 *
 * <p>使用示例：
 *
 * <pre>
 * List<String> patterns = List.of("/api/*", "/admin/**");
 *
 * PathMatchUtil.match(patterns, "/api/user");       // true
 * PathMatchUtil.match(patterns, "/api/user/list");  // false
 * PathMatchUtil.match(patterns, "/admin");           // true
 * PathMatchUtil.match(patterns, "/admin/user");      // true
 * PathMatchUtil.match(patterns, "/admin/a/b");       // true
 * </pre>
 *
 * @author zengdegui
 * @since 2026/01/14
 */
public final class PathMatchUtil {

    private PathMatchUtil() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    private static final String STAR = SymbolConstant.ASTERISK;

    private static final String DOUBLE_STAR = SymbolConstant.DOUBLE_ASTERISK;

    private static final char SLASH_CHAR = SymbolConstant.SLASH.charAt(0);

    private static final int DEFAULT_CACHE_SIZE = NumberConstant.NUMBER_ONE_THOUSAND_TWENTY_FOUR;

    /**
     * 匹配结果缓存.
     *
     * <p>key = patternsKey + ":" + path.
     */
    private static final Cache<String, Boolean> CACHE =
        Caffeine.newBuilder()
            .maximumSize(DEFAULT_CACHE_SIZE)
            .expireAfterWrite(Duration.ofMinutes(10))
            .build();

    /**
     * Trie 缓存.
     *
     * <p>key = patternsKey.
     */
    private static final Cache<String, Node> TRIE_CACHE =
        Caffeine.newBuilder()
            .maximumSize(DEFAULT_CACHE_SIZE)
            .expireAfterWrite(Duration.ofMinutes(10))
            .build();

    /**
     * 规则缓存 key 缓存.
     *
     * <p>使用 weakKeys 以集合身份（identity）比较，允许 GC 回收。
     */
    private static final Cache<Collection<String>, String> PATTERNS_KEY_CACHE =
        Caffeine.newBuilder()
            .weakKeys()
            .maximumSize(DEFAULT_CACHE_SIZE)
            .expireAfterAccess(Duration.ofMinutes(NumberConstant.NUMBER_THIRTY))
            .build();

    /**
     * 匹配路径.
     *
     * @param patterns 路径规则列表
     * @param path     待匹配路径
     * @return 是否匹配
     */
    public static boolean match(Collection<String> patterns, String path) {
        if (patterns == null || patterns.isEmpty() || path == null || path.isEmpty()) {
            return false;
        }

        String patternsKey = patternsKey(patterns);
        String cacheKey = patternsKey + SymbolConstant.COLON + path;

        Boolean cachedResult = CACHE.getIfPresent(cacheKey);
        if (cachedResult != null) {
            return cachedResult;
        }

        Node root = TRIE_CACHE.get(patternsKey, key -> buildTrie(patterns));

        boolean result = matchDfs(root, splitFast(path));

        CACHE.put(cacheKey, result);
        return result;
    }

    /**
     * 生成规则缓存 key.
     *
     * <p>排序后生成 key，保证规则顺序不同但内容相同的集合 使用相同的 Trie。
     * 使用 weakKeys 缓存避免重复计算。
     *
     * @param patterns 路径规则
     * @return cache key
     */
    private static String patternsKey(Collection<String> patterns) {
        String key = PATTERNS_KEY_CACHE.getIfPresent(patterns);
        if (key != null) {
            return key;
        }
        List<String> list = new ArrayList<>(patterns);
        Collections.sort(list);
        key = CryptoUtil.encryptBySm3(String.join(",", list));
        PATTERNS_KEY_CACHE.put(patterns, key);
        return key;
    }

    /**
     * 构建 Trie.
     *
     * @param patterns 路径规则
     * @return Trie 根节点
     */
    private static Node buildTrie(Collection<String> patterns) {
        Node root = new Node();

        for (String pattern : patterns) {
            if (pattern == null || pattern.isEmpty()) {
                continue;
            }

            addPattern(root, pattern);
        }

        return root;
    }

    /**
     * 添加路径规则.
     *
     * @param root    Trie 根节点
     * @param pattern 路径规则
     */
    private static void addPattern(Node root, String pattern) {
        String[] segments = splitFast(pattern);

        Node current = root;

        for (String segment : segments) {
            current = current.getOrCreateChild(segment);

            if (DOUBLE_STAR.equals(segment)) {
                current.doubleStar = true;
            }
        }

        current.terminal = true;
    }

    /**
     * DFS 匹配.
     *
     * <p>核心规则：
     *
     * <ul>
     *   <li>普通节点：只能消费一个 path segment
     *   <li>{@code *}：消费一个 path segment
     *   <li>{@code **}：可以消费零个或多个 path segment
     * </ul>
     *
     * @param root     Trie 根节点
     * @param segments path segments
     * @return 是否匹配
     */
    private static boolean matchDfs(Node root, String[] segments) {
        ArrayDeque<DfsStatus> stack = new ArrayDeque<>();
        stack.addLast(new DfsStatus(root, 0));

        while (!stack.isEmpty()) {
            DfsStatus state = stack.removeLast();

            Node node = state.node;
            int index = state.index;

            if (index == segments.length) {
                if (handlePathConsumed(node, stack, index)) {
                    return true;
                }
                continue;
            }

            if (node.doubleStar) {
                handleDoubleStarNode(node, stack, segments, index);
                continue;
            }

            handleNormalNode(node, stack, segments[index], index);
        }

        return false;
    }

    /**
     * 处理路径已全部消费的情况.
     *
     * <p>只有 terminal 才能表示完整规则匹配；
     * 若当前节点后面存在 **，** 可以匹配 0 个 segment，继续尝试。
     *
     * @param node  当前节点
     * @param stack DFS 栈
     * @param index 当前索引
     * @return 是否匹配成功
     */
    private static boolean handlePathConsumed(Node node, ArrayDeque<DfsStatus> stack, int index) {
        if (node.terminal) {
            return true;
        }

        Node doubleStar = node.getChild(DOUBLE_STAR);
        if (doubleStar != null) {
            stack.addLast(new DfsStatus(doubleStar, index));
        }

        return false;
    }

    /**
     * 处理 ** 节点的匹配.
     *
     * <p>** 可以消费一个 segment（仍停留在 **），也可以匹配 0 个 segment
     * 并进入子节点继续匹配当前 segment。
     *
     * @param node     ** 节点
     * @param stack    DFS 栈
     * @param segments path segments
     * @param index    当前索引
     */
    private static void handleDoubleStarNode(Node node, ArrayDeque<DfsStatus> stack, String[] segments, int index) {
        // ** 匹配当前 segment，消费一个但仍然停留在 **
        stack.addLast(new DfsStatus(node, index + 1));

        // ** 匹配 0 个 segment，进入子节点继续匹配当前 segment
        String seg = segments[index];
        Node exact = node.getChild(seg);
        if (exact != null) {
            stack.addLast(new DfsStatus(exact, index + 1));
        }

        Node star = node.getChild(STAR);
        if (star != null) {
            stack.addLast(new DfsStatus(star, index + 1));
        }

        Node ds = node.getChild(DOUBLE_STAR);
        if (ds != null) {
            stack.addLast(new DfsStatus(ds, index));
        }
    }

    /**
     * 处理普通节点的匹配.
     *
     * <p>依次尝试精确匹配、* 匹配一个 segment、** 从当前节点开始匹配（不消费当前 segment）。
     *
     * @param node    当前节点
     * @param stack   DFS 栈
     * @param segment 当前 path segment
     * @param index   当前索引
     */
    private static void handleNormalNode(Node node, ArrayDeque<DfsStatus> stack, String segment, int index) {
        // 精确匹配
        Node exact = node.getChild(segment);
        if (exact != null) {
            stack.addLast(new DfsStatus(exact, index + 1));
        }

        // * 匹配一个 path segment
        Node star = node.getChild(STAR);
        if (star != null) {
            stack.addLast(new DfsStatus(star, index + 1));
        }

        // ** 从当前节点开始匹配（不消费当前 segment，因为 ** 可以匹配 0 个 segment）
        Node doubleStar = node.getChild(DOUBLE_STAR);
        if (doubleStar != null) {
            stack.addLast(new DfsStatus(doubleStar, index));
        }
    }

    /**
     * 快速切分路径.
     *
     * <p>自动忽略开头和结尾的 {@code /}， 连续 {@code /} 不产生空 segment。
     *
     * @param path 路径
     * @return segments
     */
    private static String[] splitFast(String path) {
        if (path == null || path.isEmpty()) {
            return new String[0];
        }

        int length = path.length();
        int start = path.charAt(0) == SLASH_CHAR ? 1 : 0;

        // 先计数 segment 数量
        int count = 0;
        for (int i = start; i < length; i++) {
            if (path.charAt(i) == SLASH_CHAR && start < i) {
                count++;
            }
        }
        if (start < length) {
            count++;
        }
        if (count == 0) {
            return new String[0];
        }

        // 直接填充数组，避免 ArrayList 中间层
        String[] segments = new String[count];
        int idx = 0;
        int segStart = start;
        for (int i = start; i < length; i++) {
            if (path.charAt(i) == SLASH_CHAR) {
                if (segStart < i) {
                    segments[idx++] = path.substring(segStart, i);
                }
                segStart = i + 1;
            }
        }
        if (segStart < length) {
            segments[idx] = path.substring(segStart);
        }
        return segments;
    }

    /**
     * Trie 节点.
     *
     * <p>子节点存储采用渐进式结构：子节点数 ≤ {@link #ARRAY_THRESHOLD} 时用数组线性查找，
     * 超过阈值后自动升级为 HashMap，兼顾内存占用与查找效率。
     */
    private static final class Node {

        private static final int ARRAY_THRESHOLD = NumberConstant.NUMBER_FOUR;

        /**
         * 是否为规则终点.
         */
        private boolean terminal;

        /**
         * 是否为 ** 节点.
         */
        private boolean doubleStar;

        /**
         * 子节点存储：null、Object[]（key-node 交替）或 Map.
         */
        private Object children;

        /**
         * 子节点数量.
         */
        private int childCount;

        private Node() {
        }

        /**
         * 获取子节点.
         *
         * @param segment 路径段
         * @return 子节点，不存在返回 null
         */
        @SuppressWarnings("unchecked")
        private Node getChild(String segment) {
            if (this.children == null) {
                return null;
            }
            if (this.children instanceof Map) {
                return ((Map<String, Node>) this.children).get(segment);
            }
            Object[] arr = (Object[]) this.children;
            for (int i = 0; i < this.childCount * 2; i += 2) {
                if (segment.equals(arr[i])) {
                    return (Node) arr[i + 1];
                }
            }
            return null;
        }

        /**
         * 获取或创建子节点.
         *
         * <p>子节点数未超阈值时用数组存储，超过阈值后升级为 HashMap。
         *
         * @param segment 路径段
         * @return 子节点
         */
        @SuppressWarnings("unchecked")
        private Node getOrCreateChild(String segment) {
            Node existing = this.getChild(segment);
            if (existing != null) {
                return existing;
            }

            Node child = new Node();

            if (this.children == null) {
                this.children = new Object[ARRAY_THRESHOLD * 2];
            }

            if (this.children instanceof Map) {
                ((Map<String, Node>) this.children).put(segment, child);
            } else if (this.childCount < ARRAY_THRESHOLD) {
                Object[] arr = (Object[]) this.children;
                arr[this.childCount * 2] = segment;
                arr[this.childCount * 2 + 1] = child;
            } else {
                // 超过阈值，升级为 HashMap
                Map<String, Node> map = new HashMap<>(ARRAY_THRESHOLD * 2);
                Object[] arr = (Object[]) this.children;
                for (int i = 0; i < this.childCount * 2; i += 2) {
                    map.put((String) arr[i], (Node) arr[i + 1]);
                }
                map.put(segment, child);
                this.children = map;
            }

            this.childCount++;
            return child;
        }
    }

    /**
     * DFS 状态.
     */
    private record DfsStatus(Node node, int index) {

    }
}

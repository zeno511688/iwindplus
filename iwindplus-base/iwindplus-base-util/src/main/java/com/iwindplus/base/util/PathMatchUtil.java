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
     *
     * @param patterns 路径规则
     * @return cache key
     */
    private static String patternsKey(Collection<String> patterns) {
        List<String> list = new ArrayList<>(patterns);
        Collections.sort(list);

        String join = String.join(",", list);
        return CryptoUtil.encryptBySm3(join);
    }

    /**
     * 构建 Trie.
     *
     * @param patterns 路径规则
     * @return Trie 根节点
     */
    private static Node buildTrie(Collection<String> patterns) {
        Node root = new Node(Math.max(16, patterns.size()));

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
            Node child = current.children.get(segment);

            if (child == null) {
                child = new Node(4);
                current.children.put(segment, child);
            }

            current = child;

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

            /*
             * path 已经全部消费。
             *
             * 只有 terminal 才能表示完整规则匹配。
             */
            if (index == segments.length) {
                if (node.terminal) {
                    return true;
                }

                /*
                 * 当前节点后面存在 **：
                 *
                 * ** 可以匹配 0 个 segment，
                 * 因此继续尝试 **。
                 */
                Node doubleStar = node.children.get(DOUBLE_STAR);
                if (doubleStar != null) {
                    stack.addLast(new DfsStatus(doubleStar, index));
                }

                continue;
            }

            /*
             * 当前节点是 **。
             */
            if (node.doubleStar) {

                /*
                 * ** 匹配当前 segment。
                 *
                 * 消费一个 segment，但仍然停留在 **。
                 */
                stack.addLast(
                    new DfsStatus(node, index + 1)
                );

                /*
                 * ** 匹配 0 个 segment。
                 *
                 * 不消费当前 segment，
                 * 进入 ** 后面的子节点继续匹配当前 segment。
                 *
                 * 注意：
                 * 只有与当前 segment 匹配的子节点才会被进入，
                 * 避免跳过中间节点导致误匹配。
                 */
                String seg = segments[index];
                Node exact = node.children.get(seg);
                if (exact != null) {
                    stack.addLast(
                        new DfsStatus(exact, index + 1)
                    );
                }

                Node star = node.children.get(STAR);
                if (star != null) {
                    stack.addLast(
                        new DfsStatus(star, index + 1)
                    );
                }

                Node ds = node.children.get(DOUBLE_STAR);
                if (ds != null) {
                    stack.addLast(
                        new DfsStatus(ds, index)
                    );
                }

                continue;
            }

            String segment = segments[index];

            /*
             * 精确匹配。
             */
            Node exact = node.children.get(segment);
            if (exact != null) {
                stack.addLast(
                    new DfsStatus(exact, index + 1)
                );
            }

            /*
             * * 匹配一个 path segment。
             */
            Node star = node.children.get(STAR);
            if (star != null) {
                stack.addLast(
                    new DfsStatus(star, index + 1)
                );
            }

            /*
             * ** 从当前节点开始匹配。
             *
             * 注意：
             * 这里不能消费当前 segment，
             * 因为 ** 可以匹配 0 个 segment。
             */
            Node doubleStar = node.children.get(DOUBLE_STAR);
            if (doubleStar != null) {
                stack.addLast(
                    new DfsStatus(doubleStar, index)
                );
            }
        }

        return false;
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

        List<String> segments = new ArrayList<>(10);

        for (int i = start; i < length; i++) {
            if (path.charAt(i) == SLASH_CHAR) {
                if (start < i) {
                    segments.add(path.substring(start, i));
                }

                start = i + 1;
            }
        }

        if (start < length) {
            segments.add(path.substring(start));
        }

        return segments.toArray(new String[0]);
    }

    /**
     * Trie 节点.
     */
    private static final class Node {

        /**
         * 是否为规则终点.
         */
        private boolean terminal;

        /**
         * 是否为 ** 节点.
         */
        private boolean doubleStar;

        /**
         * 子节点.
         */
        private final Map<String, Node> children;

        private Node(int initialCapacity) {
            this.children = new HashMap<>(initialCapacity);
        }
    }

    /**
     * DFS 状态.
     */
    private static final class DfsStatus {

        private final Node node;

        private final int index;

        private DfsStatus(Node node, int index) {
            this.node = node;
            this.index = index;
        }
    }
}

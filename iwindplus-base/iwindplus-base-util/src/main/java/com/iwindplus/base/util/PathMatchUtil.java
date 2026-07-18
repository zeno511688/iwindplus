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
import com.iwindplus.base.domain.constant.CommonConstant.SymbolConstant;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * 高性能全路径匹配工具（静态工具类形式，支持模糊匹配，带缓存）.
 * <p>
 * 使用示例：
 * <pre>
 * List<String> patterns = Arrays.asList("/api/*", "/admin/**");
 * boolean matched = PathMatchUtil.match(patterns, "/api/user");
 * </pre>
 *
 * @author zengdegui
 * @since 2026/01/14
 */
public final class PathMatchUtil {

    private PathMatchUtil() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    private static final String STAR = "*";
    private static final String DOUBLE_STAR = "**";
    private static final char SLASH_CHAR = '/';
    private static final int DEFAULT_CACHE_SIZE = 1024;
    private static final int DFS_POOL_SIZE = 512;

    /**
     * 匹配结果缓存： key = patterns.hashCode + ":" + path
     */
    private static final Cache<String, Boolean> CACHE = Caffeine.newBuilder()
        .maximumSize(DEFAULT_CACHE_SIZE)
        .expireAfterWrite(Duration.ofMinutes(10))
        .build();

    /**
     * Trie 缓存：key = patterns.hashCode，value = 构建好的 Trie 根节点
     */
    private static final Cache<String, Node> TRIE_CACHE = Caffeine.newBuilder()
        .maximumSize(DEFAULT_CACHE_SIZE)
        .expireAfterWrite(Duration.ofMinutes(10))
        .build();

    /**
     * 匹配路径
     *
     * @param patterns 路径规则列表
     * @param path     待匹配路径
     * @return 是否匹配
     */

    public static boolean match(Collection<String> patterns, String path) {
        if (patterns == null || patterns.isEmpty() || path == null || path.isEmpty()) {
            return false;
        }

        // 构建缓存 key
        String patternsKey = patternsKey(patterns);
        String cacheKey = patternsKey + SymbolConstant.COLON + path;

        // 匹配结果缓存
        Boolean cachedResult = CACHE.getIfPresent(cacheKey);
        if (cachedResult != null) {
            return cachedResult;
        }

        // 获取或构建 Trie
        Node root = TRIE_CACHE.get(patternsKey, key -> buildTrie(patterns));

        // DFS 匹配
        boolean result = matchDfs(root, splitFast(path), new ArrayBlockingQueue<>(DFS_POOL_SIZE));

        // 缓存匹配结果
        CACHE.put(cacheKey, result);
        return result;
    }

    private static String patternsKey(Collection<String> patterns) {
        // 按顺序拼接，保证相同内容集合得到同一个 key
        List<String> list = new ArrayList<>(patterns);
        Collections.sort(list);
        final String join = String.join(",", list);
        return CryptoUtil.encryptBySm3(join);
    }

    private static Node buildTrie(Collection<String> patterns) {
        Node root = new Node(Math.max(16, patterns.size()));
        for (String pattern : patterns) {
            addPattern(root, pattern);
        }
        return root;
    }

    private static void addPattern(Node root, String pattern) {
        String[] segments = splitFast(pattern);
        Node cur = root;
        int estimatedChildren = Math.max(segments.length, 4);
        for (String seg : segments) {
            cur = cur.children.computeIfAbsent(seg, k -> new Node(estimatedChildren));
        }
        cur.terminal = true;
    }

    private static boolean matchDfs(Node root, String[] segments, ArrayBlockingQueue<DfsStatus> dfsPool) {
        ArrayDeque<DfsStatus> stack = new ArrayDeque<>();
        stack.addLast(getDfsStatus(dfsPool, root, 0));

        while (!stack.isEmpty()) {
            DfsStatus state = stack.removeLast();
            Node node = state.node;
            int idx = state.idx;

            recycleDfsStatus(dfsPool, state);

            if (idx == segments.length) {
                if (node.terminal || node.children.containsKey(DOUBLE_STAR)) {
                    return true;
                }
                continue;
            }

            String seg = segments[idx];

            Node exact = node.children.get(seg);
            if (exact != null) {
                stack.addLast(getDfsStatus(dfsPool, exact, idx + 1));
            }

            Node star = node.children.get(STAR);
            if (star != null) {
                stack.addLast(getDfsStatus(dfsPool, star, idx + 1));
            }

            Node ds = node.children.get(DOUBLE_STAR);
            if (ds != null) {
                stack.addLast(getDfsStatus(dfsPool, ds, idx));
                stack.addLast(getDfsStatus(dfsPool, ds, idx + 1));
            }
        }
        return false;
    }

    private static DfsStatus getDfsStatus(ArrayBlockingQueue<DfsStatus> pool, Node node, int idx) {
        DfsStatus status = pool.poll();
        if (status == null) {
            return new DfsStatus(node, idx);
        }
        status.node = node;
        status.idx = idx;
        return status;
    }

    private static void recycleDfsStatus(ArrayBlockingQueue<DfsStatus> pool, DfsStatus status) {
        status.node = null;
        status.idx = 0;
        pool.offer(status);
    }

    private static String[] splitFast(String path) {
        if (path == null || path.isEmpty()) {
            return new String[0];
        }
        int len = path.length(), start = (path.charAt(0) == SLASH_CHAR ? 1 : 0);
        List<String> list = new ArrayList<>(10);
        for (int i = start; i < len; i++) {
            if (path.charAt(i) == SLASH_CHAR) {
                if (start < i) {
                    list.add(path.substring(start, i));
                }
                start = i + 1;
            }
        }
        if (start < len) {
            list.add(path.substring(start));
        }
        return list.toArray(new String[0]);
    }

    /**
     * Trie 节点
     */
    private static final class Node {

        private boolean terminal = false;
        private final Map<String, Node> children;

        Node(int initialCapacity) {
            this.children = new HashMap<>(initialCapacity);
        }
    }

    /**
     * DFS 状态
     */
    @Data
    @SuperBuilder
    @AllArgsConstructor
    private static final class DfsStatus {

        private Node node;
        private int idx;
    }
}

/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util;

import cn.hutool.core.lang.tree.Tree;
import com.iwindplus.base.domain.constant.CommonConstant;
import java.util.ArrayList;
import java.util.List;

/**
 * 树形工具类.
 *
 * @author zengdegui
 * @since 2023/08/12 17:26
 */
public class TreesUtil {

    private TreesUtil() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 原始树追加树
     *
     * @param sourceNodes 原始树
     * @param resNode     要追加的树
     */
    public static void appendToLastLeaf(List<Tree<Long>> sourceNodes, Tree<Long> resNode) {
        if (sourceNodes == null || sourceNodes.isEmpty() || resNode == null) {
            return;
        }

        // 1. 拍平拿到所有叶子（先序遍历，保持原顺序）
        List<Tree<Long>> leaves = new ArrayList<>(10);
        collectLeaves(sourceNodes, leaves);

        if (leaves.isEmpty()) {
            // 如果没有叶子节点，直接将 resNode 添加到根节点列表中
            sourceNodes.add(resNode);
            return;
        }

        // 2. 取最后一个叶子
        Tree<Long> lastLeaf = leaves.get(leaves.size() - 1);

        // 3. 确保它有 children 容器
        if (lastLeaf.getChildren() == null) {
            lastLeaf.setChildren(new ArrayList<>(10));
        }

        // 4. 挂进去
        lastLeaf.getChildren().add(resNode);
    }

    /**
     * 先序收集所有叶子节点.
     *
     * @param sourceNodes 原始树
     * @param leaves      要追加的数
     */
    private static void collectLeaves(List<Tree<Long>> sourceNodes, List<Tree<Long>> leaves) {
        if (sourceNodes == null) {
            return;
        }

        for (Tree<Long> n : sourceNodes) {
            if (n.getChildren() == null || n.getChildren().isEmpty()) {
                leaves.add(n);
            } else {
                collectLeaves(n.getChildren(), leaves);
            }
        }
    }

}

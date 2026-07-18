/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server.core.support;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.domain.enums.ConditionTypeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.flow.domain.dto.FlowConditionNodeDTO;
import com.iwindplus.flow.domain.dto.FlowNodeDTO;
import com.iwindplus.flow.domain.enums.FlowCodeEnum;
import com.iwindplus.flow.domain.enums.FlowNodeTypeEnum;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * 流程节点路由器：负责节点遍历、条件分支匹配等纯逻辑运算，无数据库依赖.
 *
 * @author zengdegui
 * @since 2026/05/21
 */
@Component
public class FlowNodeRouter {

    /**
     * 从指定节点开始解析出下一个真正需要处理的节点（审批节点或结束节点）.
     * CC 节点和条件节点会被跳过，由 FlowExecutionHelper 在创建任务时处理.
     */
    public FlowNodeDTO resolveNextNode(FlowNodeDTO node, Map<String, Object> variables) {
        if (node == null) {
            return null;
        }
        FlowNodeTypeEnum type = node.getNodeType();
        if (FlowNodeTypeEnum.END.equals(type)) {
            return node;
        }
        if (FlowNodeTypeEnum.CONDITION_NODE.equals(type)) {
            FlowConditionNodeDTO matched = this.matchConditionBranch(node.getConditionNodes(), variables);
            if (matched == null) {
                throw new BizException(FlowCodeEnum.FLOW_CONDITION_NO_MATCH);
            }
            return this.resolveNextNode(matched.getChildNode(), variables);
        }
        if (FlowNodeTypeEnum.CC_NODE.equals(type)) {
            return this.resolveNextNode(node.getChildNode(), variables);
        }
        return node;
    }

    /**
     * 从模型节点树中按编码深度优先查找节点.
     */
    public FlowNodeDTO findNodeByCode(FlowNodeDTO root, String nodeCode) {
        if (root == null || CharSequenceUtil.isBlank(nodeCode)) {
            return null;
        }
        if (nodeCode.equals(root.getCode())) {
            return root;
        }
        if (CollUtil.isNotEmpty(root.getConditionNodes())) {
            for (FlowConditionNodeDTO cond : root.getConditionNodes()) {
                FlowNodeDTO found = this.findNodeByCode(cond.getChildNode(), nodeCode);
                if (found != null) {
                    return found;
                }
            }
        }
        return this.findNodeByCode(root.getChildNode(), nodeCode);
    }

    /**
     * 按优先级匹配条件分支；无条件分支或最后一个分支作为默认.
     */
    public FlowConditionNodeDTO matchConditionBranch(List<FlowConditionNodeDTO> conditionNodes,
        Map<String, Object> variables) {
        if (CollUtil.isEmpty(conditionNodes)) {
            return null;
        }
        List<FlowConditionNodeDTO> sorted = conditionNodes.stream()
            .sorted(Comparator.comparingInt(c -> c.getPriority() != null ? c.getPriority() : Integer.MAX_VALUE))
            .toList();

        FlowConditionNodeDTO defaultBranch = sorted.get(sorted.size() - 1);
        for (FlowConditionNodeDTO branch : sorted) {
            if (CollUtil.isEmpty(branch.getConditions())) {
                defaultBranch = branch;
                continue;
            }
            boolean allMatch = branch.getConditions().stream()
                .allMatch(cond -> this.evaluateCondition(cond.getField(), cond.getType(), cond.getValue(), variables));
            if (allMatch) {
                return branch;
            }
        }
        return defaultBranch;
    }

    /**
     * 评估单个条件表达式.
     */
    public boolean evaluateCondition(String field, ConditionTypeEnum type, String condValue,
        Map<String, Object> variables) {
        if (CharSequenceUtil.isBlank(field) || type == null || variables == null) {
            return false;
        }
        Object varValue = variables.get(field);
        if (varValue == null) {
            return false;
        }
        String varStr = varValue.toString();
        return switch (type) {
            case EQ -> varStr.equals(condValue);
            case NE -> !varStr.equals(condValue);
            case LIKE -> varStr.contains(condValue);
            case NOT_LIKE -> !varStr.contains(condValue);
            case GT, GE, LT, LE -> {
                try {
                    double v = Double.parseDouble(varStr);
                    double c = Double.parseDouble(condValue);
                    yield switch (type) {
                        case GT -> v > c;
                        case GE -> v >= c;
                        case LT -> v < c;
                        case LE -> v <= c;
                        default -> false;
                    };
                } catch (NumberFormatException e) {
                    yield false;
                }
            }
        };
    }
}
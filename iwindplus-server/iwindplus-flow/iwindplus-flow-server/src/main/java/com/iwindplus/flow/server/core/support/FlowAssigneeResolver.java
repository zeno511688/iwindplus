/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server.core.support;

import com.iwindplus.flow.domain.dto.FlowNodeDTO;
import com.iwindplus.flow.domain.dto.FlowNodePlayerDTO;

import java.util.List;
import java.util.Map;

/**
 * 流程节点审批人动态解析接口.
 * 实现此接口并注册为 Spring Bean 可在运行时动态指定节点审批人，
 * 返回 null 或空列表时引擎回退到模型中配置的静态审批人.
 *
 * @author zengdegui
 * @since 2026/05/20
 */
public interface FlowAssigneeResolver {

    /**
     * 解析节点审批人.
     *
     * @param node       当前节点配置
     * @param instanceId 流程实例主键
     * @param variables  当前流程变量
     * @return 动态审批人列表，返回 null 或空则使用节点静态配置
     */
    List<FlowNodePlayerDTO> resolve(
        FlowNodeDTO node,
        Long instanceId,
        Map<String, Object> variables
    );

}
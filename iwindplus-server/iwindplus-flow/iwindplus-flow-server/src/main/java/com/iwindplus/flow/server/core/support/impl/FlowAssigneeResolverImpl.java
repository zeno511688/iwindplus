/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server.core.support.impl;

import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.flow.domain.dto.FlowNodeDTO;
import com.iwindplus.flow.domain.dto.FlowNodePlayerDTO;
import com.iwindplus.flow.domain.enums.FlowTaskPlayerTypeEnum;
import com.iwindplus.flow.server.core.support.FlowAssigneeResolver;
import com.iwindplus.mgt.client.power.UserClient;
import com.iwindplus.mgt.domain.vo.power.UserDepartmentInfoVO;
import com.iwindplus.mgt.domain.vo.power.UserOrgInfoVO;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 流程节点审批人动态解析接口实现类.
 *
 * @author zengdegui
 * @since 2026/06/22 21:27
 */
@Component
@RequiredArgsConstructor
public class FlowAssigneeResolverImpl implements FlowAssigneeResolver {

    private final UserClient userClient;

    @Override
    public List<FlowNodePlayerDTO> resolve(FlowNodeDTO node, Long instanceId, Map<String, Object> variables) {
        if (FlowTaskPlayerTypeEnum.ROLE.equals(node.getPlayerType())) {
            final List<Long> roleIds = node.getNodePlayers().stream().map(FlowNodePlayerDTO::getId).collect(Collectors.toList());
            final ResultVO<List<UserOrgInfoVO>> response = userClient.listByRoleIds(roleIds);
            response.errorThrow();
            return response.getBizData().stream().map(userOrgInfoVO -> FlowNodePlayerDTO.builder()
                .id(userOrgInfoVO.getUserId())
                .name(userOrgInfoVO.getUserRealName())
                .build()).collect(Collectors.toList());
        } else if (FlowTaskPlayerTypeEnum.DEPARTMENT.equals(node.getPlayerType())) {
            final List<Long> roleIds = node.getNodePlayers().stream().map(FlowNodePlayerDTO::getId).collect(Collectors.toList());
            final ResultVO<List<UserDepartmentInfoVO>> response = userClient.listByDepartmentIds(roleIds);
            response.errorThrow();
            return response.getBizData().stream().map(userDepartmentInfoVO -> FlowNodePlayerDTO.builder()
                .id(userDepartmentInfoVO.getUserId())
                .name(userDepartmentInfoVO.getUserRealName())
                .build()).collect(Collectors.toList());
        }
        return node.getNodePlayers();
    }
}

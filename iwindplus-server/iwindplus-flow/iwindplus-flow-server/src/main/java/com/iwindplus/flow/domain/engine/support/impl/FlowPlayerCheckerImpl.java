/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.domain.engine.support.impl;

import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.flow.domain.engine.support.FlowPlayerChecker;
import com.iwindplus.mgt.api.upms.vo.DepartmentBaseVO;
import com.iwindplus.mgt.api.upms.vo.RoleBaseVO;
import com.iwindplus.mgt.client.upms.DepartmentClient;
import com.iwindplus.mgt.client.upms.RoleClient;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 流程参与人接口实现类.
 *
 * @author zengdegui
 * @since 2026/05/22 18:34
 */
@Component
@RequiredArgsConstructor
public class FlowPlayerCheckerImpl implements FlowPlayerChecker {

    private final RoleClient roleClient;
    private final DepartmentClient departmentClient;

    @Override
    public List<Long> listRoleIdsByUserId(Long orgId, Long userId) {
        final ResultVO<List<RoleBaseVO>> response = roleClient.listCheckedByUserId(orgId, userId);
        response.errorThrow();
        return response.getBizData().stream().map(RoleBaseVO::getId).toList();
    }

    @Override
    public List<Long> listDepartmentIdsByUserId(Long orgId, Long userId) {
        final ResultVO<List<DepartmentBaseVO>> response = this.departmentClient.listCheckedByUserId(orgId, userId);
        response.errorThrow();
        return response.getBizData().stream().map(DepartmentBaseVO::getId).toList();
    }
}

/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server.core.support;

import java.util.List;

/**
 * 流程参与人归属校验接口. 实现此接口并注册为 Spring Bean，引擎在处理角色/部门类型任务时 通过此接口判断操作人是否属于对应的角色或部门.
 *
 * @author zengdegui
 * @since 2026/05/20
 */
public interface FlowPlayerChecker {

    /**
     * 获取用户所有角色.
     *
     * @param orgId  角色主键
     * @param userId 用户主键
     * @return List<Long>
     */
    List<Long> listRoleIdsByUserId(Long orgId, Long userId);

    /**
     * 获取用户所有部门.
     *
     * @param orgId  角色主键
     * @param userId 用户主键
     * @return List<Long>
     */
    List<Long> listDepartmentIdsByUserId(Long orgId, Long userId);
}
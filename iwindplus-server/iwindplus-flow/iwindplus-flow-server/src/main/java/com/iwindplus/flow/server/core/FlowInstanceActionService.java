/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server.core;

import com.iwindplus.base.domain.vo.UserBaseVO;
import com.iwindplus.flow.domain.dto.FlowStartInstanceDTO;
import com.iwindplus.flow.domain.vo.FlowStartInstanceVO;

/**
 * 流程实例动作业务层接口.
 *
 * @author zengdegui
 * @since 2026/05/22 22:48
 */
public interface FlowInstanceActionService {

    /**
     * 发起流程实例.
     *
     * @param entity 对象
     * @return 实例主键及单号
     */
    FlowStartInstanceVO startInstance(FlowStartInstanceDTO entity);

    /**
     * 撤销流程实例（发起人撤回）.
     *
     * @param instanceId  实例主键
     * @param currentUser 当前用户
     * @return boolean
     */
    boolean revokeInstance(Long instanceId, UserBaseVO currentUser);

    /**
     * 终止流程实例（管理员强制终止）.
     *
     * @param instanceId  实例主键
     * @param currentUser 当前用户
     * @return boolean
     */
    boolean terminateInstance(Long instanceId, UserBaseVO currentUser);
}

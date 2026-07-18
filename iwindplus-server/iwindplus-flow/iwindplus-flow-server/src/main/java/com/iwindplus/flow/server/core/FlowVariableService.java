/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server.core;

import java.util.Map;

/**
 * 流程变量业务层接口.
 *
 * @author zengdegui
 * @since 2026/05/22 23:40
 */
public interface FlowVariableService {

    /**
     * 加载变量.
     *
     * @param instanceId 实例ID
     * @return 变量
     */
    Map<String, Object> loadVariables(Long instanceId);

    /**
     * 合并变量.
     *
     * @param instanceId 实例ID
     * @param variables  变量
     */
    void mergeVariables(
        Long instanceId,
        Map<String, Object> variables
    );
}

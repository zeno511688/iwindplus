/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server.core;

/**
 * 流程引擎.
 *
 * @author zengdegui
 * @since 2026/01/11 19:33
 */
public interface FlowEngine {

    /**
     * 流程实例动作.
     *
     * @return
     */
    FlowInstanceActionService instanceAction();

    /**
     * 流程任务动作.
     *
     * @return
     */
    FlowTaskActionService taskAction();
}
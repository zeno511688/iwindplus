/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.domain.engine;

import com.iwindplus.flow.domain.engine.model.FlowTaskEventDTO;

/**
 * 流程任务事件动作监听器接口.
 *
 * @author zengdegui
 * @since 2026/05/20
 */
public interface FlowTaskActionListener {

    /**
     * 接收流程任务事件.
     *
     * @param event 事件数据
     */
    void onEvent(FlowTaskEventDTO event);

}

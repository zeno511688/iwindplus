/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server.core;

import com.iwindplus.flow.domain.dto.FlowInstanceEventDTO;

/**
 * 流程实例事件监听器接口.
 *
 * @author zengdegui
 * @since 2026/05/20
 */
public interface FlowInstanceListener {

    /**
     * 接收流程实例事件.
     *
     * @param event 事件数据
     */
    void onEvent(FlowInstanceEventDTO event);

}
/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server.core;

import com.iwindplus.flow.domain.dto.FlowInstanceEventDTO;
import com.iwindplus.flow.domain.dto.FlowTaskEventDTO;

/**
 * 流程事件业务层接口.
 *
 * @author zengdegui
 * @since 2026/05/22 23:40
 */
public interface FlowEventService {

    /**
     * 发布实例事件.
     *
     * @param event 事件
     */
    void publishInstanceEvent(
        FlowInstanceEventDTO event
    );

    /**
     * 发布任务事件.
     *
     * @param event 事件
     */
    void publishTaskEvent(
        FlowTaskEventDTO event
    );
}

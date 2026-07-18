/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server.core.impl;

import com.iwindplus.flow.domain.dto.FlowInstanceEventDTO;
import com.iwindplus.flow.domain.dto.FlowTaskEventDTO;
import com.iwindplus.flow.server.core.FlowEventService;
import com.iwindplus.flow.server.core.FlowInstanceActionListener;
import com.iwindplus.flow.server.core.FlowTaskActionListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * 流程事件业务层接口实现类.
 *
 * @author zengdegui
 * @since 2026/05/22 23:44
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FlowEventServiceImpl implements FlowEventService {

    private final ObjectProvider<FlowInstanceActionListener> instanceListeners;
    private final ObjectProvider<FlowTaskActionListener> taskListeners;

    @Override
    public void publishInstanceEvent(FlowInstanceEventDTO event) {
        instanceListeners.forEach(listener -> {
            try {
                listener.onEvent(event);
            } catch (Exception e) {
                log.error("流程实例事件发布失败 eventType={}", event.getEventType(), e);
            }
        });
    }

    @Override
    public void publishTaskEvent(FlowTaskEventDTO event) {
        taskListeners.forEach(listener -> {
            try {
                listener.onEvent(event);
            } catch (Exception e) {
                log.error("流程任务事件发布失败 eventType={}", event.getEventType(), e);
            }
        });
    }
}

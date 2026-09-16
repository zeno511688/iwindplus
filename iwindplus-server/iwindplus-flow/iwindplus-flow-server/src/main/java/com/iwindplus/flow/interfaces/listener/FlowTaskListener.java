/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.interfaces.listener;

import com.iwindplus.flow.domain.engine.model.FlowTaskEventDTO;
import com.iwindplus.flow.domain.engine.FlowTaskActionListener;
import org.springframework.stereotype.Component;

/**
 * 流程任务事件监听.
 *
 * @author zengdegui
 * @since 2026/06/14 21:28
 */
@Component
public class FlowTaskListener implements FlowTaskActionListener {

    @Override
    public void onEvent(FlowTaskEventDTO event) {

    }
}

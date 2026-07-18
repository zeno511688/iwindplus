/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server.core.impl;

import com.iwindplus.flow.server.core.FlowEngine;
import com.iwindplus.flow.server.core.FlowInstanceActionService;
import com.iwindplus.flow.server.core.FlowTaskActionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 流程引擎业务实现类.
 *
 * @author zengdegui
 * @since 2026/01/11 19:33
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FlowEngineImpl implements FlowEngine {

    private final FlowInstanceActionService flowInstanceActionService;
    private final FlowTaskActionService flowTaskActionService;

    @Override
    public FlowInstanceActionService instanceAction() {
        return flowInstanceActionService;
    }

    @Override
    public FlowTaskActionService taskAction() {
        return flowTaskActionService;
    }
}
/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.support.impl;

import com.iwindplus.base.async.cmd.domain.bo.AsyncCmdBO;
import com.iwindplus.base.async.cmd.domain.enums.DispatchModeEnum;
import com.iwindplus.base.async.cmd.domain.property.AsyncCmdProperty;
import com.iwindplus.base.async.cmd.service.AsyncCmdService;
import com.iwindplus.base.async.cmd.support.AsyncCmdBizProcessor;
import lombok.extern.slf4j.Slf4j;

/**
 * 异步命令调度助手（异步）策略实现类.
 *
 * @author zengdegui
 * @since 2025/11/29 23:12
 */
@Slf4j
public class AsyncCmdDispatchHandlerAsync extends AbstractAsyncCmdDispatchHandler {

    private final AsyncCmdBizProcessor asyncCmdBizProcessor;

    public AsyncCmdDispatchHandlerAsync(
        AsyncCmdProperty property,
        AsyncCmdService asyncCmdService,
        AsyncCmdBizProcessor asyncCmdBizProcessor) {
        super(property, asyncCmdService);
        this.asyncCmdBizProcessor = asyncCmdBizProcessor;
    }

    @Override
    public DispatchModeEnum support() {
        return DispatchModeEnum.ASYNC;
    }

    @Override
    protected void doExecute(AsyncCmdBO entity) {
        asyncCmdBizProcessor.execute(entity);
    }
}
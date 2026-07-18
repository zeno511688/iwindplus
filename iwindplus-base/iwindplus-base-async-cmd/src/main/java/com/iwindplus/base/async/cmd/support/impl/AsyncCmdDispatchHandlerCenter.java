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
import lombok.extern.slf4j.Slf4j;

/**
 * 异步命令调度助手（调度中心）策略实现类.
 *
 * @author zengdegui
 * @since 2025/11/29 23:12
 */
@Slf4j
public class AsyncCmdDispatchHandlerCenter extends AbstractAsyncCmdDispatchHandler {

    public AsyncCmdDispatchHandlerCenter(
        AsyncCmdProperty property,
        AsyncCmdService asyncCmdService) {
        super(property, asyncCmdService);
    }

    @Override
    public DispatchModeEnum support() {
        return DispatchModeEnum.CENTER;
    }

    @Override
    protected void doExecute(AsyncCmdBO entity) {

    }

}

/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.support.impl;

import com.iwindplus.base.async.cmd.factory.AsyncCmdTaskHandlerStrategyFactory;
import com.iwindplus.base.async.cmd.service.AsyncCmdService;
import com.iwindplus.base.async.cmd.support.AsyncCmdExecuteHandler;
import com.iwindplus.base.async.cmd.support.AsyncCmdStateSupport;
import com.iwindplus.base.async.cmd.support.AsyncCmdTaskHandler;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 抽象异步命令执行形态策略实现类.
 *
 * @author zengdegui
 * @since 2025/11/29 23:12
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public abstract class AbstractAsyncCmdExecuteHandler implements AsyncCmdExecuteHandler {

    private final AsyncCmdTaskHandlerStrategyFactory asyncCmdTaskHandlerStrategyFactory;
    private final AsyncCmdStateSupport asyncCmdStateSupport;
    private final AsyncCmdService asyncCmdService;

    /**
     * 获取主任务助手.
     *
     * @param executeName 执行器名称
     * @return AsyncCmdTaskHandler
     */
    protected AsyncCmdTaskHandler getTaskHandler(String executeName) {
        return this.asyncCmdTaskHandlerStrategyFactory.getTaskHandler(executeName);
    }
}

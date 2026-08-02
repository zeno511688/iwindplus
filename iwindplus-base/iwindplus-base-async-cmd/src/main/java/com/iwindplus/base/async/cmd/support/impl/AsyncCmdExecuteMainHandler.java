/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.support.impl;

import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdVO;
import com.iwindplus.base.async.cmd.factory.AsyncCmdTaskHandlerStrategyFactory;
import com.iwindplus.base.async.cmd.service.AsyncCmdService;
import com.iwindplus.base.async.cmd.support.AsyncCmdStateSupport;
import com.iwindplus.base.async.cmd.support.AsyncCmdTaskHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * 异步命令主任务执行策略实现类.
 *
 * @author zengdegui
 * @since 2025/11/29 23:12
 */
@Slf4j
public class AsyncCmdExecuteMainHandler extends AbstractAsyncCmdExecuteHandler {

    public AsyncCmdExecuteMainHandler(
        AsyncCmdTaskHandlerStrategyFactory asyncCmdTaskHandlerStrategyFactory,
        AsyncCmdStateSupport asyncCmdStateSupport,
        AsyncCmdService asyncCmdService) {
        super(asyncCmdTaskHandlerStrategyFactory, asyncCmdStateSupport, asyncCmdService);
    }

    @Override
    public void execute(AsyncCmdVO entity) {
        final AsyncCmdTaskHandler handler = this.getTaskHandler(entity.getExecuteName());

        try {
            // 执行业务逻辑（无事务）
            handler.execute(entity);
            // 成功
            this.getAsyncCmdStateSupport().taskSuccess(entity, handler);
        } catch (Exception ex) {
            log.error("asyncCmd execute failed. id={}", entity.getId(), ex);
            // 失败
            this.getAsyncCmdStateSupport().taskFail(entity, handler, ex);
        }
    }
}

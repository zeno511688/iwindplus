/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.support.impl;

import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdExecuteResultEnum;
import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdStatusEnum;
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
public class AsyncCmdExecuteHandlerMain extends AbstractAsyncCmdExecuteHandler {

    public AsyncCmdExecuteHandlerMain(
        AsyncCmdTaskHandlerStrategyFactory asyncCmdTaskHandlerStrategyFactory,
        AsyncCmdStateSupport asyncCmdStateSupport,
        AsyncCmdService asyncCmdService) {
        super(asyncCmdTaskHandlerStrategyFactory, asyncCmdStateSupport, asyncCmdService);
    }

    @Override
    public void execute(AsyncCmdVO entity) {
        final AsyncCmdTaskHandler handler = this.getTaskHandler(entity.getExecuteName());
        final long start = System.currentTimeMillis();

        // 判断是否是异步等待状态
        if (AsyncCmdStatusEnum.ASYNC_WAIT.equals(entity.getStatus())) {
            this.executeCallbackAsyncWait(entity, handler, start);

            return;
        }

        try {
            // 执行业务前续期执行租约，业务耗时接近timeoutSeconds时降低被RESET_JOB误重置双跑的风险
            this.getAsyncCmdService().editExpireTime(entity.getId());

            // 执行业务逻辑（无事务），由业务方显式返回执行结果
            final AsyncCmdExecuteResultEnum result = handler.execute(entity);

            this.handleExecuteResult(entity, handler, start, result, false);
        } catch (Exception ex) {
            log.error("asyncCmd task execute failed. id={}", entity.getId(), ex);

            // 失败
            this.getAsyncCmdStateSupport().taskFail(entity, handler,
                System.currentTimeMillis() - start, ex, false);
        }
    }

    private boolean taskFail(AsyncCmdVO entity, AsyncCmdTaskHandler handler, long start, AsyncCmdExecuteResultEnum result) {
        if (AsyncCmdExecuteResultEnum.FAILED.equals(result)) {
            this.getAsyncCmdStateSupport().taskFail(entity, handler,
                System.currentTimeMillis() - start,
                new RuntimeException("asyncCmd task execute returned FAILED"), false);
            return true;
        }
        return false;
    }


}

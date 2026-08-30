/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.task.support.impl;

import com.iwindplus.base.async.task.domain.vo.AsyncTaskExecuteResultVO;
import com.iwindplus.base.async.task.domain.enums.AsyncTaskStatusEnum;
import com.iwindplus.base.async.task.domain.vo.AsyncTaskVO;
import com.iwindplus.base.async.task.factory.AsyncTaskHandlerStrategyFactory;
import com.iwindplus.base.async.task.service.AsyncTaskService;
import com.iwindplus.base.async.task.support.AsyncTaskStateSupport;
import com.iwindplus.base.async.task.support.AsyncTaskHandler;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

/**
 * 异步任务主任务执行策略实现类.
 *
 * @author zengdegui
 * @since 2025/11/29 23:12
 */
@Slf4j
public class AsyncTaskExecuteHandlerMain extends AbstractAsyncTaskExecuteHandler {

    public AsyncTaskExecuteHandlerMain(
        AsyncTaskHandlerStrategyFactory asyncTaskHandlerStrategyFactory,
        AsyncTaskStateSupport asyncTaskStateSupport,
        AsyncTaskService asyncTaskService) {
        super(asyncTaskHandlerStrategyFactory, asyncTaskStateSupport, asyncTaskService);
    }

    @Override
    public void execute(AsyncTaskVO entity) {
        final AsyncTaskHandler handler = this.getTaskHandler(entity.getExecuteName());
        final long start = System.currentTimeMillis();

        // 判断是否是异步等待状态
        if (AsyncTaskStatusEnum.WAITING.equals(entity.getStatus())) {
            this.executeCallbackAsyncWait(entity, handler, start);

            return;
        }

        try {
            // 执行业务逻辑（无事务），由业务方显式返回执行结果
            final AsyncTaskExecuteResultVO result = handler.execute(entity);

            this.handleExecuteResult(entity, handler, start, result, false);
        } catch (Exception ex) {
            log.error("asyncTask task execute failed. id={}", entity.getId(), ex);

            // 失败
            final long costTime = Optional.ofNullable(entity.getCostTime()).orElse(0L) + System.currentTimeMillis() - start;
            this.getAsyncTaskStateSupport().taskFail(entity, handler,
                costTime, ex, false);
        }
    }
}

/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.support.impl;

import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdStatusEnum;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdSubVO;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdVO;
import com.iwindplus.base.async.cmd.factory.AsyncCmdSubTaskHandlerStrategyFactory;
import com.iwindplus.base.async.cmd.factory.AsyncCmdTaskHandlerStrategyFactory;
import com.iwindplus.base.async.cmd.service.AsyncCmdService;
import com.iwindplus.base.async.cmd.service.AsyncCmdSubService;
import com.iwindplus.base.async.cmd.support.AsyncCmdStateSupport;
import com.iwindplus.base.async.cmd.support.AsyncCmdSubTaskHandler;
import com.iwindplus.base.async.cmd.support.AsyncCmdTaskHandler;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * 异步命令组任务执行策略实现类.
 *
 * @author zengdegui
 * @since 2025/11/29 23:12
 */
@Slf4j
public class AsyncCmdExecuteGroupHandler extends AbstractAsyncCmdExecuteHandler {

    private final AsyncCmdSubService asyncCmdSubService;
    private final AsyncCmdSubTaskHandlerStrategyFactory asyncCmdSubTaskHandlerStrategyFactory;

    public AsyncCmdExecuteGroupHandler(
        AsyncCmdTaskHandlerStrategyFactory asyncTaskHandlerStrategyFactory,
        AsyncCmdStateSupport asyncCmdStateSupport,
        AsyncCmdService asyncCmdService,
        AsyncCmdSubService asyncCmdSubService,
        AsyncCmdSubTaskHandlerStrategyFactory asyncCmdSubTaskHandlerStrategyFactory) {
        super(asyncTaskHandlerStrategyFactory, asyncCmdStateSupport, asyncCmdService);
        this.asyncCmdSubService = asyncCmdSubService;
        this.asyncCmdSubTaskHandlerStrategyFactory = asyncCmdSubTaskHandlerStrategyFactory;
    }

    @Override
    public void execute(AsyncCmdVO entity) {
        final AsyncCmdTaskHandler handler = this.getTaskHandler(entity.getExecuteName());
        final long start = System.currentTimeMillis();

        final List<AsyncCmdSubVO> subEntities = this.asyncCmdSubService.listUnfinished(entity.getId());
        int subTaskSuccessAdvanced = 0;
        try {
            // 执行子任务，返回成功的个数
            subTaskSuccessAdvanced = this.executeSubTask(entity, subEntities);

            // 子任务未全部成功，主任务判定为失败，主任务表只记"子任务有未完成的任务"
            final long unfinished = asyncCmdSubService.countUnfinished(entity.getId());
            if (unfinished > 0) {
                log.warn("asyncCmd group has unfinished subTask, id={} unfinished={} success={}",
                    entity.getId(), unfinished, subTaskSuccessAdvanced);

                this.getAsyncCmdStateSupport().taskFail(entity, handler,
                    System.currentTimeMillis() - start,
                    new RuntimeException("asyncCmd group has unfinished subTask"),
                    subTaskSuccessAdvanced > 0);
                return;
            }

            // 子任务全部成功 -> 主任务收尾业务 -> 主任务置成功
            handler.execute(entity);
            this.getAsyncCmdStateSupport().taskSuccess(entity, handler, System.currentTimeMillis() - start);
        } catch (Exception ex) {
            log.error("asyncCmd group execute failed, id={}", entity.getId(), ex);

            this.getAsyncCmdStateSupport().taskFail(entity, handler, System.currentTimeMillis() - start, ex, subTaskSuccessAdvanced > 0);
        }
    }

    private int executeSubTask(AsyncCmdVO entity, List<AsyncCmdSubVO> subEntities) {
        int subTaskSuccessAdvanced = 0;
        for (AsyncCmdSubVO subEntity : subEntities) {
            final AsyncCmdSubTaskHandler handler = this.getSubTaskHandler(subEntity.getExecuteName());
            final long start = System.currentTimeMillis();
            this.getAsyncCmdService().editExpireTime(entity.getId());
            asyncCmdSubService.editStatusById(subEntity.getId(), AsyncCmdStatusEnum.EXECUTE);

            try {
                handler.executeSub(subEntity);

                // 执行成功后，状态落库才计数累加
                boolean result = this.getAsyncCmdStateSupport().subTaskSuccess(subEntity, handler, System.currentTimeMillis() - start);
                if (!result) {
                    return subTaskSuccessAdvanced;
                }

                subTaskSuccessAdvanced++;
            } catch (Exception ex) {
                log.error("asyncCmd subTask execute failed, id={} asyncCmdId={} seq={}",
                    subEntity.getId(), entity.getId(), subEntity.getSeq(), ex);

                this.getAsyncCmdStateSupport().subTaskFail(subEntity, handler, System.currentTimeMillis() - start, ex);

                // 后续任务不在执行
                return subTaskSuccessAdvanced;
            }
        }

        return subTaskSuccessAdvanced;
    }

    private AsyncCmdSubTaskHandler getSubTaskHandler(String executeName) {
        return this.asyncCmdSubTaskHandlerStrategyFactory.getTaskHandler(executeName);
    }

}

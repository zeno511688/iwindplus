/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.task.support.impl;

import com.iwindplus.base.async.task.domain.dto.AsyncTaskShardSearchDTO;
import com.iwindplus.base.async.task.domain.enums.AsyncTaskJobEnum;
import com.iwindplus.base.async.task.domain.enums.AsyncTaskStatusEnum;
import com.iwindplus.base.async.task.domain.property.AsyncTaskProperty;
import com.iwindplus.base.async.task.domain.vo.AsyncTaskVO;
import com.iwindplus.base.async.task.service.AsyncTaskService;
import com.iwindplus.base.async.task.support.AsyncTaskBizProcessor;
import com.iwindplus.base.async.task.support.AsyncTaskStateSupport;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * 异步任务定时任务重试处理器.
 *
 * @author zengdegui
 * @since 2025/11/29 23:12
 */
@Slf4j
public class AsyncTaskJobHandlerRetry extends AbstractAsyncTaskJobHandler {

    private final AsyncTaskBizProcessor asyncTaskBizProcessor;
    private final AsyncTaskStateSupport asyncTaskStateSupport;

    public AsyncTaskJobHandlerRetry(
        AsyncTaskProperty property,
        AsyncTaskService asyncTaskService,
        AsyncTaskBizProcessor asyncTaskBizProcessor,
        AsyncTaskStateSupport asyncTaskStateSupport) {
        super(property, asyncTaskService);
        this.asyncTaskBizProcessor = asyncTaskBizProcessor;
        this.asyncTaskStateSupport = asyncTaskStateSupport;
    }

    @Override
    public AsyncTaskJobEnum support() {
        return AsyncTaskJobEnum.RETRY_JOB;
    }

    @Override
    protected boolean doExecute(List<AsyncTaskVO> entityList) {
        int dispatched = 0;
        int skipped = 0;
        int failed = 0;
        boolean poolFull = false;

        for (AsyncTaskVO entity : entityList) {
            if (this.shouldSkip(entity)) {
                skipped++;
                continue;
            }

            if (!this.asyncTaskBizProcessor.execute(entity)) {
                failed++;

                log.warn("重试任务投递被拒，共享池已满，已投递={}/{} id={}",
                    dispatched, entityList.size(), entity.getId());

                poolFull = true;
                break;
            }
            dispatched++;
        }

        if (dispatched > 0 || skipped > 0 || failed > 0) {
            log.info("重试任务完成，调度成功的={}, 跳过的={}, 失败的={}", dispatched, skipped, failed);
        }

        // 共享池满时返回true，避免execute方法提前结束整个捞取过程
        return poolFull || dispatched > 0;
    }

    @Override
    protected AsyncTaskShardSearchDTO buildJobSearchDTO() {
        // 查询所有未完成任务，具体是否到调度时间在 shouldSkip 中按状态判断。
        return AsyncTaskShardSearchDTO.builder()
            .statusList(AsyncTaskStatusEnum.getUnfinishedStatus())
            .build();
    }

    @Override
    protected boolean shouldSkip(AsyncTaskVO entity) {
        final AsyncTaskStatusEnum status = entity.getStatus();
        final long currentTime = System.currentTimeMillis();

        if (AsyncTaskStatusEnum.EXECUTING.equals(status)) {
            final Long expireTime = entity.getExpireTime();
            if (expireTime == null || expireTime > currentTime) {
                return true;
            }

            if (this.asyncTaskStateSupport.taskExecutingToPending(entity)) {
                return false;
            }

            log.debug("任务已被其他实例处理，跳过重新调度，id={}", entity.getId());
            return true;
        }

        final Long nextRetryTime = entity.getNextRetryTime();
        if (nextRetryTime != null && nextRetryTime > currentTime) {
            log.debug("任务尚未到重试时间，跳过调度，id={}, status={}, nextRetryTime={}",
                entity.getId(), status, nextRetryTime);
            return true;
        }

        return false;
    }

}

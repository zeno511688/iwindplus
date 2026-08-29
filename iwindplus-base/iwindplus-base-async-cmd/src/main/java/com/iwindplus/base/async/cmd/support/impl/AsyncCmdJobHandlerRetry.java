/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.support.impl;

import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdShardSearchDTO;
import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdJobEnum;
import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdStatusEnum;
import com.iwindplus.base.async.cmd.domain.property.AsyncCmdProperty;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdVO;
import com.iwindplus.base.async.cmd.service.AsyncCmdService;
import com.iwindplus.base.async.cmd.support.AsyncCmdBizProcessor;
import com.iwindplus.base.async.cmd.support.AsyncCmdStateSupport;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * 异步命令定时任务处理器.
 *
 * @author zengdegui
 * @since 2025/11/29 23:12
 */
@Slf4j
public class AsyncCmdJobHandlerRetry extends AbstractAsyncCmdJobHandler {

    private final AsyncCmdBizProcessor asyncCmdBizProcessor;
    private final AsyncCmdStateSupport asyncCmdStateSupport;

    public AsyncCmdJobHandlerRetry(
        AsyncCmdProperty property,
        AsyncCmdService asyncCmdService,
        AsyncCmdBizProcessor asyncCmdBizProcessor,
        AsyncCmdStateSupport asyncCmdStateSupport) {
        super(property, asyncCmdService);
        this.asyncCmdBizProcessor = asyncCmdBizProcessor;
        this.asyncCmdStateSupport = asyncCmdStateSupport;
    }

    @Override
    public AsyncCmdJobEnum support() {
        return AsyncCmdJobEnum.RETRY_JOB;
    }

    @Override
    protected boolean doExecute(List<AsyncCmdVO> entityList) {
        int dispatched = 0;
        int skipped = 0;
        int failed = 0;
        boolean poolFull = false;

        for (AsyncCmdVO entity : entityList) {
            if (this.shouldSkip(entity)) {
                skipped++;
                continue;
            }

            if (!this.asyncCmdBizProcessor.execute(entity)) {
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
    protected AsyncCmdShardSearchDTO buildJobSearchDTO() {
        // 查询所有未完成任务，具体是否到调度时间在 shouldSkip 中按状态判断。
        return AsyncCmdShardSearchDTO.builder()
            .statusList(AsyncCmdStatusEnum.getUnfinishedStatus())
            .build();
    }

    @Override
    protected boolean shouldSkip(AsyncCmdVO entity) {
        final AsyncCmdStatusEnum status = entity.getStatus();
        final long currentTime = System.currentTimeMillis();

        if (AsyncCmdStatusEnum.EXECUTING.equals(status)) {
            final Long expireTime = entity.getExpireTime();
            if (expireTime == null || expireTime > currentTime) {
                return true;
            }

            if (this.asyncCmdStateSupport.taskExecutingToPending(entity)) {
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

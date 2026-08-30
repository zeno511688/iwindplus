/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.task.support;

import com.iwindplus.base.async.task.domain.dto.AsyncTaskExtDTO;
import com.iwindplus.base.async.task.domain.enums.AsyncTaskStatusEnum;
import com.iwindplus.base.async.task.domain.property.AsyncTaskProperty;
import com.iwindplus.base.async.task.domain.vo.AsyncTaskVO;
import com.iwindplus.base.async.task.service.AsyncTaskService;
import com.iwindplus.base.async.task.service.AsyncTaskSubService;
import com.iwindplus.base.util.TransactionUtil;
import java.util.Optional;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import lombok.extern.slf4j.Slf4j;

/**
 * 异步任务业务处理（核心）.
 *
 * @author zengdegui
 * @since 2025/12/29 22:55
 */
@Slf4j
public record AsyncTaskBizProcessor(
    AsyncTaskProperty property,
    AsyncTaskService asyncTaskService,
    AsyncTaskSubService asyncTaskSubService,
    AsyncTaskStateSupport asyncTaskStateSupport,
    AsyncTaskExecuteHandler asyncTaskExecuteHandlerMain,
    AsyncTaskExecuteHandler asyncTaskExecuteHandlerGroup,
    ThreadPoolExecutor threadPoolExecutor) {

    /**
     * 执行任务.
     *
     * @param entity 对象
     * @return boolean
     */
    public boolean execute(AsyncTaskVO entity) {
        if (entity == null || entity.getId() == null) {
            return false;
        }

        final Long id = entity.getId();

        TransactionUtil.registerAfterCommit(
            () -> submitExecutor(id)
        );

        return true;
    }

    private boolean submitExecutor(Long id) {
        try {
            threadPoolExecutor.execute(() -> doExecute(id));

            return true;
        } catch (RejectedExecutionException ex) {
            log.warn("asyncTask executor rejected. id={}", id, ex);

            return false;
        } catch (Exception ex) {
            log.error("asyncTask executor failed. id={}", id, ex);

            return false;
        }
    }

    private void doExecute(Long id) {
        AsyncTaskVO entity = this.asyncTaskService.getDetail(id);
        if (entity == null) {
            log.info("asyncTask not exist, skip. id={}", id);
            return;
        }

        // 只有待执行/失败重试/异步等待状态才处理
        if (!needProcessStatus(entity)) {
            return;
        }

        // 待执行状态或失败重试状态，先抢占为执行中
        final AsyncTaskStatusEnum status = entity.getStatus();
        if (AsyncTaskStatusEnum.PENDING.equals(status)) {
            if (!asyncTaskStateSupport.taskPendingToExecuting(entity)) {
                log.info("asyncTask already handled. id={}", entity.getId());
                return;
            }
        } else if (AsyncTaskStatusEnum.FAILED.equals(status)) {
            if (this.reachMaxAttempts(entity)) {
                if (asyncTaskStateSupport.taskDiscard(entity)) {
                    log.info("asyncTask retry count reached max attempts, discard. id={}, retryCount={}, maxAttempts={}",
                        entity.getId(), entity.getRetryCount(), entity.getExt().getMaxAttempts());
                }
                return;
            }
            if (!asyncTaskStateSupport.taskFailedToExecute(entity)) {
                log.info("asyncTask already handled. id={}", entity.getId());
                return;
            }
        }

        final long start = System.currentTimeMillis();

        try {
            final long subTaskCount = Optional.of(entity.getSubTaskCount()).orElse(0);
            this.getExecuteHandler(subTaskCount).execute(entity);
        } catch (Exception ex) {
            log.error("asyncTask execute failed. id={}", entity.getId(), ex);

            // 兜底主任务卡在执行中等重试
            if (AsyncTaskStatusEnum.EXECUTING.equals(entity.getStatus())) {
                final long costTime = Optional.ofNullable(entity.getCostTime()).orElse(0L) + System.currentTimeMillis() - start;
                this.asyncTaskStateSupport.taskFail(entity, null, costTime, ex, false);
                return;
            }

            // 兜底主任务卡在异步等待中只能等重置状态
            if (AsyncTaskStatusEnum.WAITING.equals(entity.getStatus())) {
                final long costTime = Optional.ofNullable(entity.getCostTime()).orElse(0L) + System.currentTimeMillis() - start;
                this.asyncTaskStateSupport.taskAsyncWaitFail(entity, null, costTime, ex);
                return;
            }
        }
    }

    private boolean needProcessStatus(AsyncTaskVO entity) {
        AsyncTaskStatusEnum status = entity.getStatus();
        return AsyncTaskStatusEnum.PENDING.equals(status)
            || AsyncTaskStatusEnum.WAITING.equals(status)
            || AsyncTaskStatusEnum.FAILED.equals(status);
    }

    private boolean reachMaxAttempts(AsyncTaskVO entity) {
        final AsyncTaskExtDTO ext = entity.getExt();
        if (ext == null || Boolean.TRUE.equals(ext.getEnabledUnlimitedRetry())) {
            return false;
        }
        final int retryCount = Optional.ofNullable(entity.getRetryCount()).orElse(0);
        final int maxAttempts = Optional.ofNullable(ext.getMaxAttempts()).orElse(0);
        return retryCount >= maxAttempts;
    }

    /**
     * 选择执行策略.
     *
     * @param subTaskCount 子任务总数
     * @return AsyncTaskExecuteHandler
     */
    private AsyncTaskExecuteHandler getExecuteHandler(long subTaskCount) {
        return subTaskCount <= 0 ? asyncTaskExecuteHandlerMain : asyncTaskExecuteHandlerGroup;
    }
}

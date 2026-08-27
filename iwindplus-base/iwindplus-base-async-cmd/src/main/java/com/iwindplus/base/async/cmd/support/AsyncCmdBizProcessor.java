/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.support;

import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdExtDTO;
import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdStatusEnum;
import com.iwindplus.base.async.cmd.domain.property.AsyncCmdProperty;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdVO;
import com.iwindplus.base.async.cmd.service.AsyncCmdService;
import com.iwindplus.base.async.cmd.service.AsyncCmdSubService;
import com.iwindplus.base.util.TransactionUtil;
import java.util.Optional;
import java.util.concurrent.RejectedExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.core.executor.DtpExecutor;

/**
 * 异步命令业务处理（核心）.
 *
 * @author zengdegui
 * @since 2025/12/29 22:55
 */
@Slf4j
public record AsyncCmdBizProcessor(
    AsyncCmdProperty property,
    AsyncCmdService asyncCmdService,
    AsyncCmdSubService asyncCmdSubService,
    AsyncCmdStateSupport asyncCmdStateSupport,
    AsyncCmdExecuteHandler asyncCmdExecuteHandlerMain,
    AsyncCmdExecuteHandler asyncCmdExecuteHandlerGroup,
    DtpExecutor asyncCmdTaskExecutor) {

    /**
     * 执行任务.
     *
     * @param entity 对象
     * @return boolean
     */
    public boolean execute(AsyncCmdVO entity) {
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
            asyncCmdTaskExecutor.execute(() -> doExecute(id));

            return true;
        } catch (RejectedExecutionException ex) {
            log.warn("asyncCmd executor rejected. id={}", id, ex);

            return false;
        } catch (Exception ex) {
            log.error("asyncCmd executor failed. id={}", id, ex);

            return false;
        }
    }

    private void doExecute(Long id) {
        AsyncCmdVO entity = this.asyncCmdService.getDetail(id);
        if (entity == null) {
            log.info("asyncCmd not exist, skip. id={}", id);
            return;
        }

        // 只有待执行/失败重试/异步等待状态才处理
        if (!needProcessStatus(entity)) {
            return;
        }

        // 待执行状态或失败重试状态，先抢占为执行中
        final AsyncCmdStatusEnum status = entity.getStatus();
        if (AsyncCmdStatusEnum.PENDING.equals(status)) {
            if (!asyncCmdStateSupport.taskPendingToExecute(entity)) {
                log.info("asyncCmd already handled. id={}", entity.getId());
                return;
            }
        } else if (AsyncCmdStatusEnum.FAILED.equals(status)) {
            if (this.reachMaxAttempts(entity)) {
                if (asyncCmdStateSupport.taskDiscard(entity)) {
                    log.info("asyncCmd retry count reached max attempts, discard. id={}, retryCount={}, maxAttempts={}",
                        entity.getId(), entity.getRetryCount(), entity.getExt().getMaxAttempts());
                }
                return;
            }
            if (!asyncCmdStateSupport.taskFailedToExecute(entity)) {
                log.info("asyncCmd already handled. id={}", entity.getId());
                return;
            }
        }

        final long start = System.currentTimeMillis();

        try {
            final long subTaskCount = Optional.of(entity.getSubTaskCount()).orElse(0);
            this.getExecuteHandler(subTaskCount).execute(entity);
        } catch (Exception ex) {
            log.error("asyncCmd execute failed. id={}", entity.getId(), ex);

            // 兜底主任务卡在执行中只能等重置状态
            if (AsyncCmdStatusEnum.EXECUTE.equals(entity.getStatus())) {
                final long costTime = Optional.ofNullable(entity.getCostTime()).orElse(0L) + System.currentTimeMillis() - start;
                this.asyncCmdStateSupport.taskFail(entity, null, costTime, ex, false);
                return;
            }

            // 兜底主任务卡在异步等待中只能等重置状态
            if (AsyncCmdStatusEnum.WAITING.equals(entity.getStatus())) {
                final long costTime = Optional.ofNullable(entity.getCostTime()).orElse(0L) + System.currentTimeMillis() - start;
                this.asyncCmdStateSupport.taskAsyncWaitFail(entity, null, costTime, ex);
                return;
            }
        }
    }

    private boolean needProcessStatus(AsyncCmdVO entity) {
        AsyncCmdStatusEnum status = entity.getStatus();
        return AsyncCmdStatusEnum.PENDING.equals(status)
            || AsyncCmdStatusEnum.WAITING.equals(status)
            || AsyncCmdStatusEnum.FAILED.equals(status);
    }

    private boolean reachMaxAttempts(AsyncCmdVO entity) {
        final AsyncCmdExtDTO ext = entity.getExt();
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
     * @return AsyncCmdExecuteHandler
     */
    private AsyncCmdExecuteHandler getExecuteHandler(long subTaskCount) {
        return subTaskCount <= 0 ? asyncCmdExecuteHandlerMain : asyncCmdExecuteHandlerGroup;
    }
}

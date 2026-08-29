/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.document.support;

import com.iwindplus.base.document.domain.dto.ExportTaskExtDTO;
import com.iwindplus.base.document.domain.enums.ExportTaskStatusEnum;
import com.iwindplus.base.document.domain.property.DocumentProperty;
import com.iwindplus.base.document.domain.vo.ExportTaskVO;
import com.iwindplus.base.document.service.ExportTaskService;
import com.iwindplus.base.util.TransactionUtil;
import java.util.Optional;
import java.util.concurrent.RejectedExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.core.executor.DtpExecutor;

/**
 * 导出任务业务处理（核心）.
 *
 * @author zengdegui
 * @since 2026/08/28 17:24
 */
@Slf4j
public record ExportTaskBizProcessor(
    DocumentProperty property,
    ExportTaskService exportTaskService,
    ExportTaskStateSupport exportTaskStateSupport,
    ExportTaskExecuteHandler exportTaskExecuteHandler,
    DtpExecutor exportTaskExecutor) {

    /**
     * 执行任务.
     *
     * @param entity 对象
     * @return boolean
     */
    public boolean execute(ExportTaskVO entity) {
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
            exportTaskExecutor.execute(() -> doExecute(id));

            return true;
        } catch (RejectedExecutionException ex) {
            log.warn("exportTask executor rejected. id={}", id, ex);

            return false;
        } catch (Exception ex) {
            log.error("exportTask executor failed. id={}", id, ex);

            return false;
        }
    }

    private void doExecute(Long id) {
        ExportTaskVO entity = this.exportTaskService.getDetail(id);
        if (entity == null) {
            log.info("exportTask not exist, skip. id={}", id);
            return;
        }

        // 只有待执行/失败重试状态才处理
        if (!needProcessStatus(entity)) {
            return;
        }

        // 待执行状态或失败重试状态，先抢占为执行中
        final ExportTaskStatusEnum status = entity.getStatus();
        if (ExportTaskStatusEnum.PENDING.equals(status)) {
            if (!exportTaskStateSupport.taskPendingToExecuting(entity)) {
                log.info("exportTask already handled. id={}", entity.getId());
                return;
            }
        } else if (ExportTaskStatusEnum.FAILED.equals(status)) {
            if (this.reachMaxAttempts(entity)) {
                if (exportTaskStateSupport.taskDiscard(entity)) {
                    log.info("exportTask retry count reached max attempts, discard. id={}, retryCount={}, maxAttempts={}",
                        entity.getId(), entity.getRetryCount(), entity.getExt().getMaxAttempts());
                }
                return;
            }
            if (!exportTaskStateSupport.taskFailedToExecute(entity)) {
                log.info("exportTask already handled. id={}", entity.getId());
                return;
            }
        }

        final long start = System.currentTimeMillis();

        try {
            // 执行业务
            this.exportTaskExecuteHandler.execute(entity);
        } catch (Exception ex) {
            log.error("exportTask execute failed. id={}", entity.getId(), ex);

            // 兜底任务卡在执行中等重试
            if (ExportTaskStatusEnum.EXECUTING.equals(entity.getStatus())) {
                final long costTime = Optional.ofNullable(entity.getCostTime()).orElse(0L) + System.currentTimeMillis() - start;
                this.exportTaskStateSupport.taskFail(entity, null, costTime, ex, false);
                return;
            }
        }
    }

    private boolean needProcessStatus(ExportTaskVO entity) {
        ExportTaskStatusEnum status = entity.getStatus();
        return ExportTaskStatusEnum.PENDING.equals(status)
            || ExportTaskStatusEnum.FAILED.equals(status);
    }

    private boolean reachMaxAttempts(ExportTaskVO entity) {
        final ExportTaskExtDTO ext = entity.getExt();
        if (ext == null || Boolean.TRUE.equals(ext.getEnabledUnlimitedRetry())) {
            return false;
        }
        final int retryCount = Optional.ofNullable(entity.getRetryCount()).orElse(0);
        final int maxAttempts = Optional.ofNullable(ext.getMaxAttempts()).orElse(0);
        return retryCount >= maxAttempts;
    }
}

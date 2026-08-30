/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.export.task.support;

import com.iwindplus.base.domain.constant.CommonConstant.NumberConstant;
import com.iwindplus.base.export.task.dal.repository.ExportTaskRepository;
import com.iwindplus.base.export.task.domain.constant.ExportTaskConstant;
import com.iwindplus.base.export.task.domain.dto.ExportTaskStatusEditDTO;
import com.iwindplus.base.export.task.domain.enums.ExportTaskStatusEnum;
import com.iwindplus.base.export.task.domain.property.ExportTaskProperty;
import com.iwindplus.base.export.task.domain.vo.ExportTaskVO;
import com.iwindplus.base.export.task.service.ExportTaskService;
import com.iwindplus.base.util.DatesUtil;
import com.iwindplus.base.util.TransactionUtil;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 导出任务状态机.
 *
 * @author zengdegui
 * @since 2026/08/28 17:31
 */
@Slf4j
public record ExportTaskStateSupport(
    ExportTaskProperty property,
    ExportTaskRepository exportTaskRepository,
    ExportTaskService exportTaskService,
    TransactionTemplate transactionTemplate) {

    /**
     * 任务执行中
     *
     * @param entity 命令对象
     * @return boolean
     */
    public boolean taskPendingToExecuting(ExportTaskVO entity) {
        final long expireTime = this.exportTaskRepository.getNextExpireTime(System.currentTimeMillis());

        return this.transition(
            () -> exportTaskService.editStatusById(ExportTaskStatusEditDTO.builder()
                .id(entity.getId())
                .from(ExportTaskStatusEnum.PENDING)
                .to(ExportTaskStatusEnum.EXECUTING)
                .expireTime(expireTime)
                .build()),
            () -> {
                this.syncStatus(entity, ExportTaskStatusEnum.EXECUTING);
                entity.setExpireTime(expireTime);
            }
        );
    }

    /**
     * 执行中的任务恢复为待执行.
     *
     * @param entity 命令对象
     * @return boolean
     */
    public boolean taskExecutingToPending(ExportTaskVO entity) {
        return this.transition(
            () -> exportTaskService.editStatusById(ExportTaskStatusEditDTO.builder()
                .id(entity.getId())
                .from(ExportTaskStatusEnum.EXECUTING)
                .to(ExportTaskStatusEnum.PENDING)
                .build()),
            () -> this.syncStatus(entity, ExportTaskStatusEnum.PENDING)
        );
    }

    /**
     * 失败任务重新进入执行状态.
     *
     * @param entity 命令对象
     * @return boolean
     */
    public boolean taskFailedToExecute(ExportTaskVO entity) {
        final long now = System.currentTimeMillis();
        final long expireTime = this.exportTaskRepository.getNextExpireTime(now);

        return this.transition(
            () -> exportTaskService.editStatusById(ExportTaskStatusEditDTO.builder()
                .id(entity.getId())
                .from(ExportTaskStatusEnum.FAILED)
                .to(ExportTaskStatusEnum.EXECUTING)
                .expireTime(expireTime)
                .build()),
            () -> {
                this.syncStatus(entity, ExportTaskStatusEnum.EXECUTING);
                entity.setExpireTime(expireTime);
            }
        );
    }

    /**
     * 失败任务达到最大重试次数后丢弃.
     *
     * @param entity 命令对象
     * @return boolean
     */
    public boolean taskDiscard(ExportTaskVO entity) {
        return this.transition(
            () -> exportTaskService.editStatusById(ExportTaskStatusEditDTO.builder()
                .id(entity.getId())
                .from(ExportTaskStatusEnum.FAILED)
                .to(ExportTaskStatusEnum.DISCARD)
                .build()),
            () -> this.syncStatus(entity, ExportTaskStatusEnum.DISCARD)
        );
    }

    /**
     * 任务执行成功
     *
     * @param entity   对象
     * @param handler  任务助手
     * @param costTime 耗时
     * @return boolean
     */
    public boolean taskSuccess(
        ExportTaskVO entity,
        ExportTaskHandler handler,
        Long costTime) {

        final boolean result = this.transition(
            () -> exportTaskService.editStatusById(ExportTaskStatusEditDTO.builder()
                .id(entity.getId())
                .from(ExportTaskStatusEnum.EXECUTING)
                .to(ExportTaskStatusEnum.SUCCESS)
                .costTime(costTime)
                .progress(NumberConstant.NUMBER_ONE_HUNDRED)
                .build()),
            () -> {
                this.syncStatus(entity, ExportTaskStatusEnum.SUCCESS);
                entity.setCostTime(costTime);
                entity.setProgress(NumberConstant.NUMBER_ONE_HUNDRED);
            }
        );

        if (result && Objects.nonNull(handler)) {
            this.safeCallback(() -> handler.onTaskSuccess(entity), ExportTaskConstant.HOOK_ON_TASK_SUCCESS, entity.getId());
        }

        return result;
    }

    /**
     * 任务执行失败
     *
     * @param entity   对象
     * @param handler  任务助手
     * @param costTime 耗时
     * @param ex       异常
     * @param advanced 本轮是否有子任务成功推进
     * @return boolean
     */
    public boolean taskFail(
        ExportTaskVO entity,
        ExportTaskHandler handler,
        Long costTime,
        Exception ex,
        boolean advanced) {

        int retryCount = advanced ? 0 : Optional.ofNullable(entity.getRetryCount()).orElse(0) + 1;
        final String stack = this.getStack(ex);
        final long now = System.currentTimeMillis();
        final long nextRetryTime = DatesUtil.getNextRetryTime(now, this.property.getRetry().getFrequency(), retryCount);
        final long expireTime = this.exportTaskRepository.getNextExpireTime(now);

        final boolean result = this.transition(
            () -> this.exportTaskService.editStatusById(ExportTaskStatusEditDTO.builder()
                .id(entity.getId())
                .from(ExportTaskStatusEnum.EXECUTING)
                .to(ExportTaskStatusEnum.FAILED)
                .costTime(costTime)
                .errorMsg(stack)
                .retryCount(retryCount)
                .nextRetryTime(nextRetryTime)
                .expireTime(expireTime)
                .build()),
            () -> {
                this.syncStatus(entity, ExportTaskStatusEnum.FAILED);
                entity.setCostTime(costTime);
                entity.setErrorMsg(stack);
                entity.setRetryCount(retryCount);
                entity.setNextRetryTime(nextRetryTime);
                entity.setExpireTime(expireTime);
            }
        );

        if (result && Objects.nonNull(handler)) {
            this.safeCallback(() -> handler.onTaskFail(entity), ExportTaskConstant.HOOK_ON_TASK_FAIL, entity.getId());
        }

        return result;
    }

    /**
     * 执行一次状态迁移。业务方法只负责构建更新参数和同步实体字段。
     */
    private boolean transition(BooleanSupplier update, Runnable syncEntity) {
        return Boolean.TRUE.equals(TransactionUtil.executeInTransaction(this.transactionTemplate, () -> {
            boolean updated = update.getAsBoolean();
            if (updated) {
                syncEntity.run();
            }
            return updated;
        }));
    }

    /**
     * 同步公共状态字段。
     */
    private void syncStatus(ExportTaskVO entity, ExportTaskStatusEnum status) {
        entity.setModifiedTimestamp(System.currentTimeMillis());
        entity.setStatus(status);
    }

    /**
     * 安全回调（仅用于失败通知钩子onTaskFail/onSubTaskFail，已处于失败处理链路，异常吞掉避免递归失败）.
     *
     * @param callback 回调
     * @param name     回调方法名称
     * @param id       主键
     */
    private void safeCallback(Runnable callback, String name, Long id) {
        if (Objects.isNull(callback)) {
            return;
        }

        try {
            callback.run();
        } catch (Exception ex) {
            log.error("ExportTask callback failed. callback={} id={}", name, id, ex);
        }
    }

    /**
     * 获取异常堆栈
     *
     * @param ex 异常
     * @return String
     */
    private String getStack(Exception ex) {
        String stack = ExceptionUtils.getStackTrace(ex);
        return Boolean.TRUE.equals(property.getEnabledExceptionCapture())
            ? StringUtils.abbreviate(stack, property.getExceptionCaptureLength())
            : stack;
    }
}

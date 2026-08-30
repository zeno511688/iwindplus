/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.task.support;

import com.iwindplus.base.async.task.dal.repository.AsyncTaskRepository;
import com.iwindplus.base.async.task.dal.repository.AsyncTaskSubRepository;
import com.iwindplus.base.async.task.domain.constant.AsyncTaskConstant;
import com.iwindplus.base.async.task.domain.dto.AsyncTaskExtDTO;
import com.iwindplus.base.async.task.domain.dto.AsyncTaskStatusEditDTO;
import com.iwindplus.base.async.task.domain.enums.AsyncTaskStatusEnum;
import com.iwindplus.base.async.task.domain.property.AsyncTaskProperty;
import com.iwindplus.base.async.task.domain.vo.AsyncTaskSubVO;
import com.iwindplus.base.async.task.domain.vo.AsyncTaskVO;
import com.iwindplus.base.async.task.service.AsyncTaskService;
import com.iwindplus.base.async.task.service.AsyncTaskSubService;
import com.iwindplus.base.domain.constant.CommonConstant.NumberConstant;
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
 * 异步任务状态机.
 *
 * <p>分有无子任务两种</p>
 *
 * @author zengdegui
 * @since 2025/12/27 17:07
 */
@Slf4j
public record AsyncTaskStateSupport(
    AsyncTaskProperty property,
    AsyncTaskRepository asyncTaskRepository,
    AsyncTaskService asyncTaskService,
    AsyncTaskSubRepository asyncTaskSubRepository,
    AsyncTaskSubService asyncTaskSubService,
    TransactionTemplate transactionTemplate) {

    /**
     * 获取AsyncTaskSubRepository
     *
     * @return AsyncTaskSubRepository
     */
    public AsyncTaskSubRepository getAsyncTaskSubRepository() {
        return this.asyncTaskSubRepository;
    }

    /**
     * 任务执行中
     *
     * @param entity 任务对象
     * @return boolean
     */
    public boolean taskPendingToExecuting(AsyncTaskVO entity) {
        final long expireTime = this.asyncTaskRepository.getNextExpireTime(System.currentTimeMillis());

        return this.transition(
            () -> asyncTaskService.editStatusById(AsyncTaskStatusEditDTO.builder()
                .id(entity.getId())
                .from(AsyncTaskStatusEnum.PENDING)
                .to(AsyncTaskStatusEnum.EXECUTING)
                .expireTime(expireTime)
                .build()),
            () -> {
                this.syncStatus(entity, AsyncTaskStatusEnum.EXECUTING);
                entity.setExpireTime(expireTime);
            }
        );
    }

    /**
     * 执行中的任务恢复为待执行.
     *
     * @param entity 任务对象
     * @return boolean
     */
    public boolean taskExecutingToPending(AsyncTaskVO entity) {
        return this.transition(
            () -> asyncTaskService.editStatusById(AsyncTaskStatusEditDTO.builder()
                .id(entity.getId())
                .from(AsyncTaskStatusEnum.EXECUTING)
                .to(AsyncTaskStatusEnum.PENDING)
                .build()),
            () -> this.syncStatus(entity, AsyncTaskStatusEnum.PENDING)
        );
    }

    /**
     * 失败任务重新进入执行状态.
     *
     * @param entity 任务对象
     * @return boolean
     */
    public boolean taskFailedToExecute(AsyncTaskVO entity) {
        final long now = System.currentTimeMillis();
        final long expireTime = this.asyncTaskRepository.getNextExpireTime(now);

        return this.transition(
            () -> asyncTaskService.editStatusById(AsyncTaskStatusEditDTO.builder()
                .id(entity.getId())
                .from(AsyncTaskStatusEnum.FAILED)
                .to(AsyncTaskStatusEnum.EXECUTING)
                .expireTime(expireTime)
                .build()),
            () -> {
                this.syncStatus(entity, AsyncTaskStatusEnum.EXECUTING);
                entity.setExpireTime(expireTime);
            }
        );
    }

    /**
     * 失败任务达到最大重试次数后丢弃.
     *
     * @param entity 任务对象
     * @return boolean
     */
    public boolean taskDiscard(AsyncTaskVO entity) {
        return this.transition(
            () -> asyncTaskService.editStatusById(AsyncTaskStatusEditDTO.builder()
                .id(entity.getId())
                .from(AsyncTaskStatusEnum.FAILED)
                .to(AsyncTaskStatusEnum.DISCARD)
                .build()),
            () -> this.syncStatus(entity, AsyncTaskStatusEnum.DISCARD)
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
        AsyncTaskVO entity,
        AsyncTaskHandler handler,
        Long costTime) {

        final boolean result = this.transition(
            () -> asyncTaskService.editStatusById(AsyncTaskStatusEditDTO.builder()
                .id(entity.getId())
                .from(AsyncTaskStatusEnum.EXECUTING)
                .to(AsyncTaskStatusEnum.SUCCESS)
                .costTime(costTime)
                .progress(NumberConstant.NUMBER_ONE_HUNDRED)
                .result(entity.getResult())
                .build()),
            () -> {
                this.syncStatus(entity, AsyncTaskStatusEnum.SUCCESS);
                entity.setCostTime(costTime);
                entity.setProgress(NumberConstant.NUMBER_ONE_HUNDRED);
                // 是否删除数据
                final AsyncTaskExtDTO extData = entity.getExt();
                if (Objects.nonNull(extData) && Boolean.TRUE.equals(extData.getEnabledSuccessDelete())) {
                    asyncTaskService.removeById(entity.getId(), true);
                }
            }
        );

        if (result && Objects.nonNull(handler)) {
            this.safeCallback(() -> handler.onTaskSuccess(entity), AsyncTaskConstant.HOOK_ON_TASK_SUCCESS, entity.getId());
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
        AsyncTaskVO entity,
        AsyncTaskHandler handler,
        Long costTime,
        Exception ex,
        boolean advanced) {

        int retryCount = advanced ? 0 : Optional.ofNullable(entity.getRetryCount()).orElse(0) + 1;
        final String stack = this.getStack(ex);
        final long now = System.currentTimeMillis();
        final long nextRetryTime = DatesUtil.getNextRetryTime(now, this.property.getRetry().getFrequency(), retryCount);
        final long expireTime = this.asyncTaskRepository.getNextExpireTime(now);

        final boolean result = this.transition(
            () -> this.asyncTaskService.editStatusById(AsyncTaskStatusEditDTO.builder()
                .id(entity.getId())
                .from(AsyncTaskStatusEnum.EXECUTING)
                .to(AsyncTaskStatusEnum.FAILED)
                .costTime(costTime)
                .errorMsg(stack)
                .retryCount(retryCount)
                .nextRetryTime(nextRetryTime)
                .expireTime(expireTime)
                .build()),
            () -> {
                this.syncStatus(entity, AsyncTaskStatusEnum.FAILED);
                entity.setCostTime(costTime);
                entity.setErrorMsg(stack);
                entity.setRetryCount(retryCount);
                entity.setNextRetryTime(nextRetryTime);
                entity.setExpireTime(expireTime);
            }
        );

        if (result && Objects.nonNull(handler)) {
            this.safeCallback(() -> handler.onTaskFail(entity), AsyncTaskConstant.HOOK_ON_TASK_FAIL, entity.getId());
        }

        return result;
    }

    /**
     * 任务异步等待
     *
     * @param entity   对象
     * @param handler  任务助手
     * @param costTime 耗时
     * @return boolean
     */
    public boolean taskAsyncWait(
        AsyncTaskVO entity,
        AsyncTaskHandler handler,
        Long costTime) {

        final long now = System.currentTimeMillis();
        final Long nextRetryTime = this.getAsyncTaskSubRepository().getNextRetryTime(now);
        final Long expireTime = this.getAsyncTaskSubRepository().getNextExpireTime(now);

        final boolean result = this.transition(
            () -> asyncTaskService.editStatusById(AsyncTaskStatusEditDTO.builder()
                .id(entity.getId())
                .from(AsyncTaskStatusEnum.EXECUTING)
                .to(AsyncTaskStatusEnum.WAITING)
                .costTime(costTime)
                .nextRetryTime(nextRetryTime)
                .expireTime(expireTime)
                .build()),
            () -> {
                this.syncStatus(entity, AsyncTaskStatusEnum.WAITING);
                entity.setCostTime(costTime);
                entity.setNextRetryTime(nextRetryTime);
                entity.setExpireTime(expireTime);
            }
        );

        if (result && Objects.nonNull(handler)) {
            this.safeCallback(() -> handler.onTaskAsyncWait(entity), AsyncTaskConstant.HOOK_ON_TASK_ASYNC_WAIT, entity.getId());
        }

        return result;
    }

    /**
     * 更新主任务进度和耗时，保持执行中状态.
     *
     * @param entity   主任务对象
     * @param costTime 累计耗时
     * @param progress 进度百分比（0-100）
     * @return boolean
     */
    public boolean taskProgress(AsyncTaskVO entity, Long costTime, Integer progress) {
        final long expireTime = this.asyncTaskRepository.getNextExpireTime(System.currentTimeMillis());

        final boolean result = this.transition(
            () -> asyncTaskService.editStatusById(AsyncTaskStatusEditDTO.builder()
                .id(entity.getId())
                .from(AsyncTaskStatusEnum.EXECUTING)
                .to(AsyncTaskStatusEnum.EXECUTING)
                .costTime(costTime)
                .progress(progress)
                .expireTime(expireTime)
                .build()),
            () -> {
                entity.setCostTime(costTime);
                entity.setProgress(progress);
                entity.setExpireTime(expireTime);
            }
        );

        return result;
    }

    /**
     * 任务异步等待执行成功
     *
     * @param entity   对象
     * @param handler  任务助手
     * @param costTime 耗时
     * @return boolean
     */
    public boolean taskAsyncWaitSuccess(
        AsyncTaskVO entity,
        AsyncTaskHandler handler,
        Long costTime) {

        final boolean result = this.transition(
            () -> asyncTaskService.editStatusById(AsyncTaskStatusEditDTO.builder()
                .id(entity.getId())
                .from(AsyncTaskStatusEnum.WAITING)
                .to(AsyncTaskStatusEnum.SUCCESS)
                .costTime(costTime)
                .result(entity.getResult())
                .expireTime(0L)
                .build()),
            () -> {
                this.syncStatus(entity, AsyncTaskStatusEnum.SUCCESS);
                entity.setCostTime(costTime);
                entity.setExpireTime(0L);
            }
        );

        if (result && Objects.nonNull(handler)) {
            this.safeCallback(() -> handler.onTaskSuccess(entity), AsyncTaskConstant.HOOK_ON_TASK_SUCCESS, entity.getId());
        }

        return result;
    }

    /**
     * 任务异步等待执行失败
     *
     * @param entity  对象
     * @param handler 任务助手
     * @param ex      异常
     * @return boolean
     */
    public boolean taskAsyncWaitFail(
        AsyncTaskVO entity,
        AsyncTaskHandler handler,
        Long costTime,
        Exception ex) {

        final int retryCount = Optional.ofNullable(entity.getRetryCount()).orElse(0) + 1;
        final String stack = this.getStack(ex);
        final Long nextRetryTime = this.getAsyncTaskSubRepository().getNextRetryTime(System.currentTimeMillis());
        final long currentTimeMillis = System.currentTimeMillis();

        final boolean result = this.transition(
            () -> asyncTaskService.editStatusById(AsyncTaskStatusEditDTO.builder()
                .id(entity.getId())
                .from(AsyncTaskStatusEnum.WAITING)
                .to(AsyncTaskStatusEnum.FAILED)
                .costTime(costTime)
                .errorMsg(stack)
                .retryCount(retryCount)
                .nextRetryTime(nextRetryTime)
                .expireTime(currentTimeMillis)
                .build()),
            () -> {
                this.syncStatus(entity, AsyncTaskStatusEnum.FAILED);
                entity.setCostTime(costTime);
                entity.setErrorMsg(stack);
                entity.setRetryCount(retryCount);
                entity.setNextRetryTime(nextRetryTime);
                entity.setExpireTime(currentTimeMillis);
            }
        );

        if (result && Objects.nonNull(handler)) {
            this.safeCallback(() -> handler.onTaskFail(entity), AsyncTaskConstant.HOOK_ON_TASK_FAIL, entity.getId());
        }
        return result;
    }

    /**
     * 子任务执行成功
     *
     * @param entity   对象
     * @param handler  任务助手
     * @param costTime 耗时
     * @return boolean
     */
    public boolean subTaskSuccess(
        AsyncTaskSubVO entity,
        AsyncTaskSubHandler handler,
        Long costTime) {

        final boolean result = this.transition(
            () -> asyncTaskSubService.editStatusById(AsyncTaskStatusEditDTO.builder()
                .id(entity.getId())
                .from(AsyncTaskStatusEnum.EXECUTING)
                .to(AsyncTaskStatusEnum.SUCCESS)
                .costTime(costTime)
                .progress(NumberConstant.NUMBER_ONE_HUNDRED)
                .result(entity.getResult())
                .build()),
            () -> {
                this.syncStatus(entity, AsyncTaskStatusEnum.SUCCESS);
                entity.setCostTime(costTime);
                entity.setProgress(NumberConstant.NUMBER_ONE_HUNDRED);
            }
        );

        if (result && Objects.nonNull(handler)) {
            this.safeCallback(() -> handler.onSubTaskSuccess(entity), AsyncTaskConstant.HOOK_ON_SUB_TASK_SUCCESS, entity.getId());
        }
        return result;
    }

    /**
     * 子任务执行失败
     *
     * @param entity   对象
     * @param handler  任务助手
     * @param costTime 耗时
     * @param ex       异常
     * @return boolean
     */
    public boolean subTaskFail(
        AsyncTaskSubVO entity,
        AsyncTaskSubHandler handler,
        Long costTime,
        Exception ex) {

        final int retryCount = Optional.ofNullable(entity.getRetryCount()).orElse(0) + 1;
        final String stack = this.getStack(ex);

        final boolean result = this.transition(
            () -> this.asyncTaskSubService.editStatusById(AsyncTaskStatusEditDTO.builder()
                .id(entity.getId())
                .from(AsyncTaskStatusEnum.EXECUTING)
                .to(AsyncTaskStatusEnum.FAILED)
                .costTime(costTime)
                .errorMsg(stack)
                .retryCount(retryCount)
                .build()),
            () -> {
                this.syncStatus(entity, AsyncTaskStatusEnum.FAILED);
                entity.setCostTime(costTime);
                entity.setErrorMsg(stack);
                entity.setRetryCount(retryCount);
            }
        );

        if (result && Objects.nonNull(handler)) {
            this.safeCallback(() -> handler.onSubTaskFail(entity), AsyncTaskConstant.HOOK_ON_SUB_TASK_FAIL, entity.getId());
        }
        return result;
    }

    /**
     * 子任务异步等待
     *
     * @param entity   对象
     * @param handler  任务助手
     * @param costTime 耗时
     * @return boolean
     */
    public boolean subTaskAsyncWait(
        AsyncTaskSubVO entity,
        AsyncTaskSubHandler handler,
        Long costTime) {

        // 每次重新进入异步等待都重新计算截止时间，避免失败重试后沿用已过期的旧时间。
        final Long expireTime = this.getAsyncTaskSubRepository().getNextExpireTime(System.currentTimeMillis());

        final boolean result = this.transition(
            () -> asyncTaskSubService.editStatusById(AsyncTaskStatusEditDTO.builder()
                .id(entity.getId())
                .from(AsyncTaskStatusEnum.EXECUTING)
                .to(AsyncTaskStatusEnum.WAITING)
                .costTime(costTime)
                .result(entity.getResult())
                .expireTime(expireTime)
                .build()),
            () -> {
                this.syncStatus(entity, AsyncTaskStatusEnum.WAITING);
                entity.setCostTime(costTime);
                entity.setExpireTime(expireTime);
            }
        );

        if (result && Objects.nonNull(handler)) {
            this.safeCallback(() -> handler.onSubTaskAsyncWait(entity), AsyncTaskConstant.HOOK_ON_SUB_TASK_ASYNC_WAIT, entity.getId());
        }
        return result;
    }

    /**
     * 子任务异步等待执行成功
     *
     * @param entity   对象
     * @param handler  任务助手
     * @param costTime 耗时
     * @return boolean
     */
    public boolean subTaskAsyncWaitSuccess(
        AsyncTaskSubVO entity,
        AsyncTaskSubHandler handler,
        Long costTime) {

        final boolean result = this.transition(
            () -> asyncTaskSubService.editStatusById(AsyncTaskStatusEditDTO.builder()
                .id(entity.getId())
                .from(AsyncTaskStatusEnum.WAITING)
                .to(AsyncTaskStatusEnum.SUCCESS)
                .costTime(costTime)
                .result(entity.getResult())
                .expireTime(0L)
                .build()),
            () -> {
                this.syncStatus(entity, AsyncTaskStatusEnum.SUCCESS);
                entity.setCostTime(costTime);
                entity.setExpireTime(0L);
            }
        );

        if (result && Objects.nonNull(handler)) {
            this.safeCallback(() -> handler.onSubTaskSuccess(entity), AsyncTaskConstant.HOOK_ON_SUB_TASK_SUCCESS, entity.getId());
        }
        return result;
    }

    /**
     * 子任务异步等待执行失败
     *
     * @param entity   对象
     * @param handler  任务助手
     * @param ex       异常
     * @param costTime 耗时
     * @return boolean
     */
    public boolean subTaskAsyncWaitFail(
        AsyncTaskSubVO entity,
        AsyncTaskSubHandler handler,
        Long costTime,
        Exception ex) {

        final int retryCount = Optional.ofNullable(entity.getRetryCount()).orElse(0) + 1;
        final String stack = this.getStack(ex);

        final boolean result = this.transition(
            () -> asyncTaskSubService.editStatusById(AsyncTaskStatusEditDTO.builder()
                .id(entity.getId())
                .from(AsyncTaskStatusEnum.WAITING)
                .to(AsyncTaskStatusEnum.FAILED)
                .costTime(costTime)
                .errorMsg(stack)
                .retryCount(retryCount)
                .build()),
            () -> {
                this.syncStatus(entity, AsyncTaskStatusEnum.FAILED);
                entity.setCostTime(costTime);
                entity.setErrorMsg(stack);
                entity.setRetryCount(retryCount);
            }
        );

        if (result && Objects.nonNull(handler)) {
            this.safeCallback(() -> handler.onSubTaskFail(entity), AsyncTaskConstant.HOOK_ON_SUB_TASK_FAIL, entity.getId());
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
    private void syncStatus(AsyncTaskVO entity, AsyncTaskStatusEnum status) {
        entity.setModifiedTimestamp(System.currentTimeMillis());
        entity.setStatus(status);
    }

    /**
     * 同步公共状态字段。
     */
    private void syncStatus(AsyncTaskSubVO entity, AsyncTaskStatusEnum status) {
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
            log.error("asyncTask callback failed. callback={} id={}", name, id, ex);
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

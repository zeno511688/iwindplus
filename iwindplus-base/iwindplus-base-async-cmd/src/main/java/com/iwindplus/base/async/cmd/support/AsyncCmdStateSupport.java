/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.support;

import com.iwindplus.base.async.cmd.dal.repository.AsyncCmdRepository;
import com.iwindplus.base.async.cmd.dal.repository.AsyncCmdSubRepository;
import com.iwindplus.base.async.cmd.domain.constant.AsyncCmdConstant;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdExtDTO;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdStatusEditDTO;
import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdStatusEnum;
import com.iwindplus.base.async.cmd.domain.property.AsyncCmdProperty;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdSubVO;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdVO;
import com.iwindplus.base.async.cmd.service.AsyncCmdService;
import com.iwindplus.base.async.cmd.service.AsyncCmdSubService;
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
 * 异步命令状态机.
 *
 * <p>分有无子任务两种</p>
 *
 * @author zengdegui
 * @since 2025/12/27 17:07
 */
@Slf4j
public record AsyncCmdStateSupport(
    AsyncCmdProperty property,
    AsyncCmdRepository asyncCmdRepository,
    AsyncCmdService asyncCmdService,
    AsyncCmdSubRepository asyncCmdSubRepository,
    AsyncCmdSubService asyncCmdSubService,
    TransactionTemplate transactionTemplate) {

    /**
     * 获取AsyncCmdSubRepository
     *
     * @return AsyncCmdSubRepository
     */
    public AsyncCmdSubRepository getAsyncCmdSubRepository() {
        return this.asyncCmdSubRepository;
    }

    /**
     * 任务执行中
     *
     * @param entity 命令对象
     * @return boolean
     */
    public boolean taskToBeExecuteToExecute(AsyncCmdVO entity) {
        final long expireTime = this.asyncCmdRepository.getNextExpireTime(System.currentTimeMillis());

        return this.transition(
            () -> asyncCmdService.editStatusById(AsyncCmdStatusEditDTO.builder()
                .id(entity.getId())
                .from(AsyncCmdStatusEnum.TO_BE_EXECUTE)
                .to(AsyncCmdStatusEnum.EXECUTE)
                .expireTime(expireTime)
                .build()),
            () -> {
                this.syncStatus(entity, AsyncCmdStatusEnum.EXECUTE);
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
    public boolean taskExecuteToBeExecute(AsyncCmdVO entity) {
        return this.transition(
            () -> asyncCmdService.editStatusById(AsyncCmdStatusEditDTO.builder()
                .id(entity.getId())
                .from(AsyncCmdStatusEnum.EXECUTE)
                .to(AsyncCmdStatusEnum.TO_BE_EXECUTE)
                .build()),
            () -> this.syncStatus(entity, AsyncCmdStatusEnum.TO_BE_EXECUTE)
        );
    }

    /**
     * 失败任务重新进入执行状态.
     *
     * @param entity 命令对象
     * @return boolean
     */
    public boolean taskFailedToExecute(AsyncCmdVO entity) {
        final long now = System.currentTimeMillis();
        final long expireTime = this.asyncCmdRepository.getNextExpireTime(now);

        return this.transition(
            () -> asyncCmdService.editStatusById(AsyncCmdStatusEditDTO.builder()
                .id(entity.getId())
                .from(AsyncCmdStatusEnum.FAILED)
                .to(AsyncCmdStatusEnum.EXECUTE)
                .expireTime(expireTime)
                .build()),
            () -> {
                this.syncStatus(entity, AsyncCmdStatusEnum.EXECUTE);
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
    public boolean taskDiscard(AsyncCmdVO entity) {
        return this.transition(
            () -> asyncCmdService.editStatusById(AsyncCmdStatusEditDTO.builder()
                .id(entity.getId())
                .from(AsyncCmdStatusEnum.FAILED)
                .to(AsyncCmdStatusEnum.DISCARD)
                .build()),
            () -> this.syncStatus(entity, AsyncCmdStatusEnum.DISCARD)
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
        AsyncCmdVO entity,
        AsyncCmdTaskHandler handler,
        Long costTime) {

        final boolean result = this.transition(
            () -> asyncCmdService.editStatusById(AsyncCmdStatusEditDTO.builder()
                .id(entity.getId())
                .from(AsyncCmdStatusEnum.EXECUTE)
                .to(AsyncCmdStatusEnum.SUCCESS)
                .costTime(costTime)
                .progress(NumberConstant.NUMBER_ONE_HUNDRED)
                .result(entity.getResult())
                .build()),
            () -> {
                this.syncStatus(entity, AsyncCmdStatusEnum.SUCCESS);
                entity.setCostTime(costTime);
                entity.setProgress(NumberConstant.NUMBER_ONE_HUNDRED);
                // 是否删除数据
                final AsyncCmdExtDTO extData = entity.getExt();
                if (Objects.nonNull(extData) && Boolean.TRUE.equals(extData.getEnabledSuccessDelete())) {
                    asyncCmdService.removeById(entity.getId(), true);
                }
            }
        );

        if (result && Objects.nonNull(handler)) {
            this.safeCallback(() -> handler.onTaskSuccess(entity), AsyncCmdConstant.HOOK_ON_TASK_SUCCESS, entity.getId());
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
        AsyncCmdVO entity,
        AsyncCmdTaskHandler handler,
        Long costTime,
        Exception ex,
        boolean advanced) {

        int retryCount = advanced ? 0 : Optional.ofNullable(entity.getRetryCount()).orElse(0) + 1;
        final String stack = this.getStack(ex);
        final long now = System.currentTimeMillis();
        final long nextRetryTime = DatesUtil.getNextRetryTime(now, this.property.getRetry().getFrequency(), retryCount);
        final long expireTime = this.asyncCmdRepository.getNextExpireTime(now);

        final boolean result = this.transition(
            () -> this.asyncCmdService.editStatusById(AsyncCmdStatusEditDTO.builder()
                .id(entity.getId())
                .from(AsyncCmdStatusEnum.EXECUTE)
                .to(AsyncCmdStatusEnum.FAILED)
                .costTime(costTime)
                .errorMsg(stack)
                .retryCount(retryCount)
                .nextRetryTime(nextRetryTime)
                .expireTime(expireTime)
                .build()),
            () -> {
                this.syncStatus(entity, AsyncCmdStatusEnum.FAILED);
                entity.setCostTime(costTime);
                entity.setErrorMsg(stack);
                entity.setRetryCount(retryCount);
                entity.setNextRetryTime(nextRetryTime);
                entity.setExpireTime(expireTime);
            }
        );

        if (result && Objects.nonNull(handler)) {
            this.safeCallback(() -> handler.onTaskFail(entity), AsyncCmdConstant.HOOK_ON_TASK_FAIL, entity.getId());
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
        AsyncCmdVO entity,
        AsyncCmdTaskHandler handler,
        Long costTime) {

        final long now = System.currentTimeMillis();
        final Long nextRetryTime = this.getAsyncCmdSubRepository().getNextRetryTime(now);
        final Long expireTime = this.getAsyncCmdSubRepository().getNextExpireTime(now);

        final boolean result = this.transition(
            () -> asyncCmdService.editStatusById(AsyncCmdStatusEditDTO.builder()
                .id(entity.getId())
                .from(AsyncCmdStatusEnum.EXECUTE)
                .to(AsyncCmdStatusEnum.WAITING)
                .costTime(costTime)
                .nextRetryTime(nextRetryTime)
                .expireTime(expireTime)
                .build()),
            () -> {
                this.syncStatus(entity, AsyncCmdStatusEnum.WAITING);
                entity.setCostTime(costTime);
                entity.setNextRetryTime(nextRetryTime);
                entity.setExpireTime(expireTime);
            }
        );

        if (result && Objects.nonNull(handler)) {
            this.safeCallback(() -> handler.onTaskAsyncWait(entity), AsyncCmdConstant.HOOK_ON_TASK_ASYNC_WAIT, entity.getId());
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
    public boolean taskProgress(AsyncCmdVO entity, Long costTime, Integer progress) {
        final long expireTime = this.asyncCmdRepository.getNextExpireTime(System.currentTimeMillis());

        final boolean result = this.transition(
            () -> asyncCmdService.editStatusById(AsyncCmdStatusEditDTO.builder()
                .id(entity.getId())
                .from(AsyncCmdStatusEnum.EXECUTE)
                .to(AsyncCmdStatusEnum.EXECUTE)
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
        AsyncCmdVO entity,
        AsyncCmdTaskHandler handler,
        Long costTime) {

        final boolean result = this.transition(
            () -> asyncCmdService.editStatusById(AsyncCmdStatusEditDTO.builder()
                .id(entity.getId())
                .from(AsyncCmdStatusEnum.WAITING)
                .to(AsyncCmdStatusEnum.SUCCESS)
                .costTime(costTime)
                .result(entity.getResult())
                .expireTime(0L)
                .build()),
            () -> {
                this.syncStatus(entity, AsyncCmdStatusEnum.SUCCESS);
                entity.setCostTime(costTime);
                entity.setExpireTime(0L);
            }
        );

        if (result && Objects.nonNull(handler)) {
            this.safeCallback(() -> handler.onTaskSuccess(entity), AsyncCmdConstant.HOOK_ON_TASK_SUCCESS, entity.getId());
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
        AsyncCmdVO entity,
        AsyncCmdTaskHandler handler,
        Long costTime,
        Exception ex) {

        final int retryCount = Optional.ofNullable(entity.getRetryCount()).orElse(0) + 1;
        final String stack = this.getStack(ex);
        final Long nextRetryTime = this.getAsyncCmdSubRepository().getNextRetryTime(System.currentTimeMillis());
        final long currentTimeMillis = System.currentTimeMillis();

        final boolean result = this.transition(
            () -> asyncCmdService.editStatusById(AsyncCmdStatusEditDTO.builder()
                .id(entity.getId())
                .from(AsyncCmdStatusEnum.WAITING)
                .to(AsyncCmdStatusEnum.FAILED)
                .costTime(costTime)
                .errorMsg(stack)
                .retryCount(retryCount)
                .nextRetryTime(nextRetryTime)
                .expireTime(currentTimeMillis)
                .build()),
            () -> {
                this.syncStatus(entity, AsyncCmdStatusEnum.FAILED);
                entity.setCostTime(costTime);
                entity.setErrorMsg(stack);
                entity.setRetryCount(retryCount);
                entity.setNextRetryTime(nextRetryTime);
                entity.setExpireTime(currentTimeMillis);
            }
        );

        if (result && Objects.nonNull(handler)) {
            this.safeCallback(() -> handler.onTaskFail(entity), AsyncCmdConstant.HOOK_ON_TASK_FAIL, entity.getId());
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
        AsyncCmdSubVO entity,
        AsyncCmdSubTaskHandler handler,
        Long costTime) {

        final boolean result = this.transition(
            () -> asyncCmdSubService.editStatusById(AsyncCmdStatusEditDTO.builder()
                .id(entity.getId())
                .from(AsyncCmdStatusEnum.EXECUTE)
                .to(AsyncCmdStatusEnum.SUCCESS)
                .costTime(costTime)
                .progress(NumberConstant.NUMBER_ONE_HUNDRED)
                .result(entity.getResult())
                .build()),
            () -> {
                this.syncStatus(entity, AsyncCmdStatusEnum.SUCCESS);
                entity.setCostTime(costTime);
                entity.setProgress(NumberConstant.NUMBER_ONE_HUNDRED);
            }
        );

        if (result && Objects.nonNull(handler)) {
            this.safeCallback(() -> handler.onSubTaskSuccess(entity), AsyncCmdConstant.HOOK_ON_SUB_TASK_SUCCESS, entity.getId());
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
        AsyncCmdSubVO entity,
        AsyncCmdSubTaskHandler handler,
        Long costTime,
        Exception ex) {

        final int retryCount = Optional.ofNullable(entity.getRetryCount()).orElse(0) + 1;
        final String stack = this.getStack(ex);

        final boolean result = this.transition(
            () -> this.asyncCmdSubService.editStatusById(AsyncCmdStatusEditDTO.builder()
                .id(entity.getId())
                .from(AsyncCmdStatusEnum.EXECUTE)
                .to(AsyncCmdStatusEnum.FAILED)
                .costTime(costTime)
                .errorMsg(stack)
                .retryCount(retryCount)
                .build()),
            () -> {
                this.syncStatus(entity, AsyncCmdStatusEnum.FAILED);
                entity.setCostTime(costTime);
                entity.setErrorMsg(stack);
                entity.setRetryCount(retryCount);
            }
        );

        if (result && Objects.nonNull(handler)) {
            this.safeCallback(() -> handler.onSubTaskFail(entity), AsyncCmdConstant.HOOK_ON_SUB_TASK_FAIL, entity.getId());
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
        AsyncCmdSubVO entity,
        AsyncCmdSubTaskHandler handler,
        Long costTime) {

        // 每次重新进入异步等待都重新计算截止时间，避免失败重试后沿用已过期的旧时间。
        final Long expireTime = this.getAsyncCmdSubRepository().getNextExpireTime(System.currentTimeMillis());

        final boolean result = this.transition(
            () -> asyncCmdSubService.editStatusById(AsyncCmdStatusEditDTO.builder()
                .id(entity.getId())
                .from(AsyncCmdStatusEnum.EXECUTE)
                .to(AsyncCmdStatusEnum.WAITING)
                .costTime(costTime)
                .result(entity.getResult())
                .expireTime(expireTime)
                .build()),
            () -> {
                this.syncStatus(entity, AsyncCmdStatusEnum.WAITING);
                entity.setCostTime(costTime);
                entity.setExpireTime(expireTime);
            }
        );

        if (result && Objects.nonNull(handler)) {
            this.safeCallback(() -> handler.onSubTaskAsyncWait(entity), AsyncCmdConstant.HOOK_ON_SUB_TASK_ASYNC_WAIT, entity.getId());
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
        AsyncCmdSubVO entity,
        AsyncCmdSubTaskHandler handler,
        Long costTime) {

        final boolean result = this.transition(
            () -> asyncCmdSubService.editStatusById(AsyncCmdStatusEditDTO.builder()
                .id(entity.getId())
                .from(AsyncCmdStatusEnum.WAITING)
                .to(AsyncCmdStatusEnum.SUCCESS)
                .costTime(costTime)
                .result(entity.getResult())
                .expireTime(0L)
                .build()),
            () -> {
                this.syncStatus(entity, AsyncCmdStatusEnum.SUCCESS);
                entity.setCostTime(costTime);
                entity.setExpireTime(0L);
            }
        );

        if (result && Objects.nonNull(handler)) {
            this.safeCallback(() -> handler.onSubTaskSuccess(entity), AsyncCmdConstant.HOOK_ON_SUB_TASK_SUCCESS, entity.getId());
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
        AsyncCmdSubVO entity,
        AsyncCmdSubTaskHandler handler,
        Long costTime,
        Exception ex) {

        final int retryCount = Optional.ofNullable(entity.getRetryCount()).orElse(0) + 1;
        final String stack = this.getStack(ex);

        final boolean result = this.transition(
            () -> asyncCmdSubService.editStatusById(AsyncCmdStatusEditDTO.builder()
                .id(entity.getId())
                .from(AsyncCmdStatusEnum.WAITING)
                .to(AsyncCmdStatusEnum.FAILED)
                .costTime(costTime)
                .errorMsg(stack)
                .retryCount(retryCount)
                .build()),
            () -> {
                this.syncStatus(entity, AsyncCmdStatusEnum.FAILED);
                entity.setCostTime(costTime);
                entity.setErrorMsg(stack);
                entity.setRetryCount(retryCount);
            }
        );

        if (result && Objects.nonNull(handler)) {
            this.safeCallback(() -> handler.onSubTaskFail(entity), AsyncCmdConstant.HOOK_ON_SUB_TASK_FAIL, entity.getId());
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
    private void syncStatus(AsyncCmdVO entity, AsyncCmdStatusEnum status) {
        entity.setModifiedTimestamp(System.currentTimeMillis());
        entity.setStatus(status);
    }

    /**
     * 同步公共状态字段。
     */
    private void syncStatus(AsyncCmdSubVO entity, AsyncCmdStatusEnum status) {
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
            log.error("asyncCmd callback failed. callback={} id={}", name, id, ex);
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

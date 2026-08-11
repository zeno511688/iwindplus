/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.support;

import com.iwindplus.base.async.cmd.dal.repository.AsyncCmdRepository;
import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdStatusEnum;
import com.iwindplus.base.async.cmd.domain.property.AsyncCmdProperty;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdSubVO;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdVO;
import com.iwindplus.base.async.cmd.service.AsyncCmdService;
import com.iwindplus.base.async.cmd.service.AsyncCmdSubService;
import com.iwindplus.base.domain.constant.CommonConstant.NumberConstant;
import com.iwindplus.base.util.DatesUtil;
import java.util.Objects;
import java.util.Optional;
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
    AsyncCmdSubService asyncCmdSubService,
    TransactionTemplate transactionTemplate) {

    /**
     * 抢占执行权
     *
     * @param entity 命令对象
     * @return boolean
     */
    public boolean editLockById(AsyncCmdVO entity) {
        return Boolean.TRUE.equals(
            this.transactionTemplate.execute(status ->
                asyncCmdService.editStatusById(
                    entity.getId(),
                    AsyncCmdStatusEnum.TO_BE_EXECUTE,
                    AsyncCmdStatusEnum.EXECUTE,
                    null,
                    null,
                    null,
                    null,
                    this.asyncCmdRepository.getNextExpireTime(),
                    null
                )
            )
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

        final boolean result = Boolean.TRUE.equals(
            this.transactionTemplate.execute(status -> {
                boolean updated = asyncCmdService.editStatusById(
                    entity.getId(),
                    AsyncCmdStatusEnum.EXECUTE,
                    AsyncCmdStatusEnum.SUCCESS,
                    costTime,
                    null,
                    null,
                    null,
                    null,
                    null);
                if (updated) {
                    // 删除记录（可选）
                    if (Boolean.TRUE.equals(this.property.getEnabledSuccessDelete())) {
                        asyncCmdService.removeById(entity.getId(), this.property.getEnabledSuccessRealDelete());
                    }
                }
                return updated;
            })
        );

        if (!result) {
            return false;
        }
        entity.setStatus(AsyncCmdStatusEnum.SUCCESS);
        entity.setCostTime(costTime);
        if (Objects.isNull(handler)) {
            return true;
        }
        this.safeCallback(() -> handler.onTaskSuccess(entity), "onTaskSuccess", entity.getId());
        return true;
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

        final long currentTimeMillis = System.currentTimeMillis();
        int retryCount = advanced ? 0 : Optional.ofNullable(entity.getRetryCount()).orElse(0) + 1;
        final String stack = this.getStack(ex);
        final long nextRetryTime = DatesUtil.getNextRetryTime(currentTimeMillis, this.property.getRetry().getFrequency(), retryCount);

        final boolean result = Boolean.TRUE.equals(
            this.transactionTemplate.execute(status ->
                // 任务失败时，释放执行租约，任务成功时，不释放执行租约，任务执行中时，不释放执行租约
                this.asyncCmdService.editStatusById(
                    entity.getId(),
                    AsyncCmdStatusEnum.EXECUTE,
                    AsyncCmdStatusEnum.FAILED,
                    costTime,
                    stack,
                    retryCount,
                    nextRetryTime,
                    currentTimeMillis,
                    null
                )
            )
        );

        if (!result) {
            return false;
        }
        entity.setStatus(AsyncCmdStatusEnum.FAILED);
        entity.setRetryCount(retryCount);
        entity.setNextRetryTime(nextRetryTime);
        entity.setErrorMsg(stack);
        entity.setCostTime(costTime);
        if (Objects.isNull(handler)) {
            return true;
        }
        this.safeCallback(() -> handler.onTaskFail(entity), "onTaskFail", entity.getId());
        return true;
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

        final Long callbackExpireTime = this.getCallbackExpireTime();
        final Long nextRetryTime = this.getNextRetryTime();

        final boolean result = Boolean.TRUE.equals(
            this.transactionTemplate.execute(status ->
                asyncCmdService.editStatusById(
                    entity.getId(),
                    AsyncCmdStatusEnum.EXECUTE,
                    AsyncCmdStatusEnum.ASYNC_WAIT,
                    costTime,
                    null,
                    null,
                    nextRetryTime,
                    null,
                    callbackExpireTime
                )
            )
        );

        if (!result) {
            return false;
        }

        entity.setStatus(AsyncCmdStatusEnum.ASYNC_WAIT);
        entity.setCostTime(costTime);
        entity.setCallbackExpireTime(callbackExpireTime);
        entity.setNextRetryTime(nextRetryTime);
        if (Objects.isNull(handler)) {
            return true;
        }

        this.safeCallback(() -> handler.onTaskAsyncWait(entity), "onTaskAsyncWait", entity.getId());
        return true;
    }

    /**
     * 子任务中主任务状态转为待执行，等待下一次执行
     *
     * @param entity   对象
     * @param costTime 耗时
     * @return
     */
    public boolean taskExecuteToBeExecute(AsyncCmdVO entity, Long costTime) {
        final Long nextRetryTime = this.getNextRetryTime();

        final boolean result = Boolean.TRUE.equals(
            this.transactionTemplate.execute(status ->
                asyncCmdService.editStatusById(
                    entity.getId(),
                    AsyncCmdStatusEnum.EXECUTE,
                    AsyncCmdStatusEnum.TO_BE_EXECUTE,
                    costTime,
                    null,
                    null,
                    nextRetryTime,
                    null,
                    null
                )
            )
        );

        if (!result) {
            return false;
        }

        entity.setStatus(AsyncCmdStatusEnum.TO_BE_EXECUTE);
        entity.setNextRetryTime(nextRetryTime);
        entity.setCostTime(costTime);
        return true;
    }

    /**
     * 任务异步等待回调成功后转为待执行（callbackFirst模式，回调成功后分发子任务）.
     *
     * @param entity   对象
     * @param costTime 耗时
     * @return boolean
     */
    public boolean taskAsyncWaitToBeExecute(AsyncCmdVO entity, Long costTime) {
        final Long nextRetryTime = this.getNextRetryTime();

        final boolean result = Boolean.TRUE.equals(
            this.transactionTemplate.execute(status ->
                asyncCmdService.editStatusById(
                    entity.getId(),
                    AsyncCmdStatusEnum.ASYNC_WAIT,
                    AsyncCmdStatusEnum.TO_BE_EXECUTE,
                    costTime,
                    null,
                    null,
                    nextRetryTime,
                    null,
                    0L
                )
            )
        );

        if (!result) {
            return false;
        }

        entity.setStatus(AsyncCmdStatusEnum.TO_BE_EXECUTE);
        entity.setCallbackExpireTime(0L);
        entity.setNextRetryTime(nextRetryTime);
        entity.setCostTime(costTime);
        return true;
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
        final boolean result = Boolean.TRUE.equals(
            this.transactionTemplate.execute(status ->
                asyncCmdService.editStatusById(
                    entity.getId(),
                    AsyncCmdStatusEnum.ASYNC_WAIT,
                    AsyncCmdStatusEnum.SUCCESS,
                    costTime,
                    null,
                    null,
                    null,
                    null,
                    0L
                )
            )
        );

        if (!result) {
            return false;
        }

        entity.setStatus(AsyncCmdStatusEnum.SUCCESS);
        entity.setCallbackExpireTime(0L);
        if (Objects.isNull(handler)) {
            return true;
        }

        this.safeCallback(() -> handler.onTaskSuccess(entity), "onTaskSuccess", entity.getId());
        return true;
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
        final Long nextRetryTime = this.getNextRetryTime();

        final boolean result = Boolean.TRUE.equals(
            this.transactionTemplate.execute(status ->
                asyncCmdService.editStatusById(
                    entity.getId(),
                    AsyncCmdStatusEnum.ASYNC_WAIT,
                    AsyncCmdStatusEnum.FAILED,
                    costTime,
                    stack,
                    retryCount,
                    nextRetryTime,
                    null,
                    null
                )
            )
        );

        if (!result) {
            return false;
        }

        entity.setStatus(AsyncCmdStatusEnum.FAILED);
        entity.setRetryCount(retryCount);
        entity.setErrorMsg(stack);
        if (Objects.isNull(handler)) {
            return true;
        }

        this.safeCallback(() -> handler.onTaskFail(entity), "onTaskFail", entity.getId());
        return true;
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
        final boolean result = Boolean.TRUE.equals(
            this.transactionTemplate.execute(status ->
                asyncCmdSubService.editStatusById(
                    entity.getId(),
                    AsyncCmdStatusEnum.EXECUTE,
                    AsyncCmdStatusEnum.SUCCESS,
                    costTime,
                    null,
                    null,
                    entity.getResult(),
                    null

                )
            )
        );

        if (!result) {
            return false;
        }

        entity.setStatus(AsyncCmdStatusEnum.SUCCESS);
        entity.setCostTime(costTime);
        if (Objects.isNull(handler)) {
            return true;
        }
        this.safeCallback(() -> handler.onSubTaskSuccess(entity), "onSubTaskSuccess", entity.getId());
        return true;
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

        final boolean result = Boolean.TRUE.equals(
            this.transactionTemplate.execute(status ->
                this.asyncCmdSubService.editStatusById(
                    entity.getId(),
                    AsyncCmdStatusEnum.EXECUTE,
                    AsyncCmdStatusEnum.FAILED,
                    costTime,
                    stack,
                    retryCount,
                    null,
                    null
                )
            )
        );

        if (!result) {
            return false;
        }

        entity.setStatus(AsyncCmdStatusEnum.FAILED);
        entity.setRetryCount(retryCount);
        entity.setErrorMsg(stack);
        entity.setCostTime(costTime);
        if (Objects.isNull(handler)) {
            return true;
        }
        this.safeCallback(() -> handler.onSubTaskFail(entity), "onSubTaskFail", entity.getId());
        return true;
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
        final Long callbackExpireTime = this.getCallbackExpireTime();

        final boolean result = Boolean.TRUE.equals(
            this.transactionTemplate.execute(status ->
                asyncCmdSubService.editStatusById(
                    entity.getId(),
                    AsyncCmdStatusEnum.EXECUTE,
                    AsyncCmdStatusEnum.ASYNC_WAIT,
                    costTime,
                    null,
                    null,
                    entity.getResult(),
                    callbackExpireTime
                )
            )
        );

        if (!result) {
            return false;
        }

        entity.setStatus(AsyncCmdStatusEnum.ASYNC_WAIT);
        entity.setCostTime(costTime);
        entity.setCallbackExpireTime(callbackExpireTime);
        if (Objects.isNull(handler)) {
            return true;
        }

        this.safeCallback(() -> handler.onSubTaskAsyncWait(entity), "onSubTaskAsyncWait", entity.getId());
        return true;
    }

    /**
     * 子任务异步等待执行成功
     *
     * @param entity  对象
     * @param handler 任务助手
     * @return boolean
     */
    public boolean subTaskAsyncWaitSuccess(
        AsyncCmdSubVO entity,
        AsyncCmdSubTaskHandler handler) {

        final boolean result = Boolean.TRUE.equals(
            this.transactionTemplate.execute(status ->
                asyncCmdSubService.editStatusById(
                    entity.getId(),
                    AsyncCmdStatusEnum.ASYNC_WAIT,
                    AsyncCmdStatusEnum.SUCCESS,
                    null,
                    null,
                    null,
                    entity.getResult(),
                    null
                )
            )
        );

        if (!result) {
            return false;
        }

        entity.setStatus(AsyncCmdStatusEnum.SUCCESS);
        if (Objects.isNull(handler)) {
            return true;
        }

        this.safeCallback(() -> handler.onSubTaskSuccess(entity), "onSubTaskSuccess", entity.getId());
        return true;
    }

    /**
     * 子任务异步等待执行失败
     *
     * @param entity  对象
     * @param handler 任务助手
     * @param ex      异常
     * @return boolean
     */
    public boolean subTaskAsyncWaitFail(
        AsyncCmdSubVO entity,
        AsyncCmdSubTaskHandler handler,
        Exception ex) {

        final int retryCount = Optional.ofNullable(entity.getRetryCount()).orElse(0) + 1;
        final String stack = this.getStack(ex);

        final boolean result = Boolean.TRUE.equals(
            this.transactionTemplate.execute(status ->
                asyncCmdSubService.editStatusById(
                    entity.getId(),
                    AsyncCmdStatusEnum.ASYNC_WAIT,
                    AsyncCmdStatusEnum.FAILED,
                    null,
                    stack,
                    retryCount,
                    null,
                    null
                )
            )
        );

        if (!result) {
            return false;
        }

        entity.setStatus(AsyncCmdStatusEnum.FAILED);
        entity.setRetryCount(retryCount);
        entity.setErrorMsg(stack);
        if (Objects.isNull(handler)) {
            return true;
        }

        this.safeCallback(() -> handler.onSubTaskFail(entity), "onSubTaskFail", entity.getId());
        return true;
    }

    /**
     * 获取回调过期时间
     *
     * @return long
     */
    public Long getCallbackExpireTime() {
        return System.currentTimeMillis()
            + Optional.ofNullable(this.property.getAsyncWaitTimeoutSeconds()).orElse(1800L) * NumberConstant.NUMBER_ONE_THOUSAND;
    }

    /**
     * 获取下一次重试时间
     *
     * @return long
     */
    public Long getNextRetryTime() {
        return System.currentTimeMillis()
            + Optional.ofNullable(this.property.getAsyncWaitPoolSeconds()).orElse(60L) * NumberConstant.NUMBER_ONE_THOUSAND;
    }

    /**
     * 安全回调
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

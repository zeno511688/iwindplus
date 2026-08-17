/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.support;

import com.iwindplus.base.async.cmd.dal.repository.AsyncCmdRepository;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdStatusEditDTO;
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
     * 任务执行中
     *
     * @param entity 命令对象
     * @return boolean
     */
    public boolean taskToBeExecuteToExecute(AsyncCmdVO entity) {
        final long expireTime = this.asyncCmdRepository.getNextExpireTime(System.currentTimeMillis());

        return Boolean.TRUE.equals(
            this.transactionTemplate.execute(status -> {
                boolean updated = asyncCmdService.editStatusById(AsyncCmdStatusEditDTO.builder()
                    .id(entity.getId())
                    .from(AsyncCmdStatusEnum.TO_BE_EXECUTE)
                    .to(AsyncCmdStatusEnum.EXECUTE)
                    .expireTime(expireTime)
                    .build());
                if (updated) {
                    entity.setModifiedTimestamp(System.currentTimeMillis());
                    entity.setStatus(AsyncCmdStatusEnum.EXECUTE);
                    entity.setExpireTime(expireTime);
                }
                return updated;
            })
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
                boolean updated = asyncCmdService.editStatusById(AsyncCmdStatusEditDTO.builder()
                    .id(entity.getId())
                    .from(AsyncCmdStatusEnum.EXECUTE)
                    .to(AsyncCmdStatusEnum.SUCCESS)
                    .costTime(costTime)
                    .result(entity.getResult())
                    .build());
                if (updated) {
                    // 关键钩子纳入事务：失败回滚状态流转并外抛，由上层失败链路接管
                    entity.setModifiedTimestamp(System.currentTimeMillis());
                    entity.setStatus(AsyncCmdStatusEnum.SUCCESS);
                    entity.setCostTime(costTime);
                    if (Objects.nonNull(handler)) {
                        handler.onTaskSuccess(entity);
                    }

                    // 删除记录（可选）
                    if (Boolean.TRUE.equals(this.property.getEnabledSuccessDelete())) {
                        asyncCmdService.removeById(entity.getId(), this.property.getEnabledSuccessRealDelete());
                    }
                }
                return updated;
            })
        );

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

        final boolean result = Boolean.TRUE.equals(
            this.transactionTemplate.execute(status -> {
                // 任务失败时，释放执行租约，任务成功时，不释放执行租约，任务执行中时，不释放执行租约
                boolean updated = this.asyncCmdService.editStatusById(AsyncCmdStatusEditDTO.builder()
                    .id(entity.getId())
                    .from(AsyncCmdStatusEnum.EXECUTE)
                    .to(AsyncCmdStatusEnum.FAILED)
                    .costTime(costTime)
                    .errorMsg(stack)
                    .retryCount(retryCount)
                    .nextRetryTime(nextRetryTime)
                    .expireTime(expireTime)
                    .build());
                if (updated) {
                    entity.setModifiedTimestamp(System.currentTimeMillis());
                    entity.setStatus(AsyncCmdStatusEnum.FAILED);
                    entity.setRetryCount(retryCount);
                    entity.setNextRetryTime(nextRetryTime);
                    entity.setErrorMsg(stack);
                    entity.setCostTime(costTime);
                }
                return updated;
            })
        );

        if (result && Objects.nonNull(handler)) {
            this.safeCallback(() -> handler.onTaskFail(entity), "onTaskFail", entity.getId());
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
        final Long nextRetryTime = this.getNextRetryTime(now);
        final Long expireTime = this.getNextExpireTime(now);

        final boolean result = Boolean.TRUE.equals(
            this.transactionTemplate.execute(status -> {
                boolean updated = asyncCmdService.editStatusById(AsyncCmdStatusEditDTO.builder()
                    .id(entity.getId())
                    .from(AsyncCmdStatusEnum.EXECUTE)
                    .to(AsyncCmdStatusEnum.WAITING)
                    .costTime(costTime)
                    .nextRetryTime(nextRetryTime)
                    .expireTime(expireTime)
                    .build());
                if (updated) {
                    // 关键钩子纳入事务：失败回滚状态流转并外抛，由上层失败链路接管
                    entity.setModifiedTimestamp(System.currentTimeMillis());
                    entity.setStatus(AsyncCmdStatusEnum.WAITING);
                    entity.setCostTime(costTime);
                    entity.setNextRetryTime(nextRetryTime);
                    entity.setExpireTime(expireTime);
                    if (Objects.nonNull(handler)) {
                        handler.onTaskAsyncWait(entity);
                    }
                }
                return updated;
            })
        );

        return result;
    }

    /**
     * 子任务中主任务状态转为待执行，等待下一次执行
     *
     * @param entity   对象
     * @param costTime 耗时
     * @return
     */
    public boolean taskExecuteToBeExecute(AsyncCmdVO entity, Long costTime) {
        final Long nextRetryTime = this.getNextRetryTime(System.currentTimeMillis());

        final boolean result = Boolean.TRUE.equals(
            this.transactionTemplate.execute(status -> {
                boolean updated = asyncCmdService.editStatusById(AsyncCmdStatusEditDTO.builder()
                    .id(entity.getId())
                    .from(AsyncCmdStatusEnum.EXECUTE)
                    .to(AsyncCmdStatusEnum.TO_BE_EXECUTE)
                    .costTime(costTime)
                    .nextRetryTime(nextRetryTime)
                    .build());
                if (updated) {
                    entity.setModifiedTimestamp(System.currentTimeMillis());
                    entity.setStatus(AsyncCmdStatusEnum.TO_BE_EXECUTE);
                    entity.setNextRetryTime(nextRetryTime);
                    entity.setCostTime(costTime);
                }
                return updated;
            })
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
        final boolean result = Boolean.TRUE.equals(
            this.transactionTemplate.execute(status -> {
                boolean updated = asyncCmdService.editStatusById(AsyncCmdStatusEditDTO.builder()
                    .id(entity.getId())
                    .from(AsyncCmdStatusEnum.WAITING)
                    .to(AsyncCmdStatusEnum.SUCCESS)
                    .costTime(costTime)
                    .result(entity.getResult())
                    .expireTime(0L)
                    .build());
                if (updated) {
                    // 关键钩子纳入事务：失败回滚状态流转并外抛，等待下次轮询重试
                    entity.setModifiedTimestamp(System.currentTimeMillis());
                    entity.setStatus(AsyncCmdStatusEnum.SUCCESS);
                    entity.setExpireTime(0L);
                    if (Objects.nonNull(handler)) {
                        handler.onTaskSuccess(entity);
                    }
                }
                return updated;
            })
        );

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
        final Long nextRetryTime = this.getNextRetryTime(System.currentTimeMillis());
        final long currentTimeMillis = System.currentTimeMillis();

        final boolean result = Boolean.TRUE.equals(
            this.transactionTemplate.execute(status -> {
                boolean updated = asyncCmdService.editStatusById(AsyncCmdStatusEditDTO.builder()
                    .id(entity.getId())
                    .from(AsyncCmdStatusEnum.WAITING)
                    .to(AsyncCmdStatusEnum.FAILED)
                    .costTime(costTime)
                    .errorMsg(stack)
                    .retryCount(retryCount)
                    .nextRetryTime(nextRetryTime)
                    // 释放回调等待截止时间，便于RESET_JOB及时重置
                    .expireTime(currentTimeMillis)
                    .build());
                if (updated) {
                    entity.setModifiedTimestamp(System.currentTimeMillis());
                    entity.setStatus(AsyncCmdStatusEnum.FAILED);
                    entity.setRetryCount(retryCount);
                    entity.setErrorMsg(stack);
                    entity.setExpireTime(currentTimeMillis);
                }
                return updated;
            })
        );

        if (result && Objects.nonNull(handler)) {
            this.safeCallback(() -> handler.onTaskFail(entity), "onTaskFail", entity.getId());
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
        final boolean result = Boolean.TRUE.equals(
            this.transactionTemplate.execute(status -> {
                boolean updated = asyncCmdSubService.editStatusById(AsyncCmdStatusEditDTO.builder()
                    .id(entity.getId())
                    .from(AsyncCmdStatusEnum.EXECUTE)
                    .to(AsyncCmdStatusEnum.SUCCESS)
                    .costTime(costTime)
                    .result(entity.getResult())
                    .build()
                );
                if (updated) {
                    // 关键钩子纳入事务：失败回滚状态流转并外抛，由上层失败链路接管
                    entity.setModifiedTimestamp(System.currentTimeMillis());
                    entity.setStatus(AsyncCmdStatusEnum.SUCCESS);
                    entity.setCostTime(costTime);
                    if (Objects.nonNull(handler)) {
                        handler.onSubTaskSuccess(entity);
                    }
                }
                return updated;
            })
        );

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

        final boolean result = Boolean.TRUE.equals(
            this.transactionTemplate.execute(status -> {
                boolean updated = this.asyncCmdSubService.editStatusById(AsyncCmdStatusEditDTO.builder()
                    .id(entity.getId())
                    .from(AsyncCmdStatusEnum.EXECUTE)
                    .to(AsyncCmdStatusEnum.FAILED)
                    .costTime(costTime)
                    .errorMsg(stack)
                    .retryCount(retryCount)
                    .build());
                if (updated) {
                    entity.setModifiedTimestamp(System.currentTimeMillis());
                    entity.setStatus(AsyncCmdStatusEnum.FAILED);
                    entity.setRetryCount(retryCount);
                    entity.setErrorMsg(stack);
                    entity.setCostTime(costTime);
                }
                return updated;
            })
        );

        if (result && Objects.nonNull(handler)) {
            this.safeCallback(() -> handler.onSubTaskFail(entity), "onSubTaskFail", entity.getId());
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
        final Long expireTime = this.getNextExpireTime(System.currentTimeMillis());

        final boolean result = Boolean.TRUE.equals(
            this.transactionTemplate.execute(status -> {
                boolean updated = asyncCmdSubService.editStatusById(AsyncCmdStatusEditDTO.builder()
                    .id(entity.getId())
                    .from(AsyncCmdStatusEnum.EXECUTE)
                    .to(AsyncCmdStatusEnum.WAITING)
                    .costTime(costTime)
                    .result(entity.getResult())
                    .expireTime(expireTime)
                    .build());
                if (updated) {
                    // 关键钩子纳入事务：失败回滚状态流转并外抛，由上层失败链路接管
                    entity.setModifiedTimestamp(System.currentTimeMillis());
                    entity.setStatus(AsyncCmdStatusEnum.WAITING);
                    entity.setCostTime(costTime);
                    entity.setExpireTime(expireTime);
                    if (Objects.nonNull(handler)) {
                        handler.onSubTaskAsyncWait(entity);
                    }
                }
                return updated;
            })
        );

        return result;
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
            this.transactionTemplate.execute(status -> {
                boolean updated = asyncCmdSubService.editStatusById(AsyncCmdStatusEditDTO.builder()
                    .id(entity.getId())
                    .from(AsyncCmdStatusEnum.WAITING)
                    .to(AsyncCmdStatusEnum.SUCCESS)
                    .result(entity.getResult())
                    .expireTime(0L)
                    .build());
                if (updated) {
                    // 关键钩子纳入事务：失败回滚状态流转并外抛，等待下次轮询重试
                    entity.setModifiedTimestamp(System.currentTimeMillis());
                    entity.setStatus(AsyncCmdStatusEnum.SUCCESS);
                    entity.setExpireTime(0L);
                    if (Objects.nonNull(handler)) {
                        handler.onSubTaskSuccess(entity);
                    }
                }
                return updated;
            })
        );

        return result;
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
            this.transactionTemplate.execute(status -> {
                boolean updated = asyncCmdSubService.editStatusById(AsyncCmdStatusEditDTO.builder()
                    .id(entity.getId())
                    .from(AsyncCmdStatusEnum.WAITING)
                    .to(AsyncCmdStatusEnum.FAILED)
                    .errorMsg(stack)
                    .retryCount(retryCount)
                    .build());
                if (updated) {
                    entity.setModifiedTimestamp(System.currentTimeMillis());
                    entity.setStatus(AsyncCmdStatusEnum.FAILED);
                    entity.setRetryCount(retryCount);
                    entity.setErrorMsg(stack);
                }
                return updated;
            })
        );

        if (result && Objects.nonNull(handler)) {
            this.safeCallback(() -> handler.onSubTaskFail(entity), "onSubTaskFail", entity.getId());
        }
        return result;
    }

    /**
     * 获取下一次重试时间
     *
     *  @param baseTimeMillis 基准时间戳(毫秒)
     * @return long
     */
    public Long getNextRetryTime(long baseTimeMillis) {
        return baseTimeMillis
            + Optional.ofNullable(this.property.getAsyncWaitPollSeconds()).orElse(60L) * NumberConstant.NUMBER_ONE_THOUSAND;
    }

    /**
     * 获取回调等待截止时间（主任务复用expireTime、子任务使用expireTime字段存储，作为回调超时兜底）.
     *
     * @param baseTimeMillis 基准时间戳(毫秒)
     * @return long
     */
    public Long getNextExpireTime(long baseTimeMillis) {
        return baseTimeMillis
            + Optional.ofNullable(this.property.getAsyncWaitTimeoutSeconds()).orElse(1800L) * NumberConstant.NUMBER_ONE_THOUSAND;
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

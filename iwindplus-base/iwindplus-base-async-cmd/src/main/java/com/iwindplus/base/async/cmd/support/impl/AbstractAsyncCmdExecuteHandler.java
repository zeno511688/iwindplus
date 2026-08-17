/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.support.impl;

import com.iwindplus.base.async.cmd.dal.repository.AsyncCmdRepository;
import com.iwindplus.base.async.cmd.domain.constant.AsyncCmdConstant;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdStatusEditDTO;
import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdCallbackResultEnum;
import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdExecuteResultEnum;
import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdStatusEnum;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdVO;
import com.iwindplus.base.async.cmd.factory.AsyncCmdTaskHandlerStrategyFactory;
import com.iwindplus.base.async.cmd.service.AsyncCmdService;
import com.iwindplus.base.async.cmd.support.AsyncCmdExecuteHandler;
import com.iwindplus.base.async.cmd.support.AsyncCmdStateSupport;
import com.iwindplus.base.async.cmd.support.AsyncCmdTaskHandler;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 抽象异步命令执行形态策略实现类.
 *
 * @author zengdegui
 * @since 2025/11/29 23:12
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public abstract class AbstractAsyncCmdExecuteHandler implements AsyncCmdExecuteHandler {

    private final AsyncCmdTaskHandlerStrategyFactory asyncCmdTaskHandlerStrategyFactory;
    private final AsyncCmdStateSupport asyncCmdStateSupport;
    private final AsyncCmdRepository asyncCmdRepository;
    private final AsyncCmdService asyncCmdService;

    /**
     * 获取主任务助手.
     *
     * @param executeName 执行器名称
     * @return AsyncCmdTaskHandler
     */
    protected AsyncCmdTaskHandler getTaskHandler(String executeName) {
        return this.asyncCmdTaskHandlerStrategyFactory.getTaskHandler(executeName);
    }

    /**
     * 获取主任务回调结果.
     *
     * @param entity  异步命令视图对象
     * @param handler 异步命令任务助手
     * @return AsyncCmdCallbackResultEnum
     */
    protected AsyncCmdCallbackResultEnum getTaskCallback(AsyncCmdVO entity, AsyncCmdTaskHandler handler) {
        // 回调通知预存结果优先消费，不再调用业务侧查询
        final AsyncCmdCallbackResultEnum notified = AsyncCmdCallbackResultEnum.fromResultMap(entity.getResult());
        if (Objects.nonNull(notified)) {
            this.consumeNotifiedResult(entity);
            return notified;
        }
        try {
            final AsyncCmdCallbackResultEnum result = handler.executeCallback(entity);
            return Objects.isNull(result) ? AsyncCmdCallbackResultEnum.WAITING : result;
        } catch (Exception ex) {
            log.error("asyncCmd task callback failed, id={}", entity.getId(), ex);

            return AsyncCmdCallbackResultEnum.WAITING;
        }
    }

    /**
     * 消费预存结果后清理保留键，避免失败重试后再次读到旧预存结果.
     *
     * @param entity 异步命令视图对象
     */
    private void consumeNotifiedResult(AsyncCmdVO entity) {
        final Map<String, Object> result = entity.getResult();
        result.remove(AsyncCmdConstant.CALLBACK_RESULT_KEY);
        result.remove(AsyncCmdConstant.CALLBACK_ERROR_MSG_KEY);
        this.getAsyncCmdService().editStatusById(AsyncCmdStatusEditDTO.builder()
            .id(entity.getId())
            .result(result)
            .build());
    }

    /**
     * 异步等待执行回调.
     *
     * @param entity  异步命令视图对象
     * @param handler 异步命令执行助手
     * @param start   开始时间
     * @return AsyncCmdVO
     */
    protected AsyncCmdVO executeCallbackAsyncWait(AsyncCmdVO entity, AsyncCmdTaskHandler handler, long start) {
        final AsyncCmdCallbackResultEnum callbackResult = this.getTaskCallback(entity, handler);
        if (AsyncCmdCallbackResultEnum.SUCCESS.equals(callbackResult)) {
            final long costTime = entity.getCostTime() + System.currentTimeMillis() - start;
            if (!this.getAsyncCmdStateSupport().taskAsyncWaitSuccess(entity, handler, costTime)) {
                log.warn("asyncCmd task callback success action failed, id={}", entity.getId());
            }

            return entity;
        }

        if (AsyncCmdCallbackResultEnum.FAILED.equals(callbackResult)) {
            this.getAsyncCmdStateSupport().taskAsyncWaitFail(entity, handler,
                entity.getCostTime() + System.currentTimeMillis() - start,
                new RuntimeException("asyncCmd task callback failed"));

            return entity;
        }

        // 回调等待截止时间到期（expireTime复用存储），转失败走重置重试/丢弃链路，避免无限轮询
        final Long expireTime = entity.getExpireTime();
        if (Objects.nonNull(expireTime) && expireTime <= System.currentTimeMillis()) {
            this.getAsyncCmdStateSupport().taskAsyncWaitFail(entity, handler,
                entity.getCostTime() + System.currentTimeMillis() - start,
                new RuntimeException("asyncCmd task callback timeout"));

            return entity;
        }

        // 仍在等待中，CAS刷新下次轮询时间，避免每次 RETRY_JOB 都被拾起
        final Long nextRetryTime = this.getAsyncCmdStateSupport().getNextRetryTime(System.currentTimeMillis());
        this.getAsyncCmdService().editStatusById(AsyncCmdStatusEditDTO.builder()
            .id(entity.getId())
            .from(AsyncCmdStatusEnum.ASYNC_WAIT)
            .nextRetryTime(nextRetryTime)
            .build());
        entity.setNextRetryTime(nextRetryTime);

        return entity;
    }

    /**
     * 处理主任务执行结果，根据返回值决定状态走向.
     *
     * @param entity        主任务对象
     * @param handler       主任务助手
     * @param start         开始时间
     * @param result        业务执行返回值
     * @param stateAdvanced 是否已有状态推进（子任务场景）
     * @return boolean
     */
    protected boolean handleExecuteResult(AsyncCmdVO entity, AsyncCmdTaskHandler handler,
        long start, AsyncCmdExecuteResultEnum result, boolean stateAdvanced) {
        // 业务显式返回执行中
        if (AsyncCmdExecuteResultEnum.EXECUTE.equals(result)) {
            return false;
        }

        // 业务显式返回异步等待
        if (AsyncCmdExecuteResultEnum.ASYNC_WAIT.equals(result)) {
            final boolean taskAsyncWait = this.getAsyncCmdStateSupport().taskAsyncWait(entity, handler,
                System.currentTimeMillis() - start);
            if (!taskAsyncWait) {
                log.warn("asyncCmd task set asyncWait failed, id={}", entity.getId());
            }
            return false;
        }

        // 业务显式返回成功
        if (AsyncCmdExecuteResultEnum.SUCCESS.equals(result)) {
            // 成功
            final boolean taskSuccess = this.getAsyncCmdStateSupport().taskSuccess(entity, handler, System.currentTimeMillis() - start);
            if (!taskSuccess) {
                log.warn("asyncCmd task execute success, but taskSuccess failed, id={}", entity.getId());
            }
            return true;
        }

        // 业务显式返回失败
        if (AsyncCmdExecuteResultEnum.FAILED.equals(result)) {
            String msg = "asyncCmd task execute returned failed";
            final boolean taskFail = this.getAsyncCmdStateSupport().taskFail(entity, handler,
                System.currentTimeMillis() - start,
                new RuntimeException(msg), stateAdvanced);
            if (!taskFail) {
                log.warn("asyncCmd task execute failed, but taskFail failed, id={}", entity.getId());
            }
            return false;
        }

        return true;
    }
}

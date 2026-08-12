/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.support.impl;

import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdCallbackResultEnum;
import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdStatusEnum;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdVO;
import com.iwindplus.base.async.cmd.factory.AsyncCmdTaskHandlerStrategyFactory;
import com.iwindplus.base.async.cmd.service.AsyncCmdService;
import com.iwindplus.base.async.cmd.support.AsyncCmdExecuteHandler;
import com.iwindplus.base.async.cmd.support.AsyncCmdStateSupport;
import com.iwindplus.base.async.cmd.support.AsyncCmdTaskHandler;
import java.util.Objects;
import java.util.function.BiFunction;
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
        try {
            final AsyncCmdCallbackResultEnum result = handler.executeCallback(entity);
            return Objects.isNull(result) ? AsyncCmdCallbackResultEnum.WAITING : result;
        } catch (Exception ex) {
            log.error("asyncCmd task callback failed, id={}", entity.getId(), ex);

            return AsyncCmdCallbackResultEnum.WAITING;
        }
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
        return this.executeCallbackAsyncWait(entity, handler, start,
            (e, cost) -> this.getAsyncCmdStateSupport().taskAsyncWaitSuccess(e, handler, cost));
    }

    /**
     * 异步等待执行回调（自定义回调成功动作）.
     *
     * @param entity          异步命令视图对象
     * @param handler         异步命令执行助手
     * @param start           开始时间
     * @param onSuccessAction 回调成功动作（entity, costTime） -> boolean
     * @return AsyncCmdVO
     */
    protected AsyncCmdVO executeCallbackAsyncWait(AsyncCmdVO entity, AsyncCmdTaskHandler handler, long start,
        BiFunction<AsyncCmdVO, Long, Boolean> onSuccessAction) {
        final AsyncCmdCallbackResultEnum callbackResult = this.getTaskCallback(entity, handler);
        if (AsyncCmdCallbackResultEnum.SUCCESS.equals(callbackResult)) {
            if (!onSuccessAction.apply(entity, entity.getCostTime() + System.currentTimeMillis() - start)) {
                log.warn("asyncCmd task callback success action failed, id={}", entity.getId());

                return entity;
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

        // 仍在等待中，刷新下次轮询时间，避免每次 RETRY_JOB 都被拾起
        final Long nextRetryTime = this.getAsyncCmdStateSupport().getNextRetryTime();
        this.getAsyncCmdService().editStatusById(
            entity.getId(), AsyncCmdStatusEnum.ASYNC_WAIT, AsyncCmdStatusEnum.ASYNC_WAIT,
            null, null, null, nextRetryTime, null);
        entity.setNextRetryTime(nextRetryTime);

        return entity;
    }

    /**
     * 任务成功.
     *
     * @param entity  对象
     * @param handler 助手
     * @param start   开始时间
     */
    protected void taskSuccess(AsyncCmdVO entity, AsyncCmdTaskHandler handler, long start) {
        final long costTime = System.currentTimeMillis() - start;

        // 判断是否需要进入异步等待状态
        if (Boolean.TRUE.equals(entity.getNeedCallback())) {
            final boolean taskAsyncWait = this.getAsyncCmdStateSupport().taskAsyncWait(entity, handler, costTime);
            if (!taskAsyncWait) {
                log.warn("asyncCmd task set asyncWait failed, id={}", entity.getId());
            }

            return;
        }

        // 成功
        final boolean taskSuccess = this.getAsyncCmdStateSupport().taskSuccess(entity, handler, costTime);
        if (!taskSuccess) {
            log.warn("asyncCmd task execute success, but taskSuccess failed, id={}", entity.getId());
        }
    }

    /**
     * 任务最终成功（跳过needCallback判断，直接置SUCCESS）.
     * <p>用于callbackFirst模式：子任务完成后主任务直接置成功，不再进入异步等待</p>
     *
     * @param entity  对象
     * @param handler 助手
     * @param start   开始时间
     */
    protected void taskFinalSuccess(AsyncCmdVO entity, AsyncCmdTaskHandler handler, long start) {
        final long costTime = System.currentTimeMillis() - start;

        final boolean taskSuccess = this.getAsyncCmdStateSupport().taskSuccess(entity, handler, costTime);
        if (!taskSuccess) {
            log.warn("asyncCmd task execute success, but taskSuccess failed, id={}", entity.getId());
        }
    }
}

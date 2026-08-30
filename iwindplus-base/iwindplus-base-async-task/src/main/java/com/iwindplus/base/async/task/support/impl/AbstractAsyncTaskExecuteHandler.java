/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.task.support.impl;

import cn.hutool.core.collection.CollUtil;
import com.iwindplus.base.async.task.domain.constant.AsyncTaskConstant;
import com.iwindplus.base.async.task.domain.dto.AsyncTaskStatusEditDTO;
import com.iwindplus.base.async.task.domain.enums.AsyncTaskCallbackResultEnum;
import com.iwindplus.base.async.task.domain.enums.AsyncTaskExecuteResultEnum;
import com.iwindplus.base.async.task.domain.enums.AsyncTaskStatusEnum;
import com.iwindplus.base.async.task.domain.vo.AsyncTaskCallbackResultVO;
import com.iwindplus.base.async.task.domain.vo.AsyncTaskExecuteResultVO;
import com.iwindplus.base.async.task.domain.vo.AsyncTaskVO;
import com.iwindplus.base.async.task.factory.AsyncTaskHandlerStrategyFactory;
import com.iwindplus.base.async.task.service.AsyncTaskService;
import com.iwindplus.base.async.task.support.AsyncTaskExecuteHandler;
import com.iwindplus.base.async.task.support.AsyncTaskStateSupport;
import com.iwindplus.base.async.task.support.AsyncTaskHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 抽象异步任务执行形态策略实现类.
 *
 * @author zengdegui
 * @since 2025/11/29 23:12
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public abstract class AbstractAsyncTaskExecuteHandler implements AsyncTaskExecuteHandler {

    private final AsyncTaskHandlerStrategyFactory asyncTaskHandlerStrategyFactory;
    private final AsyncTaskStateSupport asyncTaskStateSupport;
    private final AsyncTaskService asyncTaskService;

    /**
     * 获取任务执行处理器.
     *
     * @param executeName 执行器名称
     * @return AsyncTaskHandler
     */
    protected AsyncTaskHandler getTaskHandler(String executeName) {
        return this.asyncTaskHandlerStrategyFactory.getTaskHandler(executeName);
    }

    /**
     * 获取主任务回调结果.
     *
     * @param entity  异步任务视图对象
     * @param handler 异步任务处理器
     * @return AsyncTaskCallbackResultEnum
     */
    protected AsyncTaskCallbackResultVO getTaskCallback(AsyncTaskVO entity, AsyncTaskHandler handler) {
        // 回调通知预存结果优先消费，不再调用业务侧查询
        final AsyncTaskCallbackResultEnum notified = AsyncTaskCallbackResultEnum.fromResultMap(entity.getResult());
        if (Objects.nonNull(notified)) {
            this.consumeNotifiedResult(entity);
            return AsyncTaskCallbackResultVO.setStatus(notified);
        }
        try {
            final AsyncTaskCallbackResultVO result = handler.executeCallback(entity);
            return Objects.isNull(result) ? AsyncTaskCallbackResultVO.waiting() : result;
        } catch (Exception ex) {
            log.error("asyncTask task callback failed, id={}", entity.getId(), ex);

            return AsyncTaskCallbackResultVO.waiting();
        }
    }

    /**
     * 消费预存结果后清理保留键，避免失败重试后再次读到旧预存结果.
     *
     * @param entity 异步任务视图对象
     */
    private void consumeNotifiedResult(AsyncTaskVO entity) {
        final Map<String, Object> result = entity.getResult();
        result.remove(AsyncTaskConstant.CALLBACK_RESULT_KEY);
        result.remove(AsyncTaskConstant.CALLBACK_ERROR_MSG_KEY);
        this.getAsyncTaskService().editStatusById(AsyncTaskStatusEditDTO.builder()
            .id(entity.getId())
            .result(result)
            .build());
    }

    /**
     * 异步等待执行回调.
     *
     * @param entity  异步任务视图对象
     * @param handler 异步任务执行助手
     * @param start   开始时间
     * @return AsyncTaskVO
     */
    protected AsyncTaskVO executeCallbackAsyncWait(AsyncTaskVO entity, AsyncTaskHandler handler, long start) {
        final AsyncTaskCallbackResultVO callbackResult = this.getTaskCallback(entity, handler);
        if (callbackResult == null) {
            return entity;
        }

        final AsyncTaskCallbackResultEnum status = callbackResult.getStatus();

        if (AsyncTaskCallbackResultEnum.SUCCESS.equals(status)) {
            final long costTime = Optional.ofNullable(entity.getCostTime()).orElse(0L) + System.currentTimeMillis() - start;
            if (!this.getAsyncTaskStateSupport().taskAsyncWaitSuccess(entity, handler, costTime)) {
                log.warn("asyncTask task execute callback success failed, id={}", entity.getId());
            }

            return entity;
        }

        if (AsyncTaskCallbackResultEnum.FAILED.equals(status)) {
            final long costTime = Optional.ofNullable(entity.getCostTime()).orElse(0L) + System.currentTimeMillis() - start;
            final String msg = String.format(
                "asyncTask task execute callback failed, id=%s",
                entity.getId()
            );
            if (!this.getAsyncTaskStateSupport().taskAsyncWaitFail(entity, handler, costTime, new RuntimeException(msg))) {
                log.warn(msg);
            }

            return entity;
        }

        // 回调等待截止时间到期（expireTime复用存储），转失败走重置重试/丢弃链路，避免无限轮询
        final Long expireTime = entity.getExpireTime();
        if (Objects.nonNull(expireTime) && expireTime <= System.currentTimeMillis()) {
            final long costTime = Optional.ofNullable(entity.getCostTime()).orElse(0L) + System.currentTimeMillis() - start;
            final String msg = String.format(
                "asyncTask task execute callback timeout, id=%s",
                entity.getId()
            );
            if (!this.getAsyncTaskStateSupport().taskAsyncWaitFail(entity, handler, costTime, new RuntimeException(msg))) {
                log.warn(msg);
            }

            return entity;
        }

        // 仍在等待中，CAS刷新下次轮询时间，避免每次 RETRY_JOB 都被拾起
        final Long nextRetryTime = this.getAsyncTaskStateSupport()
            .getAsyncTaskSubRepository().getNextRetryTime(System.currentTimeMillis());
        this.getAsyncTaskService().editStatusById(AsyncTaskStatusEditDTO.builder()
            .id(entity.getId())
            .from(AsyncTaskStatusEnum.WAITING)
            .nextRetryTime(nextRetryTime)
            .build());
        entity.setNextRetryTime(nextRetryTime);

        return entity;
    }

    /**
     * 处理主任务执行结果，根据返回值决定状态走向.
     *
     * @param entity        主任务对象
     * @param handler       任务处理器
     * @param start         开始时间
     * @param executeResult 业务执行返回值
     * @param stateAdvanced 是否已有状态推进（子任务场景）
     * @return boolean
     */
    protected boolean handleExecuteResult(AsyncTaskVO entity, AsyncTaskHandler handler,
        long start, AsyncTaskExecuteResultVO executeResult, boolean stateAdvanced) {
        if (executeResult == null) {
            return false;
        }

        final AsyncTaskExecuteResultEnum status = executeResult.getStatus();

        // 业务显式返回执行中
        if (AsyncTaskExecuteResultEnum.EXECUTE.equals(status)) {
            return false;
        }

        // 业务显式返回异步等待
        if (AsyncTaskExecuteResultEnum.WAITING.equals(status)) {
            final long costTime = Optional.ofNullable(entity.getCostTime()).orElse(0L) + System.currentTimeMillis() - start;
            if (!this.getAsyncTaskStateSupport().taskAsyncWait(entity, handler, costTime)) {
                log.warn("asyncTask task execute waiting failed, id={}", entity.getId());
            }
            return false;
        }

        // 业务显式返回成功
        if (AsyncTaskExecuteResultEnum.SUCCESS.equals(status)) {
            // 合并业务返回值到 result
            this.mergeResult(entity, executeResult.getResult());
            final long costTime = Optional.ofNullable(entity.getCostTime()).orElse(0L) + System.currentTimeMillis() - start;
            if (!this.getAsyncTaskStateSupport().taskSuccess(entity, handler, costTime)) {
                log.warn("asyncTask task execute success failed, id={}", entity.getId());
                return false;
            }
            return true;
        }

        // 业务显式返回失败
        if (AsyncTaskExecuteResultEnum.FAILED.equals(status)) {
            final long costTime = Optional.ofNullable(entity.getCostTime()).orElse(0L) + System.currentTimeMillis() - start;
            final String msg = String.format(
                "asyncTask task execute failed, id=%s",
                entity.getId()
            );
            if (!this.getAsyncTaskStateSupport().taskFail(entity, handler, costTime, new RuntimeException(msg), stateAdvanced)) {
                log.warn(msg);
            }
            return false;
        }

        return false;
    }

    /**
     * 合并业务返回值到主任务 result.
     */
    protected void mergeResult(AsyncTaskVO entity, Map<String, Object> businessResult) {
        if (CollUtil.isEmpty(businessResult)) {
            return;
        }
        entity.setResult(new HashMap<>(businessResult));
    }
}

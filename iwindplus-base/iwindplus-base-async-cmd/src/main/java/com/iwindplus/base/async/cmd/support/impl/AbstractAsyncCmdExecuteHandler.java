/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.support.impl;

import cn.hutool.core.collection.CollUtil;
import com.iwindplus.base.async.cmd.domain.constant.AsyncCmdConstant;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdStatusEditDTO;
import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdCallbackResultEnum;
import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdExecuteResultEnum;
import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdStatusEnum;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdCallbackResultVO;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdExecuteResultVO;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdVO;
import com.iwindplus.base.async.cmd.factory.AsyncCmdTaskHandlerStrategyFactory;
import com.iwindplus.base.async.cmd.service.AsyncCmdService;
import com.iwindplus.base.async.cmd.support.AsyncCmdExecuteHandler;
import com.iwindplus.base.async.cmd.support.AsyncCmdStateSupport;
import com.iwindplus.base.async.cmd.support.AsyncCmdTaskHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
    protected AsyncCmdCallbackResultVO getTaskCallback(AsyncCmdVO entity, AsyncCmdTaskHandler handler) {
        // 回调通知预存结果优先消费，不再调用业务侧查询
        final AsyncCmdCallbackResultEnum notified = AsyncCmdCallbackResultEnum.fromResultMap(entity.getResult());
        if (Objects.nonNull(notified)) {
            this.consumeNotifiedResult(entity);
            return AsyncCmdCallbackResultVO.setStatus(notified);
        }
        try {
            final AsyncCmdCallbackResultVO result = handler.executeCallback(entity);
            return Objects.isNull(result) ? AsyncCmdCallbackResultVO.waiting() : result;
        } catch (Exception ex) {
            log.error("asyncCmd task callback failed, id={}", entity.getId(), ex);

            return AsyncCmdCallbackResultVO.waiting();
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
        final AsyncCmdCallbackResultVO callbackResult = this.getTaskCallback(entity, handler);
        if (callbackResult == null) {
            return entity;
        }

        final AsyncCmdCallbackResultEnum status = callbackResult.getStatus();

        if (AsyncCmdCallbackResultEnum.SUCCESS.equals(status)) {
            final long costTime = Optional.ofNullable(entity.getCostTime()).orElse(0L) + System.currentTimeMillis() - start;
            if (!this.getAsyncCmdStateSupport().taskAsyncWaitSuccess(entity, handler, costTime)) {
                log.warn("asyncCmd task execute callback success failed, id={}", entity.getId());
            }

            return entity;
        }

        if (AsyncCmdCallbackResultEnum.FAILED.equals(status)) {
            final long costTime = Optional.ofNullable(entity.getCostTime()).orElse(0L) + System.currentTimeMillis() - start;
            final String msg = String.format(
                "asyncCmd task execute callback failed, id=%s",
                entity.getId()
            );
            if (!this.getAsyncCmdStateSupport().taskAsyncWaitFail(entity, handler, costTime, new RuntimeException(msg))) {
                log.warn(msg);
            }

            return entity;
        }

        // 回调等待截止时间到期（expireTime复用存储），转失败走重置重试/丢弃链路，避免无限轮询
        final Long expireTime = entity.getExpireTime();
        if (Objects.nonNull(expireTime) && expireTime <= System.currentTimeMillis()) {
            final long costTime = Optional.ofNullable(entity.getCostTime()).orElse(0L) + System.currentTimeMillis() - start;
            final String msg = String.format(
                "asyncCmd task execute callback timeout, id=%s",
                entity.getId()
            );
            if (!this.getAsyncCmdStateSupport().taskAsyncWaitFail(entity, handler, costTime, new RuntimeException(msg))) {
                log.warn(msg);
            }

            return entity;
        }

        // 仍在等待中，CAS刷新下次轮询时间，避免每次 RETRY_JOB 都被拾起
        final Long nextRetryTime = this.getAsyncCmdStateSupport().getNextRetryTime(System.currentTimeMillis());
        this.getAsyncCmdService().editStatusById(AsyncCmdStatusEditDTO.builder()
            .id(entity.getId())
            .from(AsyncCmdStatusEnum.WAITING)
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
     * @param executeResult 业务执行返回值
     * @param stateAdvanced 是否已有状态推进（子任务场景）
     * @return boolean
     */
    protected boolean handleExecuteResult(AsyncCmdVO entity, AsyncCmdTaskHandler handler,
        long start, AsyncCmdExecuteResultVO executeResult, boolean stateAdvanced) {
        if (executeResult == null) {
            return false;
        }

        final AsyncCmdExecuteResultEnum result = executeResult.getStatus();

        // 业务显式返回执行中
        if (AsyncCmdExecuteResultEnum.EXECUTE.equals(result)) {
            return false;
        }

        // 业务显式返回异步等待
        if (AsyncCmdExecuteResultEnum.WAITING.equals(result)) {
            final long costTime = Optional.ofNullable(entity.getCostTime()).orElse(0L) + System.currentTimeMillis() - start;
            if (!this.getAsyncCmdStateSupport().taskAsyncWait(entity, handler, costTime)) {
                log.warn("asyncCmd task execute waiting failed, id={}", entity.getId());
            }
            return false;
        }

        // 业务显式返回成功
        if (AsyncCmdExecuteResultEnum.SUCCESS.equals(result)) {
            // 合并业务返回值到 result
            this.mergeResult(entity, executeResult.getResult());
            final long costTime = Optional.ofNullable(entity.getCostTime()).orElse(0L) + System.currentTimeMillis() - start;
            if (!this.getAsyncCmdStateSupport().taskSuccess(entity, handler, costTime)) {
                log.warn("asyncCmd task execute success failed, id={}", entity.getId());
                return false;
            }
            return true;
        }

        // 业务显式返回失败
        if (AsyncCmdExecuteResultEnum.FAILED.equals(result)) {
            final long costTime = Optional.ofNullable(entity.getCostTime()).orElse(0L) + System.currentTimeMillis() - start;
            final String msg = String.format(
                "asyncCmd task execute failed, id=%s",
                entity.getId()
            );
            if (!this.getAsyncCmdStateSupport().taskFail(entity, handler, costTime, new RuntimeException(msg), stateAdvanced)) {
                log.warn(msg);
            }
            return false;
        }

        return false;
    }

    /**
     * 合并业务返回值到主任务 result.
     */
    protected void mergeResult(AsyncCmdVO entity, Map<String, Object> businessResult) {
        if (CollUtil.isEmpty(businessResult)) {
            return;
        }
        entity.setResult(new HashMap<>(businessResult));
    }
}

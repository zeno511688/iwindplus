/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.support.impl;

import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdCallbackResultEnum;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdVO;
import com.iwindplus.base.async.cmd.factory.AsyncCmdTaskHandlerStrategyFactory;
import com.iwindplus.base.async.cmd.service.AsyncCmdService;
import com.iwindplus.base.async.cmd.support.AsyncCmdExecuteHandler;
import com.iwindplus.base.async.cmd.support.AsyncCmdStateSupport;
import com.iwindplus.base.async.cmd.support.AsyncCmdTaskHandler;
import java.time.LocalDateTime;
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
     * 执行回调.
     *
     * @param entity  异步命令视图对象
     * @param handler 异步命令执行助手
     * @param start   开始时间
     * @return AsyncCmdVO
     */
    protected AsyncCmdVO executeCallback(AsyncCmdVO entity, AsyncCmdTaskHandler handler, long start) {
        final LocalDateTime callbackExpireTime = entity.getCallbackExpireTime();
        if (LocalDateTime.now().isAfter(callbackExpireTime)) {
            final String msg = "asyncCmd task callback timeout";

            log.warn(msg + ", id={}", entity.getId());

            this.getAsyncCmdStateSupport().taskAsyncWaitFail(entity, handler, new RuntimeException(msg));
            return entity;
        }

        final AsyncCmdCallbackResultEnum callbackResult = this.getTaskCallback(entity, handler);
        if (AsyncCmdCallbackResultEnum.SUCCESS.equals(callbackResult)) {
            if (!this.getAsyncCmdStateSupport().taskAsyncWaitSuccess(entity, handler,
                entity.getCostTime() + System.currentTimeMillis() - start)) {
                log.warn("asyncCmd task callback failed, id={}", entity.getId());

                return entity;
            }

            return entity;
        }

        if (AsyncCmdCallbackResultEnum.FAILED.equals(callbackResult)) {
            this.getAsyncCmdStateSupport().taskAsyncWaitFail(entity, handler,
                new RuntimeException("asyncCmd task callback failed"));

            return entity;
        }

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

    private AsyncCmdCallbackResultEnum getTaskCallback(AsyncCmdVO entity, AsyncCmdTaskHandler handler) {
        try {
            final AsyncCmdCallbackResultEnum result = handler.executeCallback(entity);
            return Objects.isNull(result) ? AsyncCmdCallbackResultEnum.WAITING : result;
        } catch (Exception ex) {
            log.error("asyncCmd task callback failed, id={}", entity.getId());

            return AsyncCmdCallbackResultEnum.WAITING;
        }
    }
}

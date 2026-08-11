/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.support.impl;

import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdCallbackResultEnum;
import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdStatusEnum;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdSubVO;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdVO;
import com.iwindplus.base.async.cmd.factory.AsyncCmdSubTaskHandlerStrategyFactory;
import com.iwindplus.base.async.cmd.factory.AsyncCmdTaskHandlerStrategyFactory;
import com.iwindplus.base.async.cmd.service.AsyncCmdService;
import com.iwindplus.base.async.cmd.service.AsyncCmdSubService;
import com.iwindplus.base.async.cmd.support.AsyncCmdStateSupport;
import com.iwindplus.base.async.cmd.support.AsyncCmdSubTaskHandler;
import com.iwindplus.base.async.cmd.support.AsyncCmdTaskHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.core.executor.DtpExecutor;

/**
 * 异步命令组任务执行策略实现类.
 *
 * @author zengdegui
 * @since 2025/11/29 23:12
 */
@Slf4j
public class AsyncCmdExecuteHandlerGroup extends AbstractAsyncCmdExecuteHandler {

    private final AsyncCmdSubService asyncCmdSubService;
    private final AsyncCmdSubTaskHandlerStrategyFactory asyncCmdSubTaskHandlerStrategyFactory;
    private final DtpExecutor asyncCmdSubTaskExecutor;

    public AsyncCmdExecuteHandlerGroup(
        AsyncCmdTaskHandlerStrategyFactory asyncTaskHandlerStrategyFactory,
        AsyncCmdStateSupport asyncCmdStateSupport,
        AsyncCmdService asyncCmdService,
        AsyncCmdSubService asyncCmdSubService,
        AsyncCmdSubTaskHandlerStrategyFactory asyncCmdSubTaskHandlerStrategyFactory,
        DtpExecutor asyncCmdSubTaskExecutor) {
        super(asyncTaskHandlerStrategyFactory, asyncCmdStateSupport, asyncCmdService);
        this.asyncCmdSubService = asyncCmdSubService;
        this.asyncCmdSubTaskHandlerStrategyFactory = asyncCmdSubTaskHandlerStrategyFactory;
        this.asyncCmdSubTaskExecutor = asyncCmdSubTaskExecutor;
    }

    @Override
    public void execute(AsyncCmdVO entity) {
        final AsyncCmdTaskHandler handler = this.getTaskHandler(entity.getExecuteName());
        final long start = System.currentTimeMillis();
        final boolean callbackFirst = Boolean.TRUE.equals(entity.getCallbackFirst());

        // 判断是否是异步等待状态
        if (entity.getStatus() == AsyncCmdStatusEnum.ASYNC_WAIT) {
            if (callbackFirst) {
                // callbackFirst模式: 主任务回调成功 → 转为待执行 → 下一轮分发子任务
                this.executeCallbackAsyncWait(entity, handler, start,
                    (e, cost) -> this.getAsyncCmdStateSupport().taskAsyncWaitToBeExecute(e, cost));
            } else {
                this.executeCallbackAsyncWait(entity, handler, start);
            }

            return;
        }

        final AtomicInteger advanced = new AtomicInteger(0);

        try {
            if (callbackFirst) {
                // callbackFirst模式: 判断是首次执行还是回调后分发子任务
                if (Objects.nonNull(entity.getCallbackExpireTime()) && entity.getCallbackExpireTime() == 0L) {
                    // 回调已完成（callbackExpireTime被清零），分发子任务
                    this.executeCallbackFirstSubTasks(entity, handler, start);
                } else {
                    // 首次执行: 主任务业务 → 进入异步等待
                    handler.execute(entity);
                    this.taskSuccess(entity, handler, start);
                }
            } else {
                // 默认模式: 先子任务 → 主收尾 → 成功
                final List<AsyncCmdSubVO> subEntities = this.asyncCmdSubService.listByAsyncCmdIdAndStatus(
                    entity.getId(), AsyncCmdStatusEnum.getUnfinishedStatus());

                if (!this.executeSubTaskResult(entity, handler, start, subEntities, advanced)) {
                    return;
                }

                handler.execute(entity);
                this.taskSuccess(entity, handler, start);
            }
        } catch (Exception ex) {
            log.error("asyncCmd task execute failed. id={}", entity.getId(), ex);

            this.getAsyncCmdStateSupport().taskFail(entity, handler,
                System.currentTimeMillis() - start, ex, advanced.get() > 0);
        }
    }

    /**
     * callbackFirst模式: 回调成功后分发子任务（executeSub为空实现，仅做状态流转）.
     *
     * @param entity  异步命令视图对象
     * @param handler 异步命令任务助手
     * @param start   开始时间
     */
    private void executeCallbackFirstSubTasks(AsyncCmdVO entity, AsyncCmdTaskHandler handler, long start) {
        final List<AsyncCmdSubVO> subEntities = this.asyncCmdSubService.listByAsyncCmdIdAndStatus(
            entity.getId(), AsyncCmdStatusEnum.getUnfinishedStatus());
        final AtomicInteger advanced = new AtomicInteger(0);

        // 执行子任务（executeSub为空实现，仅做状态流转）
        this.executeSubTask(entity, subEntities, advanced);

        // 有子任务仍在异步等待中
        if (this.hasSubAsyncWait(subEntities)) {
            final boolean toBeExecute = this.getAsyncCmdStateSupport().taskExecuteToBeExecute(
                entity, System.currentTimeMillis() - start);
            if (!toBeExecute) {
                log.warn("asyncCmd callbackFirst group has asyncWait, id={}", entity.getId());
            }

            return;
        }

        // 子任务未全部成功
        final long unfinished = asyncCmdSubService.countUnfinished(entity.getId());
        if (unfinished > 0) {
            String msg = "asyncCmd callbackFirst group has unfinished subTask";
            log.warn(msg + ", id={} unfinished={}", entity.getId(), unfinished);

            this.getAsyncCmdStateSupport().taskFail(entity, handler,
                System.currentTimeMillis() - start,
                new RuntimeException(msg),
                advanced.get() > 0);

            return;
        }

        // 子任务全部成功 → 主任务收尾 → 主任务置成功（跳过needCallback，直接置SUCCESS）
        handler.execute(entity);
        this.taskFinalSuccess(entity, handler, start);
    }

    private boolean executeSubTaskResult(AsyncCmdVO entity, AsyncCmdTaskHandler handler, long start,
        List<AsyncCmdSubVO> subEntities, AtomicInteger advanced) {
        // 执行子任务，返回成功的个数
        final List<AsyncCmdSubVO> subResults = this.executeSubTask(entity, subEntities, advanced);

        // 判断子任务是否在等异步调用的结果，也可以不管，等待任务重置也行
        if (this.hasSubAsyncWait(subEntities)) {
            // 将主任务设置为待执行，等待下一次执行
            final boolean asyncWait = this.getAsyncCmdStateSupport().taskExecuteToBeExecute(entity, System.currentTimeMillis() - start);
            if (!asyncWait) {
                log.warn("asyncCmd group has asyncWait, id={}", entity.getId());
            }

            return false;
        }

        // 子任务未全部成功，主任务判定为失败，主任务表只记"子任务有未完成的任务"
        final long unfinished = asyncCmdSubService.countUnfinished(entity.getId());
        if (unfinished > 0) {
            String msg = "asyncCmd group has unfinished subTask";
            log.warn(msg + ", id={} unfinished={} success={}",
                entity.getId(), unfinished, advanced.get());

            this.getAsyncCmdStateSupport().taskFail(entity, handler,
                System.currentTimeMillis() - start,
                new RuntimeException(msg),
                advanced.get() > 0);

            return false;
        }

        // 成功的数要对的上提交时的任务数
        if (subResults.size() < entity.getSubTaskCount()) {
            String msg = "asyncCmd group execute success, but subTaskCount not match";

            log.warn(msg + ", id={} subTaskCount={} success={}",
                entity.getId(), entity.getSubTaskCount(), subResults.size());

            this.getAsyncCmdStateSupport().taskFail(entity, handler,
                System.currentTimeMillis() - start,
                new RuntimeException(msg),
                advanced.get() > 0);
            return false;
        }

        // 子任务全部成功 -> 主任务收尾业务 -> 主任务置成功
        entity.setSubTasks(subResults);
        return true;
    }

    private List<AsyncCmdSubVO> executeSubTask(AsyncCmdVO entity, List<AsyncCmdSubVO> subEntities, AtomicInteger advanced) {
        final List<AsyncCmdSubVO> successResults = this.listPriorSuccess(entity, subEntities);

        final List<List<AsyncCmdSubVO>> batches = this.groupByStage(subEntities);
        for (List<AsyncCmdSubVO> batch : batches) {
            // 设置前置成功子任务结果
            final List<AsyncCmdSubVO> snapshot = List.copyOf(successResults);
            batch.forEach(item -> item.setPriorSubTasks(snapshot));

            final List<AsyncCmdSubVO> results = this.executeSubTaskGroups(entity, batch, advanced);

            // 当前批次有失败，停止后续阶段
            if (!results.stream().allMatch(this::isSuccess)) {
                return successResults;
            }

            // 成功结果传递下一阶段
            successResults.addAll(results);
        }

        return successResults;
    }

    private List<AsyncCmdSubVO> listPriorSuccess(AsyncCmdVO entity, List<AsyncCmdSubVO> subEntities) {
        if (subEntities.size() >= entity.getSubTaskCount()) {
            return new ArrayList<>(10);
        }

        return asyncCmdSubService.listByAsyncCmdIdAndStatus(
            entity.getId(),
            List.of(AsyncCmdStatusEnum.SUCCESS)
        );
    }

    private List<List<AsyncCmdSubVO>> groupByStage(List<AsyncCmdSubVO> subEntities) {
        List<List<AsyncCmdSubVO>> batches = new ArrayList<>(10);
        List<AsyncCmdSubVO> currentBatch = null;
        Integer currentStage = null;

        for (AsyncCmdSubVO subEntity : subEntities) {
            Integer stage = subEntity.getStage();
            // stage=0，每个任务单独一组
            if (stage <= 0) {
                batches.add(new ArrayList<>(List.of(subEntity)));
                currentBatch = null;
                currentStage = null;
                continue;
            }

            // stage>0，stage变化则开启新组
            if (!Objects.equals(stage, currentStage)) {
                currentBatch = new ArrayList<>(10);
                batches.add(currentBatch);
                currentStage = stage;
            }

            currentBatch.add(subEntity);
        }

        return batches;
    }

    private List<AsyncCmdSubVO> executeSubTaskGroups(AsyncCmdVO entity, List<AsyncCmdSubVO> batch, AtomicInteger advanced) {
        // 单任务直接执行
        if (batch.size() == 1) {
            return List.of(executeOneSubTask(entity, batch.get(0), advanced));
        }
        // 多任务并发执行
        return this.executeBatchSubTask(entity, batch, advanced);
    }

    private AsyncCmdSubVO executeOneSubTask(AsyncCmdVO entity, AsyncCmdSubVO subEntity, AtomicInteger advanced) {
        AsyncCmdSubTaskHandler handler = this.getSubTaskHandler(subEntity.getExecuteName());

        // 判断是否是异步等待状态
        if (subEntity.getStatus() == AsyncCmdStatusEnum.ASYNC_WAIT) {
            return this.executeSubCallbackAsyncWait(entity, handler, subEntity, advanced);
        }

        // 续期
        this.getAsyncCmdService().editExpireTime(entity.getId());

        final boolean status = asyncCmdSubService.editStatusById(subEntity.getId(), null, AsyncCmdStatusEnum.EXECUTE,
            null, null, null, null, null);
        if (!status) {
            log.warn("asyncCmd subTask execute failed, id={} asyncCmdId={} seq={}",
                subEntity.getId(), entity.getId(), subEntity.getSeq());
            return subEntity;
        }

        long start = System.currentTimeMillis();
        try {
            // 执行业务逻辑（无事务）
            handler.executeSub(subEntity);

            final long costTime = System.currentTimeMillis() - start;

            // 判断是否需要进入异步等待状态
            if (Boolean.TRUE.equals(subEntity.getNeedCallback())) {
                final boolean subTaskAsyncWait = this.getAsyncCmdStateSupport().subTaskAsyncWait(subEntity, handler, costTime);
                if (!subTaskAsyncWait) {
                    log.warn("asyncCmd subTask set asyncWait failed, id={} asyncCmdId={} seq={}",
                        subEntity.getId(), entity.getId(), subEntity.getSeq());
                }

                return subEntity;
            }

            // 成功
            final boolean subTaskSuccess = this.getAsyncCmdStateSupport().subTaskSuccess(subEntity, handler, costTime);
            if (!subTaskSuccess) {
                log.warn("asyncCmd subTask execute failed, id={} asyncCmdId={} seq={}",
                    subEntity.getId(), entity.getId(), subEntity.getSeq());
                return subEntity;
            }

            advanced.incrementAndGet();
        } catch (Exception ex) {
            log.error("asyncCmd subTask execute failed, id={} asyncCmdId={} seq={}",
                subEntity.getId(), entity.getId(), subEntity.getSeq(), ex);

            this.getAsyncCmdStateSupport().subTaskFail(subEntity, handler, System.currentTimeMillis() - start, ex);
        }

        return subEntity;
    }

    private List<AsyncCmdSubVO> executeBatchSubTask(AsyncCmdVO entity, List<AsyncCmdSubVO> batch, AtomicInteger advanced) {
        List<CompletableFuture<AsyncCmdSubVO>> futures = batch.stream()
            .map(task ->
                CompletableFuture.supplyAsync(
                    () ->
                        executeOneSubTask(entity, task, advanced)
                    , asyncCmdSubTaskExecutor
                )
            ).toList();

        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        return futures.stream()
            .map(CompletableFuture::join)
            .toList();
    }

    private AsyncCmdSubVO executeSubCallbackAsyncWait(
        AsyncCmdVO entity, AsyncCmdSubTaskHandler handler,
        AsyncCmdSubVO subEntity, AtomicInteger advanced) {

        final Long callbackExpireTime = subEntity.getCallbackExpireTime();
        if (Objects.nonNull(callbackExpireTime) && System.currentTimeMillis() > callbackExpireTime) {
            String msg = "asyncCmd subTask callback timeout";

            log.warn(msg + ", id={} asyncCmdId={} seq={}",
                subEntity.getId(), entity.getId(), subEntity.getSeq());

            this.getAsyncCmdStateSupport().subTaskAsyncWaitFail(subEntity, handler, new RuntimeException(msg));
            return subEntity;
        }

        final AsyncCmdCallbackResultEnum callbackResult = this.getSubTaskCallback(entity, subEntity, handler);
        if (AsyncCmdCallbackResultEnum.SUCCESS.equals(callbackResult)) {
            if (!this.getAsyncCmdStateSupport().subTaskAsyncWaitSuccess(subEntity, handler)) {
                log.warn("asyncCmd subTask callback failed, id={} asyncCmdId={} seq={}",
                    subEntity.getId(), entity.getId(), subEntity.getSeq());
                return subEntity;
            }

            advanced.incrementAndGet();
            return subEntity;
        }

        if (AsyncCmdCallbackResultEnum.FAILED.equals(callbackResult)) {
            this.getAsyncCmdStateSupport().subTaskAsyncWaitFail(subEntity, handler,
                new RuntimeException("asyncCmd subTask callback failed"));

            return subEntity;
        }

        return subEntity;
    }

    private AsyncCmdCallbackResultEnum getSubTaskCallback(AsyncCmdVO entity, AsyncCmdSubVO subEntity, AsyncCmdSubTaskHandler handler) {
        try {
            final AsyncCmdCallbackResultEnum result = handler.executeSubCallback(subEntity);
            return Objects.isNull(result) ? AsyncCmdCallbackResultEnum.WAITING : result;
        } catch (Exception ex) {
            log.error("asyncCmd subTask callback failed, id={} asyncCmdId={} seq={}",
                subEntity.getId(), entity.getId(), subEntity.getSeq(), ex);

            return AsyncCmdCallbackResultEnum.WAITING;
        }
    }

    private boolean isSuccess(AsyncCmdSubVO entity) {
        return AsyncCmdStatusEnum.SUCCESS == entity.getStatus();
    }

    private boolean hasSubAsyncWait(List<AsyncCmdSubVO> subEntities) {
        return subEntities.stream().anyMatch(item -> AsyncCmdStatusEnum.ASYNC_WAIT.equals(item.getStatus()));
    }

    private AsyncCmdSubTaskHandler getSubTaskHandler(String executeName) {
        return this.asyncCmdSubTaskHandlerStrategyFactory.getTaskHandler(executeName);
    }

}

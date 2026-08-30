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
import com.iwindplus.base.async.task.domain.vo.AsyncTaskSubVO;
import com.iwindplus.base.async.task.domain.vo.AsyncTaskVO;
import com.iwindplus.base.async.task.factory.AsyncTaskHandlerStrategyFactory;
import com.iwindplus.base.async.task.factory.AsyncTaskSubHandlerStrategyFactory;
import com.iwindplus.base.async.task.service.AsyncTaskService;
import com.iwindplus.base.async.task.service.AsyncTaskSubService;
import com.iwindplus.base.async.task.support.AsyncTaskHandler;
import com.iwindplus.base.async.task.support.AsyncTaskStateSupport;
import com.iwindplus.base.async.task.support.AsyncTaskSubHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;

/**
 * 异步任务组任务执行策略实现类.
 *
 * @author zengdegui
 * @since 2025/11/29 23:12
 */
@Slf4j
public class AsyncTaskExecuteHandlerGroup extends AbstractAsyncTaskExecuteHandler {

    private final AsyncTaskSubService asyncTaskSubService;
    private final AsyncTaskSubHandlerStrategyFactory asyncTaskSubHandlerStrategyFactory;
    private final ThreadPoolExecutor threadPoolExecutor;

    public AsyncTaskExecuteHandlerGroup(
        AsyncTaskHandlerStrategyFactory asyncTaskHandlerStrategyFactory,
        AsyncTaskStateSupport asyncTaskStateSupport,
        AsyncTaskService asyncTaskService,
        AsyncTaskSubService asyncTaskSubService,
        AsyncTaskSubHandlerStrategyFactory asyncTaskSubHandlerStrategyFactory,
        ThreadPoolExecutor threadPoolExecutor) {
        super(asyncTaskHandlerStrategyFactory, asyncTaskStateSupport, asyncTaskService);
        this.asyncTaskSubService = asyncTaskSubService;
        this.asyncTaskSubHandlerStrategyFactory = asyncTaskSubHandlerStrategyFactory;
        this.threadPoolExecutor = threadPoolExecutor;
    }

    @Override
    public void execute(AsyncTaskVO entity) {
        final AsyncTaskHandler handler = this.getTaskHandler(entity.getExecuteName());
        final long start = System.currentTimeMillis();

        // 判断是否是异步等待状态
        if (AsyncTaskStatusEnum.WAITING.equals(entity.getStatus())) {
            this.executeCallbackAsyncWait(entity, handler, start);
            return;
        }

        final AtomicInteger advanced = new AtomicInteger(0);

        try {
            // 先子任务 → 主收尾 → 成功
            final List<AsyncTaskSubVO> subEntities = this.asyncTaskSubService.listByAsyncTaskIdAndStatus(
                entity.getId(), AsyncTaskStatusEnum.getUnfinishedStatus());

            if (!this.executeSubTaskResult(entity, handler, start, subEntities, advanced)) {
                return;
            }

            // 执行业务逻辑（无事务），由业务方显式返回执行结果
            final AsyncTaskExecuteResultVO result = handler.execute(entity);

            this.handleExecuteResult(entity, handler, start, result, advanced.get() > 0);
        } catch (Exception ex) {
            log.error("asyncTask task execute failed. id={}", entity.getId(), ex);

            final long costTime = Optional.ofNullable(entity.getCostTime()).orElse(0L) + System.currentTimeMillis() - start;
            this.getAsyncTaskStateSupport().taskFail(entity, handler, costTime, ex, advanced.get() > 0);
        }
    }

    private boolean executeSubTaskResult(AsyncTaskVO entity, AsyncTaskHandler handler, long start,
        List<AsyncTaskSubVO> subEntities, AtomicInteger advanced) {
        // 执行子任务，返回成功的个数
        final List<AsyncTaskSubVO> subResults = this.executeSubTask(entity, subEntities, start, advanced);

        // 判断子任务是否在等异步调用的结果，不能变成失败，失败会占用重试次数
        if (this.handleSubAsyncWait(entity, subEntities, start)) {
            return false;
        }

        // 子任务未全部成功，主任务判定为失败，主任务表只记"子任务有未完成的任务"
        final long unfinished = asyncTaskSubService.countUnfinished(entity.getId());
        if (unfinished > 0) {
            final String msg = String.format(
                "asyncTask group has unfinished subTask, id=%s unfinished=%s success=%s",
                entity.getId(),
                unfinished,
                advanced.get()
            );

            log.warn(msg);

            final long costTime = Optional.ofNullable(entity.getCostTime()).orElse(0L) + System.currentTimeMillis() - start;
            this.getAsyncTaskStateSupport().taskFail(entity, handler, costTime, new RuntimeException(msg), advanced.get() > 0);

            return false;
        }

        // 成功的数要对的上提交时的任务数
        if (subResults.size() < entity.getSubTaskCount()) {
            final String msg = String.format(
                "asyncTask group execute success, but subTaskCount not match, id=%s subTaskCount=%s success=%s",
                entity.getId(),
                entity.getSubTaskCount(),
                subResults.size()
            );

            log.warn(msg);

            final long costTime = Optional.ofNullable(entity.getCostTime()).orElse(0L) + System.currentTimeMillis() - start;
            this.getAsyncTaskStateSupport().taskFail(entity, handler, costTime, new RuntimeException(msg), advanced.get() > 0);

            return false;
        }

        // 子任务全部成功 -> 主任务收尾业务 -> 主任务置成功
        entity.setSubTasks(subResults);
        return true;
    }

    private List<AsyncTaskSubVO> executeSubTask(AsyncTaskVO entity, List<AsyncTaskSubVO> subEntities, long start, AtomicInteger advanced) {
        final List<AsyncTaskSubVO> successResults = this.listPriorSuccess(entity, subEntities);

        final List<List<AsyncTaskSubVO>> batches = this.groupByStage(subEntities);
        for (List<AsyncTaskSubVO> batch : batches) {
            // 设置前置成功子任务结果
            final List<AsyncTaskSubVO> snapshot = List.copyOf(successResults);
            batch.forEach(item -> item.setPriorSubTasks(snapshot));

            final List<AsyncTaskSubVO> results = this.executeSubTaskGroups(entity, batch, start, advanced);

            // 当前批次有失败，停止后续阶段
            if (!results.stream().allMatch(this::isSuccess)) {
                return successResults;
            }

            // 成功结果传递下一阶段
            successResults.addAll(results);

            // 更新主任务进度，以数据库中的成功子任务总数为准
            this.editTaskProgress(entity, start);
        }

        return successResults;
    }

    private List<AsyncTaskSubVO> listPriorSuccess(AsyncTaskVO entity, List<AsyncTaskSubVO> subEntities) {
        if (subEntities.size() >= entity.getSubTaskCount()) {
            return new ArrayList<>(10);
        }

        return asyncTaskSubService.listByAsyncTaskIdAndStatus(
            entity.getId(),
            List.of(AsyncTaskStatusEnum.SUCCESS)
        );
    }

    private List<List<AsyncTaskSubVO>> groupByStage(List<AsyncTaskSubVO> subEntities) {
        List<List<AsyncTaskSubVO>> batches = new ArrayList<>(10);
        List<AsyncTaskSubVO> currentBatch = null;
        Integer currentStage = null;

        for (AsyncTaskSubVO subEntity : subEntities) {
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

    private List<AsyncTaskSubVO> executeSubTaskGroups(AsyncTaskVO entity, List<AsyncTaskSubVO> batch, long start, AtomicInteger advanced) {
        // 单任务直接执行
        if (batch.size() == 1) {
            return List.of(executeOneSubTask(entity, batch.get(0), start, advanced));
        }
        // 多任务并发执行
        return this.executeBatchSubTask(entity, batch, start, advanced);
    }

    private AsyncTaskSubVO executeOneSubTask(AsyncTaskVO entity, AsyncTaskSubVO subEntity, long start, AtomicInteger advanced) {
        AsyncTaskSubHandler handler = this.getSubTaskHandler(subEntity.getExecuteName());

        // 判断是否是异步等待状态
        if (AsyncTaskStatusEnum.WAITING.equals(subEntity.getStatus())) {
            return this.executeSubCallbackAsyncWait(entity, handler, subEntity, start, advanced);
        }

        // 更新子任务状态为执行中。expireTime 仅用于异步等待截止时间，进入执行中时不续期
        final boolean status = asyncTaskSubService.editStatusById(AsyncTaskStatusEditDTO.builder()
            .id(subEntity.getId())
            .to(AsyncTaskStatusEnum.EXECUTING)
            .build());
        if (!status) {
            log.warn("asyncTask subTask execute failed, id={} asyncTaskId={} seq={}",
                subEntity.getId(), entity.getId(), subEntity.getSeq());
            return subEntity;
        }

        try {
            // 执行业务逻辑（无事务），由业务方显式返回执行结果
            final AsyncTaskExecuteResultVO result = handler.executeSub(subEntity);

            this.handleSubExecuteResult(entity, subEntity, handler, start, result, advanced);
        } catch (Exception ex) {
            log.error("asyncTask subTask execute failed, id={} asyncTaskId={} seq={}",
                subEntity.getId(), entity.getId(), subEntity.getSeq(), ex);

            final long costTime = Optional.ofNullable(subEntity.getCostTime()).orElse(0L) + System.currentTimeMillis() - start;
            this.getAsyncTaskStateSupport().subTaskFail(subEntity, handler, costTime, ex);
        }

        return subEntity;
    }

    private boolean handleSubExecuteResult(AsyncTaskVO entity, AsyncTaskSubVO subEntity, AsyncTaskSubHandler handler,
        long start, AsyncTaskExecuteResultVO executeResult, AtomicInteger advanced) {
        if (executeResult == null) {
            return false;
        }

        final AsyncTaskExecuteResultEnum result = executeResult.getStatus();

        // 业务显式返回执行中
        if (AsyncTaskExecuteResultEnum.EXECUTE.equals(result)) {
            return false;
        }

        // 业务显式返回异步等待
        if (AsyncTaskExecuteResultEnum.WAITING.equals(result)) {
            final long costTime = Optional.ofNullable(subEntity.getCostTime()).orElse(0L) + System.currentTimeMillis() - start;
            if (!this.getAsyncTaskStateSupport().subTaskAsyncWait(subEntity, handler, costTime)) {
                log.warn("asyncTask subTask execute waiting failed, id={} asyncTaskId={} seq={}",
                    subEntity.getId(), entity.getId(), subEntity.getSeq());
            }
            return false;
        }

        // 业务显式返回成功
        if (AsyncTaskExecuteResultEnum.SUCCESS.equals(result)) {
            // 合并业务返回值到 result
            this.mergeSubResult(subEntity, executeResult.getResult());
            final long costTime = Optional.ofNullable(subEntity.getCostTime()).orElse(0L) + System.currentTimeMillis() - start;
            if (!this.getAsyncTaskStateSupport().subTaskSuccess(subEntity, handler, costTime)) {
                log.warn("asyncTask subTask execute success failed, id={} asyncTaskId={} seq={}",
                    subEntity.getId(), entity.getId(), subEntity.getSeq());

                return false;
            }
            advanced.incrementAndGet();
            return true;
        }

        // 业务显式返回失败
        if (AsyncTaskExecuteResultEnum.FAILED.equals(result)) {
            final long costTime = Optional.ofNullable(subEntity.getCostTime()).orElse(0L) + System.currentTimeMillis() - start;
            final String msg = String.format(
                "asyncTask subTask execute failed, id=%s asyncTaskId=%s seq=%s",
                subEntity.getId(), entity.getId(), subEntity.getSeq()
            );
            if (!this.getAsyncTaskStateSupport().subTaskFail(subEntity, handler, costTime, new RuntimeException(msg))) {
                log.warn(msg);
            }
            return false;
        }

        return false;
    }

    private List<AsyncTaskSubVO> executeBatchSubTask(AsyncTaskVO entity, List<AsyncTaskSubVO> batch, long start, AtomicInteger advanced) {
        List<CompletableFuture<AsyncTaskSubVO>> futures = batch.stream()
            .map(task ->
                CompletableFuture.supplyAsync(
                    () ->
                        executeOneSubTask(entity, task, start, advanced)
                    , threadPoolExecutor
                )
            ).toList();

        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        return futures.stream()
            .map(CompletableFuture::join)
            .toList();
    }

    private AsyncTaskSubVO executeSubCallbackAsyncWait(
        AsyncTaskVO entity, AsyncTaskSubHandler handler,
        AsyncTaskSubVO subEntity, long start, AtomicInteger advanced) {
        final AsyncTaskCallbackResultVO callbackResult = this.getSubTaskCallback(entity, subEntity, handler);
        if (callbackResult == null) {
            return subEntity;
        }

        final AsyncTaskCallbackResultEnum status = callbackResult.getStatus();
        if (AsyncTaskCallbackResultEnum.SUCCESS.equals(status)) {
            final long costTime = Optional.ofNullable(subEntity.getCostTime()).orElse(0L) + System.currentTimeMillis() - start;
            if (!this.getAsyncTaskStateSupport().subTaskAsyncWaitSuccess(subEntity, handler, costTime)) {
                log.warn("asyncTask subTask execute callback failed, id={} asyncTaskId={} seq={}",
                    subEntity.getId(), entity.getId(), subEntity.getSeq());

                return subEntity;
            }

            advanced.incrementAndGet();
            return subEntity;
        }

        if (AsyncTaskCallbackResultEnum.FAILED.equals(status)) {
            final long costTime = Optional.ofNullable(subEntity.getCostTime()).orElse(0L) + System.currentTimeMillis() - start;
            final String msg = String.format(
                "asyncTask subTask execute callback failed, id=%s asyncTaskId=%s seq=%s",
                subEntity.getId(), entity.getId(), subEntity.getSeq()
            );
            if (!this.getAsyncTaskStateSupport().subTaskAsyncWaitFail(subEntity, handler, costTime, new RuntimeException(msg))) {
                log.warn(msg);

                return subEntity;
            }

            return subEntity;
        }

        // 回调等待截止时间到期，转失败走重试链路，避免无限轮询
        final Long expireTime = subEntity.getExpireTime();
        if (Objects.nonNull(expireTime) && expireTime > 0 && expireTime <= System.currentTimeMillis()) {
            final long costTime = Optional.ofNullable(subEntity.getCostTime()).orElse(0L) + System.currentTimeMillis() - start;
            final String msg = String.format(
                "asyncTask subTask execute callback timeout, id=%s asyncTaskId=%s seq=%s",
                subEntity.getId(), entity.getId(), subEntity.getSeq()
            );
            if (!this.getAsyncTaskStateSupport().subTaskAsyncWaitFail(subEntity, handler, costTime, new RuntimeException(msg))) {
                log.warn(msg);

                return subEntity;
            }

            return subEntity;
        }

        return subEntity;
    }

    private AsyncTaskCallbackResultVO getSubTaskCallback(AsyncTaskVO entity, AsyncTaskSubVO subEntity, AsyncTaskSubHandler handler) {
        // 回调通知预存结果优先消费，不再调用业务侧查询
        final AsyncTaskCallbackResultEnum notified = AsyncTaskCallbackResultEnum.fromResultMap(subEntity.getResult());
        if (Objects.nonNull(notified)) {
            this.consumeNotifiedResult(subEntity);
            return AsyncTaskCallbackResultVO.setStatus(notified);
        }
        try {
            final AsyncTaskCallbackResultVO result = handler.executeSubCallback(subEntity);
            return Objects.isNull(result) ? AsyncTaskCallbackResultVO.waiting() : result;
        } catch (Exception ex) {
            log.error("asyncTask subTask callback failed, id={} asyncTaskId={} seq={}",
                subEntity.getId(), entity.getId(), subEntity.getSeq(), ex);

            return AsyncTaskCallbackResultVO.waiting();
        }
    }

    /**
     * 消费预存结果后清理保留键，避免失败重试后再次读到旧预存结果.
     *
     * @param subEntity 子任务视图对象
     */
    private void consumeNotifiedResult(AsyncTaskSubVO subEntity) {
        final Map<String, Object> result = subEntity.getResult();
        result.remove(AsyncTaskConstant.CALLBACK_RESULT_KEY);
        result.remove(AsyncTaskConstant.CALLBACK_ERROR_MSG_KEY);
        this.asyncTaskSubService.editStatusById(AsyncTaskStatusEditDTO.builder()
            .id(subEntity.getId())
            .result(result)
            .build());
    }

    private boolean isSuccess(AsyncTaskSubVO entity) {
        return AsyncTaskStatusEnum.SUCCESS.equals(entity.getStatus());
    }

    private boolean hasSubAsyncWait(List<AsyncTaskSubVO> subEntities) {
        return subEntities.stream().anyMatch(item -> AsyncTaskStatusEnum.WAITING.equals(item.getStatus()));
    }

    /**
     * 判断子任务是否在等异步调用的结果，保持主任务为执行中状态，更新进度.
     * <p>不能变成失败，失败会占用重试次数</p>
     *
     * @param entity      主任务
     * @param subEntities 子任务列表
     * @param start       开始时间
     * @return true=有子任务在异步等待中
     */
    private boolean handleSubAsyncWait(AsyncTaskVO entity, List<AsyncTaskSubVO> subEntities, long start) {
        if (!this.hasSubAsyncWait(subEntities)) {
            return false;
        }
        // 保持主任务为执行中状态，更新进度和耗时
        final long costTime = Optional.ofNullable(entity.getCostTime()).orElse(0L) + System.currentTimeMillis() - start;
        final int completedCount = this.getCompletedCount(entity);
        final int progress = this.calculateProgress(entity.getSubTaskCount(), completedCount);
        if (!this.getAsyncTaskStateSupport().taskProgress(entity, costTime, progress)) {
            log.warn("asyncTask update progress failed, id={}", entity.getId());
        }
        log.info("asyncTask has asyncWait, keep execute status, id={}, progress={}%", entity.getId(), progress);
        return true;
    }

    /**
     * 获取已成功的子任务总数，避免本轮无新增完成任务时进度回退.
     *
     * @param entity 主任务
     * @return 已成功子任务数
     */
    private int getCompletedCount(AsyncTaskVO entity) {
        if (entity.getSubTaskCount() == null || entity.getSubTaskCount() <= 0) {
            return 0;
        }
        final long unfinishedCount = this.asyncTaskSubService.countUnfinished(entity.getId());
        return Math.max(0, entity.getSubTaskCount() - (int) unfinishedCount);
    }

    /**
     * 计算主任务进度.
     *
     * @param totalSubTasks     子任务总数
     * @param completedSubTasks 已完成子任务数
     * @return 进度百分比（0-100）
     */
    private int calculateProgress(Integer totalSubTasks, int completedSubTasks) {
        if (totalSubTasks == null || totalSubTasks <= 0) {
            return 0;
        }
        return (int) ((completedSubTasks * 100.0) / totalSubTasks);
    }

    /**
     * 更新主任务进度.
     *
     * @param entity         主任务
     * @param start          开始时间
     */
    private void editTaskProgress(AsyncTaskVO entity, long start) {
        final long costTime = Optional.ofNullable(entity.getCostTime()).orElse(0L) + System.currentTimeMillis() - start;
        final int completedCount = this.getCompletedCount(entity);
        final int progress = this.calculateProgress(entity.getSubTaskCount(), completedCount);
        if (!this.getAsyncTaskStateSupport().taskProgress(entity, costTime, progress)) {
            log.warn("asyncTask update progress failed, id={}, progress={}%", entity.getId(), progress);
        } else {
            log.info("asyncTask progress updated, id={}, progress={}%, completed={}/{}",
                entity.getId(), progress, completedCount, entity.getSubTaskCount());
        }
    }

    /**
     * 合并业务返回值到子任务 result.
     */
    private void mergeSubResult(AsyncTaskSubVO entity, Map<String, Object> businessResult) {
        if (CollUtil.isEmpty(businessResult)) {
            return;
        }
        entity.setResult(new HashMap<>(businessResult));
    }

    private AsyncTaskSubHandler getSubTaskHandler(String executeName) {
        return this.asyncTaskSubHandlerStrategyFactory.getTaskHandler(executeName);
    }

}

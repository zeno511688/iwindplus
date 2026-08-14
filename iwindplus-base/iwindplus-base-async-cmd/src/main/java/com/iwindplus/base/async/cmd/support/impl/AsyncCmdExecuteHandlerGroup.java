/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.support.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdStatusEditDTO;
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
import java.util.Map;
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

        // 判断是否是异步等待状态
        if (AsyncCmdStatusEnum.ASYNC_WAIT.equals(entity.getStatus())) {
            this.executeCallbackAsyncWait(entity, handler, start);
            return;
        }

        final AtomicInteger advanced = new AtomicInteger(0);

        try {
            // 先子任务 → 主收尾 → 成功
            final List<AsyncCmdSubVO> subEntities = this.asyncCmdSubService.listByAsyncCmdIdAndStatus(
                entity.getId(), AsyncCmdStatusEnum.getUnfinishedStatus());

            if (!this.executeSubTaskResult(entity, handler, start, subEntities, advanced)) {
                return;
            }

            // 主收尾业务前续期执行租约（子任务执行期间租约可能已接近到期）
            this.getAsyncCmdService().editExpireTime(entity.getId());
            handler.execute(entity);
            this.taskSuccess(entity, handler, start);
        } catch (Exception ex) {
            log.error("asyncCmd task execute failed. id={}", entity.getId(), ex);

            this.getAsyncCmdStateSupport().taskFail(entity, handler,
                System.currentTimeMillis() - start, ex, advanced.get() > 0);
        }
    }

    private boolean executeSubTaskResult(AsyncCmdVO entity, AsyncCmdTaskHandler handler, long start,
        List<AsyncCmdSubVO> subEntities, AtomicInteger advanced) {
        // 执行子任务，返回成功的个数
        final List<AsyncCmdSubVO> subResults = this.executeSubTask(entity, subEntities, advanced);

        // 判断子任务是否在等异步调用的结果，不能变成失败，失败会占用重试次数
        if (this.handleSubAsyncWait(entity, subEntities, start)) {
            return false;
        }

        // 子任务未全部成功，主任务判定为失败，主任务表只记"子任务有未完成的任务"
        final long unfinished = asyncCmdSubService.countUnfinished(entity.getId());
        if (unfinished > 0) {
            final String msg = String.format(
                "asyncCmd group has unfinished subTask, id=%s unfinished=%s success=%s",
                entity.getId(),
                unfinished,
                advanced.get()
            );

            log.warn(msg);

            this.getAsyncCmdStateSupport().taskFail(entity, handler,
                System.currentTimeMillis() - start,
                new RuntimeException(msg),
                advanced.get() > 0);

            return false;
        }

        // 成功的数要对的上提交时的任务数
        if (subResults.size() < entity.getSubTaskCount()) {
            final String msg = String.format(
                "asyncCmd group execute success, but subTaskCount not match, id=%s subTaskCount=%s success=%s",
                entity.getId(),
                entity.getSubTaskCount(),
                subResults.size()
            );

            log.warn(msg);

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
        for (int index = 0; index < batches.size(); index++) {
            final List<AsyncCmdSubVO> batch = batches.get(index);
            // 设置前置成功子任务结果
            final List<AsyncCmdSubVO> snapshot = List.copyOf(successResults);
            batch.forEach(item -> item.setPriorSubTasks(snapshot));

            final List<AsyncCmdSubVO> results = this.executeSubTaskGroups(entity, batch, advanced);

            // 当前批次有失败，停止后续阶段
            if (!results.stream().allMatch(this::isSuccess)) {
                // 批次内有子任务进入异步等待（非失败）：预置后续所有占位子任务为执行中，
                // 第三方处理期间（回调到达前）进度可见
                if (this.hasSubAsyncWait(results)) {
                    this.markFollowingPlaceholdersExecuting(batches, index);
                }
                return successResults;
            }

            // 成功结果传递下一阶段
            successResults.addAll(results);
        }

        return successResults;
    }

    /**
     * 批次内子任务进入异步等待时，将后续连续的进度占位子任务全部预置为执行中.
     * <p>第三方处理期间（回调到达前）进度状态可见；回调到达恢复执行后，
     * 占位子任务已在执行中，按自身逻辑直接置成功.</p>
     * <p>从当前批次的下一批起向后扫描，仅处理占位批次，遇到含执行器的批次即停止，
     * 避免把未执行任务之后的占位也置为执行中.</p>
     *
     * @param batches 全部批次
     * @param index   当前批次下标
     */
    private void markFollowingPlaceholdersExecuting(List<List<AsyncCmdSubVO>> batches, int index) {
        // 从当前批次的下一个批次开始，只扫描连续的占位批次。
        // takeWhile 保证遇到第一个非占位批次后立即停止，不再处理后续批次。
        final List<AsyncCmdSubVO> placeholders = batches.stream()
            .skip(index + 1)
            .takeWhile(batch -> batch.size() == 1
                && CharSequenceUtil.isBlank(batch.get(0).getExecuteName()))
            // 每个占位批次强制只有一个子任务，因此直接取出该任务。
            .map(batch -> batch.get(0))
            // 已经处于 EXECUTE 或其他状态的任务无需重复预置。
            .filter(item -> AsyncCmdStatusEnum.TO_BE_EXECUTE.equals(item.getStatus()))
            .toList();
        // 没有需要预置的占位任务，直接结束。
        if (placeholders.isEmpty()) {
            return;
        }

        // 收集需要更新的子任务 ID，后续通过单条 SQL 批量执行 CAS 更新。
        final List<Long> ids = placeholders.stream()
            .map(AsyncCmdSubVO::getId)
            .toList();

        // 使用 TO_BE_EXECUTE → EXECUTE 的 CAS 条件更新，
        // 防止任务已经被其他线程/节点抢先执行后又被重复预置。
        final boolean success = asyncCmdSubService.editStatusByIds(
            ids,
            AsyncCmdStatusEditDTO.builder()
                .from(AsyncCmdStatusEnum.TO_BE_EXECUTE)
                .to(AsyncCmdStatusEnum.EXECUTE)
                .build()
        );

        if (!success) {
            log.warn("asyncCmd placeholder subTask pre-execute failed, ids={}", ids);
            return;
        }

        // 数据库批量 CAS 更新成功，同步更新内存对象状态。
        placeholders.forEach(item -> item.setStatus(AsyncCmdStatusEnum.EXECUTE));
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
            // 进度占位子任务仅适合串行：无论stage如何，强制单独一批，不与任何任务并发
            if (CharSequenceUtil.isBlank(subEntity.getExecuteName())) {
                batches.add(new ArrayList<>(List.of(subEntity)));
                currentBatch = null;
                currentStage = null;
                continue;
            }

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
        // 进度占位子任务（无执行器）：执行到位后直接置成功
        if (CharSequenceUtil.isBlank(subEntity.getExecuteName())) {
            return this.executePlaceholderSubTask(entity, subEntity, advanced);
        }

        AsyncCmdSubTaskHandler handler = this.getSubTaskHandler(subEntity.getExecuteName());

        // 判断是否是异步等待状态
        if (AsyncCmdStatusEnum.ASYNC_WAIT.equals(subEntity.getStatus())) {
            return this.executeSubCallbackAsyncWait(entity, handler, subEntity, advanced);
        }

        // 续期
        this.getAsyncCmdService().editExpireTime(entity.getId());

        final boolean status = asyncCmdSubService.editStatusById(AsyncCmdStatusEditDTO.builder()
            .id(subEntity.getId())
            .to(AsyncCmdStatusEnum.EXECUTE)
            .build());
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

    /**
     * 执行进度占位子任务（无执行器，无业务执行，执行到位后直接置成功）.
     *
     * @param entity    异步命令视图对象
     * @param subEntity 子任务视图对象
     * @param advanced  推进子任务数计数器
     * @return AsyncCmdSubVO
     */
    private AsyncCmdSubVO executePlaceholderSubTask(AsyncCmdVO entity, AsyncCmdSubVO subEntity, AtomicInteger advanced) {
        // 续期
        this.getAsyncCmdService().editExpireTime(entity.getId());

        if (AsyncCmdStatusEnum.SUCCESS.equals(subEntity.getStatus())) {
            return subEntity;
        }

        // 无业务可执行，转执行中后直接置成功
        final boolean subTaskSuccess = this.getAsyncCmdStateSupport().subTaskSuccess(subEntity, null, 0L);
        if (!subTaskSuccess) {
            log.warn("asyncCmd placeholder subTask success failed, id={} asyncCmdId={} seq={}",
                subEntity.getId(), entity.getId(), subEntity.getSeq());
            return subEntity;
        }

        advanced.incrementAndGet();
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

        // 回调等待截止时间到期，转失败走重试链路，避免无限轮询
        final Long expireTime = subEntity.getExpireTime();
        if (Objects.nonNull(expireTime) && expireTime > 0 && expireTime <= System.currentTimeMillis()) {
            this.getAsyncCmdStateSupport().subTaskAsyncWaitFail(subEntity, handler,
                new RuntimeException("asyncCmd subTask callback timeout"));

            return subEntity;
        }

        return subEntity;
    }

    private AsyncCmdCallbackResultEnum getSubTaskCallback(AsyncCmdVO entity, AsyncCmdSubVO subEntity, AsyncCmdSubTaskHandler handler) {
        // 回调通知预存结果优先消费，不再调用业务侧查询
        final AsyncCmdCallbackResultEnum notified = AsyncCmdCallbackResultEnum.fromResultMap(subEntity.getResult());
        if (Objects.nonNull(notified)) {
            this.consumeNotifiedResult(subEntity);
            return notified;
        }
        try {
            final AsyncCmdCallbackResultEnum result = handler.executeSubCallback(subEntity);
            return Objects.isNull(result) ? AsyncCmdCallbackResultEnum.WAITING : result;
        } catch (Exception ex) {
            log.error("asyncCmd subTask callback failed, id={} asyncCmdId={} seq={}",
                subEntity.getId(), entity.getId(), subEntity.getSeq(), ex);

            return AsyncCmdCallbackResultEnum.WAITING;
        }
    }

    /**
     * 消费预存结果后清理保留键，避免失败重试后再次读到旧预存结果.
     *
     * @param subEntity 子任务视图对象
     */
    private void consumeNotifiedResult(AsyncCmdSubVO subEntity) {
        final Map<String, Object> result = subEntity.getResult();
        result.remove(AsyncCmdCallbackResultEnum.CALLBACK_RESULT_KEY);
        result.remove(AsyncCmdCallbackResultEnum.CALLBACK_ERROR_MSG_KEY);
        this.asyncCmdSubService.editStatusById(AsyncCmdStatusEditDTO.builder()
            .id(subEntity.getId())
            .result(result)
            .build());
    }

    private boolean isSuccess(AsyncCmdSubVO entity) {
        return AsyncCmdStatusEnum.SUCCESS.equals(entity.getStatus());
    }

    private boolean hasSubAsyncWait(List<AsyncCmdSubVO> subEntities) {
        return subEntities.stream().anyMatch(item -> AsyncCmdStatusEnum.ASYNC_WAIT.equals(item.getStatus()));
    }

    /**
     * 判断子任务是否在等异步调用的结果，将主任务设置为待执行.
     * <p>不能变成失败，失败会占用重试次数</p>
     *
     * @param entity      主任务
     * @param subEntities 子任务列表
     * @param start       开始时间
     * @return true=有子任务在异步等待中，主任务已转为待执行
     */
    private boolean handleSubAsyncWait(AsyncCmdVO entity, List<AsyncCmdSubVO> subEntities, long start) {
        if (!this.hasSubAsyncWait(subEntities)) {
            return false;
        }
        // 将主任务设置为待执行，等待下一次执行
        final boolean toBeExecute = this.getAsyncCmdStateSupport().taskExecuteToBeExecute(
            entity, System.currentTimeMillis() - start);
        if (!toBeExecute) {
            log.warn("asyncCmd has asyncWait, id={}", entity.getId());

            return false;
        }
        return true;
    }

    private AsyncCmdSubTaskHandler getSubTaskHandler(String executeName) {
        return this.asyncCmdSubTaskHandlerStrategyFactory.getTaskHandler(executeName);
    }

}

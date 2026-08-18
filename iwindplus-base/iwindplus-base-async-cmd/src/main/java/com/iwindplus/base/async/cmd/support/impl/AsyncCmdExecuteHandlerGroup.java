/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.support.impl;

import cn.hutool.core.collection.CollUtil;
import com.iwindplus.base.async.cmd.domain.constant.AsyncCmdConstant;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdEditDTO;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdExecuteResultVO;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdStatusEditDTO;
import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdCallbackResultEnum;
import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdExecuteResultEnum;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
        if (AsyncCmdStatusEnum.WAITING.equals(entity.getStatus())) {
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

            //  主收尾业务前续期执行租约
            this.getAsyncCmdService().edit(
                AsyncCmdEditDTO.builder()
                    .id(entity.getId())
                    .expireTime(this.getAsyncCmdService().getNextExpireTime(System.currentTimeMillis()))
                    .build()
            );

            // 执行业务逻辑（无事务），由业务方显式返回执行结果
            final AsyncCmdExecuteResultVO result = handler.execute(entity);

            this.handleExecuteResult(entity, handler, start, result, advanced.get() > 0);
        } catch (Exception ex) {
            log.error("asyncCmd task execute failed. id={}", entity.getId(), ex);

            final long costTime = Optional.ofNullable(entity.getCostTime()).orElse(0L) + System.currentTimeMillis() - start;
            this.getAsyncCmdStateSupport().taskFail(entity, handler, costTime, ex, advanced.get() > 0);
        }
    }

    private boolean executeSubTaskResult(AsyncCmdVO entity, AsyncCmdTaskHandler handler, long start,
        List<AsyncCmdSubVO> subEntities, AtomicInteger advanced) {
        // 执行子任务，返回成功的个数
        final List<AsyncCmdSubVO> subResults = this.executeSubTask(entity, subEntities, start, advanced);

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

            final long costTime = Optional.ofNullable(entity.getCostTime()).orElse(0L) + System.currentTimeMillis() - start;
            this.getAsyncCmdStateSupport().taskFail(entity, handler, costTime, new RuntimeException(msg), advanced.get() > 0);

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

            final long costTime = Optional.ofNullable(entity.getCostTime()).orElse(0L) + System.currentTimeMillis() - start;
            this.getAsyncCmdStateSupport().taskFail(entity, handler, costTime, new RuntimeException(msg), advanced.get() > 0);

            return false;
        }

        // 子任务全部成功 -> 主任务收尾业务 -> 主任务置成功
        entity.setSubTasks(subResults);
        return true;
    }

    private List<AsyncCmdSubVO> executeSubTask(AsyncCmdVO entity, List<AsyncCmdSubVO> subEntities, long start, AtomicInteger advanced) {
        final List<AsyncCmdSubVO> successResults = this.listPriorSuccess(entity, subEntities);

        final List<List<AsyncCmdSubVO>> batches = this.groupByStage(subEntities);
        for (List<AsyncCmdSubVO> batch : batches) {
            // 设置前置成功子任务结果
            final List<AsyncCmdSubVO> snapshot = List.copyOf(successResults);
            batch.forEach(item -> item.setPriorSubTasks(snapshot));

            final List<AsyncCmdSubVO> results = this.executeSubTaskGroups(entity, batch, start, advanced);

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

    private List<AsyncCmdSubVO> executeSubTaskGroups(AsyncCmdVO entity, List<AsyncCmdSubVO> batch, long start, AtomicInteger advanced) {
        // 单任务直接执行
        if (batch.size() == 1) {
            return List.of(executeOneSubTask(entity, batch.get(0), start, advanced));
        }
        // 多任务并发执行
        return this.executeBatchSubTask(entity, batch, start, advanced);
    }

    private AsyncCmdSubVO executeOneSubTask(AsyncCmdVO entity, AsyncCmdSubVO subEntity, long start, AtomicInteger advanced) {
        AsyncCmdSubTaskHandler handler = this.getSubTaskHandler(subEntity.getExecuteName());

        // 判断是否是异步等待状态
        if (AsyncCmdStatusEnum.WAITING.equals(subEntity.getStatus())) {
            return this.executeSubCallbackAsyncWait(entity, handler, subEntity, start, advanced);
        }

        // 续期
        this.getAsyncCmdService().edit(
            AsyncCmdEditDTO.builder()
                .id(entity.getId())
                .expireTime(this.getAsyncCmdService().getNextExpireTime(System.currentTimeMillis()))
                .build()
        );

        final boolean status = asyncCmdSubService.editStatusById(AsyncCmdStatusEditDTO.builder()
            .id(subEntity.getId())
            .to(AsyncCmdStatusEnum.EXECUTE)
            .build());
        if (!status) {
            log.warn("asyncCmd subTask execute failed, id={} asyncCmdId={} seq={}",
                subEntity.getId(), entity.getId(), subEntity.getSeq());
            return subEntity;
        }

        try {
            // 执行业务逻辑（无事务），由业务方显式返回执行结果
            final AsyncCmdExecuteResultVO result = handler.executeSub(subEntity);

            this.handleSubExecuteResult(entity, subEntity, handler, start, result, advanced);
        } catch (Exception ex) {
            log.error("asyncCmd subTask execute failed, id={} asyncCmdId={} seq={}",
                subEntity.getId(), entity.getId(), subEntity.getSeq(), ex);

            final long costTime = Optional.ofNullable(subEntity.getCostTime()).orElse(0L) + System.currentTimeMillis() - start;
            this.getAsyncCmdStateSupport().subTaskFail(subEntity, handler, costTime, ex);
        }

        return subEntity;
    }

    private boolean handleSubExecuteResult(AsyncCmdVO entity, AsyncCmdSubVO subEntity, AsyncCmdSubTaskHandler handler,
        long start, AsyncCmdExecuteResultVO executeResult, AtomicInteger advanced) {

        final AsyncCmdExecuteResultEnum result = executeResult.getStatus();

        // 业务显式返回执行中
        if (AsyncCmdExecuteResultEnum.EXECUTE.equals(result)) {
            return false;
        }

        // 业务显式返回异步等待
        if (AsyncCmdExecuteResultEnum.WAITING.equals(result)) {
            final long costTime = Optional.ofNullable(subEntity.getCostTime()).orElse(0L) + System.currentTimeMillis() - start;
            if (!this.getAsyncCmdStateSupport().subTaskAsyncWait(subEntity, handler, costTime)) {
                log.warn("asyncCmd subTask execute waiting failed, id={} asyncCmdId={} seq={}",
                    subEntity.getId(), entity.getId(), subEntity.getSeq());
            }
            return false;
        }

        // 业务显式返回成功
        if (AsyncCmdExecuteResultEnum.SUCCESS.equals(result)) {
            // 合并业务返回值到 result
            this.mergeSubResult(subEntity, executeResult.getResult());
            final long costTime = Optional.ofNullable(subEntity.getCostTime()).orElse(0L) + System.currentTimeMillis() - start;
            if (!this.getAsyncCmdStateSupport().subTaskSuccess(subEntity, handler, costTime)) {
                log.warn("asyncCmd subTask execute success failed, id={} asyncCmdId={} seq={}",
                    subEntity.getId(), entity.getId(), subEntity.getSeq());

                return false;
            }
            advanced.incrementAndGet();
            return true;
        }

        // 业务显式返回失败
        if (AsyncCmdExecuteResultEnum.FAILED.equals(result)) {
            final long costTime = Optional.ofNullable(subEntity.getCostTime()).orElse(0L) + System.currentTimeMillis() - start;
            final String msg = String.format(
                "asyncCmd subTask execute failed, id=%s asyncCmdId=%s seq=%s",
                subEntity.getId(), entity.getId(), subEntity.getSeq()
            );
            if (!this.getAsyncCmdStateSupport().subTaskFail(subEntity, handler, costTime, new RuntimeException(msg))) {
                log.warn(msg);
            }
            return false;
        }

        return false;
    }

    private List<AsyncCmdSubVO> executeBatchSubTask(AsyncCmdVO entity, List<AsyncCmdSubVO> batch, long start, AtomicInteger advanced) {
        List<CompletableFuture<AsyncCmdSubVO>> futures = batch.stream()
            .map(task ->
                CompletableFuture.supplyAsync(
                    () ->
                        executeOneSubTask(entity, task, start, advanced)
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
        AsyncCmdSubVO subEntity, long start, AtomicInteger advanced) {

        final AsyncCmdCallbackResultEnum callbackResult = this.getSubTaskCallback(entity, subEntity, handler);
        if (AsyncCmdCallbackResultEnum.SUCCESS.equals(callbackResult)) {
            final long costTime = Optional.ofNullable(subEntity.getCostTime()).orElse(0L) + System.currentTimeMillis() - start;
            if (!this.getAsyncCmdStateSupport().subTaskAsyncWaitSuccess(subEntity, handler, costTime)) {
                log.warn("asyncCmd subTask execute callback failed, id={} asyncCmdId={} seq={}",
                    subEntity.getId(), entity.getId(), subEntity.getSeq());

                return subEntity;
            }

            advanced.incrementAndGet();
            return subEntity;
        }

        if (AsyncCmdCallbackResultEnum.FAILED.equals(callbackResult)) {
            final long costTime = Optional.ofNullable(subEntity.getCostTime()).orElse(0L) + System.currentTimeMillis() - start;
            final String msg = String.format(
                "asyncCmd subTask execute callback failed, id=%s asyncCmdId=%s seq=%s",
                subEntity.getId(), entity.getId(), subEntity.getSeq()
            );
            if (!this.getAsyncCmdStateSupport().subTaskAsyncWaitFail(subEntity, handler, costTime, new RuntimeException(msg))) {
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
                "asyncCmd subTask execute callback timeout, id=%s asyncCmdId=%s seq=%s",
                subEntity.getId(), entity.getId(), subEntity.getSeq()
            );
            if (!this.getAsyncCmdStateSupport().subTaskAsyncWaitFail(subEntity, handler, costTime, new RuntimeException(msg))) {
                log.warn(msg);

                return subEntity;
            }

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
        result.remove(AsyncCmdConstant.CALLBACK_RESULT_KEY);
        result.remove(AsyncCmdConstant.CALLBACK_ERROR_MSG_KEY);
        this.asyncCmdSubService.editStatusById(AsyncCmdStatusEditDTO.builder()
            .id(subEntity.getId())
            .result(result)
            .build());
    }

    private boolean isSuccess(AsyncCmdSubVO entity) {
        return AsyncCmdStatusEnum.SUCCESS.equals(entity.getStatus());
    }

    private boolean hasSubAsyncWait(List<AsyncCmdSubVO> subEntities) {
        return subEntities.stream().anyMatch(item -> AsyncCmdStatusEnum.WAITING.equals(item.getStatus()));
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
        final long costTime = Optional.ofNullable(entity.getCostTime()).orElse(0L) + System.currentTimeMillis() - start;
        if (!this.getAsyncCmdStateSupport().taskExecuteToBeExecute(entity, costTime)) {
            log.warn("asyncCmd has asyncWait, id={}", entity.getId());

            return false;
        }
        return true;
    }

    /**
     * 合并业务返回值到子任务 result.
     */
    private void mergeSubResult(AsyncCmdSubVO entity, Map<String, Object> businessResult) {
        if (CollUtil.isEmpty(businessResult)) {
            return;
        }
        entity.setResult(new HashMap<>(businessResult));
    }

    private AsyncCmdSubTaskHandler getSubTaskHandler(String executeName) {
        return this.asyncCmdSubTaskHandlerStrategyFactory.getTaskHandler(executeName);
    }

}

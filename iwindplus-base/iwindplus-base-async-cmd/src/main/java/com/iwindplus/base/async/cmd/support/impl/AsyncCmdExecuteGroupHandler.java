/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.support.impl;

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
public class AsyncCmdExecuteGroupHandler extends AbstractAsyncCmdExecuteHandler {

    private final AsyncCmdSubService asyncCmdSubService;
    private final AsyncCmdSubTaskHandlerStrategyFactory asyncCmdSubTaskHandlerStrategyFactory;
    private final DtpExecutor asyncCmdSubTaskExecutor;

    public AsyncCmdExecuteGroupHandler(
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

        final List<AsyncCmdSubVO> subEntities = this.asyncCmdSubService.listByAsyncCmdIdAndStatus(
            entity.getId(), AsyncCmdStatusEnum.getUnfinishedStatus());
        AtomicInteger advanced = new AtomicInteger(0);
        try {
            // 执行子任务，返回成功的个数
            List<AsyncCmdSubVO> subResults = this.executeSubTask(entity, subEntities, advanced);
            // 子任务未全部成功，主任务判定为失败，主任务表只记"子任务有未完成的任务"
            final long unfinished = asyncCmdSubService.countUnfinished(entity.getId());
            if (unfinished > 0) {
                log.warn("asyncCmd group has unfinished subTask, id={} unfinished={} success={}",
                    entity.getId(), unfinished, advanced.get());

                this.getAsyncCmdStateSupport().taskFail(entity, handler,
                    System.currentTimeMillis() - start,
                    new RuntimeException("asyncCmd group has unfinished subTask"),
                    advanced.get() > 0);
                return;
            }

            // 子任务全部成功 -> 主任务收尾业务 -> 主任务置成功
            entity.setSubTasks(subResults);
            handler.execute(entity);
            this.getAsyncCmdStateSupport().taskSuccess(entity, handler, System.currentTimeMillis() - start);
        } catch (Exception ex) {
            log.error("asyncCmd group execute failed, id={}", entity.getId(), ex);

            this.getAsyncCmdStateSupport().taskFail(entity, handler, System.currentTimeMillis() - start, ex, advanced.get() > 0);
        }
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
                currentBatch = new ArrayList<>();
                batches.add(currentBatch);
                currentStage = stage;
            }

            currentBatch.add(subEntity);
        }

        return batches;
    }

    private List<AsyncCmdSubVO> executeSubTaskGroups(AsyncCmdVO entity, List<AsyncCmdSubVO> batch, AtomicInteger advanced) {
        final List<AsyncCmdSubVO> results;
        // 单任务直接执行
        if (batch.size() == 1) {
            results = List.of(executeOneSubTask(entity, batch.get(0)));
        } else {
            // 多任务并发执行
            results = this.executeBatchSubTask(entity, batch);
        }

        int successCount =
            (int) results.stream()
                .filter(this::isSuccess)
                .count();
        advanced.addAndGet(successCount);
        return results;
    }

    private AsyncCmdSubVO executeOneSubTask(AsyncCmdVO entity, AsyncCmdSubVO subEntity) {
        AsyncCmdSubTaskHandler handler = getSubTaskHandler(subEntity.getExecuteName());
        long start = System.currentTimeMillis();

        // 续期
        this.getAsyncCmdService().editExpireTime(entity.getId());
        asyncCmdSubService.editStatusById(subEntity.getId(), AsyncCmdStatusEnum.EXECUTE);

        try {
            // 执行业务逻辑（无事务）
            handler.executeSub(subEntity);
            // 成功
            this.getAsyncCmdStateSupport().subTaskSuccess(subEntity, handler, System.currentTimeMillis() - start);
        } catch (Exception ex) {
            log.error("asyncCmd subTask execute failed, id={} asyncCmdId={} seq={}",
                subEntity.getId(), entity.getId(), subEntity.getSeq(), ex);

            this.getAsyncCmdStateSupport().subTaskFail(subEntity, handler, System.currentTimeMillis() - start, ex);
        }

        return subEntity;
    }

    private List<AsyncCmdSubVO> executeBatchSubTask(AsyncCmdVO entity, List<AsyncCmdSubVO> batch) {
        List<CompletableFuture<AsyncCmdSubVO>> futures = batch.stream()
            .map(task ->
                CompletableFuture.supplyAsync(
                    () ->
                        executeOneSubTask(entity, task)
                    , asyncCmdSubTaskExecutor
                )
            ).toList();

        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        return futures.stream()
            .map(CompletableFuture::join)
            .toList();
    }

    private boolean isSuccess(AsyncCmdSubVO entity) {
        return AsyncCmdStatusEnum.SUCCESS == entity.getStatus();
    }

    private AsyncCmdSubTaskHandler getSubTaskHandler(String executeName) {
        return this.asyncCmdSubTaskHandlerStrategyFactory.getTaskHandler(executeName);
    }

}

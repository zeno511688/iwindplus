/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.executor.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdCallbackBaseDTO;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdCallbackDTO;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdGrouSaveDTO;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdGroupSubmitDTO;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdSaveDTO;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdStatusEditDTO;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdSubCallbackDTO;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdSubSaveDTO;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdSubSubmitDTO;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdSubmitBaseDTO;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdSubmitDTO;
import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdCallbackResultEnum;
import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdStatusEnum;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdSubVO;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdSubmitVO;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdVO;
import com.iwindplus.base.async.cmd.executor.AsyncCmdExecutor;
import com.iwindplus.base.async.cmd.factory.AsyncCmdDispatchHandlerStrategyFactory;
import com.iwindplus.base.async.cmd.factory.AsyncCmdSubTaskHandlerStrategyFactory;
import com.iwindplus.base.async.cmd.factory.AsyncCmdTaskHandlerStrategyFactory;
import com.iwindplus.base.async.cmd.service.AsyncCmdService;
import com.iwindplus.base.async.cmd.service.AsyncCmdSubService;
import com.iwindplus.base.async.cmd.support.AsyncCmdDispatchHandler;
import com.iwindplus.base.async.cmd.support.AsyncCmdSubTaskHandler;
import com.iwindplus.base.async.cmd.support.AsyncCmdTaskHandler;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

/**
 * 异步命令执行器接口实现类.
 *
 * @author zengdegui
 * @since 2025/12/28 01:16
 */
@Slf4j
@RequiredArgsConstructor
public class AsyncCmdExecutorImpl implements AsyncCmdExecutor {

    private final AsyncCmdService asyncCmdService;
    private final AsyncCmdSubService asyncCmdSubService;
    private final AsyncCmdDispatchHandlerStrategyFactory asyncCmdDispatchHandlerStrategyFactory;
    private final AsyncCmdTaskHandlerStrategyFactory asyncCmdTaskHandlerStrategyFactory;
    private final AsyncCmdSubTaskHandlerStrategyFactory asyncCmdSubTaskHandlerStrategyFactory;

    @Override
    public AsyncCmdSubmitVO submit(AsyncCmdSubmitDTO entity) {
        // 校验参数
        this.checkSubmitParam(entity);
        final AsyncCmdTaskHandler handler = this.resolveTaskHandler(entity.getExecutorClass());
        if (Boolean.TRUE.equals(entity.getNeedCallback())) {
            Assert.isTrue(this.overrideCallback(handler),
                "The task declared the need for a callback, but the executor did not override executeCallback method."
                    + " executorClass=" + entity.getExecutorClass());
        }
        // 保存数据
        final AsyncCmdSaveDTO param = BeanUtil.copyProperties(entity, AsyncCmdSaveDTO.class);
        param.setExecuteName(handler.getExecuteName());
        final AsyncCmdVO result = this.asyncCmdService.save(param);

        // 立即驱动主任务执行
        this.dispatch(result);
        return this.buildSubmitResult(result);
    }

    @Override
    public AsyncCmdSubmitVO submitGroup(AsyncCmdGroupSubmitDTO entity) {
        // 校验参数
        this.checkGroupSubmitParam(entity);
        final AsyncCmdTaskHandler handler = this.resolveTaskHandler(entity.getExecutorClass());
        if (Boolean.TRUE.equals(entity.getNeedCallback())) {
            Assert.isTrue(this.overrideCallback(handler),
                "The task declared the need for a callback, but the executor did not override executeCallback method."
                    + " executorClass=" + entity.getExecutorClass());
        }
        // 保存数据
        final AsyncCmdGrouSaveDTO param = BeanUtil.copyProperties(entity, AsyncCmdGrouSaveDTO.class);
        param.setExecuteName(handler.getExecuteName());

        List<AsyncCmdSubSaveDTO> subTasks = this.buildSubTasks(entity.getSubTasks());
        param.setSubTasks(subTasks);
        param.setSubTaskCount(subTasks.size());

        final AsyncCmdVO result = this.asyncCmdService.saveGroup(param);

        // 立即驱动主任务执行
        this.dispatch(result);
        return this.buildSubmitResult(result);
    }

    @Override
    public boolean callback(AsyncCmdCallbackDTO entity) {
        // 定位主任务：优先主键，其次bizNumber
        AsyncCmdVO task = null;
        if (Objects.nonNull(entity.getId())) {
            task = this.asyncCmdService.getDetail(entity.getId());
        }
        if (Objects.isNull(task) && CharSequenceUtil.isNotBlank(entity.getBizNumber())) {
            task = this.asyncCmdService.getDetailByBizNumber(entity.getBizNumber());
        }

        boolean processed = false;

        // 主任务回调（callbackResult非空时预存结果与进度）
        if (Objects.nonNull(task)) {
            this.callbackTask(task, entity);
            processed = true;
        }

        // 子任务回调列表
        if (CollUtil.isNotEmpty(entity.getSubTasks())) {
            final List<AsyncCmdSubCallbackDTO> validSubTasks = entity.getSubTasks().stream()
                .filter(sub -> Objects.nonNull(sub) && CharSequenceUtil.isNotBlank(sub.getBizNumber()))
                .toList();

            // 批量查询子任务（1次SELECT）
            final List<String> bizNumbers = validSubTasks.stream()
                .map(AsyncCmdSubCallbackDTO::getBizNumber)
                .toList();
            final Map<String, AsyncCmdSubVO> subTaskMap = this.asyncCmdSubService.listByBizNumbers(
                    new ArrayList<>(bizNumbers)).stream()
                .filter(sub -> !AsyncCmdStatusEnum.SUCCESS.equals(sub.getStatus()))
                .collect(Collectors.toMap(AsyncCmdSubVO::getBizNumber, v -> v, (a, b) -> a));

            // 构建 id→result 和 id→progress 映射
            final Map<Long, Map<String, Object>> idToResult = new HashMap<>(validSubTasks.size());
            final Map<Long, Integer> idToProgress = new HashMap<>(validSubTasks.size());
            boolean hasAsyncWait = false;

            for (AsyncCmdSubCallbackDTO subCallback : validSubTasks) {
                final AsyncCmdSubVO subTask = subTaskMap.get(subCallback.getBizNumber());
                if (Objects.isNull(subTask)) {
                    continue;
                }
                if (Objects.nonNull(subCallback.getCallbackResult())) {
                    idToResult.put(subTask.getId(), this.buildCallbackResult(subCallback));
                }
                if (Objects.nonNull(subCallback.getProgress())) {
                    idToProgress.put(subTask.getId(), subCallback.getProgress());
                }
                if (AsyncCmdStatusEnum.ASYNC_WAIT.equals(subTask.getStatus())) {
                    hasAsyncWait = true;
                }
            }

            // 批量更新子任务结果与进度（1条CASE WHEN SQL）
            this.asyncCmdSubService.updateCallbackBatch(idToResult, idToProgress);
            processed = true;

            // ASYNC_WAIT子任务加速调度：主任务CAS刷新下次重试时间并立即投递（只dispatch 1次）
            if (hasAsyncWait && Objects.nonNull(task)) {
                final boolean updated = this.asyncCmdService.editStatusById(AsyncCmdStatusEditDTO.builder()
                    .id(task.getId())
                    .from(AsyncCmdStatusEnum.TO_BE_EXECUTE)
                    .nextRetryTime(System.currentTimeMillis())
                    .build());
                if (updated) {
                    this.dispatch(task);
                }
            }

            // 聚合进度到主任务（1次）
            if (!subTaskMap.isEmpty()) {
                final Long asyncCmdId = subTaskMap.values().iterator().next().getAsyncCmdId();
                this.aggregateSubTaskProgress(asyncCmdId);
            }
        }

        return processed;
    }

    @Override
    public void retryById(Long id) {
        Assert.notNull(id, "id must not be null");

        this.retry(this.asyncCmdService.getDetail(id));
    }

    @Override
    public void retryByBizNumber(String bizNumber) {
        Assert.hasText(bizNumber, "bizNumber must not be null");

        this.retry(this.asyncCmdService.getDetailByBizNumber(bizNumber));
    }

    @Override
    public boolean removeById(Long id) {
        Assert.notNull(id, "id must not be null");

        return asyncCmdService.removeById(id, true);
    }

    @Override
    public boolean removeByBizNumber(String bizNumber) {
        Assert.hasText(bizNumber, "bizNumber must not be null");

        return asyncCmdService.removeByBizNumber(bizNumber, true);
    }

    private void dispatch(AsyncCmdVO entity) {
        final AsyncCmdDispatchHandler dispatchHandler = this.asyncCmdDispatchHandlerStrategyFactory
            .getDispatchHandler(entity.getDispatchMode());

        dispatchHandler.execute(entity);
    }

    private void checkBaseParam(AsyncCmdSubmitBaseDTO entity) {
        Assert.notNull(entity, "entity must not be null");
        Assert.hasText(entity.getBizName(), "bizName must not be null");
        Assert.hasText(entity.getBizKey(), "bizKey must not be blank");
        Assert.hasText(entity.getBizType(), "bizType must not be blank");
    }

    private void checkSubmitParam(AsyncCmdSubmitDTO entity) {
        checkBaseParam(entity);
        Assert.notNull(entity.getExecutorClass(), "executorClass must not be null");
    }

    private void checkGroupSubmitParam(AsyncCmdGroupSubmitDTO entity) {
        checkBaseParam(entity);
        Assert.notNull(entity.getExecutorClass(), "executorClass must not be null");
        Assert.notEmpty(entity.getSubTasks(), "subTasks must not be null");
    }

    private void checkSubSubmitParam(AsyncCmdSubSubmitDTO entity, Integer index) {
        Assert.notNull(entity, "sub entity must not be null");
        Assert.notNull(entity.getExecutorClass(), "sub[" + index + "].executorClass must not be null");
        Assert.hasText(entity.getBizName(), "sub[" + index + "].bizName must not be null");
        Assert.hasText(entity.getBizKey(), "sub[" + index + "].bizKey must not be null");
        Assert.hasText(entity.getBizType(), "sub[" + index + "].bizType must not be null");
        Assert.isTrue(
            entity.getSeq() != null && entity.getSeq() > 0,
            "sub[" + index + "].seq must be greater than 0"
        );
        Assert.isTrue(
            entity.getStage() == null || entity.getStage() > 0,
            "sub[" + index + "].stage must be greater than 0"
        );
    }

    private List<AsyncCmdSubSaveDTO> buildSubTasks(List<AsyncCmdSubSubmitDTO> subTasks) {
        final List<AsyncCmdSubSaveDTO> entities = new ArrayList<>(10);
        // seq -> stage（校验seq全局从1连续，且stage按seq顺序单调不减，保证seq顺序即执行顺序）
        final Map<Integer, Integer> seqStageMap = new HashMap<>(subTasks.size());

        for (int index = 0; index < subTasks.size(); index++) {
            final AsyncCmdSubSubmitDTO subTask = subTasks.get(index);
            this.checkSubSubmitParam(subTask, index);

            Integer seq = subTask.getSeq();
            Assert.isTrue(
                !seqStageMap.containsKey(seq),
                "sub[" + index + "].seq must be unique"
            );

            Integer stage = subTask.getStage();
            stage = Optional.ofNullable(stage).orElse(0);
            seqStageMap.put(seq, stage);
            subTask.setStage(stage);

            AsyncCmdSubTaskHandler handler = this.resolveSubTaskHandler(subTask.getExecutorClass());
            // 是否需要回调，需要则执行器必须重写executeSubCallback
            if (Boolean.TRUE.equals(subTask.getNeedCallback())) {
                Assert.isTrue(Objects.nonNull(handler) && this.overrideSubCallback(handler),
                    "The subtask declared the need for a callback, but the executor did not override executeSubCallback method."
                        + "sub[" + index + "].executorClass=" + subTask.getExecutorClass());
            }
            final AsyncCmdSubSaveDTO entity = BeanUtil.copyProperties(subTask, AsyncCmdSubSaveDTO.class);
            entity.setExecuteName(handler.getExecuteName());
            entities.add(entity);
        }

        // 校验seq全局从1连续，且stage按seq顺序单调不减
        checkSeqStageOrder(seqStageMap);

        return entities;
    }

    /**
     * 解析任务执行器，用于判断是否已注册.
     *
     * @param executorClass 任务执行器类
     * @return String
     */
    private AsyncCmdTaskHandler resolveTaskHandler(Class<? extends AsyncCmdTaskHandler> executorClass) {
        return this.asyncCmdTaskHandlerStrategyFactory
            .getTaskHandler(executorClass.getSimpleName());
    }

    /**
     * 解析子任务执行器，用于判断是否已注册.
     *
     * @param executorClass 子任务执行器类
     * @return String
     */
    private AsyncCmdSubTaskHandler resolveSubTaskHandler(Class<? extends AsyncCmdSubTaskHandler> executorClass) {
        return this.asyncCmdSubTaskHandlerStrategyFactory
            .getTaskHandler(executorClass.getSimpleName());
    }

    private AsyncCmdSubmitVO buildSubmitResult(AsyncCmdVO param) {
        return AsyncCmdSubmitVO
            .builder()
            .id(param.getId())
            .bizKey(param.getBizKey())
            .bizName(param.getBizName())
            .bizType(param.getBizType())
            .bizNumber(param.getBizNumber())
            .build();
    }

    public void retry(AsyncCmdVO entity) {
        if (Objects.isNull(entity)) {
            return;
        }

        final AsyncCmdStatusEnum from = entity.getStatus();
        if (AsyncCmdStatusEnum.DISCARD != from) {
            throw new BizException(BizCodeEnum.CURRENT_STATUS_NOT_SUPPORT_RETRY, new Object[]{from});
        }

        final boolean status = this.asyncCmdService.editStatusById(AsyncCmdStatusEditDTO.builder()
            .id(entity.getId())
            .from(from)
            .to(AsyncCmdStatusEnum.TO_BE_EXECUTE)
            .retryCount(0)
            .nextRetryTime(System.currentTimeMillis())
            .build());
        if (!status) {
            log.warn("Failed to retry trigger, task status has been changed, id={}", entity.getId());

            return;
        }

        entity.setStatus(AsyncCmdStatusEnum.TO_BE_EXECUTE);
        entity.setRetryCount(0);
        // 立即驱动主任务执行
        this.dispatch(entity);
    }

    private boolean overrideCallback(AsyncCmdTaskHandler handler) {
        try {
            final Class<?> executeCallback = handler.getClass()
                .getMethod("executeCallback", AsyncCmdVO.class).getDeclaringClass();
            return !AsyncCmdTaskHandler.class.equals(executeCallback);
        } catch (NoSuchMethodException ex) {
            log.error("Override executeCallback method is not exist, handler={}", handler.getClass().getName());
            return false;
        }
    }

    private boolean overrideSubCallback(AsyncCmdSubTaskHandler handler) {
        try {
            final Class<?> executeSubCallback = handler.getClass()
                .getMethod("executeSubCallback", AsyncCmdSubVO.class).getDeclaringClass();
            return !AsyncCmdSubTaskHandler.class.equals(executeSubCallback);
        } catch (NoSuchMethodException ex) {
            log.error("Override executeSubCallback method is not exist, handler={}", handler.getClass().getName());
            return false;
        }
    }

    /**
     * 校验seq全局从1连续，且stage按seq顺序单调不减.
     * <p>执行侧按seq升序、stage变化切分批次，该校验保证seq顺序即执行顺序，支持多stage串行编排</p>
     *
     * @param seqStageMap seq到stage的映射
     */
    private void checkSeqStageOrder(Map<Integer, Integer> seqStageMap) {
        final List<Integer> seqList = seqStageMap.keySet().stream().sorted().toList();
        Integer prevStage = null;
        for (int i = 0; i < seqList.size(); i++) {
            final Integer seq = seqList.get(i);
            Assert.isTrue(
                seq == i + 1,
                "seq must continuous from 1, expect=" + (i + 1) + ", actual=" + seq
            );

            final Integer stage = seqStageMap.get(seq);
            if (Objects.nonNull(prevStage)) {
                Assert.isTrue(
                    stage >= prevStage,
                    "stage must not decrease by seq order, seq=" + seq
                        + ", stage=" + stage + ", prevStage=" + prevStage
                );
            }
            prevStage = stage;
        }
    }

    /**
     * 主任务回调通知：预存结果，由框架轮询链路消费并驱动状态流转.
     *
     * @param task   主任务
     * @param entity 回调通知对象
     */
    private void callbackTask(AsyncCmdVO task, AsyncCmdCallbackBaseDTO entity) {
        if (Objects.isNull(entity.getCallbackResult())) {
            return;
        }
        final AsyncCmdStatusEnum status = task.getStatus();
        // 已终态任务幂等忽略重复通知
        if (AsyncCmdStatusEnum.SUCCESS.equals(status) || AsyncCmdStatusEnum.DISCARD.equals(status)) {
            log.warn("asyncCmd callback ignored, task already finished. id={} status={} bizNumber={}",
                task.getId(), status, task.getBizNumber());
            return;
        }
        // 预存回调结果与进度，业务不直接修改任务状态
        final Map<String, Object> result = this.buildCallbackResult(entity);
        this.asyncCmdService.editStatusById(AsyncCmdStatusEditDTO.builder()
            .id(task.getId())
            .result(result)
            .progress(entity.getProgress())
            .build());
        task.setResult(result);
        if (Objects.nonNull(entity.getProgress())) {
            task.setProgress(entity.getProgress());
        }

        // 异步等待中：CAS刷新下次重试时间并立即投递，加速消费；否则等待下一轮正常执行/轮询消费
        if (AsyncCmdStatusEnum.ASYNC_WAIT.equals(status)) {
            final long now = System.currentTimeMillis();
            final boolean updated = this.asyncCmdService.editStatusById(AsyncCmdStatusEditDTO.builder()
                .id(task.getId())
                .from(AsyncCmdStatusEnum.ASYNC_WAIT)
                .nextRetryTime(now)
                .build());
            if (!updated) {
                log.warn("asyncCmd callback accelerate ignored, task status changed. id={}", task.getId());
                return;
            }
            task.setNextRetryTime(now);
            // 立即驱动主任务执行
            this.dispatch(task);
        }
    }

    /**
     * 构造预存结果（保留键+业务数据）.
     *
     * @param entity 回调通知对象
     * @return Map<String, Object>
     */
    private Map<String, Object> buildCallbackResult(AsyncCmdCallbackBaseDTO entity) {
        final Map<String, Object> result = MapUtil.isNotEmpty(entity.getResult())
            ? new HashMap<>(entity.getResult())
            : new HashMap<>(4);
        if (Objects.nonNull(entity.getCallbackResult())) {
            result.put(AsyncCmdCallbackResultEnum.CALLBACK_RESULT_KEY, entity.getCallbackResult().name());
        }
        if (CharSequenceUtil.isNotBlank(entity.getErrorMsg())) {
            result.put(AsyncCmdCallbackResultEnum.CALLBACK_ERROR_MSG_KEY, entity.getErrorMsg());
        }
        return result;
    }

    /**
     * 聚合子任务进度到主任务.
     * <p>查询主任务下所有子任务，成功的视为100%，其余按已上报进度计算，取均值写入主任务.</p>
     *
     * @param asyncCmdId 主任务ID
     */
    private void aggregateSubTaskProgress(Long asyncCmdId) {
        final List<AsyncCmdStatusEnum> allStatuses = new ArrayList<>(AsyncCmdStatusEnum.getUnfinishedStatus());
        allStatuses.add(AsyncCmdStatusEnum.SUCCESS);
        final List<AsyncCmdSubVO> allSubTasks = this.asyncCmdSubService.listByAsyncCmdIdAndStatus(
            asyncCmdId, allStatuses);
        if (allSubTasks.isEmpty()) {
            return;
        }
        final int progress = this.calculateAverageProgress(allSubTasks);
        this.asyncCmdService.editStatusById(AsyncCmdStatusEditDTO.builder()
            .id(asyncCmdId)
            .progress(progress)
            .build());
    }

    /**
     * 计算子任务平均进度.
     * <p>成功的子任务视为100%，其余按已上报进度计算，取均值写入主任务.</p>
     *
     * @param subTasks 子任务列表
     * @return 平均进度（0-100）
     */
    private int calculateAverageProgress(List<AsyncCmdSubVO> subTasks) {
        if (subTasks.isEmpty()) {
            return 0;
        }
        final int sum = subTasks.stream()
            .mapToInt(sub -> AsyncCmdStatusEnum.SUCCESS.equals(sub.getStatus())
                ? 100
                : (sub.getProgress() != null ? sub.getProgress() : 0))
            .sum();
        return sum / subTasks.size();
    }
}

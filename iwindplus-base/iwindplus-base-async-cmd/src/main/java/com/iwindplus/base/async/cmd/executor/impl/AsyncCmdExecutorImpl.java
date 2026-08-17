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
import com.iwindplus.base.async.cmd.dal.model.AsyncCmdDO;
import com.iwindplus.base.async.cmd.dal.model.AsyncCmdSubDO;
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
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdSubSubmitVO;
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
        // 归一化：progress>=100 等价于 callbackResult=SUCCESS
        this.normalizeCallbackResult(entity);

        // 定位主任务：优先主键，其次bizNumber
        AsyncCmdVO task = null;
        if (Objects.nonNull(entity.getId())) {
            task = this.asyncCmdService.getDetail(entity.getId());
        }
        if (Objects.isNull(task) && CharSequenceUtil.isNotBlank(entity.getBizNumber())) {
            task = this.asyncCmdService.getDetailByBizNumber(entity.getBizNumber());
        }

        boolean processed = false;
        final long now = System.currentTimeMillis();

        // --- 构建子任务更新参数 ---
        List<AsyncCmdSubDO> subUpdates = List.of();
        Integer aggregateProgress = null;

        if (CollUtil.isNotEmpty(entity.getSubTasks())) {
            final List<AsyncCmdSubCallbackDTO> validSubTasks = entity.getSubTasks().stream()
                .filter(sub -> Objects.nonNull(sub) && CharSequenceUtil.isNotBlank(sub.getBizNumber()))
                .toList();

            // 批量查询子任务（1次SELECT）
            final List<String> bizNumbers = validSubTasks.stream()
                .map(AsyncCmdSubCallbackDTO::getBizNumber)
                .toList();
            final Map<String, AsyncCmdSubVO> subTaskMap = this.asyncCmdSubService.listByBizNumbers(new ArrayList<>(bizNumbers)).stream()
                .filter(sub -> !AsyncCmdStatusEnum.SUCCESS.equals(sub.getStatus()))
                .collect(Collectors.toMap(AsyncCmdSubVO::getBizNumber, v -> v, (a, b) -> a));

            // 校验主子任务关系：不匹配直接报错
            if (Objects.nonNull(task)) {
                for (AsyncCmdSubVO sub : subTaskMap.values()) {
                    if (!task.getId().equals(sub.getAsyncCmdId())) {
                        throw new BizException(BizCodeEnum.PARAM_ERROR);
                    }
                }
            }

            // 构建子任务更新对象
            final List<AsyncCmdSubDO> updates = new ArrayList<>(validSubTasks.size());
            for (AsyncCmdSubCallbackDTO subCallback : validSubTasks) {
                this.normalizeCallbackResult(subCallback);
                final AsyncCmdSubVO subTask = subTaskMap.get(subCallback.getBizNumber());
                if (Objects.isNull(subTask)) {
                    continue;
                }
                final AsyncCmdSubDO.AsyncCmdSubDOBuilder<?, ?> builder = AsyncCmdSubDO.builder()
                    .id(subTask.getId())
                    .modifiedTimestamp(now);
                if (Objects.nonNull(subCallback.getCallbackResult())) {
                    builder.result(this.buildCallbackResult(subCallback));
                }
                if (Objects.nonNull(subCallback.getProgress())) {
                    builder.progress(subCallback.getProgress());
                }
                if (Objects.nonNull(subCallback.getCostTime())) {
                    builder.costTime(subCallback.getCostTime());
                }
                if (CharSequenceUtil.isNotBlank(subCallback.getErrorMsg())) {
                    builder.errorMsg(subCallback.getErrorMsg());
                }
                updates.add(builder.build());
            }
            subUpdates = updates;
            // 子任务聚合进度
            if (!subTaskMap.isEmpty()) {
                final Long asyncCmdId = subTaskMap.values().iterator().next().getAsyncCmdId();
                aggregateProgress = this.asyncCmdSubService.getAggregateProgress(asyncCmdId);
            }
        }

        // --- 构建主任务更新参数 ---
        AsyncCmdDO mainTaskUpdate = null;

        if (Objects.nonNull(task) && Objects.nonNull(entity.getCallbackResult())) {
            final AsyncCmdStatusEnum status = task.getStatus();
            // 已终态任务幂等忽略重复通知
            if (AsyncCmdStatusEnum.SUCCESS.equals(status) || AsyncCmdStatusEnum.DISCARD.equals(status)) {
                log.warn("asyncCmd callback ignored, task already finished. id={} status={} bizNumber={}",
                    task.getId(), status, task.getBizNumber());
            } else {
                mainTaskUpdate = AsyncCmdDO.builder()
                    .id(task.getId())
                    .result(this.buildCallbackResult(entity))
                    .progress(Objects.nonNull(aggregateProgress) ? aggregateProgress : entity.getProgress())
                    .costTime(entity.getCostTime())
                    .modifiedTimestamp(now)
                    .build();
            }
        } else if (Objects.nonNull(task) && Objects.nonNull(aggregateProgress)) {
            mainTaskUpdate = AsyncCmdDO.builder()
                .id(task.getId())
                .progress(aggregateProgress)
                .modifiedTimestamp(now)
                .build();
        }

        // --- 一次性批量更新 ---
        if (Objects.nonNull(mainTaskUpdate) || CollUtil.isNotEmpty(subUpdates)) {
            processed = this.asyncCmdService.editCallbackBatch(mainTaskUpdate, subUpdates);
        }

        // 更新成功后触发调度
        if (processed && Objects.nonNull(task)) {
            task.setNextRetryTime(mainTaskUpdate.getNextRetryTime());
            this.dispatch(task);
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
        final AsyncCmdSubmitVO result = AsyncCmdSubmitVO
            .builder()
            .id(param.getId())
            .bizKey(param.getBizKey())
            .bizName(param.getBizName())
            .bizType(param.getBizType())
            .bizNumber(param.getBizNumber())
            .build();
        if (CollUtil.isNotEmpty(param.getSubTasks())) {
            result.setSubTasks(BeanUtil.copyToList(param.getSubTasks(), AsyncCmdSubSubmitVO.class));
        }
        return result;
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
     * 归一化回调结果：双向推导.
     * <ul>
     *   <li>callbackResult 为空 且 progress>=100 → 推导为 SUCCESS</li>
     *   <li>callbackResult=SUCCESS 且 progress 为空 → 推导为 100</li>
     * </ul>
     *
     * @param entity 回调通知对象
     */
    private void normalizeCallbackResult(AsyncCmdCallbackBaseDTO entity) {
        if (Objects.isNull(entity)) {
            return;
        }
        // progress>=100 → callbackResult=SUCCESS
        if (Objects.isNull(entity.getCallbackResult())
            && Objects.nonNull(entity.getProgress())
            && entity.getProgress() >= 100) {
            entity.setCallbackResult(AsyncCmdCallbackResultEnum.SUCCESS);
        }
        // callbackResult=SUCCESS → progress=100
        if (AsyncCmdCallbackResultEnum.SUCCESS.equals(entity.getCallbackResult())
            && Objects.isNull(entity.getProgress())) {
            entity.setProgress(100);
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
}

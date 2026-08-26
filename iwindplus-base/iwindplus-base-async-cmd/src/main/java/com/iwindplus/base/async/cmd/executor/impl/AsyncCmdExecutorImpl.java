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
import com.iwindplus.base.async.cmd.domain.constant.AsyncCmdConstant;
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
import com.iwindplus.base.async.cmd.factory.AsyncCmdSubTaskHandlerStrategyFactory;
import com.iwindplus.base.async.cmd.factory.AsyncCmdTaskHandlerStrategyFactory;
import com.iwindplus.base.async.cmd.service.AsyncCmdService;
import com.iwindplus.base.async.cmd.service.AsyncCmdSubService;
import com.iwindplus.base.async.cmd.support.AsyncCmdBizProcessor;
import com.iwindplus.base.async.cmd.support.AsyncCmdSubTaskHandler;
import com.iwindplus.base.async.cmd.support.AsyncCmdTaskHandler;
import com.iwindplus.base.domain.constant.CommonConstant.NumberConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
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
    private final AsyncCmdBizProcessor asyncCmdBizProcessor;
    private final AsyncCmdTaskHandlerStrategyFactory asyncCmdTaskHandlerStrategyFactory;
    private final AsyncCmdSubTaskHandlerStrategyFactory asyncCmdSubTaskHandlerStrategyFactory;

    @Override
    public AsyncCmdSubmitVO submit(AsyncCmdSubmitDTO entity) {
        // 校验参数
        this.checkSubmitParam(entity, false);
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
        this.checkSubmitParam(entity, true);
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
        Assert.notNull(entity, "callback request must not be null");

        // 归一化回调结果
        this.normalizeCallbackResult(entity);
        // 定位主任务：优先 ID，其次 bizNumber
        final AsyncCmdVO task = this.getCallbackTask(entity);
        // 主任务存在时，终态直接幂等返回
        if (Objects.nonNull(task) && this.isFinished(task)) {
            log.warn(
                "asyncCmd callback ignored, task already finished. id={} status={} bizNumber={}",
                task.getId(),
                task.getStatus(),
                task.getBizNumber()
            );
            return true;
        }
        // 构建子任务更新
        final List<AsyncCmdSubDO> subTaskUpdates = this.buildSubTaskUpdates(entity, task);
        // 构建主任务更新
        final AsyncCmdDO taskUpdate = this.buildTask(entity, task);

        // 主任务、子任务都不存在
        Assert.isTrue(
            Objects.nonNull(taskUpdate) || CollUtil.isNotEmpty(subTaskUpdates),
            "async command callback target not found"
        );

        // 批量更新
        final boolean result = this.asyncCmdService.editCallbackBatch(taskUpdate, subTaskUpdates);
        if (!result) {
            return false;
        }

        // 主任务存在，直接调度
        if (Objects.nonNull(task)) {
            this.dispatch(task);
            return true;
        }

        // 主任务不存在，通过子任务找到主任务后调度
        if (CollUtil.isNotEmpty(subTaskUpdates)) {
            final Long asyncCmdId = subTaskUpdates.get(0).getAsyncCmdId();
            final AsyncCmdVO parentTask =
                this.asyncCmdService.getDetail(asyncCmdId);

            Assert.notNull(parentTask, "async command task not found");

            this.dispatch(parentTask);
        }

        return true;
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
        this.asyncCmdBizProcessor.execute(entity);
    }

    /**
     * 统一校验提交参数.
     *
     * @param entity 提交参数
     * @param needCheckSubTasks 是否需要校验子任务
     */
    private void checkSubmitParam(AsyncCmdSubmitBaseDTO entity, boolean needCheckSubTasks) {
        Assert.notNull(entity, "entity must not be null");
        Assert.hasText(entity.getBizName(), "bizName must not be null");
        Assert.hasText(entity.getBizKey(), "bizKey must not be blank");
        Assert.hasText(entity.getBizType(), "bizType must not be blank");

        if (entity instanceof AsyncCmdSubmitDTO asyncCmdSubmitDTO) {
            Assert.notNull(asyncCmdSubmitDTO.getExecutorClass(), "executorClass must not be null");
        } else if (entity instanceof AsyncCmdGroupSubmitDTO asyncCmdGroupSubmitDTO) {
            Assert.notNull(asyncCmdGroupSubmitDTO.getExecutorClass(), "executorClass must not be null");
            if (needCheckSubTasks) {
                Assert.notEmpty(asyncCmdGroupSubmitDTO.getSubTasks(), "subTasks must not be null");
            }
        }
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
                .getMethod(AsyncCmdConstant.METHOD_EXECUTE_CALLBACK, AsyncCmdVO.class).getDeclaringClass();
            return !AsyncCmdTaskHandler.class.equals(executeCallback);
        } catch (NoSuchMethodException ex) {
            log.error("Override executeCallback method is not exist, handler={}", handler.getClass().getName());
            return false;
        }
    }

    private boolean overrideSubCallback(AsyncCmdSubTaskHandler handler) {
        try {
            final Class<?> executeSubCallback = handler.getClass()
                .getMethod(AsyncCmdConstant.METHOD_EXECUTE_SUB_CALLBACK, AsyncCmdSubVO.class).getDeclaringClass();
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

        final Integer progress = entity.getProgress();
        final AsyncCmdCallbackResultEnum callbackResult = entity.getCallbackResult();

        // progress >= 100 且无结果 → SUCCESS
        if (Objects.isNull(callbackResult) && Objects.nonNull(progress) && progress >= NumberConstant.NUMBER_ONE_HUNDRED) {
            entity.setCallbackResult(AsyncCmdCallbackResultEnum.SUCCESS);
            return;
        }

        // SUCCESS 且无进度 → 100
        if (AsyncCmdCallbackResultEnum.SUCCESS.equals(callbackResult) && Objects.isNull(progress)) {
            entity.setProgress(NumberConstant.NUMBER_ONE_HUNDRED);
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

        // 添加回调结果
        Optional.ofNullable(entity.getCallbackResult())
            .ifPresent(callbackResult -> result.put(AsyncCmdConstant.CALLBACK_RESULT_KEY, callbackResult.name()));

        // 添加错误信息
        Optional.ofNullable(entity.getErrorMsg())
            .filter(CharSequenceUtil::isNotBlank)
            .ifPresent(errorMsg -> result.put(AsyncCmdConstant.CALLBACK_ERROR_MSG_KEY, errorMsg));

        return result;
    }

    /**
     * 定位回调对应的主任务。
     *
     * <p>优先使用主键 ID 查询，查询不到时再使用 bizNumber 查询。</p>
     *
     * @param entity 回调参数
     * @return 主任务
     */
    private AsyncCmdVO getCallbackTask(AsyncCmdCallbackDTO entity) {
        AsyncCmdVO task = null;

        if (Objects.nonNull(entity.getId())) {
            task = this.asyncCmdService.getDetail(entity.getId());
        }

        if (Objects.isNull(task) && CharSequenceUtil.isNotBlank(entity.getBizNumber())) {
            task = this.asyncCmdService.getDetailByBizNumber(entity.getBizNumber());
        }

        return task;
    }

    /**
     * 构建子任务更新参数。
     *
     * @param entity 回调参数
     * @param task   主任务
     * @return 子任务更新列表
     */
    private List<AsyncCmdSubDO> buildSubTaskUpdates(
        AsyncCmdCallbackDTO entity,
        AsyncCmdVO task) {
        final List<AsyncCmdSubCallbackDTO> subTasks = entity.getSubTasks();
        if (CollUtil.isEmpty(subTasks)) {
            return null;
        }

        final List<String> bizNumbers = subTasks.stream()
            .filter(Objects::nonNull)
            .map(AsyncCmdSubCallbackDTO::getBizNumber)
            .filter(CharSequenceUtil::isNotBlank)
            .distinct()
            .toList();

        Assert.isTrue(
            bizNumbers.size() == subTasks.size(),
            "subTasks contains invalid bizNumber"
        );

        // 一次查询全部子任务
        final List<AsyncCmdSubVO> subTaskList =
            this.asyncCmdSubService.listByBizNumbers(bizNumbers);

        final Map<String, AsyncCmdSubVO> subTaskMap = subTaskList.stream()
            .collect(Collectors.toMap(
                AsyncCmdSubVO::getBizNumber,
                Function.identity()
            ));

        // 所有回调子任务必须存在
        Assert.isTrue(
            subTaskMap.size() == bizNumbers.size(),
            "some subTasks do not exist"
        );

        // 只有主任务存在时，才校验子任务归属
        if (Objects.nonNull(task)) {
            Assert.isTrue(
                subTaskList.stream().allMatch(sub -> Objects.equals(sub.getAsyncCmdId(), task.getId())),
                "subTasks do not belong to the current task"
            );
        }

        return this.buildSubTask(subTasks, subTaskMap);
    }

    /**
     * 构建子任务更新对象。
     *
     * @param subTasks   回调子任务
     * @param subTaskMap 数据库子任务
     * @return 子任务更新列表
     */
    private List<AsyncCmdSubDO> buildSubTask(
        List<AsyncCmdSubCallbackDTO> subTasks,
        Map<String, AsyncCmdSubVO> subTaskMap) {
        final List<AsyncCmdSubDO> updates = new ArrayList<>(subTasks.size());

        for (int index = 0; index < subTasks.size(); index++) {
            final AsyncCmdSubCallbackDTO callback = subTasks.get(index);

            Assert.notNull(
                callback,
                "sub[" + index + "] must not be null"
            );

            Assert.isTrue(
                callback.getProgress() != null
                    || callback.getCallbackResult() != null,
                "sub[" + index + "].progress or callbackResult must be provided"
            );

            // 归一化：
            // progress >= 100 -> SUCCESS
            // SUCCESS + progress == null -> progress = 100
            this.normalizeCallbackResult(callback);

            final AsyncCmdSubVO subTask = subTaskMap.get(callback.getBizNumber());
            Assert.notNull(
                subTask,
                "sub[" + index + "] not found: " + callback.getBizNumber()
            );

            // 已经成功的任务忽略重复回调
            if (AsyncCmdStatusEnum.SUCCESS.equals(subTask.getStatus())) {
                continue;
            }

            final AsyncCmdSubDO.AsyncCmdSubDOBuilder<?, ?> builder = AsyncCmdSubDO.builder()
                .id(subTask.getId())
                .asyncCmdId(subTask.getAsyncCmdId())
                .progress(callback.getProgress())
                .result(this.buildCallbackResult(callback))
                .costTime(callback.getCostTime())
                .errorMsg(callback.getErrorMsg());

            if (Objects.nonNull(callback.getCallbackResult())) {
                builder.status(
                    AsyncCmdStatusEnum.valueOf(callback.getCallbackResult().name())
                );
            }

            updates.add(builder.build());
        }

        return updates;
    }

    /**
     * 构建主任务更新参数。
     *
     * @param entity 回调参数
     * @param task   主任务
     * @return 主任务更新对象
     */
    private AsyncCmdDO buildTask(
        AsyncCmdCallbackDTO entity,
        AsyncCmdVO task) {

        if (Objects.isNull(task)) {
            return null;
        }

        return AsyncCmdDO.builder()
            .id(task.getId())
            .status(AsyncCmdStatusEnum.valueOf(entity.getCallbackResult().name()))
            .result(this.buildCallbackResult(entity))
            .progress(entity.getProgress())
            .costTime(entity.getCostTime())
            .errorMsg(entity.getErrorMsg())
            .build();
    }

    /**
     * 判断主任务是否已经进入终态。
     *
     * @param task 主任务
     */
    private boolean isFinished(AsyncCmdVO task) {
        final AsyncCmdStatusEnum status = task.getStatus();
        return AsyncCmdStatusEnum.SUCCESS.equals(status) || AsyncCmdStatusEnum.DISCARD.equals(status);
    }
}

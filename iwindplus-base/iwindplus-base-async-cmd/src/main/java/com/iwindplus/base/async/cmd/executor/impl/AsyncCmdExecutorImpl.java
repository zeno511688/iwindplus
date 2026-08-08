/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.executor.impl;

import cn.hutool.core.bean.BeanUtil;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdGrouSaveDTO;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdGroupSubmitDTO;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdSaveDTO;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdSubSaveDTO;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdSubSubmitDTO;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdSubmitBaseDTO;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdSubmitDTO;
import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdStatusEnum;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdSubVO;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdSubmitVO;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdVO;
import com.iwindplus.base.async.cmd.executor.AsyncCmdExecutor;
import com.iwindplus.base.async.cmd.factory.AsyncCmdDispatchHandlerStrategyFactory;
import com.iwindplus.base.async.cmd.factory.AsyncCmdSubTaskHandlerStrategyFactory;
import com.iwindplus.base.async.cmd.factory.AsyncCmdTaskHandlerStrategyFactory;
import com.iwindplus.base.async.cmd.service.AsyncCmdService;
import com.iwindplus.base.async.cmd.support.AsyncCmdDispatchHandler;
import com.iwindplus.base.async.cmd.support.AsyncCmdSubTaskHandler;
import com.iwindplus.base.async.cmd.support.AsyncCmdTaskHandler;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
        param.setExecuteName(entity.getExecutorClass().getSimpleName());
        final AsyncCmdVO result = this.asyncCmdService.save(param);

        // 获取调度管理器
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
        param.setExecuteName(this.resolveTaskHandler(entity.getExecutorClass()).getExecuteName());

        List<AsyncCmdSubSaveDTO> subTasks = this.buildSubTasks(entity.getSubTasks());
        param.setSubTasks(subTasks);
        param.setSubTaskCount(subTasks.size());

        final AsyncCmdVO result = this.asyncCmdService.saveGroup(param);

        // 获取调度管理器
        this.dispatch(result);
        return this.buildSubmitResult(result);
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
        Assert.hasText(entity.getBizKey(), "bizKey must not be blank");
        Assert.hasText(entity.getBizType(), "bizType must not be blank");
    }

    private void checkSubmitParam(AsyncCmdSubmitDTO entity) {
        checkBaseParam(entity);
        Assert.notEmpty(entity.getContent(), "content must not be null");
        Assert.notNull(entity.getExecutorClass(), "executorClass must not be null");
    }

    private void checkGroupSubmitParam(AsyncCmdGroupSubmitDTO entity) {
        checkBaseParam(entity);
        Assert.notNull(entity.getExecutorClass(), "executorClass must not be null");
        Assert.notEmpty(entity.getSubTasks(), "subTasks must not be null");
    }

    private void checkSubSubmitParam(AsyncCmdSubSubmitDTO entity, Integer index) {
        Assert.notNull(entity, "sub entity must not be null");
        Assert.notNull(entity.getBizType(), "sub[" + index + "].bizType must not be null");
        Assert.notNull(entity.getSeq(), "sub[" + index + "].seq must not be null");
        Assert.isTrue(Objects.isNull(entity.getStage()) || entity.getStage() > 0, "sub[" + index + "].stage must be greater than 0");
        Assert.isTrue(entity.getSeq() > 0, "sub[" + index + "].seq must be greater than 0");
        Assert.notEmpty(entity.getContent(), "sub[" + index + "].content must not be null");
        Assert.notNull(entity.getExecutorClass(), "sub[" + index + "].executorClass must not be null");
    }

    private List<AsyncCmdSubSaveDTO> buildSubTasks(List<AsyncCmdSubSubmitDTO> subTasks) {
        final List<AsyncCmdSubSaveDTO> entities = new ArrayList<>(10);
        // 全局seq唯一
        final Set<Integer> seqSet = new HashSet<>(subTasks.size());
        // stage -> seq集合
        final Map<Integer, Set<Integer>> stageSeqMap = new HashMap<>(10);

        for (int index = 0; index < subTasks.size(); index++) {
            final AsyncCmdSubSubmitDTO subTask = subTasks.get(index);
            this.checkSubSubmitParam(subTask, index);

            Integer seq = subTask.getSeq();
            boolean unique = seqSet.add(seq);
            Assert.isTrue(unique, "sub[" + index + "].seq must not be unique");

            Integer stage = Optional.ofNullable(subTask.getStage()).orElse(0);
            if (stage > 0) {
                stageSeqMap.computeIfAbsent(
                        stage,
                        k -> new HashSet<>(16)
                    )
                    .add(seq);
            }

            final AsyncCmdSubTaskHandler handler = this.resolveSubTaskHandler(subTask.getExecutorClass());
            if (Boolean.TRUE.equals(subTask.getNeedCallback())) {
                Assert.isTrue(this.overrideSubCallback(handler),
                    "The subtask declared the need for a callback, but the executor did not override executeSubCallback method."
                        + "sub[" + index + "].executorClass=" + subTask.getExecutorClass());
            }

            final AsyncCmdSubSaveDTO entity = BeanUtil.copyProperties(subTask, AsyncCmdSubSaveDTO.class);
            entity.setExecuteName(handler.getExecuteName());
            entities.add(entity);
        }

        // 校验stage内seq连续
        checkStageSeqContinuous(stageSeqMap);

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

        final boolean status = this.asyncCmdService.editStatusById(entity.getId(), from, AsyncCmdStatusEnum.TO_BE_EXECUTE,
            null, null, 0, LocalDateTime.now(), null, null);
        if (!status) {
            log.warn("Failed to retry trigger, task status has been changed, id={}", entity.getId());

            return;
        }

        entity.setStatus(AsyncCmdStatusEnum.TO_BE_EXECUTE);
        entity.setRetryCount(0);
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

    private void checkStageSeqContinuous(Map<Integer, Set<Integer>> stageSeqMap) {
        stageSeqMap.forEach((stage, seqSet) -> {
            List<Integer> seqList = seqSet.stream().sorted().toList();
            for (int i = 0; i < seqList.size(); i++) {
                Assert.isTrue(
                    seqList.get(i) == i + 1,
                    "stage="
                        + stage
                        + " seq must continuous, expect="
                        + (i + 1)
                        + ", actual="
                        + seqList.get(i)
                );
            }
        });
    }
}

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
import java.util.HashSet;
import java.util.List;
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
        // 保存数据
        final AsyncCmdGrouSaveDTO param = BeanUtil.copyProperties(entity, AsyncCmdGrouSaveDTO.class);
        param.setExecuteName(this.resolveTaskExecuteName(entity.getExecutorClass()));

        List<AsyncCmdSubSaveDTO> subTasks = this.buildSubTasks(entity.getSubTasks());
        param.setSubTasks(subTasks);
        param.setSubTaskCount(subTasks.size());

        final AsyncCmdVO result = this.asyncCmdService.saveGroup(param);

        // 获取调度管理器
        this.dispatch(result);
        return this.buildSubmitResult(result);
    }

    @Override
    public boolean removeById(Long id) {
        return asyncCmdService.removeById(id, true);
    }

    @Override
    public boolean removeByBizKeyAndType(String bizKey, String bizType) {
        return asyncCmdService.removeByBizKeyAndType(bizKey, bizType, true);
    }

    @Override
    public boolean removeByBizNumber(String bizNumber) {
        return asyncCmdService.removeByBizNumber(bizNumber, true);
    }

    private void dispatch(AsyncCmdVO entity) {
        final AsyncCmdDispatchHandler dispatchHandler =
            this.asyncCmdDispatchHandlerStrategyFactory
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
        Assert.notEmpty(entity.getContent(), "sub[" + index + "].content must not be null");
        Assert.notNull(entity.getExecutorClass(), "sub[" + index + "].executorClass must not be null");
    }

    private List<AsyncCmdSubSaveDTO> buildSubTasks(List<AsyncCmdSubSubmitDTO> subTasks) {
        final Set<Object> seqSet = new HashSet<>(subTasks.size());

        for (int index = 0; index < subTasks.size(); index++) {
            final AsyncCmdSubSubmitDTO subTask = subTasks.get(index);
            this.checkSubSubmitParam(subTask, index);

            boolean unique = seqSet.add(subTask.getSeq());
            Assert.isTrue(unique, "sub[" + index + "].seq must not be unique");

            this.resolveSubTaskExecuteName(subTask.getExecutorClass());
        }
        return BeanUtil.copyToList(subTasks, AsyncCmdSubSaveDTO.class);
    }

    /**
     * 解析任务执行器名称，用于判断是否已注册.
     *
     * @param executorClass 任务执行器类
     * @return String
     */
    private String resolveTaskExecuteName(Class<? extends AsyncCmdTaskHandler> executorClass) {
        return this.asyncCmdTaskHandlerStrategyFactory
            .getTaskHandler(executorClass.getSimpleName())
            .getExecuteName();
    }

    /**
     * 解析子任务执行器名称，用于判断是否已注册.
     *
     * @param executorClass 子任务执行器类
     * @return String
     */
    private String resolveSubTaskExecuteName(Class<? extends AsyncCmdSubTaskHandler> executorClass) {
        return this.asyncCmdSubTaskHandlerStrategyFactory
            .getTaskHandler(executorClass.getSimpleName())
            .getExecuteName();
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
}

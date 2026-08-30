/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.export.task.executor.impl;

import cn.hutool.core.bean.BeanUtil;
import com.iwindplus.base.export.task.domain.dto.ExportTaskDTO;
import com.iwindplus.base.export.task.domain.dto.ExportTaskStatusEditDTO;
import com.iwindplus.base.export.task.domain.dto.ExportTaskSubmitDTO;
import com.iwindplus.base.export.task.domain.enums.ExportTaskStatusEnum;
import com.iwindplus.base.export.task.domain.vo.ExportTaskSubmitVO;
import com.iwindplus.base.export.task.domain.vo.ExportTaskVO;
import com.iwindplus.base.export.task.executor.ExportTaskExecutor;
import com.iwindplus.base.export.task.factory.ExportTaskHandlerStrategyFactory;
import com.iwindplus.base.export.task.service.ExportTaskService;
import com.iwindplus.base.export.task.support.ExportTaskBizProcessor;
import com.iwindplus.base.export.task.support.ExportTaskHandler;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

/**
 * 导出任务执行器接口实现类.
 *
 * @author zengdegui
 * @since 2026/08/29
 */
@Slf4j
@RequiredArgsConstructor
public class ExportTaskExecutorImpl implements ExportTaskExecutor {

    private final ExportTaskService exportTaskService;
    private final ExportTaskBizProcessor exportTaskBizProcessor;
    private final ExportTaskHandlerStrategyFactory exportTaskHandlerStrategyFactory;

    @Override
    public ExportTaskSubmitVO submit(ExportTaskSubmitDTO entity) {
        // 校验参数
        this.checkSubmitParam(entity);

        final ExportTaskHandler handler = this.resolveTaskHandler(entity.getExecutorClass());

        // 保存数据
        final ExportTaskDTO param = BeanUtil.copyProperties(entity, ExportTaskDTO.class);
        param.setExecuteName(handler.getExecuteName());
        final ExportTaskVO result = this.exportTaskService.save(param);

        // 立即驱动任务执行
        this.dispatch(result);

        return this.buildSubmitResult(result);
    }

    @Override
    public void retryById(Long id) {
        Assert.notNull(id, "id must not be null");

        this.retry(this.exportTaskService.getDetail(id));
    }

    @Override
    public void retryByBizNumber(String bizNumber) {
        Assert.hasText(bizNumber, "bizNumber must not be null");

        this.retry(this.exportTaskService.getDetailByBizNumber(bizNumber));
    }

    private void dispatch(ExportTaskVO entity) {
        this.exportTaskBizProcessor.execute(entity);
    }

    private void retry(ExportTaskVO entity) {
        if (entity == null) {
            log.warn("exportTask not found");
            return;
        }

        final ExportTaskStatusEnum from = entity.getStatus();
        if (ExportTaskStatusEnum.DISCARD != from) {
            throw new BizException(BizCodeEnum.CURRENT_STATUS_NOT_SUPPORT_RETRY, new Object[]{from});
        }

        final boolean status = this.exportTaskService.editStatusById(ExportTaskStatusEditDTO.builder()
            .id(entity.getId())
            .from(from)
            .to(ExportTaskStatusEnum.PENDING)
            .retryCount(0)
            .nextRetryTime(System.currentTimeMillis())
            .build());
        if (!status) {
            log.warn("Failed to retry trigger, task status has been changed, id={}", entity.getId());

            return;
        }

        entity.setStatus(ExportTaskStatusEnum.PENDING);
        entity.setRetryCount(0);

        this.dispatch(entity);
    }

    /**
     * 统一校验提交参数.
     *
     * @param entity 提交参数
     */
    private void checkSubmitParam(ExportTaskSubmitDTO entity) {
        Assert.notNull(entity, "entity must not be null");
        Assert.notNull(entity.getExecutorClass(), "executorClass must not be null");
        Assert.hasText(entity.getFileName(), "fileName must not be blank");
        Assert.notEmpty(entity.getQueryParam(), "queryParam must not be empty");
    }

    /**
     * 解析任务执行器，用于判断是否已注册.
     *
     * @param executorClass 任务执行器类
     * @return ExportTaskHandler
     */
    private ExportTaskHandler resolveTaskHandler(Class<? extends ExportTaskHandler> executorClass) {
        return this.exportTaskHandlerStrategyFactory.getTaskHandler(executorClass.getSimpleName());
    }

    /**
     * 构建提交结果.
     *
     * @param entity 导出任务
     * @return ExportTaskSubmitVO
     */
    private ExportTaskSubmitVO buildSubmitResult(ExportTaskVO entity) {
        return ExportTaskSubmitVO.builder()
            .id(entity.getId())
            .bizNumber(entity.getBizNumber())
            .build();
    }
}

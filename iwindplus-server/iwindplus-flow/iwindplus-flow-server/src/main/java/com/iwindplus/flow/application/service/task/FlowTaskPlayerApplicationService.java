/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.application.service.task;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.flow.application.service.task.dto.FlowTaskPlayerEditDTO;
import com.iwindplus.flow.application.service.task.dto.FlowTaskPlayerSaveDTO;
import com.iwindplus.flow.common.enums.FlowCodeEnum;
import com.iwindplus.flow.infrastructure.persistence.instance.FlowInstanceDO;
import com.iwindplus.flow.infrastructure.persistence.instance.FlowInstanceRepository;
import com.iwindplus.flow.infrastructure.persistence.model.FlowModelDO;
import com.iwindplus.flow.infrastructure.persistence.model.FlowModelRepository;
import com.iwindplus.flow.infrastructure.persistence.task.FlowTaskDO;
import com.iwindplus.flow.infrastructure.persistence.task.FlowTaskPlayerDO;
import com.iwindplus.flow.infrastructure.persistence.task.FlowTaskPlayerRepository;
import com.iwindplus.flow.infrastructure.persistence.task.FlowTaskRepository;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 流程任务参与人业务层接口类.
 *
 * @author zengdegui
 * @since 2024/11/03 19:11
 */
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class FlowTaskPlayerApplicationService {

    private final FlowTaskPlayerRepository flowTaskPlayerRepository;
    private final FlowInstanceRepository flowInstanceRepository;
    private final FlowModelRepository flowModelRepository;
    private final FlowTaskRepository flowTaskRepository;

    /**
     * 保存.
     *
     * @param entity 对象
     * @return boolean
     */
    public boolean save(FlowTaskPlayerSaveDTO entity) {
        final FlowInstanceDO flowInstance = this.flowInstanceRepository.getById(entity.getInstanceId());
        if (flowInstance == null) {
            throw new BizException(FlowCodeEnum.FLOW_INSTANCE_NOT_EXIST);
        }
        final FlowModelDO flowModel = this.flowModelRepository.getById(entity.getModelId());
        if (flowModel == null) {
            throw new BizException(FlowCodeEnum.FLOW_MODEL_NOT_EXIST);
        }
        final FlowTaskDO flowTask = this.flowTaskRepository.getById(entity.getTaskId());
        if (flowTask == null) {
            throw new BizException(FlowCodeEnum.FLOW_TASK_NOT_EXIST);
        }
        final FlowTaskPlayerDO model = BeanUtil.copyProperties(entity, FlowTaskPlayerDO.class);
        return this.flowTaskPlayerRepository.save(model);
    }

    /**
     * 批量删除.
     *
     * @param ids 主键集合
     * @return boolean
     */
    public boolean removeByIds(List<Long> ids) {
        List<FlowTaskPlayerDO> list = this.flowTaskPlayerRepository.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return this.flowTaskPlayerRepository.removeByIds(ids);
    }

    /**
     * 编辑.
     *
     * @param entity 对象
     * @return boolean
     */
    public boolean edit(FlowTaskPlayerEditDTO entity) {
        FlowTaskPlayerDO data = this.flowTaskPlayerRepository.getById(entity.getId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (entity.getInstanceId() != null && !data.getInstanceId().equals(entity.getInstanceId())) {
            final FlowInstanceDO flowInstance = this.flowInstanceRepository.getById(entity.getInstanceId());
            if (flowInstance == null) {
                throw new BizException(FlowCodeEnum.FLOW_INSTANCE_NOT_EXIST);
            }
        }
        if (entity.getModelId() != null && !data.getModelId().equals(entity.getModelId())) {
            final FlowModelDO flowModel = this.flowModelRepository.getById(entity.getModelId());
            if (flowModel == null) {
                throw new BizException(FlowCodeEnum.FLOW_MODEL_NOT_EXIST);
            }
        }
        if (entity.getTaskId() != null && !data.getTaskId().equals(entity.getTaskId())) {
            final FlowTaskDO flowTask = this.flowTaskRepository.getById(entity.getTaskId());
            if (flowTask == null) {
                throw new BizException(FlowCodeEnum.FLOW_TASK_NOT_EXIST);
            }
        }
        final FlowTaskPlayerDO model = BeanUtil.copyProperties(entity, FlowTaskPlayerDO.class);
        return this.flowTaskPlayerRepository.updateById(model);
    }
}

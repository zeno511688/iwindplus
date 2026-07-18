/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.flow.domain.dto.FlowTaskPlayerEditDTO;
import com.iwindplus.flow.domain.dto.FlowTaskPlayerSaveDTO;
import com.iwindplus.flow.domain.enums.FlowCodeEnum;
import com.iwindplus.flow.server.dal.model.FlowInstanceDO;
import com.iwindplus.flow.server.dal.model.FlowModelDO;
import com.iwindplus.flow.server.dal.model.FlowTaskDO;
import com.iwindplus.flow.server.dal.model.FlowTaskPlayerDO;
import com.iwindplus.flow.server.dal.repository.FlowInstanceRepository;
import com.iwindplus.flow.server.dal.repository.FlowModelRepository;
import com.iwindplus.flow.server.dal.repository.FlowTaskPlayerRepository;
import com.iwindplus.flow.server.dal.repository.FlowTaskRepository;
import com.iwindplus.flow.server.service.FlowTaskPlayerService;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 历史流程任务参与人业务层接口实现类.
 *
 * @author zengdegui
 * @since 2024/11/03 19:11
 */
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class FlowTaskPlayerServiceImpl implements FlowTaskPlayerService {

    private final FlowTaskPlayerRepository flowTaskPlayerRepository;
    private final FlowInstanceRepository flowInstanceRepository;
    private final FlowModelRepository flowModelRepository;
    private final FlowTaskRepository flowTaskRepository;

    @Override
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

    @Override
    public boolean removeByIds(List<Long> ids) {
        List<FlowTaskPlayerDO> list = this.flowTaskPlayerRepository.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return this.flowTaskPlayerRepository.removeByIds(ids);
    }

    @Override
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

/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.flow.domain.dto.FlowTaskEditDTO;
import com.iwindplus.flow.domain.dto.FlowTaskSaveDTO;
import com.iwindplus.flow.domain.dto.FlowTaskSearchDTO;
import com.iwindplus.flow.domain.enums.FlowCodeEnum;
import com.iwindplus.flow.domain.vo.FlowTaskPageVO;
import com.iwindplus.flow.server.dal.model.FlowInstanceDO;
import com.iwindplus.flow.server.dal.model.FlowModelDO;
import com.iwindplus.flow.server.dal.model.FlowTaskDO;
import com.iwindplus.flow.server.dal.repository.FlowInstanceRepository;
import com.iwindplus.flow.server.dal.repository.FlowModelRepository;
import com.iwindplus.flow.server.dal.repository.FlowTaskRepository;
import com.iwindplus.flow.server.service.FlowTaskService;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 流程任务业务层接口实现类.
 *
 * @author zengdegui
 * @since 2024/11/03 19:11
 */
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class FlowTaskServiceImpl implements FlowTaskService {

    private final FlowTaskRepository flowTaskRepository;
    private final FlowInstanceRepository flowInstanceRepository;
    private final FlowModelRepository flowModelRepository;

    @Override
    public boolean save(FlowTaskSaveDTO entity) {
        final FlowInstanceDO flowInstance = this.flowInstanceRepository.getById(entity.getInstanceId());
        if (flowInstance == null) {
            throw new BizException(FlowCodeEnum.FLOW_INSTANCE_NOT_EXIST);
        }
        final FlowModelDO flowModel = this.flowModelRepository.getById(entity.getModelId());
        if (flowModel == null) {
            throw new BizException(FlowCodeEnum.FLOW_MODEL_NOT_EXIST);
        }
        final FlowTaskDO model = BeanUtil.copyProperties(entity, FlowTaskDO.class);
        return this.flowTaskRepository.save(model);
    }

    @Override
    public boolean removeByIds(List<Long> ids) {
        List<FlowTaskDO> list = this.flowTaskRepository.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return this.flowTaskRepository.removeByIds(ids);
    }

    @Override
    public boolean edit(FlowTaskEditDTO entity) {
        FlowTaskDO data = this.flowTaskRepository.getById(entity.getId());
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
        final FlowTaskDO model = BeanUtil.copyProperties(entity, FlowTaskDO.class);
        return this.flowTaskRepository.updateById(model);
    }

    @Override
    public IPage<FlowTaskPageVO> myPendingPage(FlowTaskSearchDTO entity) {
        PageDTO<FlowTaskPageVO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        return this.flowTaskRepository.getBaseMapper().selectMyPendingPage(page, entity);
    }
}

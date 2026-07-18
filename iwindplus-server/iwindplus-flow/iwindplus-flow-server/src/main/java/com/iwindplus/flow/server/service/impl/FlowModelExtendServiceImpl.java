/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.flow.domain.dto.FlowModelExtendDTO;
import com.iwindplus.flow.domain.enums.FlowCodeEnum;
import com.iwindplus.flow.server.dal.model.FlowModelDO;
import com.iwindplus.flow.server.dal.model.FlowModelExtendDO;
import com.iwindplus.flow.server.dal.repository.FlowModelExtendRepository;
import com.iwindplus.flow.server.dal.repository.FlowModelRepository;
import com.iwindplus.flow.server.service.FlowModelExtendService;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 流程模型扩展业务层接口实现类.
 *
 * @author zengdegui
 * @since 2024/11/03 19:11
 */
@Service
@CacheConfig(cacheNames = {"flowModelExtend"})
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class FlowModelExtendServiceImpl implements FlowModelExtendService {

    private final FlowModelExtendRepository flowModelExtendRepository;
    private final FlowModelRepository flowModelRepository;

    @Override
    public boolean save(FlowModelExtendDTO entity) {
        final FlowModelDO flowModel = this.flowModelRepository.getById(entity.getModelId());
        if (flowModel == null) {
            throw new BizException(FlowCodeEnum.FLOW_MODEL_NOT_EXIST);
        }
        return flowModelExtendRepository.save(entity);
    }

    @Override
    public boolean removeByModelIds(List<Long> modelIds) {
        return CollUtil.isNotEmpty(modelIds) && SqlHelper.retBool(this.flowModelExtendRepository.getBaseMapper().deleteByModelIds(modelIds));
    }

    @Override
    public boolean edit(FlowModelExtendDTO entity) {
        if (ObjectUtil.isEmpty(entity.getModelContent())) {
            return Boolean.FALSE;
        }
        FlowModelExtendDO data = this.flowModelExtendRepository.getOne(Wrappers.lambdaQuery(FlowModelExtendDO.class)
            .eq(FlowModelExtendDO::getModelId, entity.getModelId()));
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (entity.getModelId() != null && !data.getModelId().equals(entity.getModelId())) {
            final FlowModelDO flowModel = this.flowModelRepository.getById(entity.getModelId());
            if (flowModel == null) {
                throw new BizException(FlowCodeEnum.FLOW_MODEL_NOT_EXIST);
            }
        }
        final FlowModelExtendDO model = BeanUtil.copyProperties(entity, FlowModelExtendDO.class);
        model.setId(data.getId());
        this.flowModelExtendRepository.updateById(model);
        return Boolean.TRUE;
    }
}

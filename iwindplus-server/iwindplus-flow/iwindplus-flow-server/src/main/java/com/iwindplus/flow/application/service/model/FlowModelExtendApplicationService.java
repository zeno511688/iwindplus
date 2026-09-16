/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.application.service.model;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.flow.application.service.model.dto.FlowModelExtendDTO;
import com.iwindplus.flow.common.enums.FlowCodeEnum;
import com.iwindplus.flow.infrastructure.persistence.model.FlowModelDO;
import com.iwindplus.flow.infrastructure.persistence.model.FlowModelExtendDO;
import com.iwindplus.flow.infrastructure.persistence.model.FlowModelExtendRepository;
import com.iwindplus.flow.infrastructure.persistence.model.FlowModelRepository;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 流程模型扩展业务层接口类.
 *
 * @author zengdegui
 * @since 2024/11/03 19:11
 */
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class FlowModelExtendApplicationService {

    private final FlowModelExtendRepository flowModelExtendRepository;
    private final FlowModelRepository flowModelRepository;

    /**
     * 添加.
     *
     * @param entity 对象
     * @return boolean
     */
    public boolean save(FlowModelExtendDTO entity) {
        final FlowModelDO flowModel = this.flowModelRepository.getById(entity.getModelId());
        if (flowModel == null) {
            throw new BizException(FlowCodeEnum.FLOW_MODEL_NOT_EXIST);
        }
        return flowModelExtendRepository.save(entity);
    }

    /**
     * 通过模型主键真实删除.
     *
     * @param modelIds 模型主键集合
     * @return boolean
     */
    public boolean removeByModelIds(List<Long> modelIds) {
        return CollUtil.isNotEmpty(modelIds) && SqlHelper.retBool(this.flowModelExtendRepository.getBaseMapper().deleteByModelIds(modelIds));
    }

    /**
     * 编辑.
     *
     * @param entity 对象
     * @return boolean
     */
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

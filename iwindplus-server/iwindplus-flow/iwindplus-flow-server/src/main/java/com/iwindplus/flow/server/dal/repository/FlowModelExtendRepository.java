/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server.dal.repository;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.yulichang.repository.JoinCrudRepository;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.flow.domain.dto.FlowModelExtendDTO;
import com.iwindplus.flow.server.dal.mapper.FlowModelExtendMapper;
import com.iwindplus.flow.server.dal.model.FlowModelExtendDO;
import java.util.Objects;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 流程模型扩展聚合层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Repository
public class FlowModelExtendRepository extends JoinCrudRepository<FlowModelExtendMapper, FlowModelExtendDO> {

    /**
     * 保存流程模型扩展信息.
     *
     * @param entity 流程模型扩展信息
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean save(FlowModelExtendDTO entity) {
        final FlowModelExtendDO model = BeanUtil.copyProperties(entity, FlowModelExtendDO.class);
        super.save(model);
        entity.setId(model.getId());
        return Boolean.TRUE;
    }

    /**
     * 修改流程模型扩展信息.
     *
     * @param entity 模型扩展信息
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean edit(FlowModelExtendDTO entity) {
        if (ObjectUtil.isEmpty(entity.getModelContent())) {
            return Boolean.FALSE;
        }
        FlowModelExtendDO data = super.getOne(Wrappers.lambdaQuery(FlowModelExtendDO.class)
            .eq(FlowModelExtendDO::getModelId, entity.getModelId()));
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Objects.isNull(entity.getVersion())) {
            entity.setVersion(data.getVersion());
        }

        final FlowModelExtendDO model = BeanUtil.copyProperties(entity, FlowModelExtendDO.class);
        model.setId(data.getId());
        super.updateById(model);
        return Boolean.TRUE;
    }

    /**
     * 根据模型ID获取模型扩展信息.
     *
     * @param modelId 模型ID
     * @return 模型扩展信息
     */
    public FlowModelExtendDO getByModelId(Long modelId) {
        FlowModelExtendDO data = super.getOne(Wrappers.lambdaQuery(FlowModelExtendDO.class)
            .eq(FlowModelExtendDO::getModelId, modelId));
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return data;
    }
}

/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.application.service.instance;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.flow.application.service.instance.dto.FlowInstanceExtendEditDTO;
import com.iwindplus.flow.application.service.instance.dto.FlowInstanceExtendSaveDTO;
import com.iwindplus.flow.common.enums.FlowCodeEnum;
import com.iwindplus.flow.infrastructure.persistence.instance.FlowInstanceDO;
import com.iwindplus.flow.infrastructure.persistence.instance.FlowInstanceExtendDO;
import com.iwindplus.flow.infrastructure.persistence.instance.FlowInstanceExtendRepository;
import com.iwindplus.flow.infrastructure.persistence.instance.FlowInstanceRepository;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 流程实例扩展业务层接口类.
 *
 * @author zengdegui
 * @since 2024/11/03 19:11
 */
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class FlowInstanceExtendApplicationService {

    private final FlowInstanceRepository flowInstanceRepository;
    private final FlowInstanceExtendRepository flowInstanceExtendRepository;

    /**
     * 保存.
     *
     * @param entity 对象
     * @return boolean
     */
    public boolean save(FlowInstanceExtendSaveDTO entity) {
        final FlowInstanceDO flowInstance = this.flowInstanceRepository.getById(entity.getInstanceId());
        if (flowInstance == null) {
            throw new BizException(FlowCodeEnum.FLOW_INSTANCE_NOT_EXIST);
        }
        final FlowInstanceExtendDO model = BeanUtil.copyProperties(entity, FlowInstanceExtendDO.class);
        this.flowInstanceExtendRepository.save(model);
        return Boolean.TRUE;
    }

    /**
     * 批量删除.
     *
     * @param ids 主键集合
     * @return boolean
     */
    public boolean removeByIds(List<Long> ids) {
        List<FlowInstanceExtendDO> list = this.flowInstanceExtendRepository.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        this.flowInstanceExtendRepository.removeByIds(ids);
        return Boolean.TRUE;
    }

    /**
     * 编辑.
     *
     * @param entity 对象
     * @return boolean
     */
    public boolean edit(FlowInstanceExtendEditDTO entity) {
        FlowInstanceExtendDO data = this.flowInstanceExtendRepository.getById(entity.getId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (entity.getInstanceId() != null && !data.getInstanceId().equals(entity.getInstanceId())) {
            final FlowInstanceDO flowInstance = this.flowInstanceRepository.getById(entity.getInstanceId());
            if (flowInstance == null) {
                throw new BizException(FlowCodeEnum.FLOW_INSTANCE_NOT_EXIST);
            }
        }
        if (Objects.isNull(entity.getVersion())) {
            entity.setVersion(data.getVersion());
        }
        final FlowInstanceExtendDO model = BeanUtil.copyProperties(entity, FlowInstanceExtendDO.class);
        this.flowInstanceExtendRepository.updateById(model);
        return Boolean.TRUE;
    }
}

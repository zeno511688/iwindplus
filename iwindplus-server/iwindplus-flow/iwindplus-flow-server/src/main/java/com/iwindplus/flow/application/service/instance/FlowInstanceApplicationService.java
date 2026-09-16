/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.application.service.instance;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.redis.executor.RedissonExecutor;
import com.iwindplus.flow.application.service.instance.dto.FlowInstanceEditDTO;
import com.iwindplus.flow.application.service.instance.dto.FlowInstanceSaveDTO;
import com.iwindplus.flow.common.enums.FlowCodeEnum;
import com.iwindplus.flow.infrastructure.persistence.instance.FlowInstanceDO;
import com.iwindplus.flow.infrastructure.persistence.instance.FlowInstanceRepository;
import com.iwindplus.flow.infrastructure.persistence.model.FlowModelDO;
import com.iwindplus.flow.infrastructure.persistence.model.FlowModelRepository;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 流程实例业务层接口类.
 *
 * @author zengdegui
 * @since 2024/11/03 19:11
 */
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class FlowInstanceApplicationService {

    private final RedissonExecutor redissonExecutor;
    private final FlowInstanceRepository flowInstanceRepository;
    private final FlowModelRepository flowModelRepository;

    /**
     * 保存.
     *
     * @param entity 对象
     * @return boolean
     */
    public boolean save(FlowInstanceSaveDTO entity) {
        if (CharSequenceUtil.isBlank(entity.getBizNumber())) {
            entity.setCode(this.redissonExecutor.serialNum().getSerialNumDate("FI"));
        }
        final FlowModelDO flowModel = this.flowModelRepository.getById(entity.getModelId());
        if (flowModel == null) {
            throw new BizException(FlowCodeEnum.FLOW_MODEL_NOT_EXIST);
        }
        this.flowInstanceRepository.getCodeIsExist(entity.getCode().trim());
        this.flowInstanceRepository.getBizNumberIsExist(entity.getBizNumber().trim());
        final FlowInstanceDO model = BeanUtil.copyProperties(entity, FlowInstanceDO.class);
        this.flowInstanceRepository.save(model);
        return Boolean.TRUE;
    }

    /**
     * 批量删除.
     *
     * @param ids 主键集合
     * @return boolean
     */
    public boolean removeByIds(List<Long> ids) {
        List<FlowInstanceDO> list = this.flowInstanceRepository.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        this.flowInstanceRepository.removeByIds(ids);
        return Boolean.TRUE;
    }

    /**
     * 编辑.
     *
     * @param entity 对象
     * @return boolean
     */
    public boolean edit(FlowInstanceEditDTO entity) {
        FlowInstanceDO data = this.flowInstanceRepository.getById(entity.getId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (CharSequenceUtil.isNotBlank(entity.getBizNumber()) && !CharSequenceUtil.equals(data.getBizNumber(), entity.getBizNumber().trim())) {
            this.flowInstanceRepository.getBizNumberIsExist(entity.getBizNumber().trim());
        }
        if (entity.getModelId() != null && !data.getModelId().equals(entity.getModelId())) {
            final FlowModelDO flowModel = this.flowModelRepository.getById(entity.getModelId());
            if (flowModel == null) {
                throw new BizException(FlowCodeEnum.FLOW_MODEL_NOT_EXIST);
            }
        }
        if (Objects.isNull(entity.getVersion())) {
            entity.setVersion(data.getVersion());
        }
        final FlowInstanceDO model = BeanUtil.copyProperties(entity, FlowInstanceDO.class);
        this.flowInstanceRepository.updateById(model);
        return Boolean.TRUE;
    }
}

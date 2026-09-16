/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.application.query.model;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.flow.application.query.model.dto.FlowModelSearchDTO;
import com.iwindplus.flow.application.query.model.vo.FlowModelExtVO;
import com.iwindplus.flow.application.query.model.vo.FlowModelPageVO;
import com.iwindplus.flow.common.constant.FlowConstant.RedisCacheConstant;
import com.iwindplus.flow.infrastructure.persistence.model.FlowModelDO;
import com.iwindplus.flow.infrastructure.persistence.model.FlowModelRepository;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 流程模型查询业务层.
 *
 * @author zengdegui
 * @since 2024/11/03 19:11
 */
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_FLOW_MODEL})
@RequiredArgsConstructor
public class FlowModelQueryService {

    private final FlowModelRepository flowModelRepository;

    /**
     * 分页查询.
     *
     * @param entity 对象
     * @return IPage<FlowModelPageVO>
     */
    public IPage<FlowModelPageVO> page(FlowModelSearchDTO entity) {
        PageDTO<FlowModelDO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        return this.flowModelRepository.getBaseMapper().selectPageByCondition(page, entity);
    }

    /**
     * 通过主键端查找.
     *
     * @param id 主键
     * @return FlowModelExtVO
     */
    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    public FlowModelExtVO getDetail(Long id) {
        FlowModelExtVO data = this.flowModelRepository.getBaseMapper().selectDetailById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return data;
    }
}

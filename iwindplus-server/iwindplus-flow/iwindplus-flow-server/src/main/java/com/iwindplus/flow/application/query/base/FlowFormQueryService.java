/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.application.query.base;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.flow.application.query.base.dto.FlowFormSearchDTO;
import com.iwindplus.flow.application.query.base.vo.FlowFormBaseExtendVO;
import com.iwindplus.flow.application.query.base.vo.FlowFormPageVO;
import com.iwindplus.flow.application.query.base.vo.FlowFormVO;
import com.iwindplus.flow.common.constant.FlowConstant.RedisCacheConstant;
import com.iwindplus.flow.infrastructure.persistence.base.FlowFormDO;
import com.iwindplus.flow.infrastructure.persistence.base.FlowFormRepository;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 流程表单查询业务层.
 *
 * @author zengdegui
 * @since 2024/11/03 19:11
 */
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_FLOW_FORM})
@RequiredArgsConstructor
public class FlowFormQueryService {

    private final FlowFormRepository flowFormRepository;

    /**
     * 分页查询.
     *
     * @param entity 对象
     * @return IPage<FlowFormPageVO>
     */
    public IPage<FlowFormPageVO> page(FlowFormSearchDTO entity) {
        PageDTO<FlowFormDO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        LambdaQueryWrapper<FlowFormDO> queryWrapper = Wrappers.lambdaQuery(FlowFormDO.class)
            .orderByDesc(FlowFormDO::getModifiedTimestamp);
        if (Objects.nonNull(entity.getStatus())) {
            queryWrapper.eq(FlowFormDO::getStatus, entity.getStatus());
        }
        if (CharSequenceUtil.isNotBlank(entity.getCode())) {
            queryWrapper.eq(FlowFormDO::getCode, entity.getCode().trim());
        }
        if (CharSequenceUtil.isNotBlank(entity.getName())) {
            queryWrapper.like(FlowFormDO::getName, entity.getName().trim());
        }
        queryWrapper.select(FlowFormDO::getId, FlowFormDO::getCreatedTimestamp, FlowFormDO::getCreatedBy,
            FlowFormDO::getModifiedTimestamp,
            FlowFormDO::getModifiedBy, FlowFormDO::getVersion, FlowFormDO::getStatus, FlowFormDO::getName, FlowFormDO::getCode,
            FlowFormDO::getSeq, FlowFormDO::getBuildInFlag);
        final PageDTO<FlowFormDO> modelPage = this.flowFormRepository.page(page, queryWrapper);
        return modelPage.convert(model -> BeanUtil.copyProperties(model, FlowFormPageVO.class));
    }

    /**
     * 查询启用的.
     *
     * @return List<FlowFormBaseExtendVO>
     */
    @Cacheable(key = "#root.methodName", unless = "#result == null")
    public List<FlowFormBaseExtendVO> listEnabled() {
        LambdaQueryWrapper<FlowFormDO> queryWrapper = Wrappers.lambdaQuery(FlowFormDO.class)
            .eq(FlowFormDO::getStatus, EnableStatusEnum.ENABLE)
            .select(FlowFormDO::getId, FlowFormDO::getName, FlowFormDO::getContent)
            .orderByAsc(List.of(FlowFormDO::getSeq));
        List<FlowFormDO> list = this.flowFormRepository.list(queryWrapper);
        if (CollUtil.isEmpty(list)) {
            return null;
        }
        return BeanUtil.copyToList(list, FlowFormBaseExtendVO.class);
    }

    /**
     * 通过主键端查找.
     *
     * @param id 主键
     * @return FlowFormVO
     */
    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    public FlowFormVO getDetail(Long id) {
        FlowFormDO data = this.flowFormRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        FlowFormVO result = BeanUtil.copyProperties(data, FlowFormVO.class);
        return result;
    }
}

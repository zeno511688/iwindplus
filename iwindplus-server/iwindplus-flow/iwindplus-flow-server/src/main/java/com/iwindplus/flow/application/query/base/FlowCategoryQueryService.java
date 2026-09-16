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
import com.iwindplus.flow.application.query.base.dto.FlowCategorySearchDTO;
import com.iwindplus.flow.application.query.base.vo.FlowCategoryBaseVO;
import com.iwindplus.flow.application.query.base.vo.FlowCategoryPageVO;
import com.iwindplus.flow.application.query.base.vo.FlowCategoryVO;
import com.iwindplus.flow.common.constant.FlowConstant.RedisCacheConstant;
import com.iwindplus.flow.infrastructure.persistence.base.FlowCategoryDO;
import com.iwindplus.flow.infrastructure.persistence.base.FlowCategoryRepository;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 流程分类查询业务层.
 *
 * @author zengdegui
 * @since 2024/11/03 19:11
 */
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_FLOW_CATEGORY})
@RequiredArgsConstructor
public class FlowCategoryQueryService {

    private final FlowCategoryRepository flowCategoryRepository;

    /**
     * 列表.
     *
     * @param entity 对象
     * @return IPage<FlowCategoryPageVO>
     */
    public IPage<FlowCategoryPageVO> page(FlowCategorySearchDTO entity) {
        PageDTO<FlowCategoryDO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        LambdaQueryWrapper<FlowCategoryDO> queryWrapper = Wrappers.lambdaQuery(FlowCategoryDO.class)
            .orderByDesc(FlowCategoryDO::getModifiedTimestamp);
        if (Objects.nonNull(entity.getStatus())) {
            queryWrapper.eq(FlowCategoryDO::getStatus, entity.getStatus());
        }
        if (CharSequenceUtil.isNotBlank(entity.getCode())) {
            queryWrapper.eq(FlowCategoryDO::getCode, entity.getCode().trim());
        }
        if (CharSequenceUtil.isNotBlank(entity.getName())) {
            queryWrapper.eq(FlowCategoryDO::getName, entity.getName().trim());
        }
        queryWrapper.select(FlowCategoryDO::getId, FlowCategoryDO::getCreatedTimestamp, FlowCategoryDO::getCreatedBy,
            FlowCategoryDO::getModifiedTimestamp,
            FlowCategoryDO::getModifiedBy, FlowCategoryDO::getVersion, FlowCategoryDO::getStatus, FlowCategoryDO::getName, FlowCategoryDO::getCode,
            FlowCategoryDO::getSeq, FlowCategoryDO::getBuildInFlag);
        final PageDTO<FlowCategoryDO> modelPage = this.flowCategoryRepository.page(page, queryWrapper);
        return modelPage.convert(model -> BeanUtil.copyProperties(model, FlowCategoryPageVO.class));
    }

    /**
     * 查询启用的.
     *
     * @return List<FlowCategoryBaseVO>
     */
    @Cacheable(key = "#root.methodName", unless = "#result == null")
    public List<FlowCategoryBaseVO> listEnabled() {
        LambdaQueryWrapper<FlowCategoryDO> queryWrapper = Wrappers.lambdaQuery(FlowCategoryDO.class)
            .eq(FlowCategoryDO::getStatus, EnableStatusEnum.ENABLE)
            .select(FlowCategoryDO::getId, FlowCategoryDO::getCode, FlowCategoryDO::getName)
            .orderByAsc(List.of(FlowCategoryDO::getSeq));
        List<FlowCategoryDO> list = this.flowCategoryRepository.list(queryWrapper);
        if (CollUtil.isEmpty(list)) {
            return null;
        }
        return BeanUtil.copyToList(list, FlowCategoryBaseVO.class);
    }

    /**
     * 通过主键端查找.
     *
     * @param id 主键
     * @return FlowCategoryVO
     */
    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    public FlowCategoryVO getDetail(Long id) {
        FlowCategoryDO data = this.flowCategoryRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return BeanUtil.copyProperties(data, FlowCategoryVO.class);
    }

}

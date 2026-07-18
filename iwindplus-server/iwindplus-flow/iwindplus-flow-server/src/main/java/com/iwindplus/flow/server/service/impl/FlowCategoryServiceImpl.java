/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server.service.impl;

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
import com.iwindplus.base.redis.service.RedissonService;
import com.iwindplus.flow.domain.constant.FlowConstant.RedisCacheConstant;
import com.iwindplus.flow.domain.dto.FlowCategoryEditDTO;
import com.iwindplus.flow.domain.dto.FlowCategorySaveDTO;
import com.iwindplus.flow.domain.dto.FlowCategorySearchDTO;
import com.iwindplus.flow.domain.enums.FlowCodePrefixEnum;
import com.iwindplus.flow.domain.vo.FlowCategoryBaseVO;
import com.iwindplus.flow.domain.vo.FlowCategoryPageVO;
import com.iwindplus.flow.domain.vo.FlowCategoryVO;
import com.iwindplus.flow.server.dal.model.FlowCategoryDO;
import com.iwindplus.flow.server.dal.repository.FlowCategoryRepository;
import com.iwindplus.flow.server.service.FlowCategoryService;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 流程分类业务层接口实现类.
 *
 * @author zengdegui
 * @since 2024/11/03 19:11
 */
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_FLOW_CATEGORY})
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class FlowCategoryServiceImpl implements FlowCategoryService {

    private final RedissonService redissonService;
    private final FlowCategoryRepository flowCategoryRepository;

    @CacheEvict(allEntries = true)
    @Override
    public boolean save(FlowCategorySaveDTO entity) {
        entity.setStatus(EnableStatusEnum.ENABLE);
        this.flowCategoryRepository.getNameIsExist(entity.getName());
        entity.setSeq(this.flowCategoryRepository.getNextSeq());
        if (CharSequenceUtil.isBlank(entity.getCode())) {
            entity.setCode(this.redissonService.serialNum().getSerialNumDate(FlowCodePrefixEnum.CATEGORY_PREFIX.code()));
        }
        this.flowCategoryRepository.getCodeIsExist(entity.getCode());
        final FlowCategoryDO model = BeanUtil.copyProperties(entity, FlowCategoryDO.class);
        this.flowCategoryRepository.save(model);
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean removeByIds(List<Long> ids) {
        List<FlowCategoryDO> list = this.flowCategoryRepository.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        this.flowCategoryRepository.removeByIds(ids);
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean edit(FlowCategoryEditDTO entity) {
        FlowCategoryDO data = this.flowCategoryRepository.getById(entity.getId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (CharSequenceUtil.isNotBlank(entity.getName()) && !CharSequenceUtil.equals(data.getName(), entity.getName().trim())) {
            this.flowCategoryRepository.getNameIsExist(entity.getName().trim());
        }
        if (Objects.isNull(entity.getVersion())) {
            entity.setVersion(data.getVersion());
        }
        final FlowCategoryDO model = BeanUtil.copyProperties(entity, FlowCategoryDO.class);
        this.flowCategoryRepository.updateById(model);
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean editStatus(Long id, EnableStatusEnum status) {
        FlowCategoryDO data = this.flowCategoryRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (status.equals(data.getStatus())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        FlowCategoryDO param = new FlowCategoryDO();
        param.setId(id);
        param.setStatus(status);
        param.setVersion(data.getVersion());
        this.flowCategoryRepository.updateById(param);
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean editBuildIn(Long id, Boolean buildInFlag) {
        FlowCategoryDO data = this.flowCategoryRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        FlowCategoryDO param = new FlowCategoryDO();
        param.setId(id);
        param.setBuildInFlag(buildInFlag);
        param.setVersion(data.getVersion());
        this.flowCategoryRepository.updateById(param);
        return Boolean.TRUE;
    }

    @Override
    public IPage<FlowCategoryPageVO> page(FlowCategorySearchDTO entity) {
        PageDTO<FlowCategoryDO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        LambdaQueryWrapper<FlowCategoryDO> queryWrapper = Wrappers.lambdaQuery(FlowCategoryDO.class)
            .orderByDesc(FlowCategoryDO::getModifiedTime);
        if (Objects.nonNull(entity.getStatus())) {
            queryWrapper.eq(FlowCategoryDO::getStatus, entity.getStatus());
        }
        if (CharSequenceUtil.isNotBlank(entity.getCode())) {
            queryWrapper.eq(FlowCategoryDO::getCode, entity.getCode().trim());
        }
        if (CharSequenceUtil.isNotBlank(entity.getName())) {
            queryWrapper.eq(FlowCategoryDO::getName, entity.getName().trim());
        }
        queryWrapper.select(FlowCategoryDO::getId, FlowCategoryDO::getCreatedTime, FlowCategoryDO::getCreatedTimestamp, FlowCategoryDO::getCreatedBy,
            FlowCategoryDO::getModifiedTime, FlowCategoryDO::getModifiedTimestamp,
            FlowCategoryDO::getModifiedBy, FlowCategoryDO::getVersion, FlowCategoryDO::getStatus, FlowCategoryDO::getName, FlowCategoryDO::getCode,
            FlowCategoryDO::getSeq, FlowCategoryDO::getBuildInFlag);
        final PageDTO<FlowCategoryDO> modelPage = this.flowCategoryRepository.page(page, queryWrapper);
        return modelPage.convert(model -> BeanUtil.copyProperties(model, FlowCategoryPageVO.class));
    }

    @Cacheable(key = "#root.methodName", unless = "#result == null")
    @Override
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

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    @Override
    public FlowCategoryVO getDetail(Long id) {
        FlowCategoryDO data = this.flowCategoryRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        FlowCategoryVO result = BeanUtil.copyProperties(data, FlowCategoryVO.class);
        return result;
    }

}

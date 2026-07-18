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
import com.iwindplus.flow.domain.dto.FlowFormEditDTO;
import com.iwindplus.flow.domain.dto.FlowFormSaveDTO;
import com.iwindplus.flow.domain.dto.FlowFormSearchDTO;
import com.iwindplus.flow.domain.enums.FlowCodePrefixEnum;
import com.iwindplus.flow.domain.vo.FlowFormBaseExtendVO;
import com.iwindplus.flow.domain.vo.FlowFormPageVO;
import com.iwindplus.flow.domain.vo.FlowFormVO;
import com.iwindplus.flow.server.dal.model.FlowFormDO;
import com.iwindplus.flow.server.dal.repository.FlowFormRepository;
import com.iwindplus.flow.server.service.FlowFormService;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 流程表单业务层接口实现类.
 *
 * @author zengdegui
 * @since 2024/11/03 19:11
 */
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_FLOW_FORM})
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class FlowFormServiceImpl implements FlowFormService {

    private final RedissonService redissonService;
    private final FlowFormRepository flowFormRepository;

    @CacheEvict(allEntries = true)
    @Override
    public boolean save(FlowFormSaveDTO entity) {
        entity.setStatus(EnableStatusEnum.ENABLE);
        this.flowFormRepository.getNameIsExist(entity.getName());
        entity.setSeq(this.flowFormRepository.getNextSeq());
        if (CharSequenceUtil.isBlank(entity.getCode())) {
            entity.setCode(this.redissonService.serialNum().getSerialNumDate(FlowCodePrefixEnum.FORM_PREFIX.code()));
        }
        this.flowFormRepository.getCodeIsExist(entity.getCode());
        final FlowFormDO model = BeanUtil.copyProperties(entity, FlowFormDO.class);
        this.flowFormRepository.save(model);
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean removeByIds(List<Long> ids) {
        List<FlowFormDO> list = this.flowFormRepository.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        this.flowFormRepository.removeByIds(ids);
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean edit(FlowFormEditDTO entity) {
        FlowFormDO data = this.flowFormRepository.getById(entity.getId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (CharSequenceUtil.isNotBlank(entity.getName()) && !CharSequenceUtil.equals(data.getName(), entity.getName().trim())) {
            this.flowFormRepository.getNameIsExist(entity.getName().trim());
        }
        if (Objects.isNull(entity.getVersion())) {
            entity.setVersion(data.getVersion());
        }
        final FlowFormDO model = BeanUtil.copyProperties(entity, FlowFormDO.class);
        this.flowFormRepository.updateById(model);
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean editStatus(Long id, EnableStatusEnum status) {
        FlowFormDO data = this.flowFormRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (status.equals(data.getStatus())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        FlowFormDO param = new FlowFormDO();
        param.setId(id);
        param.setStatus(status);
        param.setVersion(data.getVersion());
        this.flowFormRepository.updateById(param);
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean editBuildIn(Long id, Boolean buildInFlag) {
        FlowFormDO data = this.flowFormRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        FlowFormDO param = new FlowFormDO();
        param.setId(id);
        param.setBuildInFlag(buildInFlag);
        param.setVersion(data.getVersion());
        this.flowFormRepository.updateById(param);
        return Boolean.TRUE;
    }

    @Override
    public IPage<FlowFormPageVO> page(FlowFormSearchDTO entity) {
        PageDTO<FlowFormDO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        LambdaQueryWrapper<FlowFormDO> queryWrapper = Wrappers.lambdaQuery(FlowFormDO.class)
            .orderByDesc(FlowFormDO::getModifiedTime);
        if (Objects.nonNull(entity.getStatus())) {
            queryWrapper.eq(FlowFormDO::getStatus, entity.getStatus());
        }
        if (CharSequenceUtil.isNotBlank(entity.getCode())) {
            queryWrapper.eq(FlowFormDO::getCode, entity.getCode().trim());
        }
        if (CharSequenceUtil.isNotBlank(entity.getName())) {
            queryWrapper.like(FlowFormDO::getName, entity.getName().trim());
        }
        queryWrapper.select(FlowFormDO::getId, FlowFormDO::getCreatedTime, FlowFormDO::getCreatedTimestamp, FlowFormDO::getCreatedBy,
            FlowFormDO::getModifiedTime, FlowFormDO::getModifiedTimestamp,
            FlowFormDO::getModifiedBy, FlowFormDO::getVersion, FlowFormDO::getStatus, FlowFormDO::getName, FlowFormDO::getCode,
            FlowFormDO::getSeq, FlowFormDO::getBuildInFlag);
        final PageDTO<FlowFormDO> modelPage = this.flowFormRepository.page(page, queryWrapper);
        return modelPage.convert(model -> BeanUtil.copyProperties(model, FlowFormPageVO.class));
    }

    @Cacheable(key = "#root.methodName", unless = "#result == null")
    @Override
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

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    @Override
    public FlowFormVO getDetail(Long id) {
        FlowFormDO data = this.flowFormRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        FlowFormVO result = BeanUtil.copyProperties(data, FlowFormVO.class);
        return result;
    }

}

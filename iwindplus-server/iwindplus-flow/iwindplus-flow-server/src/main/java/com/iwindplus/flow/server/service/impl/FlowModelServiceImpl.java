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
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.redis.service.RedissonService;
import com.iwindplus.flow.domain.constant.FlowConstant.RedisCacheConstant;
import com.iwindplus.flow.domain.dto.FlowModelContentDTO;
import com.iwindplus.flow.domain.dto.FlowModelEditDTO;
import com.iwindplus.flow.domain.dto.FlowModelExtendDTO;
import com.iwindplus.flow.domain.dto.FlowModelExtendDTO.FlowModelExtendDTOBuilder;
import com.iwindplus.flow.domain.dto.FlowModelSaveDTO;
import com.iwindplus.flow.domain.dto.FlowModelSearchDTO;
import com.iwindplus.flow.domain.enums.FlowCodeEnum;
import com.iwindplus.flow.domain.enums.FlowCodePrefixEnum;
import com.iwindplus.flow.domain.enums.FlowFormTypeEnum;
import com.iwindplus.flow.domain.enums.FlowModelStatusEnum;
import com.iwindplus.flow.domain.vo.FlowModelExtVO;
import com.iwindplus.flow.domain.vo.FlowModelPageVO;
import com.iwindplus.flow.server.dal.model.FlowCategoryDO;
import com.iwindplus.flow.server.dal.model.FlowFormDO;
import com.iwindplus.flow.server.dal.model.FlowModelDO;
import com.iwindplus.flow.server.dal.model.FlowModelExtendDO;
import com.iwindplus.flow.server.dal.repository.FlowCategoryRepository;
import com.iwindplus.flow.server.dal.repository.FlowFormRepository;
import com.iwindplus.flow.server.dal.repository.FlowModelExtendRepository;
import com.iwindplus.flow.server.dal.repository.FlowModelRepository;
import com.iwindplus.flow.server.service.FlowModelService;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 流程模型业务层接口实现类.
 *
 * @author zengdegui
 * @since 2024/11/03 19:11
 */
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_FLOW_MODEL})
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class FlowModelServiceImpl implements FlowModelService {

    private final RedissonService redissonService;
    private final FlowCategoryRepository flowCategoryRepository;
    private final FlowModelRepository flowModelRepository;
    private final FlowModelExtendRepository flowModelExtendRepository;
    private final FlowFormRepository flowFormRepository;

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_FLOW_FORM}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_FLOW_CATEGORY}, allEntries = true),
        }
    )
    @Override
    public boolean save(FlowModelSaveDTO entity) {
        if (Objects.isNull(entity.getStatus())) {
            entity.setStatus(FlowModelStatusEnum.TO_BE_PUBLISHED);
        }
        if (FlowModelStatusEnum.PUBLISHED.equals(entity.getStatus())) {
            entity.setModelVersion(1);
        }
        if (Objects.isNull(entity.getFormType())) {
            entity.setFormType(FlowFormTypeEnum.NONE);
        }
        if (FlowFormTypeEnum.NONE.equals(entity.getFormType())) {
            entity.setFormId(null);
        }
        this.flowModelRepository.getNameIsExist(entity.getName());
        entity.setSeq(this.flowModelRepository.getNextSeq());
        if (CharSequenceUtil.isBlank(entity.getCode())) {
            entity.setCode(this.redissonService.serialNum().getSerialNumDate(FlowCodePrefixEnum.MODEL_PREFIX.code()));
        }
        this.flowModelRepository.getCodeIsExist(entity.getCode());
        final FlowCategoryDO flowCategory = this.flowCategoryRepository.getById(entity.getCategoryId());
        if (flowCategory == null) {
            throw new BizException(FlowCodeEnum.FLOW_CATEGORY_NOT_EXIST);
        }
        if (entity.getFormId() != null) {
            final FlowFormDO flowForm = this.flowFormRepository.getById(entity.getFormId());
            if (flowForm == null) {
                throw new BizException(FlowCodeEnum.FLOW_FORM_NOT_EXIST);
            }
        }
        final FlowModelDO model = BeanUtil.copyProperties(entity, FlowModelDO.class);
        this.flowModelRepository.save(model);
        entity.setId(model.getId());
        // 扩展字段
        FlowModelExtendDTO flowModelExtendDTO = this.buildFlowModelExtendDTO(entity.getId(), entity.getFormId(), entity.getModelContent());
        this.flowModelExtendRepository.save(flowModelExtendDTO);
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_FLOW_FORM}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_FLOW_CATEGORY}, allEntries = true),
        }
    )
    @Override
    public boolean removeByIds(List<Long> ids) {
        List<FlowModelDO> list = this.flowModelRepository.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        boolean matchStatus = list.stream().filter(Objects::nonNull).anyMatch(m -> FlowModelStatusEnum.DISABLED.equals(m.getStatus()));
        if (!matchStatus) {
            throw new BizException(FlowCodeEnum.DISABLED_CAN_DELETE);
        }
        boolean match = list.stream().filter(Objects::nonNull).anyMatch(FlowModelDO::getBuildInFlag);
        if (Boolean.TRUE.equals(match)) {
            throw new BizException(BizCodeEnum.HAS_BUILD_IN_DATA);
        }
        this.flowModelRepository.removeByIds(ids);
        this.flowModelExtendRepository.remove(Wrappers.lambdaUpdate(FlowModelExtendDO.class).in(FlowModelExtendDO::getModelId, ids));
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_FLOW_FORM}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_FLOW_CATEGORY}, allEntries = true),
        }
    )
    @Override
    public boolean edit(FlowModelEditDTO entity) {
        entity.setStatus(null);
        FlowModelDO data = this.flowModelRepository.getById(entity.getId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (FlowModelStatusEnum.PUBLISHED.equals(data.getStatus())) {
            throw new BizException(FlowCodeEnum.PUBLISHED_NOT_EDIT);
        }
        if (!FlowFormTypeEnum.FORM.equals(entity.getFormType())) {
            entity.setFormId(0L);
        }
        if (entity.getCategoryId() != null && !data.getCategoryId().equals(entity.getCategoryId())) {
            final FlowCategoryDO flowCategory = this.flowCategoryRepository.getById(entity.getCategoryId());
            if (flowCategory == null) {
                throw new BizException(FlowCodeEnum.FLOW_CATEGORY_NOT_EXIST);
            }
        }
        if (entity.getFormId() != null && !data.getFormId().equals(entity.getFormId())) {
            final FlowFormDO flowForm = this.flowFormRepository.getById(entity.getFormId());
            if (flowForm == null) {
                throw new BizException(FlowCodeEnum.FLOW_FORM_NOT_EXIST);
            }
        }
        // 校验名称是否存在
        if (CharSequenceUtil.isNotBlank(entity.getName()) && !CharSequenceUtil.equals(data.getName(), entity.getName().trim())) {
            this.flowModelRepository.getNameIsExist(entity.getName().trim());
        }
        if (CharSequenceUtil.isNotBlank(entity.getCode()) && !CharSequenceUtil.equals(data.getCode(), entity.getCode().trim())) {
            this.flowModelRepository.getCodeIsExist(entity.getCode().trim());
        }
        if (Objects.isNull(entity.getVersion())) {
            entity.setVersion(data.getVersion());
        }
        final FlowModelDO model = BeanUtil.copyProperties(entity, FlowModelDO.class);
        this.flowModelRepository.updateById(model);
        // 扩展字段
        FlowModelExtendDTO flowModelExtendDTO = this.buildFlowModelExtendDTO(entity.getId(), entity.getFormId(), entity.getModelContent());
        this.flowModelExtendRepository.edit(flowModelExtendDTO);
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_FLOW_FORM}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_FLOW_CATEGORY}, allEntries = true),
        }
    )
    @Override
    public boolean editStatus(Long id, FlowModelStatusEnum status) {
        FlowModelDO data = this.flowModelRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (status.equals(data.getStatus())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        if (FlowModelStatusEnum.PUBLISHED.equals(status)) {
            // 发布
            return this.editDeploy(data);
        }
        if (FlowModelStatusEnum.DISABLED.equals(status)) {
            // 禁用
            return this.copyModel(data, false);
        }

        FlowModelDO param = new FlowModelDO();
        param.setId(id);
        param.setStatus(status);
        param.setVersion(data.getVersion());
        this.flowModelRepository.updateById(param);
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_FLOW_FORM}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_FLOW_CATEGORY}, allEntries = true),
        }
    )
    @Override
    public boolean editBuildIn(Long id, Boolean buildInFlag) {
        FlowModelDO data = this.flowModelRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (buildInFlag.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        FlowModelDO param = new FlowModelDO();
        param.setId(id);
        param.setBuildInFlag(buildInFlag);
        param.setVersion(data.getVersion());
        this.flowModelRepository.updateById(param);
        return Boolean.TRUE;
    }

    @Override
    public IPage<FlowModelPageVO> page(FlowModelSearchDTO entity) {
        PageDTO<FlowModelDO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        return this.flowModelRepository.getBaseMapper().selectPageByCondition(page, entity);
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    @Override
    public FlowModelExtVO getDetail(Long id) {
        FlowModelExtVO data = this.flowModelRepository.getBaseMapper().selectDetailById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return data;
    }

    private FlowModelExtendDTO buildFlowModelExtendDTO(Long id, Long formId, FlowModelContentDTO modelContent) {
        final FlowModelExtendDTOBuilder<?, ?> builder = FlowModelExtendDTO
            .builder()
            .modelContent(modelContent)
            .formContent("")
            .modelId(id);
        if (Objects.isNull(formId) || formId == 0) {
            return builder.build();
        }

        // 获取表单内容
        final FlowFormDO form = this.flowFormRepository.getById(formId);
        if (Objects.isNull(form)) {
            return builder.build();
        }
        builder.formContent(form.getContent());
        return builder.build();
    }

    public boolean editDeploy(FlowModelDO data) {
        // 版本大于0的模型，且已发布，copy
        if (data.getModelVersion() > 0 && FlowModelStatusEnum.PUBLISHED.equals(data.getStatus())) {
            return this.copyModel(data, true);
        } else {
            FlowModelDO param = new FlowModelDO();
            param.setId(data.getId());
            param.setVersion(data.getVersion());
            param.setStatus(FlowModelStatusEnum.PUBLISHED);
            param.setModelVersion(data.getModelVersion() + 1);
            return this.flowModelRepository.updateById(param);
        }
    }

    public boolean copyModel(FlowModelDO data, boolean deployFlag) {
        // 将旧数据更新成历史版本
        FlowModelDO sourceParam = new FlowModelDO();
        sourceParam.setId(data.getId());
        sourceParam.setStatus(FlowModelStatusEnum.HISTORY);
        sourceParam.setVersion(data.getVersion());
        this.flowModelRepository.updateById(sourceParam);

        // 复制模型，版本号加1
        FlowModelDO param = BeanUtil.copyProperties(data, FlowModelDO.class);
        param.setId(null);
        param.setVersion(0);
        if (deployFlag) {
            param.setStatus(FlowModelStatusEnum.PUBLISHED);
            param.setModelVersion(data.getModelVersion() + 1);
        } else {
            param.setStatus(FlowModelStatusEnum.DISABLED);
        }
        this.flowModelRepository.save(param);

        // 复制模型扩展信息
        final FlowModelExtendDO modelExtend = this.flowModelExtendRepository.getOne(Wrappers.lambdaQuery(FlowModelExtendDO.class)
            .eq(FlowModelExtendDO::getModelId, data.getId()));
        if (Objects.isNull(modelExtend)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        final FlowModelExtendDO modelExtendParam = BeanUtil.copyProperties(modelExtend, FlowModelExtendDO.class);
        modelExtendParam.setId(null);
        modelExtendParam.setModelId(param.getId());
        modelExtendParam.setVersion(0);
        this.flowModelExtendRepository.save(modelExtendParam);

        return Boolean.TRUE;
    }

}

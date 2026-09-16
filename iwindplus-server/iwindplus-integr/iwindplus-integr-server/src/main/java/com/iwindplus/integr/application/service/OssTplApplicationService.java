/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.integr.application.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.integr.common.constant.IntegrConstant.RedisCacheConstant;
import com.iwindplus.integr.application.service.dto.OssTplEditDTO;
import com.iwindplus.integr.application.service.dto.OssTplSaveDTO;
import com.iwindplus.integr.infrastructure.persistence.OssTplDO;
import com.iwindplus.integr.infrastructure.persistence.OssTplRepository;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 对象存储模板业务层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Service
@Transactional(rollbackFor = Exception.class)
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_OSS_TPL})
@RequiredArgsConstructor
public class OssTplApplicationService {

    private final OssTplRepository ossTplRepository;

    /**
     * 添加.
     *
     * @param entity 对象
     * @return boolean
     */
    @CacheEvict(allEntries = true)
    public boolean save(OssTplSaveDTO entity) {
        this.ossTplRepository.getNameIsExist(entity.getName().trim(), entity.getOrgId());
        this.ossTplRepository.getBucketNameIsExist(entity.getBucketName().trim(), entity.getOrgId());

        entity.setStatus(EnableStatusEnum.ENABLE);
        entity.setBuildInFlag(Boolean.FALSE);
        String code = IdUtil.simpleUUID();
        entity.setCode(code);
        final OssTplDO model = BeanUtil.copyProperties(entity, OssTplDO.class);
        this.ossTplRepository.save(model);
        entity.setId(model.getId());
        return Boolean.TRUE;
    }

    /**
     * 批量删除.
     *
     * @param ids 主键集合
     * @return boolean
     */
    @CacheEvict(allEntries = true)
    public boolean removeByIds(List<Long> ids) {
        List<OssTplDO> list = this.ossTplRepository.listByIds(ids);
        if (Objects.isNull(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        boolean match = list.stream().filter(Objects::nonNull).anyMatch(OssTplDO::getBuildInFlag);
        if (Boolean.TRUE.equals(match)) {
            throw new BizException(BizCodeEnum.HAS_BUILD_IN_DATA);
        }
        this.ossTplRepository.removeByIds(ids);
        return Boolean.TRUE;
    }

    /**
     * 编辑.
     *
     * @param entity 对象
     * @return boolean
     */
    @CacheEvict(allEntries = true)
    public boolean edit(OssTplEditDTO entity) {
        // 编辑
        OssTplDO data = this.ossTplRepository.getById(entity.getId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (CharSequenceUtil.isNotBlank(entity.getName()) && !CharSequenceUtil.equals(data.getName(), entity.getName().trim())) {
            this.ossTplRepository.getNameIsExist(entity.getName().trim(), entity.getOrgId());
        }
        if (CharSequenceUtil.isNotBlank(entity.getCode()) && !CharSequenceUtil.equals(data.getCode(), entity.getCode().trim())) {
            this.ossTplRepository.getCodeIsExist(entity.getCode().trim(), entity.getOrgId());
        }
        if (CharSequenceUtil.isNotBlank(entity.getBucketName()) && !CharSequenceUtil.equals(data.getBucketName(), entity.getBucketName().trim())) {
            this.ossTplRepository.getBucketNameIsExist(entity.getBucketName().trim(), entity.getOrgId());
        }
        if (Objects.isNull(entity.getVersion())) {
            entity.setVersion(data.getVersion());
        }
        final OssTplDO model = BeanUtil.copyProperties(entity, OssTplDO.class);
        this.ossTplRepository.updateById(model);
        return Boolean.TRUE;
    }

    /**
     * 编辑状态.
     *
     * @param id     主键
     * @param status 状态
     * @return boolean
     */
    @CacheEvict(allEntries = true)
    public boolean editStatus(Long id, EnableStatusEnum status) {
        OssTplDO data = this.ossTplRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (status.equals(data.getStatus())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        OssTplDO entity = new OssTplDO();
        entity.setId(id);
        entity.setStatus(status);
        entity.setVersion(data.getVersion());
        this.ossTplRepository.updateById(entity);
        return Boolean.TRUE;
    }

    /**
     * 编辑设为内置.
     *
     * @param id          主键
     * @param buildInFlag 是否内置
     * @return boolean
     */
    @CacheEvict(allEntries = true)
    public boolean editBuildIn(Long id, Boolean buildInFlag) {
        OssTplDO data = this.ossTplRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (buildInFlag.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        OssTplDO param = new OssTplDO();
        param.setId(id);
        param.setBuildInFlag(buildInFlag);
        param.setVersion(data.getVersion());
        this.ossTplRepository.updateById(param);
        return Boolean.TRUE;
    }
}

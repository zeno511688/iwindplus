/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.application.service.upms.permission;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.redis.executor.RedissonExecutor;
import com.iwindplus.integr.client.OssClient;
import com.iwindplus.mgt.application.service.upms.organization.OrgApplicationService;
import com.iwindplus.mgt.application.service.upms.permission.dto.MenuEditDTO;
import com.iwindplus.mgt.application.service.upms.permission.dto.MenuSaveDTO;
import com.iwindplus.mgt.common.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.infrastructure.configuration.MgtProperty;
import com.iwindplus.mgt.infrastructure.persistence.upms.permission.MenuDO;
import com.iwindplus.mgt.infrastructure.persistence.upms.permission.MenuRepository;
import com.iwindplus.mgt.infrastructure.persistence.upms.permission.ResourceDO;
import com.iwindplus.mgt.infrastructure.persistence.upms.permission.ResourceRepository;
import com.iwindplus.mgt.infrastructure.persistence.upms.permission.RoleMenuRepository;
import com.iwindplus.mgt.infrastructure.persistence.upms.permission.RoleResourceRepository;
import com.iwindplus.mgt.common.enums.MgtCodeEnum;
import com.iwindplus.mgt.common.enums.MgtCodePrefixEnum;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 资源业务层接口实现类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Slf4j
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_MENU})
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class MenuApplicationService {

    private final MenuRepository menuRepository;
    private final RoleMenuRepository roleMenuRepository;
    private final RoleResourceRepository roleResourceRepository;
    private final ResourceRepository resourceRepository;
    private final OssClient ossClient;
    private final RedissonExecutor redissonExecutor;
    private final MgtProperty property;

    @CacheEvict(allEntries = true)
    public boolean save(MenuSaveDTO entity) {
        entity.setStatus(EnableStatusEnum.ENABLE);
        entity.setBuildInFlag(Boolean.FALSE);
        this.menuRepository.getNameIsExist(entity.getName().trim(), entity.getSystemId(), entity.getParentId());
        entity.setSeq(this.menuRepository.getNextSeq(entity.getSystemId(), entity.getParentId()));
        entity.setLevel(this.menuRepository.getLevel(entity.getSystemId(), entity.getParentId()));
        if (CharSequenceUtil.isBlank(entity.getCode())) {
            entity.setCode(this.redissonExecutor.serialNum().getSerialNumDate(MgtCodePrefixEnum.MENU_PREFIX.getValue()));
        } else {
            if (!entity.getCode().startsWith(MgtCodePrefixEnum.MENU_PREFIX.getValue())) {
                throw new BizException(MgtCodeEnum.MENU_PREFIX_ERROR);
            }
        }
        this.menuRepository.getCodeIsExist(entity.getCode());
        if (CharSequenceUtil.isNotBlank(entity.getRouteUrl())) {
            this.menuRepository.getRouteUrlIsExist(entity.getRouteUrl().trim(), entity.getSystemId(), entity.getParentId());
        }
        final MenuDO model = BeanUtil.copyProperties(entity, MenuDO.class);
        this.menuRepository.save(model);
        entity.setId(model.getId());
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    public boolean removeByIds(List<Long> ids) {
        List<MenuDO> list = this.menuRepository.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        boolean match = list.stream().filter(Objects::nonNull).anyMatch(MenuDO::getBuildInFlag);
        if (Boolean.TRUE.equals(match)) {
            throw new BizException(BizCodeEnum.HAS_BUILD_IN_DATA);
        }
        // 判断是否有子集
        boolean data = SqlHelper.retBool(this.menuRepository.count(Wrappers.lambdaQuery(MenuDO.class)
            .in(MenuDO::getParentId, ids)));
        if (data) {
            throw new BizException(MgtCodeEnum.CHILDREN_NOT_DELETED);
        }
        // 判断是否有资源
        final boolean hasResource = SqlHelper.retBool(this.resourceRepository.count(Wrappers.lambdaQuery(ResourceDO.class)
            .in(ResourceDO::getMenuId, ids)));
        if (Boolean.TRUE.equals(hasResource)) {
            throw new BizException(MgtCodeEnum.RESOURCE_NOT_DELETED);
        }
        this.roleMenuRepository.getBaseMapper().deleteByMenuIds(ids);
        this.roleResourceRepository.getBaseMapper().deleteByMenuIds(ids);
        this.menuRepository.removeByIds(ids);
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    public boolean edit(MenuEditDTO entity) {
        MenuDO data = this.menuRepository.getById(entity.getId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (CharSequenceUtil.isNotBlank(entity.getName()) && !CharSequenceUtil.equals(data.getName(), entity.getName().trim())) {
            this.menuRepository.getNameIsExist(entity.getName().trim(), data.getSystemId(), data.getParentId());
        }
        if (Objects.isNull(entity.getVersion())) {
            entity.setVersion(data.getVersion());
        }
        entity.setLevel(this.menuRepository.getLevel(data.getSystemId(), entity.getParentId()));
        if (CharSequenceUtil.isNotBlank(entity.getCode()) && !CharSequenceUtil.equals(data.getCode(), entity.getCode().trim())) {
            this.menuRepository.getCodeIsExist(entity.getCode());
        }
        if (CharSequenceUtil.isNotBlank(entity.getRouteUrl()) && !CharSequenceUtil.equals(data.getRouteUrl(), entity.getRouteUrl().trim())) {
            this.menuRepository.getRouteUrlIsExist(entity.getRouteUrl().trim(), data.getSystemId(), data.getParentId());
        }
        final MenuDO model = BeanUtil.copyProperties(entity, MenuDO.class);
        this.menuRepository.updateById(model);
        this.removeOldPic(entity, data);
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    public boolean editStatus(Long id, EnableStatusEnum status) {
        MenuDO data = this.menuRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (status.equals(data.getStatus())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        MenuDO param = new MenuDO();
        param.setId(id);
        param.setStatus(status);
        param.setVersion(data.getVersion());
        this.menuRepository.updateById(param);
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    public boolean editBuildIn(Long id, Boolean buildInFlag) {
        MenuDO data = this.menuRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (buildInFlag.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        MenuDO param = new MenuDO();
        param.setId(id);
        param.setBuildInFlag(buildInFlag);
        param.setVersion(data.getVersion());
        this.menuRepository.updateById(param);
        return Boolean.TRUE;
    }

    private void removeOldPic(MenuEditDTO entity, MenuDO data) {
        List<String> relativePaths = new ArrayList<>(10);
        if (CharSequenceUtil.isNotBlank(entity.getIconUrl())
            && CharSequenceUtil.isNotBlank(data.getIconUrl())
            && !CharSequenceUtil.equals(data.getIconUrl(), entity.getIconUrl().trim())) {
            relativePaths.add(data.getIconUrl());
        }
        if (CollUtil.isNotEmpty(relativePaths)) {
            OrgApplicationService.removeFiles(this.ossClient,
                this.property.getOss().getCode(),
                this.property.getOss().getTplCode(), relativePaths);
        }
    }
}

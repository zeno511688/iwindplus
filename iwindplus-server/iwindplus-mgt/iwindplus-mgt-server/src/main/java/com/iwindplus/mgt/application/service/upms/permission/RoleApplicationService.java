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
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.redis.executor.RedissonExecutor;
import com.iwindplus.mgt.application.query.upms.permission.vo.RoleVO;
import com.iwindplus.mgt.application.service.upms.permission.dto.RoleDTO;
import com.iwindplus.mgt.application.service.upms.permission.dto.RoleEditDTO;
import com.iwindplus.mgt.application.service.upms.permission.dto.RoleSaveDTO;
import com.iwindplus.mgt.common.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.infrastructure.persistence.upms.permission.RoleDO;
import com.iwindplus.mgt.infrastructure.persistence.upms.permission.RoleMenuRepository;
import com.iwindplus.mgt.infrastructure.persistence.upms.permission.RoleRepository;
import com.iwindplus.mgt.infrastructure.persistence.upms.permission.RoleResourceRepository;
import com.iwindplus.mgt.infrastructure.persistence.upms.user.UserRoleRepository;
import com.iwindplus.mgt.common.enums.MgtCodeEnum;
import com.iwindplus.mgt.common.enums.MgtCodePrefixEnum;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 角色业务层接口实现类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Slf4j
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_ROLE})
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class RoleApplicationService {

    private final RoleRepository roleRepository;
    private final RedissonExecutor redissonExecutor;
    private final RoleMenuRepository roleMenuRepository;
    private final RoleResourceRepository roleResourceRepository;
    private final UserRoleRepository userRoleRepository;

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE_MENU}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_MENU}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_POSITION}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
        }
    )
    public boolean save(RoleSaveDTO entity) {
        entity.setStatus(EnableStatusEnum.ENABLE);
        entity.setBuildInFlag(Boolean.FALSE);
        this.roleRepository.getNameIsExist(entity.getName().trim(), entity.getOrgId());
        if (CharSequenceUtil.isBlank(entity.getCode())) {
            entity.setCode(this.redissonExecutor.serialNum().getSerialNumDate(MgtCodePrefixEnum.ROLE_PREFIX.getValue()));
        } else {
            if (!entity.getCode().startsWith(MgtCodePrefixEnum.ROLE_PREFIX.getValue())) {
                throw new BizException(MgtCodeEnum.ROLE_PREFIX_ERROR);
            }
        }
        this.roleRepository.getCodeIsExist(entity.getCode().trim(), entity.getOrgId());
        entity.setSeq(this.roleRepository.getNextSeq(entity.getOrgId()));
        if (Boolean.TRUE.equals(entity.getDefaultFlag())) {
            final List<RoleDO> roleList = this.roleRepository.editCancelDefault(null, entity.getOrgId());
            if (CollUtil.isNotEmpty(roleList)) {
                this.roleRepository.updateBatchById(roleList, Constants.DEFAULT_BATCH_SIZE);
            }
        }
        final RoleDO model = BeanUtil.copyProperties(entity, RoleDO.class);
        boolean data = this.roleRepository.save(model);
        entity.setId(model.getId());
        Long roleId = entity.getId();
        // 角色菜单关系表维护
        this.roleMenuRepository.editBatchMenu(roleId, entity.getMenuIds());
        this.roleResourceRepository.editBatchResource(roleId, entity.getResourceIds());
        return data;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE_MENU}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_MENU}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_POSITION}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
        }
    )
    public List<RoleVO> saveBatchInit(List<RoleDTO> entities) {
        return this.roleRepository.saveBatchInit(entities);
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE_MENU}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_MENU}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_POSITION}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
        }
    )
    public boolean removeByIds(List<Long> ids) {
        List<RoleDO> list = this.roleRepository.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        boolean match = list.stream().anyMatch(RoleDO::getBuildInFlag);
        if (Boolean.TRUE.equals(match)) {
            throw new BizException(BizCodeEnum.HAS_BUILD_IN_DATA);
        }
        this.userRoleRepository.getBaseMapper().deleteByRoleIds(ids);
        this.roleMenuRepository.getBaseMapper().deleteByRoleIds(ids);
        this.roleRepository.removeByIds(ids);
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE_MENU}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_MENU}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_POSITION}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
        }
    )
    public boolean edit(RoleEditDTO entity) {
        // 编辑
        RoleDO data = this.roleRepository.getById(entity.getId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        // 校验名称是否存在
        if (CharSequenceUtil.isNotBlank(entity.getName()) && !CharSequenceUtil.equals(data.getName(), entity.getName().trim())) {
            this.roleRepository.getNameIsExist(entity.getName().trim(), data.getOrgId());
        }
        if (CharSequenceUtil.isNotBlank(entity.getCode()) && !CharSequenceUtil.equals(data.getCode(), entity.getCode().trim())) {
            this.roleRepository.getCodeIsExist(entity.getCode().trim(), data.getOrgId());
        }
        List<RoleDO> paramList = new ArrayList<>(10);
        if (Boolean.TRUE.equals(entity.getDefaultFlag())) {
            final List<RoleDO> roleList = this.roleRepository.editCancelDefault(data.getId(), entity.getOrgId());
            if (CollUtil.isNotEmpty(roleList)) {
                paramList.addAll(roleList);
            }
        }
        if (Objects.isNull(entity.getVersion())) {
            entity.setVersion(data.getVersion());
        }
        final RoleDO model = BeanUtil.copyProperties(entity, RoleDO.class);
        paramList.add(model);
        this.roleRepository.updateBatchById(paramList, Constants.DEFAULT_BATCH_SIZE);

        Long roleId = data.getId();
        // 角色资源关系表维护
        this.roleMenuRepository.editBatchMenu(roleId, entity.getMenuIds());
        this.roleResourceRepository.editBatchResource(roleId, entity.getResourceIds());
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE_MENU}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_MENU}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_POSITION}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
        }
    )
    public boolean editStatus(Long id, EnableStatusEnum status) {
        RoleDO data = this.roleRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (status.equals(data.getStatus())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        RoleDO param = new RoleDO();
        param.setId(id);
        param.setStatus(status);
        param.setVersion(data.getVersion());
        this.roleRepository.updateById(param);
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE_MENU}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_MENU}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_POSITION}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
        }
    )
    public boolean editDefault(Long id, Long orgId) {
        RoleDO data = this.roleRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }

        List<RoleDO> paramList = new ArrayList<>(10);
        final List<RoleDO> roleList = this.roleRepository.editCancelDefault(id, orgId);
        if (CollUtil.isNotEmpty(roleList)) {
            paramList.addAll(roleList);
        }
        RoleDO param = new RoleDO();
        param.setId(id);
        param.setDefaultFlag(true);
        param.setVersion(data.getVersion());
        paramList.add(param);
        this.roleRepository.updateBatchById(paramList, Constants.DEFAULT_BATCH_SIZE);
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE_MENU}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_MENU}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_POSITION}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
        }
    )
    public boolean editBuildIn(Long id, Boolean buildInFlag) {
        RoleDO data = this.roleRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (buildInFlag.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        RoleDO param = new RoleDO();
        param.setId(id);
        param.setBuildInFlag(buildInFlag);
        param.setVersion(data.getVersion());
        this.roleRepository.updateById(param);
        return Boolean.TRUE;
    }

}

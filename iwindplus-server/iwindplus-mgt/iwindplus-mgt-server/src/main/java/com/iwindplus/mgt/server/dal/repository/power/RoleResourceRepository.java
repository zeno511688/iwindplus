/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.dal.repository.power;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.github.yulichang.repository.JoinCrudRepository;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.mgt.domain.dto.power.RoleResourceDTO;
import com.iwindplus.mgt.server.dal.mapper.power.RoleResourceMapper;
import com.iwindplus.mgt.server.dal.model.power.ResourceDO;
import com.iwindplus.mgt.server.dal.model.power.RoleResourceDO;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 角色资源关系聚合层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Repository
@RequiredArgsConstructor
public class RoleResourceRepository extends JoinCrudRepository<RoleResourceMapper, RoleResourceDO> {

    private final ResourceRepository resourceRepository;

    /**
     * 保存.
     *
     * @param entity 对象
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean save(RoleResourceDTO entity) {
        List<RoleResourceDO> entities = new ArrayList<>(10);
        final RoleResourceDO model = BeanUtil.copyProperties(entity, RoleResourceDO.class);
        this.buildParam(model, entities);
        if (CollUtil.isNotEmpty(entities)) {
            super.saveBatch(entities, DEFAULT_BATCH_SIZE);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    /**
     * 保存.
     *
     * @param roleId      角色主键
     * @param resourceIds 资源主键集合
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean saveBatchResource(Long roleId, Set<Long> resourceIds) {
        if (Objects.isNull(roleId) || CollUtil.isEmpty(resourceIds)) {
            return Boolean.FALSE;
        }
        final List<ResourceDO> list = this.resourceRepository.list(Wrappers.<ResourceDO>lambdaQuery()
            .in(ResourceDO::getId, resourceIds)
            .eq(ResourceDO::getStatus, EnableStatusEnum.ENABLE)
            .select(ResourceDO::getId, ResourceDO::getMenuId));
        if (CollUtil.isEmpty(list)) {
            return Boolean.FALSE;
        }
        Map<Long, Long> dataMap = list.stream()
            .collect(Collectors.toMap(ResourceDO::getId, ResourceDO::getMenuId));
        if (MapUtil.isEmpty(dataMap)) {
            return Boolean.FALSE;
        }
        List<RoleResourceDO> entities = new ArrayList<>(10);
        resourceIds.stream().forEach(resourceId -> {
            RoleResourceDO entity = RoleResourceDO.builder()
                .roleId(roleId)
                .menuId(dataMap.get(resourceId))
                .resourceId(resourceId)
                .build();
            this.buildParam(entity, entities);
        });
        if (CollUtil.isNotEmpty(entities)) {
            super.saveBatch(entities, DEFAULT_BATCH_SIZE);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    /**
     * 保存.
     *
     * @param resourceId 资源主键
     * @param roleIds    角色主键集合
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean saveBatchRole(Long resourceId, Set<Long> roleIds) {
        if (Objects.isNull(resourceId) || CollUtil.isEmpty(roleIds)) {
            return Boolean.FALSE;
        }
        final ResourceDO data = this.resourceRepository.getOne(Wrappers.<ResourceDO>lambdaQuery()
            .eq(ResourceDO::getId, resourceId).select(ResourceDO::getMenuId));
        if (Objects.isNull(data)) {
            return Boolean.FALSE;
        }
        List<RoleResourceDO> entities = new ArrayList<>(10);
        roleIds.stream().forEach(roleId -> {
            RoleResourceDO entity = RoleResourceDO.builder()
                .roleId(roleId)
                .menuId(data.getMenuId())
                .resourceId(resourceId)
                .build();
            this.buildParam(entity, entities);
        });
        if (CollUtil.isNotEmpty(entities)) {
            super.saveBatch(entities, DEFAULT_BATCH_SIZE);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    /**
     * 保存.
     *
     * @param entities 对象集合
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean saveBatch(List<RoleResourceDTO> entities) {
        if (CollUtil.isEmpty(entities)) {
            return Boolean.FALSE;
        }
        List<RoleResourceDO> params = new ArrayList<>(10);
        entities.stream().forEach(entity -> {
            RoleResourceDO model = BeanUtil.copyProperties(entity, RoleResourceDO.class);
            this.buildParam(model, params);
        });
        if (CollUtil.isNotEmpty(params)) {
            super.saveBatch(params, DEFAULT_BATCH_SIZE);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    /**
     * 编辑.
     *
     * @param roleId      角色主键
     * @param resourceIds 资源主键集合
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean editBatchResource(Long roleId, Set<Long> resourceIds) {
        if (Objects.isNull(roleId)) {
            return Boolean.FALSE;
        }
        super.baseMapper.deleteByRoleIds(Arrays.asList(roleId));
        this.saveBatchResource(roleId, resourceIds);
        return Boolean.TRUE;
    }

    /**
     * 编辑.
     *
     * @param resourceId 资源主键
     * @param roleIds    角色主键集合
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean editBatchRole(Long resourceId, Set<Long> roleIds) {
        if (Objects.isNull(resourceId)) {
            return Boolean.FALSE;
        }
        super.baseMapper.deleteByResourceIds(Arrays.asList(resourceId));
        this.saveBatchRole(resourceId, roleIds);
        return Boolean.TRUE;
    }

    private void buildParam(RoleResourceDO entity, List<RoleResourceDO> entities) {
        long count = super.count(Wrappers.lambdaQuery(RoleResourceDO.class)
            .eq(RoleResourceDO::getRoleId, entity.getRoleId())
            .eq(RoleResourceDO::getMenuId, entity.getMenuId())
            .eq(RoleResourceDO::getResourceId, entity.getResourceId()));
        if (!SqlHelper.retBool(count)) {
            entities.add(entity);
        }
    }

}

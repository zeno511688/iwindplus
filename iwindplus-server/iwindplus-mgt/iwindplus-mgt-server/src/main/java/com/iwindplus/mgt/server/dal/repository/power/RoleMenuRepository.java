/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.dal.repository.power;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.github.yulichang.repository.JoinCrudRepository;
import com.iwindplus.mgt.domain.dto.power.RoleMenuDTO;
import com.iwindplus.mgt.server.dal.mapper.power.RoleMenuMapper;
import com.iwindplus.mgt.server.dal.model.power.RoleMenuDO;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 角色菜单关系聚合层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Repository
public class RoleMenuRepository extends JoinCrudRepository<RoleMenuMapper, RoleMenuDO> {

    /**
     * 保存.
     *
     * @param entity 对象
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean save(RoleMenuDTO entity) {
        List<RoleMenuDO> entities = new ArrayList<>(10);
        final RoleMenuDO model = BeanUtil.copyProperties(entity, RoleMenuDO.class);
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
     * @param roleId  角色主键
     * @param menuIds 菜单主键集合
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean saveBatchMenu(Long roleId, Set<Long> menuIds) {
        if (Objects.isNull(roleId) || CollUtil.isEmpty(menuIds)) {
            return Boolean.FALSE;
        }
        List<RoleMenuDO> entities = new ArrayList<>(10);
        menuIds.stream().forEach(menuId -> {
            RoleMenuDO entity = RoleMenuDO.builder()
                .roleId(roleId)
                .menuId(menuId)
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
     * @param menuId  菜单主键
     * @param roleIds 角色主键集合
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean saveBatchRole(Long menuId, Set<Long> roleIds) {
        if (Objects.isNull(menuId) || CollUtil.isEmpty(roleIds)) {
            return Boolean.FALSE;
        }
        List<RoleMenuDO> entities = new ArrayList<>(10);
        roleIds.stream().forEach(roleId -> {
            RoleMenuDO entity = RoleMenuDO.builder()
                .roleId(roleId)
                .menuId(menuId)
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
    public boolean saveBatch(List<RoleMenuDTO> entities) {
        if (CollUtil.isEmpty(entities)) {
            return Boolean.FALSE;
        }

        List<RoleMenuDO> params = new ArrayList<>(10);
        entities.stream().forEach(entity -> {
            RoleMenuDO model = BeanUtil.copyProperties(entity, RoleMenuDO.class);
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
     * @param roleId  角色主键
     * @param menuIds 菜单主键集合
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean editBatchMenu(Long roleId, Set<Long> menuIds) {
        if (Objects.isNull(roleId)) {
            return Boolean.FALSE;
        }

        super.baseMapper.deleteByRoleIds(Arrays.asList(roleId));
        this.saveBatchMenu(roleId, menuIds);
        return Boolean.TRUE;
    }

    /**
     * 编辑.
     *
     * @param menuId  菜单主键
     * @param roleIds 角色主键集合
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean editBatchRole(Long menuId, Set<Long> roleIds) {
        if (Objects.isNull(menuId)) {
            return Boolean.FALSE;
        }

        super.baseMapper.deleteByMenuIds(Arrays.asList(menuId));
        this.saveBatchRole(menuId, roleIds);
        return Boolean.TRUE;
    }

    private void buildParam(RoleMenuDO entity, List<RoleMenuDO> entities) {
        long count = super.count(Wrappers.lambdaQuery(RoleMenuDO.class)
            .eq(RoleMenuDO::getRoleId, entity.getRoleId())
            .eq(RoleMenuDO::getMenuId, entity.getMenuId()));
        if (!SqlHelper.retBool(count)) {
            entities.add(entity);
        }
    }
}

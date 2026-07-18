/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Maps;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.mgt.domain.constant.MgtConstant;
import com.iwindplus.mgt.domain.dto.InitDataDTO;
import com.iwindplus.mgt.domain.dto.power.RoleMenuDTO;
import com.iwindplus.mgt.domain.dto.power.RoleResourceDTO;
import com.iwindplus.mgt.domain.dto.power.UserGroupRoleDTO;
import com.iwindplus.mgt.domain.dto.power.UserGroupUserDTO;
import com.iwindplus.mgt.domain.dto.power.UserOrgDTO;
import com.iwindplus.mgt.domain.enums.MgtCodeEnum;
import com.iwindplus.mgt.server.dal.model.power.DepartmentDO;
import com.iwindplus.mgt.server.dal.model.power.PositionDO;
import com.iwindplus.mgt.server.dal.model.power.RoleDO;
import com.iwindplus.mgt.server.dal.model.power.RoleMenuDO;
import com.iwindplus.mgt.server.dal.model.power.RoleResourceDO;
import com.iwindplus.mgt.server.dal.model.power.UserGroupRoleDO;
import com.iwindplus.mgt.server.dal.model.power.UserGroupUserDO;
import com.iwindplus.mgt.server.dal.model.power.UserOrgDO;
import com.iwindplus.mgt.server.dal.model.power.UserPositionDO;
import com.iwindplus.mgt.server.dal.repository.power.DepartmentRepository;
import com.iwindplus.mgt.server.dal.repository.power.OrgRepository;
import com.iwindplus.mgt.server.dal.repository.power.PositionRepository;
import com.iwindplus.mgt.server.dal.repository.power.RoleMenuRepository;
import com.iwindplus.mgt.server.dal.repository.power.RoleRepository;
import com.iwindplus.mgt.server.dal.repository.power.RoleResourceRepository;
import com.iwindplus.mgt.server.dal.repository.power.UserGroupRepository;
import com.iwindplus.mgt.server.dal.repository.power.UserGroupRoleRepository;
import com.iwindplus.mgt.server.dal.repository.power.UserGroupUserRepository;
import com.iwindplus.mgt.server.dal.repository.power.UserOrgRepository;
import com.iwindplus.mgt.server.dal.repository.power.UserPositionRepository;
import com.iwindplus.mgt.server.dal.repository.power.UserRepository;
import com.iwindplus.mgt.server.service.InitService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 初始化业务层接口实现类.
 *
 * @author zengdegui
 * @since 2024/08/16 22:42
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class InitServiceImpl implements InitService {

    private final RoleRepository roleRepository;
    private final RoleMenuRepository roleMenuRepository;
    private final RoleResourceRepository roleResourceRepository;
    private final UserGroupRoleRepository userGroupRoleRepository;
    private final UserGroupRepository userGroupRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final UserOrgRepository userOrgRepository;
    private final UserPositionRepository userPositionRepository;
    private final UserGroupUserRepository userGroupUserRepository;
    private final OrgRepository orgRepository;
    private final UserRepository userRepository;

    @Override
    public Boolean initData(InitDataDTO entity) {
        Long userId = entity.getUserId();
        Set<Long> roleIds = entity.getRoleIds();
        Long orgId = entity.getOrgId();
        this.checkParam(entity);
        // 初始化角色菜单资源至新组织.
        Map<Long, Long> roleIdMap = this.initRoleMenuResourceToOrg(orgId, roleIds);
        // 初始化用户组织关系
        this.initUserToOrg(roleIdMap, orgId, userId);
        // 初始化用户职位关系
        this.initUserToPosition(orgId, userId);
        // 初始化用户组至新组织.
        Map<Long, Long> userGroupIdMap = this.initUserGroupToOrg(orgId, roleIdMap);
        // 初始化用户组用户关系
        this.initUserToUserGroup(userId, userGroupIdMap);
        return Boolean.TRUE;
    }

    private void checkParam(InitDataDTO entity) {
        Long orgId = entity.getOrgId();
        Long userId = entity.getUserId();
        Set<Long> roleIds = entity.getRoleIds();
        if (CollUtil.isEmpty(this.roleRepository.list(Wrappers.lambdaQuery(RoleDO.class).in(RoleDO::getId, roleIds)))) {
            throw new BizException(MgtCodeEnum.ROLE_NOT_EXIST);
        }
        if (Objects.nonNull(userId) && Objects.isNull(this.userRepository.getById(userId))) {
            throw new BizException(MgtCodeEnum.USER_NOT_EXIST);
        }

        if (Objects.nonNull(orgId) && Objects.isNull(this.orgRepository.getById(orgId))) {
            throw new BizException(MgtCodeEnum.ORG_NOT_EXIST);
        }
    }

    private Map<Long, Long> initRoleMenuResourceToOrg(Long orgId, Set<Long> roleIds) {
        if (Objects.isNull(orgId)) {
            return Collections.emptyMap();
        }
        Set<Long> roleIdSet = Set.copyOf(roleIds);
        List<RoleMenuDO> roleMenuList = this.roleMenuRepository.list(Wrappers.lambdaQuery(RoleMenuDO.class)
            .in(RoleMenuDO::getRoleId, roleIds));
        if (CollUtil.isEmpty(roleMenuList)) {
            return Collections.emptyMap();
        }
        // 初始化角色
        Map<Long, Long> roleIdMap = this.roleRepository.editInitToClient(orgId, roleIdSet);
        // 初始化角色菜单关系
        roleMenuList.forEach(roleMenu -> {
            roleMenu.setId(null);
            roleMenu.setVersion(0);
            roleMenu.setRoleId(roleIdMap.get(roleMenu.getRoleId()));
            roleMenu.setRemark(MgtConstant.REMARK_INIT);
        });
        List<RoleMenuDTO> roleMenuDtoList = BeanUtil.copyToList(roleMenuList, RoleMenuDTO.class);
        this.roleMenuRepository.saveBatch(roleMenuDtoList);

        List<RoleResourceDO> roleResourceList = this.roleResourceRepository.list(Wrappers.lambdaQuery(RoleResourceDO.class)
            .in(RoleResourceDO::getRoleId, roleIds));
        if (CollUtil.isEmpty(roleResourceList)) {
            return Collections.emptyMap();
        }
        // 初始化角色资源关系
        roleResourceList.forEach(roleResource -> {
            roleResource.setId(null);
            roleResource.setVersion(0);
            roleResource.setRoleId(roleIdMap.get(roleResource.getRoleId()));
            roleResource.setRemark(MgtConstant.REMARK_INIT);
        });
        List<RoleResourceDTO> roleResourceDtoList = BeanUtil.copyToList(roleMenuList, RoleResourceDTO.class);
        this.roleResourceRepository.saveBatch(roleResourceDtoList);
        return roleIdMap;
    }

    private Map<Long, Long> initUserGroupToOrg(Long orgId, Map<Long, Long> roleIdMap) {
        if (Objects.isNull(orgId)) {
            return Collections.emptyMap();
        }
        Set<Long> roleIds = roleIdMap.keySet();
        List<UserGroupRoleDO> userGroupRoleList = this.userGroupRoleRepository.list(Wrappers.lambdaQuery(UserGroupRoleDO.class)
            .in(UserGroupRoleDO::getRoleId, roleIds));
        if (CollUtil.isEmpty(userGroupRoleList)) {
            return Collections.emptyMap();
        }
        Set<Long> userGroupIdSet = userGroupRoleList.stream().map(UserGroupRoleDO::getUserGroupId).collect(Collectors.toSet());
        // 初始化用户组
        Map<Long, Long> userGroupIdMap = this.userGroupRepository.editInitToOrg(orgId, userGroupIdSet);
        // 初始化用户组角色关系
        userGroupRoleList.forEach(userGroupRole -> {
            userGroupRole.setId(null);
            userGroupRole.setVersion(0);
            userGroupRole.setRoleId(roleIdMap.get(userGroupRole.getRoleId()));
            userGroupRole.setRemark(MgtConstant.REMARK_INIT);
        });
        List<UserGroupRoleDTO> userGroupRoleDtoList = BeanUtil.copyToList(userGroupRoleList, UserGroupRoleDTO.class);
        this.userGroupRoleRepository.saveBatch(userGroupRoleDtoList);
        return userGroupIdMap;
    }

    private void initUserToOrg(Map<Long, Long> roleIdMap, Long orgId, Long userId) {
        if (Objects.isNull(userId)) {
            return;
        }
        List<Long> roleIds = new ArrayList<>(roleIdMap.keySet());
        Long roleId = roleIds.get(0);
        final RoleDO role = this.roleRepository.getById(roleId);
        if (Objects.isNull(role)) {
            return;
        }
        List<UserOrgDO> userOrgList = this.userOrgRepository.list(Wrappers.lambdaQuery(UserOrgDO.class)
            .eq(UserOrgDO::getUserId, userId)
            .eq(UserOrgDO::getChecked, Boolean.TRUE));
        if (CollUtil.isEmpty(userOrgList)) {
            return;
        }
        userOrgList.forEach(userOrg -> {
            userOrg.setId(null);
            userOrg.setVersion(0);
            userOrg.setOrgId(orgId);
            userOrg.setRemark(MgtConstant.REMARK_INIT);
        });
        List<UserOrgDTO> userOrgDtoList = BeanUtil.copyToList(userOrgList, UserOrgDTO.class);
        this.userOrgRepository.saveBatch(userOrgDtoList);
    }

    private void initUserToUserGroup(Long userId, Map<Long, Long> userGroupIdMap) {
        if (Objects.isNull(userId)) {
            return;
        }
        if (MapUtil.isEmpty(userGroupIdMap)) {
            return;
        }
        final Set<Long> groupIds = userGroupIdMap.keySet();
        List<UserGroupUserDO> userGroupUserList = this.userGroupUserRepository.list(Wrappers.lambdaQuery(UserGroupUserDO.class)
            .in(UserGroupUserDO::getUserGroupId, groupIds)
            .eq(UserGroupUserDO::getUserId, userId));
        if (CollUtil.isEmpty(userGroupUserList)) {
            return;
        }
        userGroupUserList.forEach(userGroupUser -> {
            userGroupUser.setId(null);
            userGroupUser.setVersion(0);
            userGroupUser.setUserGroupId(userGroupIdMap.get(userGroupUser.getUserGroupId()));
            userGroupUser.setRemark(MgtConstant.REMARK_INIT);
        });
        // 初始化用户组用户关系
        List<UserGroupUserDTO> userGroupUserDtoList = BeanUtil.copyToList(userGroupUserList, UserGroupUserDTO.class);
        this.userGroupUserRepository.saveBatch(userGroupUserDtoList);
    }

    private void initUserToPosition(Long orgId, Long userId) {
        if (Objects.isNull(userId)) {
            return;
        }
        List<UserPositionDO> userPositionList = this.userPositionRepository.list(Wrappers.lambdaQuery(UserPositionDO.class)
            .eq(UserPositionDO::getUserId, userId));
        if (CollUtil.isEmpty(userPositionList)) {
            return;
        }
        Set<Long> positionIdSet = userPositionList.stream().map(UserPositionDO::getPositionId).collect(Collectors.toSet());
        final List<PositionDO> positionList = this.positionRepository.list(Wrappers.lambdaQuery(PositionDO.class)
            .in(PositionDO::getId, positionIdSet));
        if (CollUtil.isEmpty(positionList)) {
            return;
        }
        Set<Long> departmentIdSet = positionList.stream().map(PositionDO::getDepartmentId).collect(Collectors.toSet());
        final List<DepartmentDO> departmentList = this.departmentRepository.list(Wrappers.lambdaQuery(DepartmentDO.class)
            .in(DepartmentDO::getId, departmentIdSet));
        if (CollUtil.isEmpty(departmentList)) {
            return;
        }
        final Map<Long, Long> departmentMap = this.departmentRepository.editInitToOrg(orgId, departmentIdSet);
        Map<Long, Long> idMap = this.editInitPositionToOrg(orgId, positionList, departmentMap);
        this.editInitUserPositionToOrg(userPositionList, idMap);
    }

    private Map<Long, Long> editInitPositionToOrg(Long orgId, List<PositionDO> positionList, Map<Long, Long> departmentMap) {
        Map<Long, Long> idMap = Maps.newHashMap();
        List<PositionDO> positionTmpList = new ArrayList<>(10);
        positionList.stream().forEach(positionDO -> {
            final Long departmentId = departmentMap.get(positionDO.getDepartmentId());
            if (Objects.nonNull(departmentId)) {
                final PositionDO positionTmp = BeanUtil.copyProperties(positionDO, PositionDO.class);
                positionTmp.setId(null);
                positionTmp.setOrgId(orgId);
                positionTmp.setDepartmentId(departmentId);
                positionTmp.setVersion(0);
                positionTmp.setRemark(MgtConstant.REMARK_INIT);
                positionTmpList.add(positionTmp);
            }
        });
        if (CollUtil.isNotEmpty(positionTmpList)) {
            this.positionRepository.saveBatch(positionTmpList, Constants.DEFAULT_BATCH_SIZE);
            positionList.forEach(oldData -> positionTmpList.forEach(newData -> {
                if (oldData.getName().equals(newData.getName())) {
                    idMap.put(oldData.getId(), newData.getId());
                }
            }));
        }
        return idMap;
    }

    private void editInitUserPositionToOrg(List<UserPositionDO> userPositionList, Map<Long, Long> idMap) {
        List<UserPositionDO> userPositionTmpList = new ArrayList<>(10);
        userPositionList.stream().forEach(userPosition -> {
            final Long positionId = idMap.get(userPosition.getPositionId());
            if (Objects.nonNull(positionId)) {
                final UserPositionDO userPositionTmp = BeanUtil.copyProperties(userPosition, UserPositionDO.class);
                userPositionTmp.setId(null);
                userPositionTmp.setVersion(0);
                userPositionTmp.setPositionId(positionId);
                userPositionTmp.setRemark(MgtConstant.REMARK_INIT);
            }
        });
        if (CollUtil.isNotEmpty(userPositionTmpList)) {
            this.userPositionRepository.saveBatch(userPositionTmpList, Constants.DEFAULT_BATCH_SIZE);
        }
    }
}

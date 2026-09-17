/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.application.query.upms.user;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.mgt.application.query.upms.user.dto.UserGroupSearchDTO;
import com.iwindplus.mgt.application.query.upms.user.vo.UserGroupBaseCheckedVO;
import com.iwindplus.mgt.application.query.upms.user.vo.UserGroupExtendVO;
import com.iwindplus.mgt.application.query.upms.user.vo.UserGroupPageVO;
import com.iwindplus.mgt.common.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.infrastructure.persistence.upms.user.UserGroupDO;
import com.iwindplus.mgt.infrastructure.persistence.upms.user.UserGroupRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 组查询业务层.
 *
 * @author zengdegui
 * @since 2019/10/9
 */
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP})
@RequiredArgsConstructor
public class UserGroupQueryService {

    private final UserGroupRepository userGroupRepository;

    /**
     * 分页查询.
     *
     * @param entity 查询条件
     * @return 分页结果
     */
    public IPage<UserGroupPageVO> page(UserGroupSearchDTO entity) {
        return this.userGroupRepository.page(entity);
    }

    /**
     * 根据id查询详情(扩展).
     *
     * @param id id
     * @return 详情
     */
    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    public UserGroupExtendVO getDetailExtend(Long id) {
        return this.userGroupRepository.getBaseMapper().selectDetailById(id);
    }

    /**
     * 根据组织id和用户id查询列表(扩展).
     *
     * @param orgId  组织id
     * @param userId 用户id
     * @return 列表
     */
    @Cacheable(key = "#root.methodName + '_' + #p0 + '_' + #p1", condition = "#p1 != null", unless = "#result == null")
    public List<UserGroupBaseCheckedVO> listByUserId(Long orgId, Long userId) {
        List<UserGroupBaseCheckedVO> allList = this.userGroupRepository.getBaseMapper().selectListByOrgId(orgId);
        if (CollUtil.isEmpty(allList)) {
            return null;
        }
        if (Objects.nonNull(userId)) {
            List<UserGroupBaseCheckedVO> checkedList = this.userGroupRepository.getBaseMapper().selectListByUserId(orgId, userId);
            if (CollUtil.isNotEmpty(checkedList)) {
                allList = this.listWithChecked(allList, checkedList);
            }
        }
        return allList;
    }

    /**
     * 根据组织id和角色id查询列表(扩展).
     *
     * @param orgId  组织id
     * @param roleId 角色id
     * @return 列表
     */
    @Cacheable(key = "#root.methodName + '_' + #p0 + '_' + #p1", condition = "#p0 != null", unless = "#result == null")
    public List<UserGroupBaseCheckedVO> listByRoleId(Long orgId, Long roleId) {
        List<UserGroupBaseCheckedVO> allList = this.userGroupRepository.getBaseMapper().selectListByOrgId(orgId);
        if (CollUtil.isEmpty(allList)) {
            return null;
        }
        if (Objects.nonNull(roleId)) {
            List<UserGroupBaseCheckedVO> checkedList = this.userGroupRepository.getBaseMapper().selectListByRoleId(orgId, roleId);
            if (CollUtil.isNotEmpty(checkedList)) {
                allList = this.listWithChecked(allList, checkedList);
            }
        }
        return allList;
    }

    /**
     * 根据组织id和用户id查询列表(扩展).
     *
     * @param allList     所有列表
     * @param checkedList 已选列表
     * @return 列表
     */
    private List<UserGroupBaseCheckedVO> listWithChecked(List<UserGroupBaseCheckedVO> allList, List<UserGroupBaseCheckedVO> checkedList) {
        return allList.stream().filter(Objects::nonNull).peek(map -> checkedList.stream()
                .filter(m -> Objects.equals(m.getId(), map.getId())).forEach(m -> map.setChecked(m.getChecked())))
            .collect(Collectors.toCollection(ArrayList::new));
    }
}

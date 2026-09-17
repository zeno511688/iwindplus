/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.application.query.upms.permission;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.mgt.api.upms.vo.RoleBaseVO;
import com.iwindplus.mgt.application.query.upms.permission.vo.RoleBaseCheckedVO;
import com.iwindplus.mgt.application.query.upms.permission.vo.RoleExtendVO;
import com.iwindplus.mgt.application.query.upms.permission.vo.RolePageVO;
import com.iwindplus.mgt.application.service.upms.permission.dto.RoleSearchDTO;
import com.iwindplus.mgt.common.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.infrastructure.persistence.upms.permission.RoleRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 角色查询业务层.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Slf4j
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_ROLE})
@RequiredArgsConstructor
public class RoleQueryService {

    private final RoleRepository roleRepository;

    /**
     * 分页查询.
     *
     * @param entity 查询参数
     * @return 分页查询结果
     */
    public IPage<RolePageVO> page(RoleSearchDTO entity) {
        return this.roleRepository.page(entity);
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    public RoleExtendVO getDetailExtend(Long id) {
        return this.roleRepository.getBaseMapper().selectDetailById(id);
    }

    @Cacheable(key = "#root.methodName + '_' + #p0 + '_' + #p1", condition = "#p0 != null", unless = "#result == null")
    public List<RoleBaseCheckedVO> listByUserId(Long orgId, Long userId) {
        List<RoleBaseCheckedVO> allList = this.roleRepository.getBaseMapper().selectListByOrgId(orgId);
        if (CollUtil.isEmpty(allList)) {
            return null;
        }
        if (Objects.nonNull(userId)) {
            List<RoleBaseCheckedVO> checkedList = this.roleRepository.getBaseMapper().selectListByUserId(orgId, userId);
            if (CollUtil.isNotEmpty(checkedList)) {
                allList = this.listWithChecked(allList, checkedList);
            }
        }
        return allList;
    }

    @Cacheable(key = "#root.methodName + '_' + #p0 + '_' + #p1", condition = "#p0 != null && #p1 != null", unless = "#result == null")
    public List<RoleBaseVO> listCheckedByUserId(Long orgId, Long userId) {
        return this.roleRepository.listCheckedByUserId(orgId, userId);
    }

    @Cacheable(key = "#root.methodName + '_' + #p0 + '_' + #p1", condition = "#p0 != null", unless = "#result == null")
    public List<RoleBaseCheckedVO> listByUserGroupId(Long orgId, Long userGroupId) {
        List<RoleBaseCheckedVO> allList = this.roleRepository.getBaseMapper().selectListByOrgId(orgId);
        if (CollUtil.isEmpty(allList)) {
            return null;
        }
        if (Objects.nonNull(userGroupId)) {
            List<RoleBaseCheckedVO> checkedList = this.roleRepository.getBaseMapper().selectListByUserGroupId(orgId, userGroupId);
            if (CollUtil.isNotEmpty(checkedList)) {
                allList = this.listWithChecked(allList, checkedList);
            }
        }
        return allList;
    }

    private List<RoleBaseCheckedVO> listWithChecked(List<RoleBaseCheckedVO> allList, List<RoleBaseCheckedVO> checkedList) {
        return allList.stream().peek(map -> checkedList.stream()
                .filter(m -> Objects.equals(m.getId(), map.getId())).forEach(m -> map.setChecked(m.getChecked())))
            .collect(Collectors.toCollection(ArrayList::new));
    }

}

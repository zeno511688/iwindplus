/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.application.query.upms.organization;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.vo.BaseTreeCheckedVO;
import com.iwindplus.mgt.api.upms.vo.DepartmentBaseVO;
import com.iwindplus.mgt.application.query.upms.organization.vo.DepartmentExtendVO;
import com.iwindplus.mgt.common.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.infrastructure.persistence.upms.organization.DepartmentRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 部门查询业务层.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_DEPARTMENT})
@RequiredArgsConstructor
public class DepartmentQueryService {

    private final DepartmentRepository departmentRepository;

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    public List<Tree<Long>> listByOrgId(Long orgId) {
        List<BaseTreeCheckedVO> allList = this.departmentRepository.getBaseMapper().selectListByOrgId(orgId, null);
        if (CollUtil.isEmpty(allList)) {
            return null;
        }
        return this.listBaseTreeChecked(allList, 0L);
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    public List<Tree<Long>> listEnabledByOrgId(Long orgId) {
        List<BaseTreeCheckedVO> allList = this.departmentRepository.getBaseMapper().selectListByOrgId(orgId, EnableStatusEnum.ENABLE);
        if (CollUtil.isEmpty(allList)) {
            return null;
        }
        return this.listBaseTreeChecked(allList, 0L);
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    public DepartmentExtendVO getDetailExtend(Long id) {
        return this.departmentRepository.getBaseMapper().selectDetailById(id);
    }

    @Cacheable(key = "#root.methodName + '_' + #p0 + '_' + #p1", condition = "#p0 != null && #p1 != null", unless = "#result == null")
    public List<DepartmentBaseVO> listCheckedByUserId(Long orgId, Long userId) {
        return this.departmentRepository.getBaseMapper().selectListCheckedByUserId(orgId, userId);
    }

    private List<Tree<Long>> listBaseTreeChecked(List<BaseTreeCheckedVO> allList, Long rootId) {
        TreeNodeConfig config = new TreeNodeConfig();
        config.setWeightKey("seq");
        return TreeUtil.build(allList, rootId, config, (object, tree) -> {
            tree.setId(object.getId());
            tree.setParentId(object.getParentId());
            tree.setWeight(object.getSeq());
            tree.setName(object.getName());
            tree.putExtra("level", object.getLevel());
            Optional.ofNullable(object.getChecked())
                .ifPresent(val -> tree.putExtra("checked", val));
        });
    }
}

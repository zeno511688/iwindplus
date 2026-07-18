/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.dal.repository.power;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.github.yulichang.repository.JoinCrudRepository;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.mgt.domain.enums.MgtCodeEnum;
import com.iwindplus.mgt.server.dal.mapper.power.PositionMapper;
import com.iwindplus.mgt.server.dal.model.power.PositionDO;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

/**
 * 职位聚合问层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Repository
public class PositionRepository extends JoinCrudRepository<PositionMapper, PositionDO> {

    /**
     * 职位名称是否已存在.
     *
     * @param name         职位名称
     * @param departmentId 部门主键
     * @param orgId        组织主键
     */
    public void getNameIsExist(String name, Long departmentId, Long orgId) {
        final LambdaQueryWrapper<PositionDO> queryWrapper = Wrappers.lambdaQuery(PositionDO.class)
            .eq(PositionDO::getName, name)
            .eq(PositionDO::getDepartmentId, departmentId)
            .eq(PositionDO::getOrgId, orgId);
        boolean result = SqlHelper.retBool(super.count(queryWrapper));
        if (result) {
            throw new BizException(MgtCodeEnum.NAME_EXIST);
        }
    }

    /**
     * 职位编码是否已存在.
     *
     * @param code         职位编码
     * @param departmentId 部门主键
     * @param orgId        组织主键
     */
    public void getCodeIsExist(String code, Long departmentId, Long orgId) {
        final LambdaQueryWrapper<PositionDO> queryWrapper = Wrappers.lambdaQuery(PositionDO.class)
            .eq(PositionDO::getCode, code)
            .eq(PositionDO::getDepartmentId, departmentId)
            .eq(PositionDO::getOrgId, orgId);
        boolean result = SqlHelper.retBool(super.count(queryWrapper));
        if (Boolean.TRUE.equals(result)) {
            throw new BizException(MgtCodeEnum.CODE_EXIST);
        }
    }

    /**
     * 查询下一个排序号.
     *
     * @param departmentId 部门主键
     * @param orgId        组织主键
     * @return Integer
     */
    public Integer getNextSeq(Long departmentId, Long orgId) {
        QueryWrapper<PositionDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(PositionDO::getDepartmentId, departmentId);
        queryWrapper.lambda().eq(PositionDO::getOrgId, orgId);
        queryWrapper.select("max(seq) as seq");
        Function<Object, Integer> function = val -> Integer.valueOf(val.toString());
        Integer data = super.getObj(queryWrapper, function);
        return Optional.ofNullable(data).map(x -> x + 1).orElse(1);
    }

    /**
     * 通过职位ID集合获取部门ID集合.
     *
     * @param positionIds 职位ID集合
     * @return 部门ID集合
     */
    public Set<Long> getDepartmentIdsByPositionIds(Set<Long> positionIds) {
        if (positionIds == null || positionIds.isEmpty()) {
            return Set.of();
        }
        List<PositionDO> positions = this.list(
            Wrappers.lambdaQuery(PositionDO.class)
                .in(PositionDO::getId, positionIds)
                .eq(PositionDO::getStatus, EnableStatusEnum.ENABLE)
                .orderByAsc(PositionDO::getSeq)
                .select(PositionDO::getDepartmentId)
        );
        if (positions == null || positions.isEmpty()) {
            return Set.of();
        }
        return positions.stream()
            .map(PositionDO::getDepartmentId)
            .filter(id -> id != null)
            .collect(Collectors.toSet());
    }
}

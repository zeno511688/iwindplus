/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.setup.server.dal.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.github.yulichang.repository.JoinCrudRepository;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.mgt.domain.enums.MgtCodeEnum;
import com.iwindplus.setup.server.dal.mapper.RegionMapper;
import com.iwindplus.setup.server.dal.model.RegionDO;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.stereotype.Repository;

/**
 * 省市区聚合层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Repository
public class RegionRepository extends JoinCrudRepository<RegionMapper, RegionDO> {

    /**
     * 检测名称是否存在.
     *
     * @param name     名称
     * @param parentId 父类主键
     */
    public void getNameIsExist(String name, Long parentId) {
        QueryWrapper<RegionDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(RegionDO::getName, name);
        if (Objects.nonNull(parentId)) {
            queryWrapper.lambda().eq(RegionDO::getParentId, parentId);
        }
        boolean result = SqlHelper.retBool(super.count(queryWrapper));
        if (Boolean.TRUE.equals(result)) {
            throw new BizException(MgtCodeEnum.NAME_EXIST);
        }
    }

    /**
     * 查询下一个排序号.
     *
     * @param parentId 父类主键
     * @return Integer
     */
    public Integer getNextSeq(Long parentId) {
        QueryWrapper<RegionDO> queryWrapper = new QueryWrapper<>();
        if (Objects.nonNull(parentId)) {
            queryWrapper.lambda().eq(RegionDO::getParentId, parentId);
        }
        queryWrapper.select("max(seq) as seq");
        Function<Object, Integer> function = val -> Integer.valueOf(val.toString());
        Integer data = super.getObj(queryWrapper, function);
        return Optional.ofNullable(data).map(x -> x + 1).orElse(1);
    }

    /**
     * 查询级别.
     *
     * @param parentId 父类主键
     * @return Integer
     */
    public Integer getLevel(Long parentId) {
        if (Objects.isNull(parentId)) {
            return 1;
        }
        QueryWrapper<RegionDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(RegionDO::getId, parentId);
        queryWrapper.lambda().select(RegionDO::getLevel);
        Function<Object, Integer> function = val -> Integer.valueOf(val.toString());
        Integer data = super.getObj(queryWrapper, function);
        return Optional.ofNullable(data).map(x -> x + 1).orElse(1);
    }

}

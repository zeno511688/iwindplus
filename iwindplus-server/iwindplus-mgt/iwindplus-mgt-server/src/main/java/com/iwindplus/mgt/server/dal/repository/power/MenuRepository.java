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
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.mgt.domain.enums.MgtCodeEnum;
import com.iwindplus.mgt.server.dal.mapper.power.MenuMapper;
import com.iwindplus.mgt.server.dal.model.power.MenuDO;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.stereotype.Repository;

/**
 * 菜单聚合问层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Repository
public class MenuRepository extends JoinCrudRepository<MenuMapper, MenuDO> {

    /**
     * 获取名称是否已存在.
     *
     * @param name     名称
     * @param systemId 系统主键
     * @param parentId 父类主键
     */
    public void getNameIsExist(String name, Long systemId, Long parentId) {
        final LambdaQueryWrapper<MenuDO> queryWrapper = Wrappers.lambdaQuery(MenuDO.class)
            .eq(MenuDO::getName, name)
            .eq(MenuDO::getSystemId, systemId);
        if (Objects.nonNull(parentId)) {
            queryWrapper.eq(MenuDO::getParentId, parentId);
        }
        boolean result = SqlHelper.retBool(super.count(queryWrapper));
        if (Boolean.TRUE.equals(result)) {
            throw new BizException(MgtCodeEnum.NAME_EXIST);
        }
    }

    /**
     * 获取编码是否已存在.
     *
     * @param code 编码
     */
    public void getCodeIsExist(String code) {
        final LambdaQueryWrapper<MenuDO> queryWrapper = Wrappers.lambdaQuery(MenuDO.class)
            .eq(MenuDO::getCode, code);
        boolean result = SqlHelper.retBool(super.count(queryWrapper));
        if (result) {
            throw new BizException(MgtCodeEnum.CODE_EXIST);
        }
    }

    /**
     * 获取路由路径是否已存在.
     *
     * @param routeUrl 路由路径
     * @param systemId 系统主键
     * @param parentId 父类主键
     */
    public void getRouteUrlIsExist(String routeUrl, Long systemId, Long parentId) {
        final LambdaQueryWrapper<MenuDO> queryWrapper = Wrappers.lambdaQuery(MenuDO.class)
            .eq(MenuDO::getRouteUrl, routeUrl)
            .eq(MenuDO::getSystemId, systemId);
        if (Objects.nonNull(parentId)) {
            queryWrapper.eq(MenuDO::getParentId, parentId);
        }
        boolean result = SqlHelper.retBool(super.count(queryWrapper));
        if (Boolean.TRUE.equals(result)) {
            throw new BizException(MgtCodeEnum.ROUTE_URL_EXIST);
        }
    }

    /**
     * 获取下一个排序序号.
     *
     * @param systemId 系统主键
     * @param parentId 父类主键
     * @return Integer
     */
    public Integer getNextSeq(Long systemId, Long parentId) {
        QueryWrapper<MenuDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(MenuDO::getSystemId, systemId);
        if (Objects.nonNull(parentId)) {
            queryWrapper.lambda().eq(MenuDO::getParentId, parentId);
        }
        queryWrapper.select("max(seq) as seq");
        Function<Object, Integer> function = val -> Integer.valueOf(val.toString());
        Integer data = super.getObj(queryWrapper, function);
        return Optional.ofNullable(data).map(x -> x + 1).orElse(1);
    }

    /**
     * 获取菜单级别.
     *
     * @param systemId 系统主键
     * @param parentId 父类主键
     * @return 菜单级别
     */
    public Integer getLevel(Long systemId, Long parentId) {
        if (Objects.isNull(parentId)) {
            return 1;
        }
        Function<Object, Integer> function = val -> Integer.valueOf(val.toString());
        Integer data = super.getObj(Wrappers.lambdaQuery(MenuDO.class)
            .eq(MenuDO::getId, parentId)
            .eq(MenuDO::getSystemId, systemId)
            .select(MenuDO::getLevel), function);
        return Optional.ofNullable(data).map(x -> x + 1).orElse(1);
    }
}

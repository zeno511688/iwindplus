/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.dal.repository.system;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.github.yulichang.repository.JoinCrudRepository;
import com.iwindplus.base.domain.constant.CommonConstant.GatewayRouteConstant;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.mgt.domain.dto.system.ServerRouteParamDTO;
import com.iwindplus.mgt.domain.enums.MgtCodeEnum;
import com.iwindplus.mgt.server.dal.mapper.system.ServerMapper;
import com.iwindplus.mgt.server.dal.model.system.ServerDO;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.stereotype.Repository;

/**
 * 服务聚合问层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Repository
public class ServerRepository extends JoinCrudRepository<ServerMapper, ServerDO> {

    /**
     * 根据路由ID查询主键.
     *
     * @param routeId 路由ID
     * @return Long
     */
    public Long getIdByRouteId(String routeId) {
        ServerDO result = super.getOne(Wrappers.lambdaQuery(ServerDO.class)
            .eq(ServerDO::getRouteId, routeId.trim()));
        if (Objects.isNull(result)) {
            return null;
        }
        return result.getId();
    }

    /**
     * 获取服务名称是否已存在.
     *
     * @param name 服务名称
     */
    public void getNameIsExist(String name) {
        LambdaQueryWrapper<ServerDO> queryWrapper = Wrappers.lambdaQuery(ServerDO.class)
            .eq(ServerDO::getName, name);
        boolean result = SqlHelper.retBool(super.count(queryWrapper));
        if (Boolean.TRUE.equals(result)) {
            throw new BizException(MgtCodeEnum.NAME_EXIST);
        }
    }

    /**
     * 获取路由ID是否已存在.
     *
     * @param routeId 路由ID
     */
    public void getRouteIdIsExist(String routeId) {
        LambdaQueryWrapper<ServerDO> queryWrapper = Wrappers.lambdaQuery(ServerDO.class)
            .eq(ServerDO::getRouteId, routeId);
        boolean result = SqlHelper.retBool(super.count(queryWrapper));
        if (Boolean.TRUE.equals(result)) {
            throw new BizException(MgtCodeEnum.ROUTE_ID_EXIST);
        }
    }

    /**
     * 检查路由规则是否已存在.
     *
     * @param predicates 路由规则
     */
    public boolean checkPredicates(List<ServerRouteParamDTO> predicates) {
        final String pattern = this.getPattern(predicates);
        if (CharSequenceUtil.isNotBlank(pattern)) {
            return SqlHelper.retBool(super.baseMapper.selectCountByPattern(pattern));
        }
        return false;
    }

    /**
     * 检查路由规则是否已存在.
     *
     * @param predicates 断言
     */
    public void getPredicatesIsExist(List<ServerRouteParamDTO> predicates) {
        boolean result = this.checkPredicates(predicates);
        if (Boolean.TRUE.equals(result)) {
            throw new BizException(MgtCodeEnum.SERVER_PREDICATE_EXIST);
        }
    }

    /**
     * 查询下一个排序号.
     *
     * @return Integer
     */
    public Integer getNextSeq() {
        QueryWrapper<ServerDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("max(seq) as seq");
        Function<Object, Integer> function = val -> Integer.valueOf(val.toString());
        Integer data = super.getObj(queryWrapper, function);
        return Optional.ofNullable(data).map(x -> x + 1).orElse(1);
    }

    /**
     * 获取路由规则的匹配模式.
     *
     * @param predicates 断言
     * @return String
     */
    public String getPattern(List<ServerRouteParamDTO> predicates) {
        return CollUtil.emptyIfNull(predicates).stream()
            .filter(p -> GatewayRouteConstant.PATH.equalsIgnoreCase(p.getName()))
            .map(p -> p.getArgs())
            .filter(Objects::nonNull)
            .map(m -> m.get(GatewayRouteConstant.GENKEY_0))
            .filter(Objects::nonNull)
            .findFirst()
            .map(String::trim)
            .orElse(null);
    }
}

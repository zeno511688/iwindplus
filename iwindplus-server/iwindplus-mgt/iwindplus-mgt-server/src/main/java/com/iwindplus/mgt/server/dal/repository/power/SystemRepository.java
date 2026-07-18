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
import com.iwindplus.mgt.server.dal.mapper.power.SystemMapper;
import com.iwindplus.mgt.server.dal.model.power.SystemDO;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.stereotype.Repository;

/**
 * 系统聚合层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Repository
public class SystemRepository extends JoinCrudRepository<SystemMapper, SystemDO> {

    /**
     * 获取名称是否已存在.
     *
     * @param name 名称
     */
    public void getNameIsExist(String name) {
        final LambdaQueryWrapper<SystemDO> queryWrapper = Wrappers.lambdaQuery(SystemDO.class)
            .eq(SystemDO::getName, name);
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
        final LambdaQueryWrapper<SystemDO> queryWrapper = Wrappers.lambdaQuery(SystemDO.class)
            .eq(SystemDO::getCode, code);
        boolean result = SqlHelper.retBool(super.count(queryWrapper));
        if (Boolean.TRUE.equals(result)) {
            throw new BizException(MgtCodeEnum.CODE_EXIST);
        }
    }

    /**
     * 获取下一个排序序号.
     *
     * @return Integer
     */
    public Integer getNextSeq() {
        QueryWrapper<SystemDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("max(seq) as seq");
        Function<Object, Integer> function = val -> Integer.valueOf(val.toString());
        Integer data = super.getObj(queryWrapper, function);
        return Optional.ofNullable(data).map(x -> x + 1).orElse(1);
    }

}

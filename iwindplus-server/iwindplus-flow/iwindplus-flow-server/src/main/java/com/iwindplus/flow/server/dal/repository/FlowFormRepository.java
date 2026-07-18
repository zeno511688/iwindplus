/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server.dal.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.github.yulichang.repository.JoinCrudRepository;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.flow.domain.enums.FlowCodeEnum;
import com.iwindplus.flow.server.dal.mapper.FlowFormMapper;
import com.iwindplus.flow.server.dal.model.FlowFormDO;
import com.iwindplus.mgt.domain.enums.MgtCodeEnum;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.stereotype.Repository;

/**
 * 流程表单聚合层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Repository
public class FlowFormRepository extends JoinCrudRepository<FlowFormMapper, FlowFormDO> {

    /**
     * 获取流程表单名称是否存在.
     *
     * @param name 流程表单名称
     */
    public void getNameIsExist(String name) {
        QueryWrapper<FlowFormDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FlowFormDO::getName, name);
        boolean result = SqlHelper.retBool(super.count(queryWrapper));
        if (Boolean.TRUE.equals(result)) {
            throw new BizException(MgtCodeEnum.NAME_EXIST);
        }
    }

    /**
     * 获取流程表单编码是否存在.
     *
     * @param code 流程表单编码
     */
    public void getCodeIsExist(String code) {
        QueryWrapper<FlowFormDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FlowFormDO::getCode, code);
        boolean result = SqlHelper.retBool(super.count(queryWrapper));
        if (result) {
            throw new BizException(FlowCodeEnum.CODE_EXIST);
        }
    }

    /**
     * 获取流程表单最大排序号.
     *
     * @return Integer
     */
    public Integer getNextSeq() {
        QueryWrapper<FlowFormDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("max(seq) as seq");
        Function<Object, Integer> function = val -> Integer.valueOf(val.toString());
        Integer data = super.getObj(queryWrapper, function);
        return Optional.ofNullable(data).map(x -> x + 1).orElse(1);
    }
}

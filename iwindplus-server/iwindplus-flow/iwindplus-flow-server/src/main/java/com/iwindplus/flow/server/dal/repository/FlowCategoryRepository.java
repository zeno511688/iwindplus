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
import com.iwindplus.flow.server.dal.mapper.FlowCategoryMapper;
import com.iwindplus.flow.server.dal.model.FlowCategoryDO;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.stereotype.Repository;

/**
 * 流程分类聚合层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Repository
public class FlowCategoryRepository extends JoinCrudRepository<FlowCategoryMapper, FlowCategoryDO> {

    /**
     * 获取名称是否已存在.
     *
     * @param name 名称
     */
    public void getNameIsExist(String name) {
        QueryWrapper<FlowCategoryDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FlowCategoryDO::getName, name);
        boolean result = SqlHelper.retBool(super.count(queryWrapper));
        if (Boolean.TRUE.equals(result)) {
            throw new BizException(FlowCodeEnum.NAME_EXIST);
        }
    }

    /**
     * 获取编码是否已存在.
     *
     * @param code 编码
     */
    public void getCodeIsExist(String code) {
        QueryWrapper<FlowCategoryDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FlowCategoryDO::getCode, code);
        boolean result = SqlHelper.retBool(super.count(queryWrapper));
        if (result) {
            throw new BizException(FlowCodeEnum.CODE_EXIST);
        }
    }

    /**
     * 获取下一个排序号.
     *
     * @return Integer
     */
    public Integer getNextSeq() {
        QueryWrapper<FlowCategoryDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("max(seq) as seq");
        Function<Object, Integer> function = val -> Integer.valueOf(val.toString());
        Integer data = super.getObj(queryWrapper, function);
        return Optional.ofNullable(data).map(x -> x + 1).orElse(1);
    }
}

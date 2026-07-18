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
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.flow.domain.enums.FlowCodeEnum;
import com.iwindplus.flow.server.dal.mapper.FlowInstanceMapper;
import com.iwindplus.flow.server.dal.model.FlowInstanceDO;
import org.springframework.stereotype.Repository;

/**
 * 流程实例聚合层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Repository
public class FlowInstanceRepository extends JoinCrudRepository<FlowInstanceMapper, FlowInstanceDO> {

    /**
     * 获取编码是否已存在.
     *
     * @param code 编码
     */
    public void getCodeIsExist(String code) {
        QueryWrapper<FlowInstanceDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FlowInstanceDO::getCode, code);
        boolean result = SqlHelper.retBool(super.count(queryWrapper));
        if (result) {
            throw new BizException(FlowCodeEnum.CODE_EXIST);
        }
    }

    /**
     * 获取业务流水号是否已存在.
     *
     * @param bizNumber 业务流水号
     */
    public void getBizNumberIsExist(String bizNumber) {
        QueryWrapper<FlowInstanceDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FlowInstanceDO::getBizNumber, bizNumber);
        boolean result = SqlHelper.retBool(super.count(queryWrapper));
        if (result) {
            throw new BizException(BizCodeEnum.BIZ_NUMBER_EXIST, new Object[]{bizNumber});
        }
    }
}

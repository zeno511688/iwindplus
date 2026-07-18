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
import com.iwindplus.flow.domain.enums.FlowModelStatusEnum;
import com.iwindplus.flow.domain.vo.FlowModelExtVO;
import com.iwindplus.flow.server.dal.mapper.FlowModelMapper;
import com.iwindplus.flow.server.dal.model.FlowModelDO;
import com.iwindplus.mgt.domain.enums.MgtCodeEnum;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.stereotype.Repository;

/**
 * 流程模型聚合层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Repository
public class FlowModelRepository extends JoinCrudRepository<FlowModelMapper, FlowModelDO> {

    /**
     * 获取流程模型名称是否存在.
     *
     * @param name 流程模型名称
     */
    public void getNameIsExist(String name) {
        QueryWrapper<FlowModelDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FlowModelDO::getName, name);
        queryWrapper.lambda().ne(FlowModelDO::getStatus, FlowModelStatusEnum.HISTORY);
        boolean result = SqlHelper.retBool(super.count(queryWrapper));
        if (Boolean.TRUE.equals(result)) {
            throw new BizException(MgtCodeEnum.NAME_EXIST);
        }
    }

    /**
     * 获取流程模型编码是否存在.
     *
     * @param code 模型编码
     */
    public void getCodeIsExist(String code) {
        // 待发布和已发布的不能重复
        QueryWrapper<FlowModelDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FlowModelDO::getCode, code);
        queryWrapper.lambda().ne(FlowModelDO::getStatus, FlowModelStatusEnum.HISTORY);
        boolean result = SqlHelper.retBool(super.count(queryWrapper));
        if (result) {
            throw new BizException(FlowCodeEnum.CODE_EXIST);
        }
    }

    /**
     * 获取下一个排序号
     *
     * @return Integer
     */
    public Integer getNextSeq() {
        QueryWrapper<FlowModelDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("max(seq) as seq");
        Function<Object, Integer> function = val -> Integer.valueOf(val.toString());
        Integer data = super.getObj(queryWrapper, function);
        return Optional.ofNullable(data).map(x -> x + 1).orElse(1);
    }

    /**
     * 通过编码和状态查找最新的一条已发布的.
     *
     * @param code 编码
     * @return FlowModelExtVO
     */
    public FlowModelExtVO getNewestPublishedByCode(String code) {
        return this.getNewestOneByCode(code, FlowModelStatusEnum.PUBLISHED);
    }

    /**
     * 通过编码和状态查找最新的一条历史版本的.
     *
     * @param code 编码
     * @return FlowModelExtVO
     */
    public FlowModelExtVO getNewestHistoryByCode(String code) {
        return this.getNewestOneByCode(code, FlowModelStatusEnum.HISTORY);
    }

    /**
     * 通过编码和状态查找最新的一条.
     *
     * @param code   编码
     * @param status 状态
     * @return FlowModelExtVO
     */
    public FlowModelExtVO getNewestOneByCode(String code, FlowModelStatusEnum status) {
        final FlowModelExtVO data = super.baseMapper.selectNewestOneByCondition(code, status);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return data;
    }
}

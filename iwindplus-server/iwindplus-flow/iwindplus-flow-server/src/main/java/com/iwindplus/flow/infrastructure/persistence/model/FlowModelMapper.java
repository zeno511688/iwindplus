/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.infrastructure.persistence.model;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.github.yulichang.base.MPJBaseMapper;
import com.iwindplus.flow.application.query.model.dto.FlowModelSearchDTO;
import com.iwindplus.flow.common.enums.FlowModelStatusEnum;
import com.iwindplus.flow.application.query.model.vo.FlowModelExtVO;
import com.iwindplus.flow.application.query.model.vo.FlowModelPageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 流程模型数据访问层接口类.
 *
 * @author zengdegui
 * @since 2024/11/03 19:03
 */
@Mapper
public interface FlowModelMapper extends MPJBaseMapper<FlowModelDO> {

    /**
     * 通过主键详情.
     *
     * @param id 主键
     * @return FlowModelExtVO
     */
    FlowModelExtVO selectDetailById(Long id);

    /**
     * 通过编码和状态查找最新的一条.
     *
     * @param code   编码
     * @param status 状态
     * @return FlowModelExtVO
     */
    FlowModelExtVO selectNewestOneByCondition(@Param("code") String code, @Param("status") FlowModelStatusEnum status);

    /**
     * 列表.
     *
     * @param page   分页对象
     * @param entity 对象
     * @return IPage<FlowModelPageVO>
     */
    IPage<FlowModelPageVO> selectPageByCondition(PageDTO<FlowModelDO> page, @Param(Constants.WRAPPER) FlowModelSearchDTO entity);
}

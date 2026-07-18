/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server.dal.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.github.yulichang.base.MPJBaseMapper;
import com.iwindplus.flow.domain.dto.FlowHisInstanceSearchDTO;
import com.iwindplus.flow.domain.vo.FlowHisInstancePageVO;
import com.iwindplus.flow.server.dal.model.FlowHisInstanceDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 历史流程实例数据访问层接口类.
 *
 * @author zengdegui
 * @since 2024/11/03 19:03
 */
@Mapper
public interface FlowHisInstanceMapper extends MPJBaseMapper<FlowHisInstanceDO> {

    /**
     * 我的发起/已办/抄送我的，所有分页查询.
     *
     * @param page   分页对象
     * @param entity 查询条件
     * @return IPage<FlowHisInstancePageVO>
     */
    IPage<FlowHisInstancePageVO> selectPage(PageDTO<FlowHisInstancePageVO> page,
        @Param(Constants.WRAPPER) FlowHisInstanceSearchDTO entity);

}

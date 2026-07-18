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
import com.iwindplus.flow.domain.dto.FlowApprovalRecordSearchDTO;
import com.iwindplus.flow.domain.vo.FlowHisTaskPageVO;
import com.iwindplus.flow.server.dal.model.FlowHisTaskDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 历史流程任务数据访问层接口类.
 *
 * @author zengdegui
 * @since 2024/11/03 19:03
 */
@Mapper
public interface FlowHisTaskMapper extends MPJBaseMapper<FlowHisTaskDO> {

    /**
     * 审批记录分页查询.
     *
     * @param page   分页对象
     * @param entity 查询条件
     * @return IPage<FlowHisTaskPageVO>
     */
    IPage<FlowHisTaskPageVO> selectApprovalRecordPage(PageDTO<FlowHisTaskPageVO> page,
        @Param(Constants.WRAPPER) FlowApprovalRecordSearchDTO entity);
}

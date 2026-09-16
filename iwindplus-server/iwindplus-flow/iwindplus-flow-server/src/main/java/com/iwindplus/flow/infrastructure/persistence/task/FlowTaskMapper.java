/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.infrastructure.persistence.task;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.github.yulichang.base.MPJBaseMapper;
import com.iwindplus.flow.application.query.task.dto.FlowTaskSearchDTO;
import com.iwindplus.flow.application.query.task.vo.FlowTaskPageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 流程任务数据访问层接口类.
 *
 * @author zengdegui
 * @since 2024/11/03 19:03
 */
@Mapper
public interface FlowTaskMapper extends MPJBaseMapper<FlowTaskDO> {

    /**
     * 我的待办分页查询.
     *
     * @param page   分页对象
     * @param entity 查询条件
     * @return IPage<FlowTaskPageVO>
     */
    IPage<FlowTaskPageVO> selectMyPendingPage(PageDTO<FlowTaskPageVO> page,
        @Param(Constants.WRAPPER) FlowTaskSearchDTO entity);
}

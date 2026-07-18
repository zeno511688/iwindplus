/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server.dal.mapper;

import com.github.yulichang.base.MPJBaseMapper;
import com.iwindplus.flow.server.dal.model.FlowModelExtendDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/**
 * 流程模型扩展数据访问层接口类.
 *
 * @author zengdegui
 * @since 2024/11/03 19:03
 */
@Mapper
public interface FlowModelExtendMapper extends MPJBaseMapper<FlowModelExtendDO> {

    /**
     * 通过模型主键真实删除.
     *
     * @param modelIds 模型主键集合
     * @return int
     */
    int deleteByModelIds(List<Long> modelIds);
}

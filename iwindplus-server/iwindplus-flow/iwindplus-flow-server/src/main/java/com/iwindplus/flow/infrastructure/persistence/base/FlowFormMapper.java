/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.infrastructure.persistence.base;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 流程表单数据访问层接口类.
 *
 * @author zengdegui
 * @since 2024/11/03 19:03
 */
@Mapper
public interface FlowFormMapper extends BaseMapper<FlowFormDO> {
}

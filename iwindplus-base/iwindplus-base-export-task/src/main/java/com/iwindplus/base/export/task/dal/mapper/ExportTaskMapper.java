/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.export.task.dal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iwindplus.base.export.task.dal.model.ExportTaskDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 导出任务Mapper.
 *
 * @author zengdegui
 * @since 2026/08/27
 */
@Mapper
public interface ExportTaskMapper extends BaseMapper<ExportTaskDO> {
}

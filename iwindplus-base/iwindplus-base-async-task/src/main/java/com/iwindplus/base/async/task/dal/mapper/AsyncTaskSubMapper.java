/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.task.dal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iwindplus.base.async.task.dal.model.AsyncTaskSubDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 异步任务子表数据访问接口.
 *
 * @author zengdegui
 * @since 2025/9/14
 */
@Mapper
public interface AsyncTaskSubMapper extends BaseMapper<AsyncTaskSubDO> {

}

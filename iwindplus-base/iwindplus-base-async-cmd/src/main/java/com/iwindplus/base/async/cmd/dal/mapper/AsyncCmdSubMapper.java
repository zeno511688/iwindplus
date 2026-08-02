/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.dal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iwindplus.base.async.cmd.dal.model.AsyncCmdSubDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 异步命令子表数据访问接口.
 *
 * @author zengdegui
 * @since 2025/9/14
 */
@Mapper
public interface AsyncCmdSubMapper extends BaseMapper<AsyncCmdSubDO> {

}

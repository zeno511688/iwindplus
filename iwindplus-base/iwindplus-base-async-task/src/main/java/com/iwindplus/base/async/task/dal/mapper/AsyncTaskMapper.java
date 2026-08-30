/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.task.dal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.iwindplus.base.async.task.dal.model.AsyncTaskDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 异步任务数据访问层接口.
 *
 * @author zengdegui
 * @since 2025/9/14
 */
@Mapper
public interface AsyncTaskMapper extends BaseMapper<AsyncTaskDO> {

    /**
     * 批量真实删除.
     *
     * @param ids 主键集合
     * @return int
     */
    int deleteDataByIds(@Param(Constants.LIST) List<Long> ids);

    /**
     * 真实删除.
     *
     * @param id 主键
     * @return int
     */
    int deleteDataById(@Param("id") Long id);

    /**
     * 真实删除.
     *
     * @param env       环境
     * @param bizNumber 业务流水号
     * @return int
     */
    int deleteByBizNumber(@Param("env") String env, @Param("bizNumber") String bizNumber);
}

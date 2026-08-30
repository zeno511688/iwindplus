/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.dtx.server.support;

import com.iwindplus.dtx.domain.enums.DtxTaskJobEnum;

/**
 * 分布式事务任务job处理器.
 *
 * @author zengdegui
 * @since 2025/12/27 17:07
 */
public interface DtxTaskJobHandler {

    /**
     * 获取支持的job类型.
     *
     * @return DtxJobEnum
     */
    DtxTaskJobEnum support();

    /**
     * 执行job.
     *
     * @param shardIndex 分片索引
     * @param shardTotal 分片总数
     */
    void execute(Integer shardIndex, Integer shardTotal);
}

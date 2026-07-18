/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.dtx.server.support;

import com.iwindplus.dtx.domain.enums.DtxJobEnum;

/**
 * 分布式事务job执行助手.
 *
 * @author zengdegui
 * @since 2025/12/27 17:07
 */
public interface DtxJobHandler {

    /**
     * 获取支持的job类型.
     *
     * @return DtxJobEnum
     */
    DtxJobEnum support();

    /**
     * 执行job.
     *
     * @param shardingIndex 分片
     */
    void execute(int shardingIndex);
}

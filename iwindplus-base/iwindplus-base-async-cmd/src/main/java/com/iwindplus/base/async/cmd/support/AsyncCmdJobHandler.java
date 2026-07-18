/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.support;

import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdJobEnum;

/**
 * 异步命令job助手接口.
 *
 * @author zengdegui
 * @since 2025/12/27 17:07
 */
public interface AsyncCmdJobHandler {

    /**
     * 获取支持的job类型.
     *
     * @return AsyncCmdJobEnum
     */
    AsyncCmdJobEnum support();

    /**
     * 执行job.
     *
     * @param shardingIndex 分片
     */
    void execute(int shardingIndex);
}

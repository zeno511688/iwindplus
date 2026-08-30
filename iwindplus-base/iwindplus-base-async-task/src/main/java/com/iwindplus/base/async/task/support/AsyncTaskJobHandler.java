/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.task.support;

import com.iwindplus.base.async.task.domain.enums.AsyncTaskJobEnum;

/**
 * 异步任务job助手接口.
 *
 * @author zengdegui
 * @since 2025/12/27 17:07
 */
public interface AsyncTaskJobHandler {

    /**
     * 获取支持的job类型.
     *
     * @return AsyncTaskJobEnum
     */
    AsyncTaskJobEnum support();

    /**
     * 执行job.
     *
     * @param shardIndex 分片索引
     * @param shardTotal 分片总数
     */
    void execute(Integer shardIndex, Integer shardTotal);
}

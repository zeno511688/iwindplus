/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.document.support;

import com.iwindplus.base.document.domain.enums.DocumentTaskJobEnum;

/**
 * 文档任务job处理器接口.
 *
 * @author zengdegui
 * @since 2026/08/28
 */
public interface DocumentTaskJobHandler {

    /**
     * 获取支持的job类型.
     *
     * @return DocumentTaskJobEnum
     */
    DocumentTaskJobEnum support();

    /**
     * 执行job.
     *
     * @param shardIndex 分片索引
     * @param shardTotal 分片总数
     */
    void execute(Integer shardIndex, Integer shardTotal);
}

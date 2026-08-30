/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.task.support;

import com.iwindplus.base.async.task.domain.vo.AsyncTaskVO;

/**
 * 异步任务执行形态策略接口.
 *
 * <p>分有无子任务两种</p>
 *
 * @author zengdegui
 * @since 2025/12/27 17:07
 */
public interface AsyncTaskExecuteHandler {

    /**
     * 执行业务.
     *
     * @param entity 对象
     */
    void execute(AsyncTaskVO entity);
}

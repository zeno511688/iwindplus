/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.executor;

import com.iwindplus.base.async.cmd.domain.bo.AsyncCmdExecutorBO;
import com.iwindplus.base.async.cmd.domain.bo.AsyncCmdExecutorBaseBO;

/**
 * 异步命令执行器接口.
 *
 * @author zengdegui
 * @since 2025/12/28 00:17
 */
public interface AsyncCmdExecutor {

    /**
     * 提交任务.
     *
     * @param entity 对象
     */
    void submit(AsyncCmdExecutorBO entity);

    /**
     * 通过业务流水号移除任务.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean removeByCondition(AsyncCmdExecutorBaseBO entity);
}

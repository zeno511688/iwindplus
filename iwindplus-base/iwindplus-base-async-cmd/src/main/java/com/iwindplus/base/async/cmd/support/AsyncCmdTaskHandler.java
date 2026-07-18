/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.support;

import com.iwindplus.base.async.cmd.domain.bo.AsyncCmdExecutorBO;

/**
 * 异步命令任务接口.
 *
 * @author zengdegui
 * @since 2025/09/21 20:18
 */
public interface AsyncCmdTaskHandler {

    /**
     * 获取执行器名称（有默认值不需要实现）.
     *
     * @return 执行器名称
     */
    default String getExecuteName() {
        return this.getClass().getSimpleName();
    }

    /**
     * 执行业务.
     *
     * @param entity 对象
     */
    void execute(AsyncCmdExecutorBO entity);
}

/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.support;

import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdVO;

/**
 * 异步命令主任务助手.
 *
 * <p>无子任务时，主任务即业务本体。抢占执行权 -> 回调主任务execute -> 置成功/失败</p>
 * <p>有子任务时，等子任务全部完成后execute做收尾</p>
 * <p>任务成功或失败执行onTaskSuccess/onTaskFail</p>
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
    void execute(AsyncCmdVO entity);

    /**
     * 任务成功.
     *
     * @param entity 命令对象
     */
    default void onTaskSuccess(AsyncCmdVO entity) {
    }

    /**
     * 任务失败.
     *
     * @param entity 命令对象
     */
    default void onTaskFail(AsyncCmdVO entity) {
    }
}

/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.support;

import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdCallbackResultEnum;
import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdExecuteResultEnum;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdVO;

/**
 * 异步命令主任务助手.
 *
 * <p>无子任务时，主任务即业务本体。抢占执行权 -> 回调主任务execute -> 置成功/失败</p>
 * <p>有子任务时，等子任务全部完成后execute做收尾</p>
 * <p>任务成功或失败执行onTaskSuccess/onTaskFail</p>
 * <p>重试次数耗尽被丢弃时执行onTaskDiscard</p>
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
     * <p>由业务方显式返回执行结果，决定任务状态走向：
     * SUCCESS → 成功，FAILED → 失败，ASYNC_WAIT → 进入异步等待</p>
     *
     * @param entity 对象
     * @return AsyncCmdExecuteResultEnum 执行结果
     */
    AsyncCmdExecuteResultEnum execute(AsyncCmdVO entity);

    /**
     * 任务获取异步等待结果.
     *
     * @param entity 命令对象
     * @return AsyncCmdCallbackResultEnum
     */
    default AsyncCmdCallbackResultEnum executeCallback(AsyncCmdVO entity) {
        return AsyncCmdCallbackResultEnum.WAITING;
    }

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

    /**
     * 任务异步等待，首次挂起时触发.
     *
     * @param entity 命令对象
     */
    default void onTaskAsyncWait(AsyncCmdVO entity) {

    }

    /**
     * 任务丢弃，重试次数耗尽被置为DISCARD时触发，可用于补偿/告警等业务善后.
     *
     * @param entity 命令对象
     */
    default void onTaskDiscard(AsyncCmdVO entity) {

    }
}

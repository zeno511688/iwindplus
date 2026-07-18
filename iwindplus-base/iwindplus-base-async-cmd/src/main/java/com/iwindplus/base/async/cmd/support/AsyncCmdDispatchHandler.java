/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.support;

import com.iwindplus.base.async.cmd.domain.bo.AsyncCmdBO;
import com.iwindplus.base.async.cmd.domain.enums.DispatchModeEnum;

/**
 * 异步命令调度助手接口.
 *
 * @author zengdegui
 * @since 2025/12/28 00:00
 */
public interface AsyncCmdDispatchHandler {

    /**
     * 获取支持的调度类型.
     *
     * @return DispatchModeEnum
     */
    DispatchModeEnum support();

    /**
     * 执行job.
     *
     * @param entity 对象
     */
    void execute(AsyncCmdBO entity);
}

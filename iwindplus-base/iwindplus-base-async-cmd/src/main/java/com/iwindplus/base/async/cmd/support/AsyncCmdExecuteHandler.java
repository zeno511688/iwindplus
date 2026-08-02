/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.support;

import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdVO;

/**
 * 异步命令执行形态策略接口.
 *
 * <p>分有无子任务两种</p>
 *
 * @author zengdegui
 * @since 2025/12/27 17:07
 */
public interface AsyncCmdExecuteHandler {

    /**
     * 执行业务.
     *
     * @param entity 对象
     */
    void execute(AsyncCmdVO entity);
}

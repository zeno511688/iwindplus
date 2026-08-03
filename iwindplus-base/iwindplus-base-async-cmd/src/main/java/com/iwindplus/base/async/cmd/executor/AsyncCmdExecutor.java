/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.executor;

import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdGroupSubmitDTO;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdSubmitDTO;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdSubmitVO;

/**
 * 异步命令执行器接口.
 *
 * @author zengdegui
 * @since 2025/12/28 00:17
 */
public interface AsyncCmdExecutor {

    /**
     * 提交任务（无子任务）.
     *
     * @param entity 对象
     * @return AsyncCmdSubmitVO
     */
    AsyncCmdSubmitVO submit(AsyncCmdSubmitDTO entity);

    /**
     * 提交任务（带子任务）.
     *
     * @param entity 对象
     * @return AsyncCmdSubmitVO
     */
    AsyncCmdSubmitVO submitGroup(AsyncCmdGroupSubmitDTO entity);

    /**
     * 移除任务.
     *
     * @param id 主键
     * @return boolean
     */
    boolean removeById(Long id);

    /**
     * 通过业务key和类型移除任务.
     *
     * @param bizKey  业务key
     * @param bizType 业务类型
     * @return boolean
     */
    boolean removeByBizKeyAndType(String bizKey, String bizType);

    /**
     * 通过业务流水号移除任务.
     *
     * @param bizNumber 业务流水号
     * @return boolean
     */
    boolean removeByBizNumber(String bizNumber);
}

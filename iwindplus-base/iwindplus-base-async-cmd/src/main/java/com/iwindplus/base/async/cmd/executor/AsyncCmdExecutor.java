/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.executor;

import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdCallbackDTO;
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
     * 回调通知上报.
     *
     * <p>业务收到外部系统回调后调用，框架预存结果并驱动状态流转，业务无需直接修改任务状态</p>
     * <p>主任务通过id或bizNumber定位，子任务通过subTasks列表携带各自的回调结果</p>
     *
     * @param entity 对象
     * @return boolean
     */
    boolean callback(AsyncCmdCallbackDTO entity);

    /**
     * 通过主键人工触发重试.
     *
     * @param id 主键
     */
    void retryById(Long id);

    /**
     * 通过业务流水号人工触发重试.
     *
     * @param bizNumber 业务流水号
     */
    void retryByBizNumber(String bizNumber);

    /**
     * 通过主键移除任务.
     *
     * @param id 主键
     * @return boolean
     */
    boolean removeById(Long id);

    /**
     * 通过业务流水号移除任务.
     *
     * @param bizNumber 业务流水号
     * @return boolean
     */
    boolean removeByBizNumber(String bizNumber);
}

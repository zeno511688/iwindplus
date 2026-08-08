/*
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */
package com.iwindplus.base.async.cmd.service;

import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdStatusEnum;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdSubVO;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 异步命令子表业务层接口类.
 *
 * @author zengdegui
 * @since 2026/8/2
 */
public interface AsyncCmdSubService {

    /**
     * 通过主键修改状态.
     *
     * @param id                 主键
     * @param from               从状态
     * @param to                 到状态
     * @param costTime           耗时
     * @param errorMsg           错误信息
     * @param retryCount         重试次数
     * @param result             结果
     * @param callbackExpireTime 等待异步结果的截止时间
     * @return boolean
     */
    boolean editStatusById(Long id, AsyncCmdStatusEnum from, AsyncCmdStatusEnum to,
        Long costTime, String errorMsg, Integer retryCount, Map<String, Object> result, LocalDateTime callbackExpireTime);

    /**
     * 通过异步命令主键获取子任务数量.
     *
     * @param asyncCmdId 异步命令主键
     * @return long
     */
    long countByAsyncCmdId(Long asyncCmdId);

    /**
     * 通过异步命令主键获取未执行成功的子任务数量.
     *
     * @param asyncCmdId 异步命令主键
     * @return long
     */
    long countUnfinished(Long asyncCmdId);

    /**
     * 通过异步命令主键和状态获取子任务列表（按排序号升序）.
     *
     * @param asyncCmdId 异步命令主键
     * @param statusList 状态集合
     * @return List<AsyncCmdSubVO>
     */
    List<AsyncCmdSubVO> listByAsyncCmdIdAndStatus(Long asyncCmdId, List<AsyncCmdStatusEnum> statusList);
}

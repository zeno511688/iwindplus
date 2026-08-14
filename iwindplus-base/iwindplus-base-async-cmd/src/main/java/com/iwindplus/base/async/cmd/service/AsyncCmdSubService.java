/*
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */
package com.iwindplus.base.async.cmd.service;

import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdStatusEditDTO;
import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdStatusEnum;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdSubVO;
import java.util.List;

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
     * @param entity 状态流转对象（空字段不更新）
     * @return boolean
     */
    boolean editStatusById(AsyncCmdStatusEditDTO entity);

    /**
     * 通过业务流水号查找子任务.
     *
     * @param bizNumber 业务流水号
     * @return AsyncCmdSubVO
     */
    AsyncCmdSubVO getDetailByBizNumber(String bizNumber);

    /**
     * 通过异步命令主键查找子任务.
     *
     * @param asyncCmdId 异步命令主键
     * @param bizKey     业务键
     * @return AsyncCmdSubVO
     */
    AsyncCmdSubVO getDetailByAsyncCmdId(Long asyncCmdId, String bizKey);

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

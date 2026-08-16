/*
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */
package com.iwindplus.base.async.cmd.service;

import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdStatusEditDTO;
import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdStatusEnum;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdSubVO;
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
     * @param entity 状态流转对象（空字段不更新）
     * @return boolean
     */
    boolean editStatusById(AsyncCmdStatusEditDTO entity);

    /**
     * 通过主键列表批量修改状态.
     *
     * @param ids    主键列表
     * @param entity 状态流转对象（仅使用from/to字段）
     * @return boolean
     */
    boolean editStatusByIds(List<Long> ids, AsyncCmdStatusEditDTO entity);

    /**
     * 批量更新子任务回调结果与进度（单条SQL）.
     *
     * @param idToResult   主键→回调结果映射
     * @param idToProgress 主键→进度映射
     * @return boolean
     */
    boolean editCallbackBatch(Map<Long, Map<String, Object>> idToResult, Map<Long, Integer> idToProgress);

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

    /**
     * 通过业务流水号列表批量查询子任务.
     *
     * @param bizNumbers 业务流水号列表
     * @return List<AsyncCmdSubVO>
     */
    List<AsyncCmdSubVO> listByBizNumbers(List<String> bizNumbers);

    /**
     * 聚合子任务进度到主任务.
     * <p>查询主任务下所有子任务，成功的视为 100%，其余按已上报进度计算，取均值写入主任务.</p>
     *
     * @param asyncCmdId 主任务 ID
     */
    void aggregateProgress(Long asyncCmdId);

}

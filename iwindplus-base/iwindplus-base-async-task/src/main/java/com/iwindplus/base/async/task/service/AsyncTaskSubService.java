/*
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */
package com.iwindplus.base.async.task.service;

import com.iwindplus.base.async.task.domain.dto.AsyncTaskStatusEditDTO;
import com.iwindplus.base.async.task.domain.enums.AsyncTaskStatusEnum;
import com.iwindplus.base.async.task.domain.vo.AsyncTaskSubVO;
import java.util.List;

/**
 * 异步任务子表业务层接口类.
 *
 * @author zengdegui
 * @since 2026/8/2
 */
public interface AsyncTaskSubService {

    /**
     * 通过主键修改状态.
     *
     * @param entity 状态流转对象（空字段不更新）
     * @return boolean
     */
    boolean editStatusById(AsyncTaskStatusEditDTO entity);

    /**
     * 通过主键列表批量修改状态.
     *
     * @param ids    主键列表
     * @param entity 状态流转对象（仅使用from/to字段）
     * @return boolean
     */
    boolean editStatusByIds(List<Long> ids, AsyncTaskStatusEditDTO entity);

    /**
     * 通过业务流水号查找子任务.
     *
     * @param bizNumber 业务流水号
     * @return AsyncTaskSubVO
     */
    AsyncTaskSubVO getDetailByBizNumber(String bizNumber);

    /**
     * 通过异步任务主键查找子任务.
     *
     * @param asyncTaskId 异步任务主键
     * @param bizKey     业务键
     * @return AsyncTaskSubVO
     */
    AsyncTaskSubVO getDetailByAsyncTaskId(Long asyncTaskId, String bizKey);

    /**
     * 通过异步任务主键获取子任务数量.
     *
     * @param asyncTaskId 异步任务主键
     * @return long
     */
    long countByAsyncTaskId(Long asyncTaskId);

    /**
     * 通过异步任务主键获取未执行成功的子任务数量.
     *
     * @param asyncTaskId 异步任务主键
     * @return long
     */
    long countUnfinished(Long asyncTaskId);

    /**
     * 通过异步任务主键获取未超时的子任务数量.
     *
     * @param asyncTaskId 异步任务主键
     * @return long
     */
    long countNotTimeout(Long asyncTaskId);

    /**
     * 通过异步任务主键和状态获取子任务列表（按排序号升序）.
     *
     * @param asyncTaskId 异步任务主键
     * @param statusList 状态集合
     * @return List<AsyncTaskSubVO>
     */
    List<AsyncTaskSubVO> listByAsyncTaskIdAndStatus(Long asyncTaskId, List<AsyncTaskStatusEnum> statusList);

    /**
     * 通过业务流水号列表批量查询子任务.
     *
     * @param bizNumbers 业务流水号列表
     * @return List<AsyncTaskSubVO>
     */
    List<AsyncTaskSubVO> listByBizNumbers(List<String> bizNumbers);
}

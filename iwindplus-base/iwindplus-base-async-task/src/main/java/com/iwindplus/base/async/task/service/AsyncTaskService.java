/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.task.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.iwindplus.base.async.task.dal.model.AsyncTaskDO;
import com.iwindplus.base.async.task.dal.model.AsyncTaskSubDO;
import com.iwindplus.base.async.task.domain.dto.AsyncTaskEditDTO;
import com.iwindplus.base.async.task.domain.dto.AsyncTaskGrouSaveDTO;
import com.iwindplus.base.async.task.domain.dto.AsyncTaskGroupSearchDTO;
import com.iwindplus.base.async.task.domain.dto.AsyncTaskSaveDTO;
import com.iwindplus.base.async.task.domain.dto.AsyncTaskSearchDTO;
import com.iwindplus.base.async.task.domain.dto.AsyncTaskShardSearchDTO;
import com.iwindplus.base.async.task.domain.dto.AsyncTaskStatusEditDTO;
import com.iwindplus.base.async.task.domain.vo.AsyncTaskGroupVO;
import com.iwindplus.base.async.task.domain.vo.AsyncTaskPageVO;
import com.iwindplus.base.async.task.domain.vo.AsyncTaskVO;
import java.util.List;

/**
 * 异步任务业务层接口类.
 *
 * @author zengdegui
 * @since 2025/9/14
 */
public interface AsyncTaskService {

    /**
     * 添加.
     *
     * @param entity 对象
     * @return AsyncTaskVO
     */
    AsyncTaskVO save(AsyncTaskSaveDTO entity);

    /**
     * 添加组任务.
     *
     * @param entity 对象
     * @return AsyncTaskVO
     */
    AsyncTaskVO saveGroup(AsyncTaskGrouSaveDTO entity);

    /**
     * 删除.
     *
     * @param id      主键
     * @param deleted 是否真删
     * @return boolean
     */
    boolean removeById(Long id, boolean deleted);

    /**
     * 批量删除.
     *
     * @param ids     主键集合
     * @param deleted 是否真删
     * @return boolean
     */
    boolean removeByIds(List<Long> ids, boolean deleted);

    /**
     * 通过业务流水号删除.
     *
     * @param bizNumber 业务流水号
     * @param deleted   是否真删
     * @return boolean
     */
    boolean removeByBizNumber(String bizNumber, boolean deleted);

    /**
     * 编辑.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean edit(AsyncTaskEditDTO entity);

    /**
     * 批量更新.
     *
     * @param entities  对象集合
     * @param batchSize 批次大小
     * @return boolean
     */
    boolean editBatch(List<AsyncTaskEditDTO> entities, int batchSize);

    /**
     * 批量更新.
     *
     * @param entities 对象集合
     * @return boolean
     */
    default boolean editBatch(List<AsyncTaskEditDTO> entities) {
        return this.editBatch(entities, Constants.DEFAULT_BATCH_SIZE);
    }

    /**
     * 回调批量更新：写入主任务预存结果与子任务回调数据.
     *
     * @param task     主任务更新对象（null时跳过）
     * @param subTasks 子任务更新对象列表（空时跳过）
     * @return boolean
     */
    boolean editCallbackBatch(AsyncTaskDO task, List<AsyncTaskSubDO> subTasks);

    /**
     * 通过主键修改状态.
     *
     * @param entity 状态流转对象（空字段不更新）
     * @return boolean
     */
    boolean editStatusById(AsyncTaskStatusEditDTO entity);

    /**
     * 列表.
     *
     * @param entity 对象
     * @return IPage<AsyncTaskPageVO>
     */
    IPage<AsyncTaskPageVO> page(AsyncTaskSearchDTO entity);

    /**
     * 通过主键查找.
     *
     * @param id 主键
     * @return AsyncTaskVO
     */
    AsyncTaskVO getDetail(Long id);

    /**
     * 通过业务流水号查找.
     *
     * @param bizNumber 业务流水号
     * @return AsyncTaskVO
     */
    AsyncTaskVO getDetailByBizNumber(String bizNumber);

    /**
     * 通过条件查找.
     *
     * @param entity 对象
     * @return AsyncTaskGroupVO
     */
    AsyncTaskGroupVO getGroupDetail(AsyncTaskGroupSearchDTO entity);

    /**
     * 获取每页条数.
     *
     * @return Integer
     */
    Integer getSize();

    /**
     * 分片查询.
     *
     * @param entity 搜索条件
     * @return List<AsyncTaskVO>
     */
    List<AsyncTaskVO> listByShard(AsyncTaskShardSearchDTO entity);
}

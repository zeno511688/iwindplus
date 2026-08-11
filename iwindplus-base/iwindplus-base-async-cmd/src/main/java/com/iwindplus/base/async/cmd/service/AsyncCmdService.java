/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdEditDTO;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdGrouSaveDTO;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdGroupSearchDTO;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdSaveDTO;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdSearchDTO;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdShardSearchDTO;
import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdStatusEnum;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdGroupVO;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdPageVO;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdVO;
import java.util.List;

/**
 * 异步命令业务层接口类.
 *
 * @author zengdegui
 * @since 2025/9/14
 */
public interface AsyncCmdService {

    /**
     * 添加.
     *
     * @param entity 对象
     * @return AsyncCmdVO
     */
    AsyncCmdVO save(AsyncCmdSaveDTO entity);

    /**
     * 添加组任务.
     *
     * @param entity 对象
     * @return AsyncCmdVO
     */
    AsyncCmdVO saveGroup(AsyncCmdGrouSaveDTO entity);

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
    boolean edit(AsyncCmdEditDTO entity);

    /**
     * 批量更新.
     *
     * @param entities  对象集合
     * @param batchSize 批次大小
     * @return boolean
     */
    boolean editBatch(List<AsyncCmdEditDTO> entities, int batchSize);

    /**
     * 批量更新.
     *
     * @param entities 对象集合
     * @return boolean
     */
    default boolean editBatch(List<AsyncCmdEditDTO> entities) {
        return this.editBatch(entities, Constants.DEFAULT_BATCH_SIZE);
    }

    /**
     * 通过主键修改状态.
     *
     * @param id                 主键
     * @param from               从状态
     * @param to                 到状态
     * @param costTime           耗时
     * @param errorMsg           错误信息
     * @param retryCount         重试次数
     * @param nextRetryTime      下一次重试时间
     * @param expireTime         续约时间
     * @param callbackExpireTime 等待异步结果的截止时间
     * @return boolean
     */
    boolean editStatusById(Long id, AsyncCmdStatusEnum from, AsyncCmdStatusEnum to,
        Long costTime, String errorMsg, Integer retryCount,
        Long nextRetryTime, Long expireTime, Long callbackExpireTime);

    /**
     * 续订租期时间.
     *
     * @param id 主键
     * @return boolean
     */
    boolean editExpireTime(Long id);

    /**
     * 列表.
     *
     * @param entity 对象
     * @return IPage<AsyncCmdPageVO>
     */
    IPage<AsyncCmdPageVO> page(AsyncCmdSearchDTO entity);

    /**
     * 通过主键查找.
     *
     * @param id 主键
     * @return AsyncCmdVO
     */
    AsyncCmdVO getDetail(Long id);

    /**
     * 通过业务流水号查找.
     *
     * @param bizNumber 业务流水号
     * @return AsyncCmdVO
     */
    AsyncCmdVO getDetailByBizNumber(String bizNumber);

    /**
     * 通过条件查找.
     *
     * @param entity 对象
     * @return AsyncCmdGroupVO
     */
    AsyncCmdGroupVO getGroupDetail(AsyncCmdGroupSearchDTO entity);

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
     * @return List<AsyncCmdVO>
     */
    List<AsyncCmdVO> listByShard(AsyncCmdShardSearchDTO entity);
}

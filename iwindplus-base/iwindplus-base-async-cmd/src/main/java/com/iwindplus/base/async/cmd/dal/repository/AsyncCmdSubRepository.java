/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */


package com.iwindplus.base.async.cmd.dal.repository;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.repository.CrudRepository;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.iwindplus.base.async.cmd.dal.mapper.AsyncCmdSubMapper;
import com.iwindplus.base.async.cmd.dal.model.AsyncCmdSubDO;
import com.iwindplus.base.async.cmd.dal.model.AsyncCmdSubDO.AsyncCmdSubDOBuilder;
import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdStatusEnum;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

/**
 * 异步命令子任务聚合层接口类.
 *
 * @author zengdegui
 * @since 2025/9/14
 */
public class AsyncCmdSubRepository extends CrudRepository<AsyncCmdSubMapper, AsyncCmdSubDO> {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(AsyncCmdSubDO entity) {
        return super.updateById(entity);
    }

    /**
     * 通过主键修改状态.
     *
     * @param id 主键
     * @param to 到状态
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatusById(Long id, AsyncCmdStatusEnum to) {
        return updateStatusById(id, null, to);
    }

    /**
     * 通过主键修改状态.
     *
     * @param id   主键
     * @param from 从状态
     * @param to   到状态
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatusById(Long id, AsyncCmdStatusEnum from, AsyncCmdStatusEnum to) {
        return updateStatusById(id, from, to, null, null);
    }

    /**
     * 通过主键修改状态.
     *
     * @param id         主键
     * @param from       从状态
     * @param to         到状态
     * @param errorMsg   错误信息
     * @param retryCount 重试次数
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatusById(Long id, AsyncCmdStatusEnum from, AsyncCmdStatusEnum to,
        String errorMsg, Integer retryCount) {
        final AsyncCmdSubDOBuilder<?, ?> builder = AsyncCmdSubDO.builder()
            .status(to)
            .modifiedTime(LocalDateTime.now())
            .modifiedTimestamp(System.currentTimeMillis());
        if (CharSequenceUtil.isNotBlank(errorMsg)) {
            builder.errorMsg(errorMsg);
        }
        if (retryCount != null) {
            builder.retryCount(retryCount);
        }
        AsyncCmdSubDO update = builder.build();

        final LambdaUpdateWrapper<AsyncCmdSubDO> updateWrapper = Wrappers.<AsyncCmdSubDO>lambdaUpdate()
            .eq(AsyncCmdSubDO::getId, id);
        if (from != null) {
            updateWrapper.eq(AsyncCmdSubDO::getStatus, from);
        }

        return super.update(update, updateWrapper);
    }

    /**
     * 通过异步命令主键获取子任务数量.
     *
     * @param asyncCmdId 异步命令主键
     * @param notStatus  不等于的状态
     * @return long
     */
    public long countByAsyncCmdId(Long asyncCmdId, AsyncCmdStatusEnum notStatus) {
        final LambdaQueryWrapper<AsyncCmdSubDO> queryWrapper = Wrappers.<AsyncCmdSubDO>lambdaQuery()
            .eq(AsyncCmdSubDO::getAsyncCmdId, asyncCmdId);
        if (notStatus != null) {
            queryWrapper.ne(AsyncCmdSubDO::getStatus, notStatus);
        }
        return super.count(queryWrapper);
    }

    /**
     * 通过异步命令主键获取子任务列表（按排序号升序）.
     *
     * @param asyncCmdId 异步命令主键
     * @param statusList 状态列表
     * @return List<AsyncCmdSubDO>
     */
    public List<AsyncCmdSubDO> listByAsyncCmdId(Long asyncCmdId, List<AsyncCmdStatusEnum> statusList) {
        final LambdaQueryWrapper<AsyncCmdSubDO> queryWrapper = Wrappers.<AsyncCmdSubDO>lambdaQuery()
            .eq(AsyncCmdSubDO::getAsyncCmdId, asyncCmdId)
            .orderByAsc(AsyncCmdSubDO::getSeq)
            .orderByAsc(AsyncCmdSubDO::getId);
        if (CollUtil.isNotEmpty(statusList)) {
            queryWrapper.in(AsyncCmdSubDO::getStatus, statusList);
        }
        return super.list(queryWrapper);
    }

    /**
     * 判断异步命令子任务是否已存在.
     *
     * @param asyncCmdId 异步命令主键
     */
    public void getAsyncCmdSubIsExist(Long asyncCmdId) {
        boolean result = SqlHelper.retBool(super.count(Wrappers.lambdaQuery(AsyncCmdSubDO.class)
            .eq(AsyncCmdSubDO::getAsyncCmdId, asyncCmdId)));
        if (Boolean.TRUE.equals(result)) {
            throw new BizException(BizCodeEnum.DATA_EXIST);
        }
    }
}

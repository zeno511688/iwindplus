/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */


package com.iwindplus.base.async.cmd.dal.repository;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
        return updateStatusById(id, null, to, null, null);
    }

    /**
     * 通过主键修改状态.
     *
     * @param id       主键
     * @param from     从状态
     * @param to       到状态
     * @param costTime 耗时
     * @param result   结果
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatusById(Long id, AsyncCmdStatusEnum from, AsyncCmdStatusEnum to, Long costTime, Map<String, Object> result) {
        return updateStatusById(id, from, to, costTime, null, null, result, null);
    }

    /**
     * 通过主键修改状态.
     *
     * @param id                 主键
     * @param from               从状态
     * @param to                 到状态
     * @param costTime           耗时
     * @param result             结果
     * @param callbackExpireTime 等待异步结果的截止时间
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatusById(Long id, AsyncCmdStatusEnum from, AsyncCmdStatusEnum to, Long costTime, Map<String, Object> result,
        Long callbackExpireTime) {
        return updateStatusById(id, from, to, costTime, null, null, result, callbackExpireTime);
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
     * @param result             结果
     * @param callbackExpireTime 等待异步结果的截止时间
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatusById(Long id, AsyncCmdStatusEnum from, AsyncCmdStatusEnum to,
        Long costTime, String errorMsg, Integer retryCount, Map<String, Object> result, Long callbackExpireTime) {
        final AsyncCmdSubDOBuilder<?, ?> builder = AsyncCmdSubDO.builder()
            .status(to)
            .modifiedTimestamp(System.currentTimeMillis());
        if (Objects.nonNull(costTime)) {
            builder.costTime(costTime);
        }
        if (CharSequenceUtil.isNotBlank(errorMsg)) {
            builder.errorMsg(errorMsg);
        }
        if (retryCount != null) {
            builder.retryCount(retryCount);
        }
        if (MapUtil.isNotEmpty(result)) {
            builder.result(result);
        }
        if (Objects.nonNull(callbackExpireTime)) {
            builder.callbackExpireTime(callbackExpireTime);
        }

        final LambdaUpdateWrapper<AsyncCmdSubDO> updateWrapper = Wrappers.<AsyncCmdSubDO>lambdaUpdate()
            .eq(AsyncCmdSubDO::getId, id);
        if (from != null) {
            updateWrapper.eq(AsyncCmdSubDO::getStatus, from);
        }

        return super.update(builder.build(), updateWrapper);
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
     * @param asyncCmdId    异步命令主键
     * @param statusList    状态列表
     * @param showTextField 是否获取text字段
     * @return List<AsyncCmdSubDO>
     */
    public List<AsyncCmdSubDO> listByAsyncCmdId(Long asyncCmdId, List<AsyncCmdStatusEnum> statusList, Boolean showTextField) {
        final LambdaQueryWrapper<AsyncCmdSubDO> queryWrapper = Wrappers.<AsyncCmdSubDO>lambdaQuery()
            .eq(AsyncCmdSubDO::getAsyncCmdId, asyncCmdId)
            .orderByAsc(AsyncCmdSubDO::getSeq)
            .orderByAsc(AsyncCmdSubDO::getId);
        if (CollUtil.isNotEmpty(statusList)) {
            queryWrapper.in(AsyncCmdSubDO::getStatus, statusList);
        }

        if (Boolean.FALSE.equals(showTextField)) {
            queryWrapper.select(AsyncCmdSubDO::getId, AsyncCmdSubDO::getCreatedTimestamp, AsyncCmdSubDO::getCreatedBy,
                AsyncCmdSubDO::getModifiedTimestamp, AsyncCmdSubDO::getModifiedBy, AsyncCmdSubDO::getVersion,
                AsyncCmdSubDO::getStatus, AsyncCmdSubDO::getBizType, AsyncCmdSubDO::getStage, AsyncCmdSubDO::getSeq,
                AsyncCmdSubDO::getRetryCount, AsyncCmdSubDO::getCostTime, AsyncCmdSubDO::getAsyncCmdId, AsyncCmdSubDO::getRemark
            );
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

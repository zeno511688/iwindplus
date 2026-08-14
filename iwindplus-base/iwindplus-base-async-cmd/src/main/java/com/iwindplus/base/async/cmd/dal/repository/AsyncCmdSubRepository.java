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
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdStatusEditDTO;
import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdStatusEnum;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import java.util.List;
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
     * @param entity 状态流转对象（空字段不更新）
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatusById(AsyncCmdStatusEditDTO entity) {
        final AsyncCmdSubDOBuilder<?, ?> builder = AsyncCmdSubDO.builder()
            .modifiedTimestamp(System.currentTimeMillis());
        if (Objects.nonNull(entity.getTo())) {
            builder.status(entity.getTo());
        }
        if (Objects.nonNull(entity.getCostTime())) {
            builder.costTime(entity.getCostTime());
        }
        if (CharSequenceUtil.isNotBlank(entity.getErrorMsg())) {
            builder.errorMsg(entity.getErrorMsg());
        }
        if (entity.getRetryCount() != null) {
            builder.retryCount(entity.getRetryCount());
        }
        if (Objects.nonNull(entity.getResult())) {
            builder.result(entity.getResult());
        }
        if (entity.getExpireTime() != null) {
            builder.expireTime(entity.getExpireTime());
        }

        final LambdaUpdateWrapper<AsyncCmdSubDO> updateWrapper = Wrappers.<AsyncCmdSubDO>lambdaUpdate()
            .eq(AsyncCmdSubDO::getId, entity.getId());
        if (entity.getFrom() != null) {
            updateWrapper.eq(AsyncCmdSubDO::getStatus, entity.getFrom());
        }

        return super.update(builder.build(), updateWrapper);
    }

    /**
     * 通过主键列表批量修改状态（单条SQL，带from状态CAS前置条件，仅流转status字段）.
     *
     * @param ids    主键列表
     * @param entity 状态流转对象（仅使用from/to字段）
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatusByIds(List<Long> ids, AsyncCmdStatusEditDTO entity) {
        if (CollUtil.isEmpty(ids) || Objects.isNull(entity.getTo())) {
            return false;
        }

        final AsyncCmdSubDO data = AsyncCmdSubDO.builder()
            .status(entity.getTo())
            .modifiedTimestamp(System.currentTimeMillis())
            .build();

        final LambdaUpdateWrapper<AsyncCmdSubDO> updateWrapper = Wrappers.<AsyncCmdSubDO>lambdaUpdate()
            .in(AsyncCmdSubDO::getId, ids);
        if (entity.getFrom() != null) {
            updateWrapper.eq(AsyncCmdSubDO::getStatus, entity.getFrom());
        }

        return super.update(data, updateWrapper);
    }

    /**
     * 通过业务流水号查找子任务（取最新一条）.
     *
     * @param bizNumber 业务流水号
     * @return AsyncCmdSubDO
     */
    public AsyncCmdSubDO getByBizNumber(String bizNumber) {
        return super.getOne(Wrappers.<AsyncCmdSubDO>lambdaQuery()
            .eq(AsyncCmdSubDO::getBizNumber, bizNumber)
            .orderByDesc(AsyncCmdSubDO::getId)
            .last("LIMIT 1"));
    }

    /**
     * 通过异步命令主键和业务键查找子任务（取最新一条）.
     *
     * @param asyncCmdId 异步命令主键
     * @param bizKey     业务键
     * @return AsyncCmdSubDO
     */
    public AsyncCmdSubDO getByAsyncCmdId(Long asyncCmdId, String bizKey) {
        return super.getOne(Wrappers.<AsyncCmdSubDO>lambdaQuery()
            .eq(AsyncCmdSubDO::getAsyncCmdId, asyncCmdId)
            .eq(AsyncCmdSubDO::getBizKey, bizKey)
            .orderByDesc(AsyncCmdSubDO::getId)
            .last("LIMIT 1"));
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
                AsyncCmdSubDO::getStatus, AsyncCmdSubDO::getBizType, AsyncCmdSubDO::getBizName, AsyncCmdSubDO::getBizKey,
                AsyncCmdSubDO::getStage, AsyncCmdSubDO::getSeq,
                AsyncCmdSubDO::getRetryCount, AsyncCmdSubDO::getCostTime, AsyncCmdSubDO::getExpireTime,
                AsyncCmdSubDO::getAsyncCmdId, AsyncCmdSubDO::getRemark
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

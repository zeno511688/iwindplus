/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.dal.repository;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.repository.CrudRepository;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.iwindplus.base.async.cmd.dal.mapper.AsyncCmdMapper;
import com.iwindplus.base.async.cmd.dal.model.AsyncCmdDO;
import com.iwindplus.base.async.cmd.dal.model.AsyncCmdDO.AsyncCmdDOBuilder;
import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdStatusEnum;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

/**
 * 异步命令聚合层接口类.
 *
 * @author zengdegui
 * @since 2023/9/1
 */
public class AsyncCmdRepository extends CrudRepository<AsyncCmdMapper, AsyncCmdDO> {

    /**
     * 保存.
     *
     * @param entity 对象
     * @return boolean
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(AsyncCmdDO entity) {
        return super.save(entity);
    }

    /**
     * 通过环境和业务流水号删除.
     *
     * @param env       环境
     * @param bizType   业务类型
     * @param eventType 事件类型
     * @param bizNumber 业务流水号
     * @param deleted   是否真删
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByCondition(String env, String bizType,
        String eventType, String bizNumber, boolean deleted) {
        // 真实删除
        if (Boolean.TRUE.equals(deleted)) {
            return super.baseMapper.deleteByCondition(env, bizType, eventType, bizNumber) > 0;
        }
        return super.remove(Wrappers.lambdaQuery(AsyncCmdDO.class)
            .eq(AsyncCmdDO::getEnv, env.trim())
            .eq(AsyncCmdDO::getBizType, bizType.trim())
            .eq(AsyncCmdDO::getEventType, eventType.trim())
            .eq(AsyncCmdDO::getBizNumber, bizNumber.trim()));
    }

    /**
     * 批量真实删除.
     *
     * @param ids     主键集合
     * @param deleted 是否真删
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByIds(List<Long> ids, boolean deleted) {
        // 真实删除
        if (Boolean.TRUE.equals(deleted)) {
            return super.baseMapper.deleteDataByIds(ids) > 0;
        }
        return super.removeByIds(ids);
    }

    /**
     * 真实删除.
     *
     * @param id      主键
     * @param deleted 是否真删
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteById(Long id, boolean deleted) {
        // 真实删除
        if (Boolean.TRUE.equals(deleted)) {
            return super.baseMapper.deleteDataById(id) > 0;
        }
        return super.removeById(id);
    }

    /**
     * 修改.
     *
     * @param entity 对象
     * @return boolean
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(AsyncCmdDO entity) {
        return super.updateById(entity);
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
        AsyncCmdDO update = AsyncCmdDO
            .builder()
            .status(to)
            .modifiedTime(LocalDateTime.now())
            .modifiedTimestamp(System.currentTimeMillis())
            .build();

        final LambdaUpdateWrapper<AsyncCmdDO> updateWrapper = Wrappers.<AsyncCmdDO>lambdaUpdate()
            .eq(AsyncCmdDO::getId, id)
            .eq(AsyncCmdDO::getStatus, from);

        return super.update(update, updateWrapper);
    }

    /**
     * 通过主键修改状态.
     *
     * @param id            主键
     * @param from          从状态
     * @param to            到状态
     * @param errorMsg      错误信息
     * @param retryCount    重试次数
     * @param nextRetryTime 下次重试时间
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatusById(Long id, AsyncCmdStatusEnum from, AsyncCmdStatusEnum to
        , String errorMsg, Integer retryCount, LocalDateTime nextRetryTime) {
        final AsyncCmdDOBuilder<?, ?> builder = AsyncCmdDO.builder()
            .status(to)
            .modifiedTime(LocalDateTime.now())
            .modifiedTimestamp(System.currentTimeMillis());
        if (CharSequenceUtil.isNotBlank(errorMsg)) {
            builder.errorMsg(errorMsg);
        }
        if (retryCount != null) {
            builder.retryCount(retryCount);
        }
        if (nextRetryTime != null) {
            builder.nextRetryTime(nextRetryTime);
        }
        AsyncCmdDO update = builder.build();

        final LambdaUpdateWrapper<AsyncCmdDO> updateWrapper = Wrappers.<AsyncCmdDO>lambdaUpdate()
            .eq(AsyncCmdDO::getId, id)
            .eq(AsyncCmdDO::getStatus, from);

        return super.update(update, updateWrapper);
    }

    /**
     * 获取业务流水号是否已存在.
     *
     * @param bizNumber 业务流水号
     */
    public void getBizNumberIsExist(String bizNumber) {
        boolean result = SqlHelper.retBool(super.count(Wrappers.lambdaQuery(AsyncCmdDO.class)
            .eq(AsyncCmdDO::getBizNumber, bizNumber)));
        if (Boolean.TRUE.equals(result)) {
            throw new BizException(BizCodeEnum.BIZ_NUMBER_EXIST, new Object[]{bizNumber});
        }
    }
}

/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */


package com.iwindplus.base.async.task.dal.repository;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.repository.CrudRepository;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.iwindplus.base.async.task.dal.mapper.AsyncTaskSubMapper;
import com.iwindplus.base.async.task.dal.model.AsyncTaskSubDO;
import com.iwindplus.base.async.task.dal.model.AsyncTaskSubDO.AsyncTaskSubDOBuilder;
import com.iwindplus.base.async.task.domain.dto.AsyncTaskStatusEditDTO;
import com.iwindplus.base.async.task.domain.enums.AsyncTaskStatusEnum;
import com.iwindplus.base.async.task.domain.property.AsyncTaskProperty;
import com.iwindplus.base.async.task.domain.vo.AsyncTaskSubVO;
import com.iwindplus.base.domain.constant.CommonConstant.NumberConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

/**
 * 异步任务子任务聚合层接口类.
 *
 * @author zengdegui
 * @since 2025/9/14
 */
@RequiredArgsConstructor
public class AsyncTaskSubRepository extends CrudRepository<AsyncTaskSubMapper, AsyncTaskSubDO> {

    private final AsyncTaskProperty property;

    /**
     * 批量保存.
     *
     * @param entities 实体对象列表
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public List<AsyncTaskSubVO> saveBatch(List<AsyncTaskSubDO> entities) {
        entities.forEach(entity -> {
            if (CharSequenceUtil.isBlank(entity.getBizNumber())) {
                entity.setBizNumber(IdWorker.getIdStr());
            }
        });
        super.saveBatch(entities, Constants.DEFAULT_BATCH_SIZE);
        return BeanUtil.copyToList(entities, AsyncTaskSubVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(AsyncTaskSubDO entity) {
        return super.updateById(entity);
    }

    /**
     * 通过主键修改状态.
     *
     * @param entity 状态流转对象（空字段不更新）
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatusById(AsyncTaskStatusEditDTO entity) {
        final AsyncTaskSubDOBuilder<?, ?> builder = AsyncTaskSubDO.builder()
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
        if (entity.getProgress() != null) {
            builder.progress(entity.getProgress());
        }

        final LambdaUpdateWrapper<AsyncTaskSubDO> updateWrapper = Wrappers.<AsyncTaskSubDO>lambdaUpdate()
            .eq(AsyncTaskSubDO::getId, entity.getId());
        if (entity.getFrom() != null) {
            updateWrapper.eq(AsyncTaskSubDO::getStatus, entity.getFrom());
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
    public boolean updateStatusByIds(List<Long> ids, AsyncTaskStatusEditDTO entity) {
        if (CollUtil.isEmpty(ids) || Objects.isNull(entity.getTo())) {
            return false;
        }

        final AsyncTaskSubDO data = AsyncTaskSubDO.builder()
            .status(entity.getTo())
            .modifiedTimestamp(System.currentTimeMillis())
            .build();

        final LambdaUpdateWrapper<AsyncTaskSubDO> updateWrapper = Wrappers.<AsyncTaskSubDO>lambdaUpdate()
            .in(AsyncTaskSubDO::getId, ids);
        if (entity.getFrom() != null) {
            updateWrapper.eq(AsyncTaskSubDO::getStatus, entity.getFrom());
        }

        return super.update(data, updateWrapper);
    }

    /**
     * 通过业务流水号查找子任务（取最新一条）.
     *
     * @param bizNumber 业务流水号
     * @return AsyncTaskSubDO
     */
    public AsyncTaskSubDO getByBizNumber(String bizNumber) {
        return super.getOne(Wrappers.<AsyncTaskSubDO>lambdaQuery()
            .eq(AsyncTaskSubDO::getBizNumber, bizNumber)
            .orderByDesc(AsyncTaskSubDO::getId)
            .last("LIMIT 1"));
    }

    /**
     * 通过异步任务主键和业务键查找子任务（取最新一条）.
     *
     * @param asyncTaskId 异步任务主键
     * @param bizKey     业务键
     * @return AsyncTaskSubDO
     */
    public AsyncTaskSubDO getByAsyncTaskId(Long asyncTaskId, String bizKey) {
        return super.getOne(Wrappers.<AsyncTaskSubDO>lambdaQuery()
            .eq(AsyncTaskSubDO::getAsyncTaskId, asyncTaskId)
            .eq(AsyncTaskSubDO::getBizKey, bizKey)
            .orderByDesc(AsyncTaskSubDO::getId)
            .last("LIMIT 1"));
    }

    /**
     * 通过异步任务主键获取子任务数量.
     *
     * @param asyncTaskId 异步任务主键
     * @param notStatus  不等于的状态
     * @return long
     */
    public long countByAsyncTaskId(Long asyncTaskId, AsyncTaskStatusEnum notStatus) {
        final LambdaQueryWrapper<AsyncTaskSubDO> queryWrapper = Wrappers.<AsyncTaskSubDO>lambdaQuery()
            .eq(AsyncTaskSubDO::getAsyncTaskId, asyncTaskId);
        if (notStatus != null) {
            queryWrapper.ne(AsyncTaskSubDO::getStatus, notStatus);
        }
        return super.count(queryWrapper);
    }

    /**
     * 通过异步任务主键获取仍在有效执行或等待的子任务数量. EXECUTE 状态按最近更新时间和执行超时时间判断，WAITING 状态按异步等待截止时间判断。
     *
     * @param asyncTaskId 异步任务主键
     * @return long
     */
    public long countNotTimeout(Long asyncTaskId) {
        final long currentTime = System.currentTimeMillis();
        final long executeExpireTime = currentTime
            - Optional.ofNullable(this.property.getTimeoutSeconds()).orElse(60L) * NumberConstant.NUMBER_ONE_THOUSAND;
        return super.count(Wrappers.<AsyncTaskSubDO>lambdaQuery()
            .eq(AsyncTaskSubDO::getAsyncTaskId, asyncTaskId)
            .and(wrapper -> wrapper
                .and(executeWrapper -> executeWrapper
                    .eq(AsyncTaskSubDO::getStatus, AsyncTaskStatusEnum.EXECUTING)
                    .gt(AsyncTaskSubDO::getModifiedTimestamp, executeExpireTime))
                .or(waitingWrapper -> waitingWrapper
                    .eq(AsyncTaskSubDO::getStatus, AsyncTaskStatusEnum.WAITING)
                    .gt(AsyncTaskSubDO::getExpireTime, currentTime))
            ));
    }

    /**
     * 通过异步任务主键获取子任务列表（按排序号升序）.
     *
     * @param asyncTaskId    异步任务主键
     * @param statusList    状态列表
     * @param showTextField 是否获取text字段
     * @return List<AsyncTaskSubDO>
     */
    public List<AsyncTaskSubDO> listByAsyncTaskId(Long asyncTaskId, List<AsyncTaskStatusEnum> statusList, Boolean showTextField) {
        return this.listByAsyncTaskId(asyncTaskId, statusList, showTextField, null);
    }

    /**
     * 通过异步任务主键获取子任务列表，并支持按展示标识过滤.
     *
     * @param asyncTaskId    异步任务主键
     * @param statusList    状态列表
     * @param showTextField 是否获取text字段
     * @param needDisplay   是否只获取需要展示的子任务
     * @return List<AsyncTaskSubDO>
     */
    public List<AsyncTaskSubDO> listByAsyncTaskId(
        Long asyncTaskId, List<AsyncTaskStatusEnum> statusList,
        Boolean showTextField, Boolean needDisplay) {
        final LambdaQueryWrapper<AsyncTaskSubDO> queryWrapper = Wrappers.<AsyncTaskSubDO>lambdaQuery()
            .eq(AsyncTaskSubDO::getAsyncTaskId, asyncTaskId)
            .orderByAsc(AsyncTaskSubDO::getSeq)
            .orderByAsc(AsyncTaskSubDO::getId);
        if (CollUtil.isNotEmpty(statusList)) {
            queryWrapper.in(AsyncTaskSubDO::getStatus, statusList);
        }
        if (Objects.nonNull(needDisplay)) {
            queryWrapper.eq(AsyncTaskSubDO::getNeedDisplay, needDisplay);
        }

        if (Boolean.FALSE.equals(showTextField)) {
            queryWrapper.select(AsyncTaskSubDO::getId, AsyncTaskSubDO::getCreatedTimestamp, AsyncTaskSubDO::getCreatedBy,
                AsyncTaskSubDO::getModifiedTimestamp, AsyncTaskSubDO::getModifiedBy, AsyncTaskSubDO::getVersion,
                AsyncTaskSubDO::getStatus, AsyncTaskSubDO::getBizType, AsyncTaskSubDO::getBizName, AsyncTaskSubDO::getBizKey,
                AsyncTaskSubDO::getStage, AsyncTaskSubDO::getSeq,
                AsyncTaskSubDO::getRetryCount, AsyncTaskSubDO::getCostTime, AsyncTaskSubDO::getExpireTime,
                AsyncTaskSubDO::getAsyncTaskId, AsyncTaskSubDO::getRemark
            );
        }

        return super.list(queryWrapper);
    }

    /**
     * 通过业务流水号列表批量查询子任务.
     *
     * @param bizNumbers 业务流水号列表
     * @return List<AsyncTaskSubDO>
     */
    public List<AsyncTaskSubDO> listByBizNumbers(List<String> bizNumbers) {
        if (CollUtil.isEmpty(bizNumbers)) {
            return List.of();
        }
        return super.list(Wrappers.<AsyncTaskSubDO>lambdaQuery()
            .in(AsyncTaskSubDO::getBizNumber, bizNumbers));
    }

    /**
     * 判断异步任务子任务是否已存在.
     *
     * @param asyncTaskId 异步任务主键
     */
    public void getAsyncTaskSubIsExist(Long asyncTaskId) {
        boolean result = SqlHelper.retBool(super.count(Wrappers.lambdaQuery(AsyncTaskSubDO.class)
            .eq(AsyncTaskSubDO::getAsyncTaskId, asyncTaskId)));
        if (Boolean.TRUE.equals(result)) {
            throw new BizException(BizCodeEnum.DATA_EXIST);
        }
    }

    /**
     * 获取下一次重试时间
     *
     * @param baseTimeMillis 基准时间戳(毫秒)
     * @return long
     */
    public Long getNextRetryTime(long baseTimeMillis) {
        return baseTimeMillis
            + Optional.ofNullable(this.property.getAsyncWaitPollSeconds()).orElse(60L) * NumberConstant.NUMBER_ONE_THOUSAND;
    }

    /**
     * 获取回调等待截止时间（主任务复用expireTime、子任务使用expireTime字段存储，作为回调超时兜底）.
     *
     * @param baseTimeMillis 基准时间戳(毫秒)
     * @return long
     */
    public Long getNextExpireTime(long baseTimeMillis) {
        return baseTimeMillis
            + Optional.ofNullable(this.property.getAsyncWaitTimeoutSeconds()).orElse(1800L) * NumberConstant.NUMBER_ONE_THOUSAND;
    }
}

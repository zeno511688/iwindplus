/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.task.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.base.async.task.dal.model.AsyncTaskDO;
import com.iwindplus.base.async.task.dal.model.AsyncTaskSubDO;
import com.iwindplus.base.async.task.dal.repository.AsyncTaskRepository;
import com.iwindplus.base.async.task.dal.repository.AsyncTaskSubRepository;
import com.iwindplus.base.async.task.domain.dto.AsyncTaskEditDTO;
import com.iwindplus.base.async.task.domain.dto.AsyncTaskGrouSaveDTO;
import com.iwindplus.base.async.task.domain.dto.AsyncTaskGroupSearchDTO;
import com.iwindplus.base.async.task.domain.dto.AsyncTaskSaveDTO;
import com.iwindplus.base.async.task.domain.dto.AsyncTaskSearchDTO;
import com.iwindplus.base.async.task.domain.dto.AsyncTaskShardSearchDTO;
import com.iwindplus.base.async.task.domain.dto.AsyncTaskStatusEditDTO;
import com.iwindplus.base.async.task.domain.property.AsyncTaskProperty;
import com.iwindplus.base.async.task.domain.vo.AsyncTaskGroupVO;
import com.iwindplus.base.async.task.domain.vo.AsyncTaskPageVO;
import com.iwindplus.base.async.task.domain.vo.AsyncTaskSubBaseVO;
import com.iwindplus.base.async.task.domain.vo.AsyncTaskVO;
import com.iwindplus.base.async.task.service.AsyncTaskService;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.util.TransactionUtil;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadPoolExecutor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 异步任务业务层接口实现类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public class AsyncTaskServiceImpl implements AsyncTaskService {

    private final AsyncTaskProperty property;
    private final AsyncTaskRepository asyncTaskRepository;
    private final AsyncTaskSubRepository asyncTaskSubRepository;
    private final ThreadPoolExecutor threadPoolExecutor;
    private final TransactionTemplate transactionTemplate;

    @Override
    public AsyncTaskVO save(AsyncTaskSaveDTO entity) {
        return this.asyncTaskRepository.save(entity);
    }

    @Override
    public AsyncTaskVO saveGroup(AsyncTaskGrouSaveDTO entity) {
        return TransactionUtil.executeInTransaction(this.transactionTemplate, () -> this.asyncTaskRepository.saveGroup(entity));
    }

    @Override
    public boolean removeById(Long id, boolean deleted) {
        return Boolean.TRUE.equals(TransactionUtil.executeInTransaction(this.transactionTemplate, () -> this.asyncTaskRepository.deleteById(id, deleted)));
    }

    @Override
    public boolean removeByIds(List<Long> ids, boolean deleted) {
        return Boolean.TRUE.equals(TransactionUtil.executeInTransaction(this.transactionTemplate, () -> this.asyncTaskRepository.deleteByIds(ids, deleted)));
    }

    @Override
    public boolean removeByBizNumber(String bizNumber, boolean deleted) {
        return Boolean.TRUE.equals(TransactionUtil.executeInTransaction(this.transactionTemplate, () ->
            this.asyncTaskRepository.deleteByBizNumber(SpringUtil.getActiveProfile(), bizNumber, deleted)
        ));
    }

    @Override
    public boolean edit(AsyncTaskEditDTO entity) {
        final AsyncTaskDO model = BeanUtil.copyProperties(entity, AsyncTaskDO.class);
        return this.asyncTaskRepository.updateById(model);
    }

    @Override
    public boolean editBatch(List<AsyncTaskEditDTO> entities, int batchSize) {
        if (CollUtil.isEmpty(entities)) {
            return Boolean.FALSE;
        }

        final List<AsyncTaskDO> doList = BeanUtil.copyToList(entities, AsyncTaskDO.class);
        return this.asyncTaskRepository.updateBatchById(doList, batchSize);
    }

    @Override
    public boolean editCallbackBatch(AsyncTaskDO task, List<AsyncTaskSubDO> subTasks) {
        return Boolean.TRUE.equals(TransactionUtil.executeInTransaction(this.transactionTemplate, () -> {
            boolean result = false;
            if (Objects.nonNull(task)) {
                task.setNextRetryTime(System.currentTimeMillis());
                result = this.asyncTaskRepository.updateById(task);
            }
            if (CollUtil.isNotEmpty(subTasks)) {
                result = this.asyncTaskSubRepository.updateBatchById(subTasks, 1000) || result;
            }
            return result;
        }));
    }

    @Override
    public boolean editStatusById(AsyncTaskStatusEditDTO entity) {
        return this.asyncTaskRepository.updateStatusById(entity);
    }

    @Override
    public IPage<AsyncTaskPageVO> page(AsyncTaskSearchDTO entity) {
        PageDTO<AsyncTaskDO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);

        LambdaQueryWrapper<AsyncTaskDO> queryWrapper = Wrappers.lambdaQuery(AsyncTaskDO.class)
            .orderByDesc(AsyncTaskDO::getModifiedTimestamp)
            .eq(AsyncTaskDO::getEnv, SpringUtil.getActiveProfile());

        // 添加查询条件
        this.addQueryConditions(queryWrapper, entity);
        showField(queryWrapper);

        final PageDTO<AsyncTaskDO> modelPage = this.asyncTaskRepository.page(page, queryWrapper);
        return modelPage.convert(model -> BeanUtil.copyProperties(model, AsyncTaskPageVO.class));
    }

    @Override
    public AsyncTaskVO getDetail(Long id) {
        AsyncTaskDO data = this.asyncTaskRepository.getById(id);
        if (Objects.isNull(data)) {
            return null;
        }
        return BeanUtil.copyProperties(data, AsyncTaskVO.class);
    }

    @Override
    public AsyncTaskVO getDetailByBizNumber(String bizNumber) {
        final AsyncTaskDO data = this.asyncTaskRepository.getOne(Wrappers.lambdaQuery(AsyncTaskDO.class)
            .eq(AsyncTaskDO::getEnv, SpringUtil.getActiveProfile())
            .eq(AsyncTaskDO::getBizNumber, bizNumber)
            .orderByDesc(AsyncTaskDO::getId)
            .last("LIMIT 1"));
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return BeanUtil.copyProperties(data, AsyncTaskVO.class);
    }

    @Override
    public AsyncTaskGroupVO getGroupDetail(AsyncTaskGroupSearchDTO entity) {
        AsyncTaskVO data = null;
        if (Objects.nonNull(entity.getId())) {
            data = this.getDetail(entity.getId());
        } else if (CharSequenceUtil.isNotBlank(entity.getBizNumber())) {
            data = this.getDetailByBizNumber(entity.getBizNumber());
        } else if (CharSequenceUtil.isNotBlank(entity.getBizKey())
            && CharSequenceUtil.isNotBlank(entity.getBizType())) {
            data = this.getDetailByBizKeyAndType(entity.getBizKey(), entity.getBizType());
        }

        if (Objects.nonNull(data) && Boolean.TRUE.equals(data.getNeedDisplay())) {
            final AsyncTaskGroupVO result = BeanUtil.copyProperties(data, AsyncTaskGroupVO.class);

            final List<AsyncTaskSubDO> subList = this.asyncTaskSubRepository.listByAsyncTaskId(
                data.getId(), null, Boolean.FALSE, Boolean.TRUE);
            if (CollUtil.isNotEmpty(subList)) {
                final List<AsyncTaskSubBaseVO> subTasks = BeanUtil.copyToList(subList, AsyncTaskSubBaseVO.class);
                result.setSubTasks(subTasks);
            }

            return result;
        }
        return null;
    }

    @Override
    public Integer getSize() {
        int activeCount = threadPoolExecutor.getActiveCount();
        int maxPoolSize = threadPoolExecutor.getMaximumPoolSize();
        final int queueSize = threadPoolExecutor.getQueue().size();
        int available = maxPoolSize - activeCount - queueSize;
        return Math.max(0, Math.min(this.property.getMaxPageSize(), available));
    }

    @Override
    public List<AsyncTaskVO> listByShard(AsyncTaskShardSearchDTO entity) {
        LambdaQueryWrapper<AsyncTaskDO> queryWrapper = Wrappers.lambdaQuery(AsyncTaskDO.class)
            .eq(AsyncTaskDO::getEnv, SpringUtil.getActiveProfile())
            .gt(AsyncTaskDO::getId, Objects.isNull(entity.getLastId()) ? 0L : entity.getLastId())
            .orderByAsc(AsyncTaskDO::getId)
            .last("LIMIT " + entity.getSize());

        final Integer shardTotal = entity.getShardTotal();
        if (Objects.nonNull(shardTotal) && shardTotal > 1) {
            final int shardIndex = Objects.isNull(entity.getShardIndex()) ? 0 : entity.getShardIndex();
            // 使用范围分片，可以利用主键索引提升性能
            // 每个分片处理 ID 范围为 [minId, maxId) 的数据
            long minId = Objects.isNull(entity.getLastId()) ? 0L : entity.getLastId();
            long shardMinId = minId + shardIndex * entity.getSize();
            long shardMaxId = shardMinId + entity.getSize();
            queryWrapper.ge(AsyncTaskDO::getId, shardMinId)
                .lt(AsyncTaskDO::getId, shardMaxId);
        }
        if (Objects.nonNull(entity.getStatus())) {
            queryWrapper.eq(AsyncTaskDO::getStatus, entity.getStatus());
        }
        if (CollUtil.isNotEmpty(entity.getStatusList())) {
            queryWrapper.in(AsyncTaskDO::getStatus, entity.getStatusList());
        }

        final List<AsyncTaskDO> list = this.asyncTaskRepository.list(queryWrapper);
        if (CollUtil.isEmpty(list)) {
            return null;
        }

        return BeanUtil.copyToList(list, AsyncTaskVO.class);
    }

    private AsyncTaskVO getDetailByBizKeyAndType(String bizKey, String bizType) {
        final AsyncTaskDO data = this.asyncTaskRepository.getOne(Wrappers.lambdaQuery(AsyncTaskDO.class)
            .eq(AsyncTaskDO::getEnv, SpringUtil.getActiveProfile())
            .eq(AsyncTaskDO::getBizKey, bizKey)
            .eq(AsyncTaskDO::getBizType, bizType)
            .orderByDesc(AsyncTaskDO::getId)
            .last("LIMIT 1"));
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }

        return BeanUtil.copyProperties(data, AsyncTaskVO.class);
    }

    private void showField(LambdaQueryWrapper<AsyncTaskDO> queryWrapper) {
        queryWrapper.select(AsyncTaskDO::getId, AsyncTaskDO::getCreatedTimestamp, AsyncTaskDO::getCreatedBy,
            AsyncTaskDO::getModifiedTimestamp, AsyncTaskDO::getModifiedBy, AsyncTaskDO::getVersion,
            AsyncTaskDO::getStatus, AsyncTaskDO::getEnv, AsyncTaskDO::getBizName, AsyncTaskDO::getBizKey, AsyncTaskDO::getBizType,
            AsyncTaskDO::getExecuteName, AsyncTaskDO::getBizNumber, AsyncTaskDO::getExpireTime,
            AsyncTaskDO::getRetryCount, AsyncTaskDO::getNextRetryTime, AsyncTaskDO::getSubTaskCount, AsyncTaskDO::getCostTime,
            AsyncTaskDO::getRemark
        );
    }

    /**
     * 添加查询条件.
     *
     * @param queryWrapper 查询包装器
     * @param entity 查询参数
     */
    private void addQueryConditions(LambdaQueryWrapper<AsyncTaskDO> queryWrapper, AsyncTaskSearchDTO entity) {
        Optional.ofNullable(entity.getStatus()).ifPresent(status -> queryWrapper.eq(AsyncTaskDO::getStatus, status));
        Optional.ofNullable(entity.getStatusList())
            .filter(CollUtil::isNotEmpty)
            .ifPresent(statusList -> queryWrapper.in(AsyncTaskDO::getStatus, statusList));

        Optional.ofNullable(entity.getBizKey())
            .filter(CharSequenceUtil::isNotBlank)
            .ifPresent(bizKey -> queryWrapper.eq(AsyncTaskDO::getBizKey, bizKey.trim()));

        Optional.ofNullable(entity.getBizType())
            .filter(CharSequenceUtil::isNotBlank)
            .ifPresent(bizType -> queryWrapper.eq(AsyncTaskDO::getBizType, bizType.trim()));

        Optional.ofNullable(entity.getBizNumber())
            .filter(CharSequenceUtil::isNotBlank)
            .ifPresent(bizNumber -> queryWrapper.eq(AsyncTaskDO::getBizNumber, bizNumber.trim()));

        Optional.ofNullable(entity.getExecuteName())
            .filter(CharSequenceUtil::isNotBlank)
            .ifPresent(executeName -> queryWrapper.eq(AsyncTaskDO::getExecuteName, executeName.trim()));
    }
}

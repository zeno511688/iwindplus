/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.base.async.cmd.dal.model.AsyncCmdDO;
import com.iwindplus.base.async.cmd.dal.model.AsyncCmdSubDO;
import com.iwindplus.base.async.cmd.dal.repository.AsyncCmdRepository;
import com.iwindplus.base.async.cmd.dal.repository.AsyncCmdSubRepository;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdEditDTO;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdGrouSaveDTO;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdGroupSearchDTO;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdSaveDTO;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdSearchDTO;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdShardSearchDTO;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdStatusEditDTO;
import com.iwindplus.base.async.cmd.domain.property.AsyncCmdProperty;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdGroupVO;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdPageVO;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdSubBaseVO;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdVO;
import com.iwindplus.base.async.cmd.service.AsyncCmdService;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.util.TransactionUtil;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 异步命业务层接口实现类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public class AsyncCmdServiceImpl implements AsyncCmdService {

    private final AsyncCmdProperty property;
    private final AsyncCmdRepository asyncCmdRepository;
    private final AsyncCmdSubRepository asyncCmdSubRepository;
    private final DtpExecutor asyncCmdTaskExecutor;
    private final TransactionTemplate transactionTemplate;

    @Override
    public AsyncCmdVO save(AsyncCmdSaveDTO entity) {
        return this.asyncCmdRepository.save(entity);
    }

    @Override
    public AsyncCmdVO saveGroup(AsyncCmdGrouSaveDTO entity) {
        return TransactionUtil.executeInTransaction(this.transactionTemplate, () -> this.asyncCmdRepository.saveGroup(entity));
    }

    @Override
    public boolean removeById(Long id, boolean deleted) {
        return Boolean.TRUE.equals(TransactionUtil.executeInTransaction(this.transactionTemplate, () -> this.asyncCmdRepository.deleteById(id, deleted)));
    }

    @Override
    public boolean removeByIds(List<Long> ids, boolean deleted) {
        return Boolean.TRUE.equals(TransactionUtil.executeInTransaction(this.transactionTemplate, () -> this.asyncCmdRepository.deleteByIds(ids, deleted)));
    }

    @Override
    public boolean removeByBizNumber(String bizNumber, boolean deleted) {
        return Boolean.TRUE.equals(TransactionUtil.executeInTransaction(this.transactionTemplate, () ->
            this.asyncCmdRepository.deleteByBizNumber(SpringUtil.getActiveProfile(), bizNumber, deleted)
        ));
    }

    @Override
    public boolean edit(AsyncCmdEditDTO entity) {
        final AsyncCmdDO model = BeanUtil.copyProperties(entity, AsyncCmdDO.class);
        this.asyncCmdRepository.updateById(model);
        return Boolean.TRUE;
    }

    @Override
    public boolean editBatch(List<AsyncCmdEditDTO> entities, int batchSize) {
        if (CollUtil.isEmpty(entities)) {
            return Boolean.FALSE;
        }

        List<AsyncCmdDO> doList = BeanUtil.copyToList(entities, AsyncCmdDO.class);
        this.asyncCmdRepository.updateBatchById(doList, batchSize);
        return Boolean.TRUE;
    }

    @Override
    public boolean editCallbackBatch(AsyncCmdDO mainTask, List<AsyncCmdSubDO> subTasks) {
        return Boolean.TRUE.equals(TransactionUtil.executeInTransaction(this.transactionTemplate, () -> {
            boolean result = false;
            if (Objects.nonNull(mainTask)) {
                mainTask.setNextRetryTime(System.currentTimeMillis());
                result = this.asyncCmdRepository.updateById(mainTask);
            }
            if (CollUtil.isNotEmpty(subTasks)) {
                this.asyncCmdSubRepository.updateBatchById(subTasks, 1000);
                result = true;
            }
            return result;
        }));
    }

    @Override
    public boolean editStatusById(AsyncCmdStatusEditDTO entity) {
        return this.asyncCmdRepository.updateStatusById(entity);
    }

    @Override
    public IPage<AsyncCmdPageVO> page(AsyncCmdSearchDTO entity) {
        PageDTO<AsyncCmdDO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);

        LambdaQueryWrapper<AsyncCmdDO> queryWrapper = Wrappers.lambdaQuery(AsyncCmdDO.class)
            .orderByDesc(AsyncCmdDO::getModifiedTimestamp)
            .eq(AsyncCmdDO::getEnv, SpringUtil.getActiveProfile());

        // 添加查询条件
        this.addQueryConditions(queryWrapper, entity);
        showField(queryWrapper);

        final PageDTO<AsyncCmdDO> modelPage = this.asyncCmdRepository.page(page, queryWrapper);
        return modelPage.convert(model -> BeanUtil.copyProperties(model, AsyncCmdPageVO.class));
    }

    @Override
    public AsyncCmdVO getDetail(Long id) {
        AsyncCmdDO data = this.asyncCmdRepository.getById(id);
        if (Objects.isNull(data)) {
            return null;
        }
        return BeanUtil.copyProperties(data, AsyncCmdVO.class);
    }

    @Override
    public AsyncCmdVO getDetailByBizNumber(String bizNumber) {
        final AsyncCmdDO data = this.asyncCmdRepository.getOne(Wrappers.lambdaQuery(AsyncCmdDO.class)
            .eq(AsyncCmdDO::getEnv, SpringUtil.getActiveProfile())
            .eq(AsyncCmdDO::getBizNumber, bizNumber)
            .orderByDesc(AsyncCmdDO::getId)
            .last("LIMIT 1"));
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return BeanUtil.copyProperties(data, AsyncCmdVO.class);
    }

    @Override
    public AsyncCmdGroupVO getGroupDetail(AsyncCmdGroupSearchDTO entity) {
        AsyncCmdVO data = null;
        if (Objects.nonNull(entity.getId())) {
            data = this.getDetail(entity.getId());
        } else if (CharSequenceUtil.isNotBlank(entity.getBizNumber())) {
            data = this.getDetailByBizNumber(entity.getBizNumber());
        } else if (CharSequenceUtil.isNotBlank(entity.getBizKey())
            && CharSequenceUtil.isNotBlank(entity.getBizType())) {
            data = this.getDetailByBizKeyAndType(entity.getBizKey(), entity.getBizType());
        }

        if (Objects.nonNull(data)) {
            final AsyncCmdGroupVO result = BeanUtil.copyProperties(data, AsyncCmdGroupVO.class);

            final List<AsyncCmdSubDO> subList = this.asyncCmdSubRepository.listByAsyncCmdId(data.getId(), null, false);
            if (CollUtil.isNotEmpty(subList)) {
                final List<AsyncCmdSubBaseVO> subTasks = BeanUtil.copyToList(subList, AsyncCmdSubBaseVO.class);
                result.setSubTasks(subTasks);
            }

            return result;
        }
        return null;
    }

    @Override
    public Integer getSize() {
        int activeCount = asyncCmdTaskExecutor.getActiveCount();
        int maxPoolSize = asyncCmdTaskExecutor.getMaximumPoolSize();
        final int queueSize = asyncCmdTaskExecutor.getQueue().size();
        int available = maxPoolSize - activeCount - queueSize;
        return Math.max(0, Math.min(this.property.getMaxPageSize(), available));
    }

    @Override
    public List<AsyncCmdVO> listByShard(AsyncCmdShardSearchDTO entity) {
        LambdaQueryWrapper<AsyncCmdDO> queryWrapper = Wrappers.lambdaQuery(AsyncCmdDO.class)
            .eq(AsyncCmdDO::getEnv, SpringUtil.getActiveProfile())
            .gt(AsyncCmdDO::getId, Objects.isNull(entity.getLastId()) ? 0L : entity.getLastId())
            .orderByAsc(AsyncCmdDO::getId)
            .last("LIMIT " + entity.getSize());

        final Integer shardTotal = entity.getShardTotal();
        if (Objects.nonNull(shardTotal) && shardTotal > 1) {
            final int shardIndex = Objects.isNull(entity.getShardIndex()) ? 0 : entity.getShardIndex();
            // 使用范围分片，可以利用主键索引提升性能
            // 每个分片处理 ID 范围为 [minId, maxId) 的数据
            long minId = Objects.isNull(entity.getLastId()) ? 0L : entity.getLastId();
            long shardMinId = minId + shardIndex * entity.getSize();
            long shardMaxId = shardMinId + entity.getSize();
            queryWrapper.ge(AsyncCmdDO::getId, shardMinId)
                .lt(AsyncCmdDO::getId, shardMaxId);
        }
        if (Objects.nonNull(entity.getStatus())) {
            queryWrapper.eq(AsyncCmdDO::getStatus, entity.getStatus());
        }
        if (CollUtil.isNotEmpty(entity.getStatusList())) {
            queryWrapper.in(AsyncCmdDO::getStatus, entity.getStatusList());
        }
        if (Objects.nonNull(entity.getExpireTime())) {
            queryWrapper.le(AsyncCmdDO::getExpireTime, entity.getExpireTime());
        }
        if (Objects.nonNull(entity.getNextRetryTime())) {
            queryWrapper.le(AsyncCmdDO::getNextRetryTime, entity.getNextRetryTime());
        }

        final List<AsyncCmdDO> list = this.asyncCmdRepository.list(queryWrapper);
        if (CollUtil.isEmpty(list)) {
            return null;
        }

        return BeanUtil.copyToList(list, AsyncCmdVO.class);
    }

    private AsyncCmdVO getDetailByBizKeyAndType(String bizKey, String bizType) {
        final AsyncCmdDO data = this.asyncCmdRepository.getOne(Wrappers.lambdaQuery(AsyncCmdDO.class)
            .eq(AsyncCmdDO::getEnv, SpringUtil.getActiveProfile())
            .eq(AsyncCmdDO::getBizKey, bizKey)
            .eq(AsyncCmdDO::getBizType, bizType)
            .orderByDesc(AsyncCmdDO::getId)
            .last("LIMIT 1"));
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }

        return BeanUtil.copyProperties(data, AsyncCmdVO.class);
    }

    private void showField(LambdaQueryWrapper<AsyncCmdDO> queryWrapper) {
        queryWrapper.select(AsyncCmdDO::getId, AsyncCmdDO::getCreatedTimestamp, AsyncCmdDO::getCreatedBy,
            AsyncCmdDO::getModifiedTimestamp, AsyncCmdDO::getModifiedBy, AsyncCmdDO::getVersion,
            AsyncCmdDO::getStatus, AsyncCmdDO::getEnv, AsyncCmdDO::getBizName, AsyncCmdDO::getBizKey, AsyncCmdDO::getBizType,
            AsyncCmdDO::getExecuteName, AsyncCmdDO::getBizNumber, AsyncCmdDO::getExpireTime,
            AsyncCmdDO::getRetryCount, AsyncCmdDO::getNextRetryTime, AsyncCmdDO::getSubTaskCount, AsyncCmdDO::getCostTime,
            AsyncCmdDO::getRemark
        );
    }

    /**
     * 添加查询条件.
     *
     * @param queryWrapper 查询包装器
     * @param entity 查询参数
     */
    private void addQueryConditions(LambdaQueryWrapper<AsyncCmdDO> queryWrapper, AsyncCmdSearchDTO entity) {
        Optional.ofNullable(entity.getStatus()).ifPresent(status -> queryWrapper.eq(AsyncCmdDO::getStatus, status));
        Optional.ofNullable(entity.getStatusList())
            .filter(CollUtil::isNotEmpty)
            .ifPresent(statusList -> queryWrapper.in(AsyncCmdDO::getStatus, statusList));

        Optional.ofNullable(entity.getBizKey())
            .filter(CharSequenceUtil::isNotBlank)
            .ifPresent(bizKey -> queryWrapper.eq(AsyncCmdDO::getBizKey, bizKey.trim()));

        Optional.ofNullable(entity.getBizType())
            .filter(CharSequenceUtil::isNotBlank)
            .ifPresent(bizType -> queryWrapper.eq(AsyncCmdDO::getBizType, bizType.trim()));

        Optional.ofNullable(entity.getBizNumber())
            .filter(CharSequenceUtil::isNotBlank)
            .ifPresent(bizNumber -> queryWrapper.eq(AsyncCmdDO::getBizNumber, bizNumber.trim()));

        Optional.ofNullable(entity.getExecuteName())
            .filter(CharSequenceUtil::isNotBlank)
            .ifPresent(executeName -> queryWrapper.eq(AsyncCmdDO::getExecuteName, executeName.trim()));
    }
}

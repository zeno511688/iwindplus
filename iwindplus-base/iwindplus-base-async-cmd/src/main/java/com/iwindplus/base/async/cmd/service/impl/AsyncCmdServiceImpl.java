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
import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdStatusEnum;
import com.iwindplus.base.async.cmd.domain.property.AsyncCmdProperty;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdGroupVO;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdPageVO;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdSubVO;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdVO;
import com.iwindplus.base.async.cmd.service.AsyncCmdService;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
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
        return this.transactionTemplate.execute(status ->
            this.asyncCmdRepository.saveGroup(entity)
        );
    }

    @Override
    public boolean removeById(Long id, boolean deleted) {
        return Boolean.TRUE.equals(
            this.transactionTemplate.execute(status ->
                this.asyncCmdRepository.deleteById(id, deleted)
            )
        );
    }

    @Override
    public boolean removeByIds(List<Long> ids, boolean deleted) {
        return Boolean.TRUE.equals(
            this.transactionTemplate.execute(status ->
                this.asyncCmdRepository.deleteByIds(ids, deleted)
            )
        );
    }

    @Override
    public boolean removeByBizKeyAndType(String bizKey, String bizType, boolean deleted) {
        return Boolean.TRUE.equals(
            this.transactionTemplate.execute(status ->
                this.asyncCmdRepository.deleteByBizKeyAndType(
                    SpringUtil.getActiveProfile(), bizKey, bizType, deleted)
            )
        );
    }

    @Override
    public boolean removeByBizNumber(String bizNumber, boolean deleted) {
        return Boolean.TRUE.equals(
            this.transactionTemplate.execute(status ->
                this.asyncCmdRepository.deleteByBizNumber(
                    SpringUtil.getActiveProfile(), bizNumber, deleted)
            )
        );
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
    public boolean editStatusById(Long id, AsyncCmdStatusEnum from, AsyncCmdStatusEnum to,
        Long costTime) {
        return this.asyncCmdRepository.updateStatusById(id, from, to, costTime);
    }

    @Override
    public boolean editStatusById(Long id, AsyncCmdStatusEnum from, AsyncCmdStatusEnum to,
        Long costTime, boolean renewFlag) {
        return this.asyncCmdRepository.updateStatusById(id, from, to, costTime, renewFlag);
    }

    @Override
    public boolean editStatusById(Long id, AsyncCmdStatusEnum from, AsyncCmdStatusEnum to,
        Long costTime, String errorMsg, Integer retryCount, LocalDateTime nextRetryTime) {
        return this.asyncCmdRepository.updateStatusById(id, from, to, costTime, errorMsg, retryCount, nextRetryTime, false);
    }

    @Override
    public boolean editExpireTime(Long id) {
        return this.asyncCmdRepository.editExpireTime(id);
    }

    @Override
    public IPage<AsyncCmdPageVO> page(AsyncCmdSearchDTO entity) {
        PageDTO<AsyncCmdDO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        LambdaQueryWrapper<AsyncCmdDO> queryWrapper = Wrappers.lambdaQuery(AsyncCmdDO.class)
            .orderByAsc(AsyncCmdDO::getId)
            .eq(AsyncCmdDO::getEnv, SpringUtil.getActiveProfile());
        if (Objects.nonNull(entity.getStatus())) {
            queryWrapper.eq(AsyncCmdDO::getStatus, entity.getStatus());
        }
        if (CharSequenceUtil.isNotBlank(entity.getBizKey())) {
            queryWrapper.eq(AsyncCmdDO::getBizKey, entity.getBizKey().trim());
        }
        if (CharSequenceUtil.isNotBlank(entity.getBizType())) {
            queryWrapper.eq(AsyncCmdDO::getBizType, entity.getBizType().trim());
        }
        if (CharSequenceUtil.isNotBlank(entity.getBizNumber())) {
            queryWrapper.eq(AsyncCmdDO::getBizNumber, entity.getBizNumber().trim());
        }
        if (CollUtil.isNotEmpty(entity.getStatusList())) {
            queryWrapper.in(AsyncCmdDO::getStatus, entity.getStatusList());
        }
        if (CharSequenceUtil.isNotBlank(entity.getExecuteName())) {
            queryWrapper.eq(AsyncCmdDO::getExecuteName, entity.getExecuteName().trim());
        }

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
        final AsyncCmdVO data = Objects.nonNull(entity.getId())
            ? this.getDetail(entity.getId())
            : this.getDetailByBizNumber(entity.getBizNumber());

        if (Objects.nonNull(data)) {
            final AsyncCmdGroupVO result = BeanUtil.copyProperties(data, AsyncCmdGroupVO.class);

            final List<AsyncCmdSubDO> subList = this.asyncCmdSubRepository.listByAsyncCmdId(data.getId(), null);
            if (CollUtil.isNotEmpty(subList)) {
                final List<AsyncCmdSubVO> subTasks = BeanUtil.copyToList(subList, AsyncCmdSubVO.class);
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
        final Integer size = this.getSize();
        if (size == 0) {
            return null;
        }

        LambdaQueryWrapper<AsyncCmdDO> queryWrapper = Wrappers.lambdaQuery(AsyncCmdDO.class)
            .eq(AsyncCmdDO::getEnv, SpringUtil.getActiveProfile())
            .gt(AsyncCmdDO::getId, entity.getLastId())
            .apply("MOD(id, {0}) = {1}",
                entity.getShardTotal(),
                entity.getShardIndex()
            )
            .orderByAsc(AsyncCmdDO::getId)
            .last("LIMIT " + size);
        if (Objects.nonNull(entity.getStatus())) {
            queryWrapper.eq(AsyncCmdDO::getStatus, entity.getStatus());
        }
        if (CollUtil.isNotEmpty(entity.getStatusList())) {
            queryWrapper.in(AsyncCmdDO::getStatus, entity.getStatusList());
        }
        if (Objects.nonNull(entity.getExpireTime())) {
            queryWrapper.lt(AsyncCmdDO::getExpireTime, entity.getExpireTime());
        }
        if (Objects.nonNull(entity.getRetryTime())) {
            queryWrapper.le(AsyncCmdDO::getNextRetryTime, entity.getRetryTime());
        }

        showField(queryWrapper);

        final List<AsyncCmdDO> list = this.asyncCmdRepository.list(queryWrapper);
        if (CollUtil.isEmpty(list)) {
            return null;
        }

        return BeanUtil.copyToList(list, AsyncCmdVO.class);
    }

    private void showField(LambdaQueryWrapper<AsyncCmdDO> queryWrapper) {
        queryWrapper.select(AsyncCmdDO::getId, AsyncCmdDO::getCreatedTime, AsyncCmdDO::getCreatedTimestamp, AsyncCmdDO::getCreatedBy,
            AsyncCmdDO::getModifiedTime, AsyncCmdDO::getModifiedTimestamp, AsyncCmdDO::getModifiedBy, AsyncCmdDO::getVersion,
            AsyncCmdDO::getStatus, AsyncCmdDO::getEnv, AsyncCmdDO::getBizKey, AsyncCmdDO::getBizType, AsyncCmdDO::getExecuteName,
            AsyncCmdDO::getDispatchMode, AsyncCmdDO::getBizNumber, AsyncCmdDO::getExpireTime, AsyncCmdDO::getRetryCount,
            AsyncCmdDO::getNextRetryTime, AsyncCmdDO::getSubTaskCount, AsyncCmdDO::getCostTime, AsyncCmdDO::getRemark
        );
    }
}

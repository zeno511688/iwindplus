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
import cn.hutool.core.util.IdUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.base.async.cmd.dal.model.AsyncCmdDO;
import com.iwindplus.base.async.cmd.dal.repository.AsyncCmdRepository;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdEditDTO;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdSaveDTO;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdSearchDTO;
import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdStatusEnum;
import com.iwindplus.base.async.cmd.domain.enums.DispatchModeEnum;
import com.iwindplus.base.async.cmd.domain.property.AsyncCmdProperty;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdPageVO;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdVO;
import com.iwindplus.base.async.cmd.service.AsyncCmdService;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.util.DatesUtil;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.core.executor.DtpExecutor;

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
    private final DtpExecutor asyncCmdTaskExecutor;

    @Override
    public Long save(AsyncCmdSaveDTO entity) {
        entity.setEnv(SpringUtil.getActiveProfile());
        LocalDateTime expireTime = LocalDateTime.now().plusSeconds(Optional.ofNullable(this.property.getTimeoutSeconds()).orElse(60L));
        entity.setExpireTime(expireTime);
        entity.setNextRetryTime(LocalDateTime.now());
        if (Objects.isNull(entity.getDispatchMode())) {
            entity.setDispatchMode(DispatchModeEnum.ASYNC);
        }
        // 如果业务流水号为空，则生成一个
        if (CharSequenceUtil.isBlank(entity.getBizNumber())) {
            entity.setBizNumber(IdUtil.simpleUUID());
        }
        this.asyncCmdRepository.getBizNumberIsExist(entity.getBizNumber().trim());
        entity.setStatus(AsyncCmdStatusEnum.TO_BE_EXECUTE);
        final AsyncCmdDO model = BeanUtil.copyProperties(entity, AsyncCmdDO.class);
        this.asyncCmdRepository.save(model);
        entity.setId(model.getId());
        BeanUtil.copyProperties(model, entity);
        return entity.getId();
    }

    @Override
    public boolean removeById(Long id, boolean deleted) {
        this.asyncCmdRepository.deleteById(id, deleted);
        return Boolean.TRUE;
    }

    @Override
    public boolean removeByIds(List<Long> ids, boolean deleted) {
        this.asyncCmdRepository.deleteByIds(ids, deleted);
        return Boolean.TRUE;
    }

    @Override
    public boolean removeByCondition(String bizType,
        String eventType, String bizNumber, boolean deleted) {
        String env = SpringUtil.getActiveProfile();
        this.asyncCmdRepository.deleteByCondition(env, bizType, eventType, bizNumber, deleted);
        return Boolean.TRUE;
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
    public boolean editStatusById(Long id, AsyncCmdStatusEnum from, AsyncCmdStatusEnum to) {
        return this.asyncCmdRepository.updateStatusById(id, from, to);
    }

    @Override
    public boolean editStatusById(Long id, AsyncCmdStatusEnum from, AsyncCmdStatusEnum to, String errorMsg, Integer retryCount,
        LocalDateTime nextRetryTime) {
        return this.asyncCmdRepository.updateStatusById(id, from, to, errorMsg, retryCount, nextRetryTime);
    }

    @Override
    public IPage<AsyncCmdPageVO> page(AsyncCmdSearchDTO entity) {
        PageDTO<AsyncCmdDO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        LambdaQueryWrapper<AsyncCmdDO> queryWrapper = Wrappers.lambdaQuery(AsyncCmdDO.class)
            .orderByDesc(AsyncCmdDO::getModifiedTime)
            .eq(AsyncCmdDO::getEnv, SpringUtil.getActiveProfile());
        if (Objects.nonNull(entity.getStatus())) {
            queryWrapper.eq(AsyncCmdDO::getStatus, entity.getStatus());
        }
        if (CharSequenceUtil.isNotBlank(entity.getBizType())) {
            queryWrapper.eq(AsyncCmdDO::getBizType, entity.getBizType().trim());
        }
        if (CharSequenceUtil.isNotBlank(entity.getEventType())) {
            queryWrapper.eq(AsyncCmdDO::getEventType, entity.getEventType().trim());
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
        if (Objects.nonNull(entity.getExpireTime())) {
            queryWrapper.lt(AsyncCmdDO::getExpireTime, entity.getExpireTime());
        }
        if (Objects.nonNull(entity.getRetryTime())) {
            queryWrapper.le(AsyncCmdDO::getNextRetryTime, entity.getRetryTime());
        }
        if (Objects.isNull(entity.getShowContent()) || Boolean.FALSE.equals(entity.getShowContent())) {
            queryWrapper.select(AsyncCmdDO::getId, AsyncCmdDO::getCreatedTime, AsyncCmdDO::getCreatedTimestamp, AsyncCmdDO::getCreatedBy,
                AsyncCmdDO::getModifiedTime, AsyncCmdDO::getModifiedTimestamp, AsyncCmdDO::getModifiedBy, AsyncCmdDO::getVersion,
                AsyncCmdDO::getStatus, AsyncCmdDO::getEnv, AsyncCmdDO::getExecuteName, AsyncCmdDO::getDispatchMode, AsyncCmdDO::getBizNumber,
                AsyncCmdDO::getRetryCount, AsyncCmdDO::getNextRetryTime
            );
        }

        final PageDTO<AsyncCmdDO> modelPage = this.asyncCmdRepository.page(page, queryWrapper);
        return modelPage.convert(model -> BeanUtil.copyProperties(model, AsyncCmdPageVO.class));
    }

    @Override
    public AsyncCmdVO getDetail(Long id) {
        AsyncCmdDO data = this.asyncCmdRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return BeanUtil.copyProperties(data, AsyncCmdVO.class);
    }

    @Override
    public Integer getSize() {
        int activeCount = asyncCmdTaskExecutor.getActiveCount();
        int maxPoolSize = asyncCmdTaskExecutor.getMaximumPoolSize();
        int available = maxPoolSize - activeCount;
        return Math.min(this.property.getMaxPageSize(), available);
    }

    @Override
    public LocalDateTime getNextRetryTime(LocalDateTime baseTime, Integer retryCount) {
        final List<LocalDateTime> times = DatesUtil.convertFrequencyToLocalDateTime(
            baseTime, this.property.getRetry().getFrequency());
        if (CollUtil.isEmpty(times)) {
            return LocalDateTime.now().plusSeconds(5);
        }
        int index = retryCount != null ? Math.max(0, retryCount - 1) : 0;
        return times.get(Math.min(index, times.size() - 1));
    }

}

/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.task.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.iwindplus.base.async.task.dal.model.AsyncTaskSubDO;
import com.iwindplus.base.async.task.dal.repository.AsyncTaskSubRepository;
import com.iwindplus.base.async.task.domain.dto.AsyncTaskStatusEditDTO;
import com.iwindplus.base.async.task.domain.enums.AsyncTaskStatusEnum;
import com.iwindplus.base.async.task.domain.vo.AsyncTaskSubVO;
import com.iwindplus.base.async.task.service.AsyncTaskSubService;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 异步命子任务业务层接口实现类.
 *
 * @author zengdegui
 * @since 2026/08/02 13:49
 */
@Slf4j
@RequiredArgsConstructor
public class AsyncTaskSubServiceImpl implements AsyncTaskSubService {

    private final AsyncTaskSubRepository asyncTaskSubRepository;

    @Override
    public boolean editStatusById(AsyncTaskStatusEditDTO entity) {
        return this.asyncTaskSubRepository.updateStatusById(entity);
    }

    @Override
    public boolean editStatusByIds(List<Long> ids, AsyncTaskStatusEditDTO entity) {
        return this.asyncTaskSubRepository.updateStatusByIds(ids, entity);
    }

    @Override
    public AsyncTaskSubVO getDetailByBizNumber(String bizNumber) {
        final AsyncTaskSubDO data = this.asyncTaskSubRepository.getByBizNumber(bizNumber);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }

        return BeanUtil.copyProperties(data, AsyncTaskSubVO.class);
    }

    @Override
    public AsyncTaskSubVO getDetailByAsyncTaskId(Long asyncTaskId, String bizKey) {
        final AsyncTaskSubDO data = this.asyncTaskSubRepository.getByAsyncTaskId(asyncTaskId, bizKey);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }

        return BeanUtil.copyProperties(data, AsyncTaskSubVO.class);
    }

    @Override
    public long countByAsyncTaskId(Long asyncTaskId) {
        return this.asyncTaskSubRepository.countByAsyncTaskId(asyncTaskId, null);
    }

    @Override
    public long countUnfinished(Long asyncTaskId) {
        return this.asyncTaskSubRepository.countByAsyncTaskId(asyncTaskId, AsyncTaskStatusEnum.SUCCESS);
    }

    @Override
    public long countNotTimeout(Long asyncTaskId) {
        return this.asyncTaskSubRepository.countNotTimeout(asyncTaskId);
    }

    @Override
    public List<AsyncTaskSubVO> listByAsyncTaskIdAndStatus(Long asyncTaskId, List<AsyncTaskStatusEnum> statusList) {
        final List<AsyncTaskSubDO> list = this.asyncTaskSubRepository.listByAsyncTaskId(asyncTaskId,
            statusList, Boolean.TRUE);
        if (CollUtil.isEmpty(list)) {
            return CollUtil.newArrayList();
        }

        return BeanUtil.copyToList(list, AsyncTaskSubVO.class);
    }

    @Override
    public List<AsyncTaskSubVO> listByBizNumbers(List<String> bizNumbers) {
        final List<AsyncTaskSubDO> list = this.asyncTaskSubRepository.listByBizNumbers(bizNumbers);
        if (CollUtil.isEmpty(list)) {
            return List.of();
        }
        return BeanUtil.copyToList(list, AsyncTaskSubVO.class);
    }
}

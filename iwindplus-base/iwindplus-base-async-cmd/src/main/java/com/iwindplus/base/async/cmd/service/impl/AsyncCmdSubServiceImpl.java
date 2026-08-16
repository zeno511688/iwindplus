/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.iwindplus.base.async.cmd.dal.model.AsyncCmdSubDO;
import com.iwindplus.base.async.cmd.dal.repository.AsyncCmdRepository;
import com.iwindplus.base.async.cmd.dal.repository.AsyncCmdSubRepository;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdStatusEditDTO;
import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdStatusEnum;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdSubVO;
import com.iwindplus.base.async.cmd.service.AsyncCmdSubService;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
public class AsyncCmdSubServiceImpl implements AsyncCmdSubService {

    private final AsyncCmdRepository asyncCmdRepository;
    private final AsyncCmdSubRepository asyncCmdSubRepository;

    @Override
    public boolean editStatusById(AsyncCmdStatusEditDTO entity) {
        return this.asyncCmdSubRepository.updateStatusById(entity);
    }

    @Override
    public boolean editStatusByIds(List<Long> ids, AsyncCmdStatusEditDTO entity) {
        return this.asyncCmdSubRepository.updateStatusByIds(ids, entity);
    }

    @Override
    public boolean editCallbackBatch(Map<Long, Map<String, Object>> idToResult, Map<Long, Integer> idToProgress) {
        return this.asyncCmdSubRepository.updateCallbackBatch(idToResult, idToProgress);
    }

    @Override
    public AsyncCmdSubVO getDetailByBizNumber(String bizNumber) {
        final AsyncCmdSubDO data = this.asyncCmdSubRepository.getByBizNumber(bizNumber);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }

        return BeanUtil.copyProperties(data, AsyncCmdSubVO.class);
    }

    @Override
    public AsyncCmdSubVO getDetailByAsyncCmdId(Long asyncCmdId, String bizKey) {
        final AsyncCmdSubDO data = this.asyncCmdSubRepository.getByAsyncCmdId(asyncCmdId, bizKey);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }

        return BeanUtil.copyProperties(data, AsyncCmdSubVO.class);
    }

    @Override
    public long countByAsyncCmdId(Long asyncCmdId) {
        return this.asyncCmdSubRepository.countByAsyncCmdId(asyncCmdId, null);
    }

    @Override
    public long countUnfinished(Long asyncCmdId) {
        return this.asyncCmdSubRepository.countByAsyncCmdId(asyncCmdId, AsyncCmdStatusEnum.SUCCESS);
    }

    @Override
    public List<AsyncCmdSubVO> listByAsyncCmdIdAndStatus(Long asyncCmdId, List<AsyncCmdStatusEnum> statusList) {
        final List<AsyncCmdSubDO> list = this.asyncCmdSubRepository.listByAsyncCmdId(asyncCmdId,
            statusList, true);
        if (CollUtil.isEmpty(list)) {
            return CollUtil.newArrayList();
        }

        return BeanUtil.copyToList(list, AsyncCmdSubVO.class);
    }

    @Override
    public List<AsyncCmdSubVO> listByBizNumbers(List<String> bizNumbers) {
        final List<AsyncCmdSubDO> list = this.asyncCmdSubRepository.listByBizNumbers(bizNumbers);
        if (CollUtil.isEmpty(list)) {
            return List.of();
        }
        return BeanUtil.copyToList(list, AsyncCmdSubVO.class);
    }

    @Override
    public void aggregateProgress(Long asyncCmdId) {
        final List<AsyncCmdStatusEnum> allStatuses = new ArrayList<>(AsyncCmdStatusEnum.getUnfinishedStatus());
        allStatuses.add(AsyncCmdStatusEnum.SUCCESS);
        final List<AsyncCmdSubVO> allSubTasks = this.listByAsyncCmdIdAndStatus(asyncCmdId, allStatuses);
        if (allSubTasks.isEmpty()) {
            return;
        }
        final int progress = this.calculateAverageProgress(allSubTasks);
        this.asyncCmdRepository.updateStatusById(AsyncCmdStatusEditDTO.builder()
            .id(asyncCmdId)
            .progress(progress)
            .build());
    }

    /**
     * 计算子任务平均进度.
     * <p>成功的子任务视为100%，其余按已上报进度计算，取均值写入主任务.</p>
     *
     * @param subTasks 子任务列表
     * @return 平均进度（0-100）
     */
    private int calculateAverageProgress(List<AsyncCmdSubVO> subTasks) {
        if (subTasks.isEmpty()) {
            return 0;
        }
        final int sum = subTasks.stream()
            .mapToInt(sub -> AsyncCmdStatusEnum.SUCCESS.equals(sub.getStatus())
                ? 100
                : (sub.getProgress() != null ? sub.getProgress() : 0))
            .sum();
        return sum / subTasks.size();
    }
}

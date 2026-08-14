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
import com.iwindplus.base.async.cmd.dal.repository.AsyncCmdSubRepository;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdStatusEditDTO;
import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdStatusEnum;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdSubVO;
import com.iwindplus.base.async.cmd.service.AsyncCmdSubService;
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
public class AsyncCmdSubServiceImpl implements AsyncCmdSubService {

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
}

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
import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdStatusEnum;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdSubVO;
import com.iwindplus.base.async.cmd.service.AsyncCmdSubService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
    public boolean editStatusById(Long id, AsyncCmdStatusEnum from, AsyncCmdStatusEnum to,
        Long costTime, String errorMsg, Integer retryCount, Map<String, Object> result, LocalDateTime callbackExpireTime) {
        return this.asyncCmdSubRepository.updateStatusById(id, from, to, costTime, errorMsg, retryCount, result, callbackExpireTime);
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

/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.support.impl;

import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdShardSearchDTO;
import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdJobEnum;
import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdStatusEnum;
import com.iwindplus.base.async.cmd.domain.property.AsyncCmdProperty;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdVO;
import com.iwindplus.base.async.cmd.service.AsyncCmdService;
import com.iwindplus.base.async.cmd.support.AsyncCmdBizProcessor;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * 异步命令重试job助手策略实现类.
 *
 * @author zengdegui
 * @since 2025/11/29 23:12
 */
@Slf4j
public class AsyncCmdJobRetryHandler extends AbstractAsyncCmdJobHandler {

    private final AsyncCmdBizProcessor asyncCmdBizProcessor;

    public AsyncCmdJobRetryHandler(
        AsyncCmdProperty property,
        AsyncCmdService asyncCmdService,
        AsyncCmdBizProcessor asyncCmdBizProcessor) {
        super(property, asyncCmdService);
        this.asyncCmdBizProcessor = asyncCmdBizProcessor;
    }

    @Override
    public AsyncCmdJobEnum support() {
        return AsyncCmdJobEnum.RETRY_JOB;
    }

    @Override
    protected boolean doExecute(List<AsyncCmdVO> entityList) {
        int dispatched = 0;
        for (AsyncCmdVO entity : entityList) {
            if (!this.asyncCmdBizProcessor.execute(entity)) {
                log.warn("重试任务投递被拒，共享池已满，已投递={}/{} id={}",
                    dispatched, entityList.size(), entity.getId());

                return false;
            }
            dispatched++;
        }
        return true;
    }

    @Override
    protected AsyncCmdShardSearchDTO buildJobSearchDTO() {
        // 查询状态为待执行
        return AsyncCmdShardSearchDTO.builder()
            .statusList(AsyncCmdStatusEnum.getRetryStatus())
            .nextRetryTime(LocalDateTime.now())
            .build();
    }

}

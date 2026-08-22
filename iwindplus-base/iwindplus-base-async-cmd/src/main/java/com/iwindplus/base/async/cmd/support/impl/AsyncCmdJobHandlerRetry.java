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
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * 异步命令重试job助手策略实现类.
 *
 * @author zengdegui
 * @since 2025/11/29 23:12
 */
@Slf4j
public class AsyncCmdJobHandlerRetry extends AbstractAsyncCmdJobHandler {

    private final AsyncCmdBizProcessor asyncCmdBizProcessor;

    public AsyncCmdJobHandlerRetry(
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
        int skipped = 0;
        
        for (AsyncCmdVO entity : entityList) {
            // 检查 WAITING 状态的任务是否真的超时
            if (AsyncCmdStatusEnum.WAITING.equals(entity.getStatus())) {
                // 如果未超时，跳过重试
                if (entity.getExpireTime() != null && entity.getExpireTime() > System.currentTimeMillis()) {
                    skipped++;
                    log.debug("WAITING 状态任务未超时，跳过重试，id={}, expireTime={}", 
                        entity.getId(), entity.getExpireTime());
                    continue;
                }
                // 已超时，需要重试
                log.info("WAITING 状态任务已超时，开始重试，id={}, expireTime={}", 
                    entity.getId(), entity.getExpireTime());
            }
            
            if (!this.asyncCmdBizProcessor.execute(entity)) {
                log.warn("重试任务投递被拒，共享池已满，已投递={}/{} id={}",
                    dispatched, entityList.size(), entity.getId());

                return false;
            }
            dispatched++;
        }
        
        if (skipped > 0) {
            log.info("重试任务完成，dispatched={}, skipped={}", dispatched, skipped);
        }
        return true;
    }

    @Override
    protected AsyncCmdShardSearchDTO buildJobSearchDTO() {
        // 查询状态为待执行
        return AsyncCmdShardSearchDTO.builder()
            .statusList(AsyncCmdStatusEnum.getRetryStatus())
            .nextRetryTime(System.currentTimeMillis())
            .build();
    }

}

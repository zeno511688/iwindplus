/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.support.impl;

import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdExtDTO;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdShardSearchDTO;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdStatusEditDTO;
import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdJobEnum;
import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdStatusEnum;
import com.iwindplus.base.async.cmd.domain.property.AsyncCmdProperty;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdVO;
import com.iwindplus.base.async.cmd.factory.AsyncCmdTaskHandlerStrategyFactory;
import com.iwindplus.base.async.cmd.service.AsyncCmdService;
import com.iwindplus.base.async.cmd.support.AsyncCmdTaskHandler;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

/**
 * 异步命令重置job助手策略实现类.
 *
 * @author zengdegui
 * @since 2025/11/29 23:12
 */
@Slf4j
public class AsyncCmdJobHandlerReset extends AbstractAsyncCmdJobHandler {

    private final AsyncCmdTaskHandlerStrategyFactory asyncCmdTaskHandlerStrategyFactory;

    public AsyncCmdJobHandlerReset(
        AsyncCmdProperty property,
        AsyncCmdService asyncCmdService,
        AsyncCmdTaskHandlerStrategyFactory asyncCmdTaskHandlerStrategyFactory) {
        super(property, asyncCmdService);
        this.asyncCmdTaskHandlerStrategyFactory = asyncCmdTaskHandlerStrategyFactory;
    }

    @Override
    public AsyncCmdJobEnum support() {
        return AsyncCmdJobEnum.RESET_JOB;
    }

    @Override
    protected boolean doExecute(List<AsyncCmdVO> entityList) {
        if (entityList.isEmpty()) {
            return true;
        }

        int reset = 0;
        int discard = 0;
        int skipped = 0;
        int failed = 0;

        for (AsyncCmdVO entity : entityList) {
            try {
                // 检查是否应该跳过
                if (this.shouldSkip(entity)) {
                    skipped++;
                    continue;
                }

                final AsyncCmdExtDTO ext = entity.getExt();
                boolean exceed = !ext.getEnabledUnlimitedRetry() && entity.getRetryCount() > ext.getMaxAttempts();
                AsyncCmdStatusEnum status = exceed
                    ? AsyncCmdStatusEnum.DISCARD
                    : AsyncCmdStatusEnum.TO_BE_EXECUTE;
                final boolean result = super.getAsyncCmdService()
                    .editStatusById(
                        AsyncCmdStatusEditDTO.builder()
                            .id(entity.getId())
                            .from(entity.getStatus())
                            .to(status)
                            .build());
                if (!result) {
                    skipped++;
                    log.warn("重置任务调过，id={} from={}", entity.getId(), entity.getStatus());

                    continue;
                }

                if (exceed) {
                    discard++;
                    entity.setStatus(AsyncCmdStatusEnum.DISCARD);
                    this.safeDiscardCallback(entity);
                } else {
                    reset++;
                }
            } catch (Exception e) {
                failed++;

                log.error("重置任务失败，id={}", entity.getId(), e);
            }
        }

        log.info("重置任务，size={} reset={}, discard={}, skipped={}, failed={}",
            entityList.size(), reset, discard, skipped, failed);

        return true;
    }


    /**
     * 触发任务丢弃钩子，吞异常避免影响JOB循环（丢弃已是恢复出口，不能被钩子卡住）.
     *
     * @param entity 异步命令视图对象
     */
    private void safeDiscardCallback(AsyncCmdVO entity) {
        try {
            final AsyncCmdTaskHandler handler = this.asyncCmdTaskHandlerStrategyFactory
                .getTaskHandler(entity.getExecuteName());
            if (Objects.nonNull(handler)) {
                handler.onTaskDiscard(entity);
            }
        } catch (Exception ex) {
            log.error("任务丢弃钩子执行失败，id={}", entity.getId(), ex);
        }
    }

    @Override
    protected AsyncCmdShardSearchDTO buildJobSearchDTO() {
        return AsyncCmdShardSearchDTO.builder()
            .statusList(AsyncCmdStatusEnum.getRestStatus())
            .expireTime(System.currentTimeMillis())
            .build();
    }

    @Override
    protected boolean shouldSkip(AsyncCmdVO entity) {
        // 查询后再次校验状态，避免任务已被其他线程处理时重复重置。
        // 主任务已过期时，不受子任务 WAITING/EXECUTE 状态影响。
        final AsyncCmdStatusEnum status = entity.getStatus();
        return !AsyncCmdStatusEnum.getRestStatus().contains(status);
    }
}

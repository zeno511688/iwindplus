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
import com.iwindplus.base.async.cmd.domain.property.AsyncCmdProperty.RetryConfig;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdVO;
import com.iwindplus.base.async.cmd.service.AsyncCmdService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * 异步命令重置job助手策略实现类.
 *
 * @author zengdegui
 * @since 2025/11/29 23:12
 */
@Slf4j
public class AsyncCmdJobResetHandler extends AbstractAsyncCmdJobHandler {

    public AsyncCmdJobResetHandler(
        AsyncCmdProperty property,
        AsyncCmdService asyncCmdService) {
        super(property, asyncCmdService);
    }

    @Override
    public AsyncCmdJobEnum support() {
        return AsyncCmdJobEnum.RESET_JOB;
    }

    @Override
    protected boolean doExecute(List<AsyncCmdVO> entityList) {
        final RetryConfig retryConfig = super.getProperty().getRetry();
        final boolean unlimited = Boolean.TRUE.equals(retryConfig.getEnabledUnlimitedRetry());
        final int maxAttempts = retryConfig.getMaxAttempts();

        if (entityList.isEmpty()) {
            return true;
        }

        int reset = 0;
        int discard = 0;
        int skipped = 0;
        int failed = 0;

        for (AsyncCmdVO entity : entityList) {
            try {
                boolean exceed = !unlimited && entity.getRetryCount() > maxAttempts;
                AsyncCmdStatusEnum status = exceed
                    ? AsyncCmdStatusEnum.DISCARD
                    : AsyncCmdStatusEnum.TO_BE_EXECUTE;
                final boolean result = super.getAsyncCmdService()
                    .editStatusById(entity.getId(), entity.getStatus(), status,
                        null, null, null, LocalDateTime.now(), null, null);
                if (!result) {
                    skipped++;
                    log.warn("重置任务调过，id={} from={}", entity.getId(), entity.getStatus());

                    continue;
                }

                if (exceed) {
                    discard++;
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

    @Override
    protected AsyncCmdShardSearchDTO buildJobSearchDTO() {
        return AsyncCmdShardSearchDTO.builder()
            .statusList(AsyncCmdStatusEnum.getRestStatus())
            .expireTime(LocalDateTime.now())
            .build();
    }
}

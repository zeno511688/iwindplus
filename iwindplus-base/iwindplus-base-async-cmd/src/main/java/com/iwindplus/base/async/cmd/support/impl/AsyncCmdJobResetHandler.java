/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.support.impl;

import cn.hutool.core.collection.CollUtil;
import com.iwindplus.base.async.cmd.domain.bo.AsyncCmdBO;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdEditDTO;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdSearchDTO;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdSearchDTO.AsyncCmdSearchDTOBuilder;
import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdJobEnum;
import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdStatusEnum;
import com.iwindplus.base.async.cmd.domain.property.AsyncCmdProperty;
import com.iwindplus.base.async.cmd.domain.property.AsyncCmdProperty.RetryConfig;
import com.iwindplus.base.async.cmd.service.AsyncCmdService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional(rollbackFor = Exception.class)
    protected void doExecute(List<AsyncCmdBO> entityList) {
        final RetryConfig retryConfig = super.getProperty().getRetry();
        final boolean unlimited = Boolean.TRUE.equals(retryConfig.getEnabledUnlimitedRetry());
        final int maxAttempts = retryConfig.getMaxAttempts();

        List<AsyncCmdEditDTO> entities = new ArrayList<>(10);
        entityList.forEach(entity -> {
            boolean exceed = entity.getRetryCount() > maxAttempts;
            AsyncCmdEditDTO.AsyncCmdEditDTOBuilder builder = AsyncCmdEditDTO.builder()
                .id(entity.getId());
            if (!unlimited && exceed) {
                // 超过最大次数并且不是无限重试 → 丢弃
                builder.status(AsyncCmdStatusEnum.DISCARD);
            } else {
                // 否则继续执行
                builder.status(AsyncCmdStatusEnum.TO_BE_EXECUTE);
            }
            entities.add(builder.build());
        });

        if (CollUtil.isEmpty(entities)) {
            return;
        }

        log.info("重置任务，size={}", entities.size());
        super.getAsyncCmdService().editBatch(entities);
    }

    @Override
    protected AsyncCmdSearchDTO buildJobSearchDTO() {
        final AsyncCmdSearchDTOBuilder<?, ?> builder = AsyncCmdSearchDTO.builder()
            .taskName("reset job")
            .statusList(AsyncCmdStatusEnum.getPendingStatus())
            .expireTime(LocalDateTime.now())
            .showContent(true);
        return builder.build();
    }
}

/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.support.impl;

import com.iwindplus.base.async.cmd.domain.bo.AsyncCmdBO;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdSearchDTO;
import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdJobEnum;
import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdStatusEnum;
import com.iwindplus.base.async.cmd.domain.property.AsyncCmdProperty;
import com.iwindplus.base.async.cmd.service.AsyncCmdService;
import com.iwindplus.base.async.cmd.support.AsyncCmdBizProcessor;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional(rollbackFor = Exception.class)
    @Override
    protected void doExecute(List<AsyncCmdBO> entityList) {
        entityList.forEach(asyncCmdBizProcessor::execute);
    }

    @Override
    protected AsyncCmdSearchDTO buildJobSearchDTO() {
        // 查询状态为待执行
        return AsyncCmdSearchDTO.builder()
            .taskName("retry job")
            .status(AsyncCmdStatusEnum.TO_BE_EXECUTE)
            .retryTime(LocalDateTime.now())
            .showContent(true)
            .build();
    }

}

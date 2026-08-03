/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.support;

import com.iwindplus.base.async.cmd.domain.property.AsyncCmdProperty;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdVO;
import com.iwindplus.base.async.cmd.service.AsyncCmdService;
import com.iwindplus.base.async.cmd.service.AsyncCmdSubService;
import com.iwindplus.base.util.TransactionUtil;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.core.executor.DtpExecutor;

/**
 * 异步命令业务处理（核心）.
 *
 * @author zengdegui
 * @since 2025/12/29 22:55
 */
@Slf4j
public record AsyncCmdBizProcessor(
    AsyncCmdProperty property,
    AsyncCmdService asyncCmdService,
    AsyncCmdSubService asyncCmdSubService,
    AsyncCmdStateSupport asyncCmdStateSupport,
    AsyncCmdExecuteHandler asyncCmdExecuteMainHandler,
    AsyncCmdExecuteHandler asyncCmdExecuteGroupHandler,
    DtpExecutor asyncCmdTaskExecutor) {

    /**
     * 异步执行任务
     */
    public void execute(AsyncCmdVO entity) {
        if (entity == null || entity.getId() == null) {
            return;
        }

        final Long id = entity.getId();

        TransactionUtil.registerAfterCommit(() -> {
            try {
                this.asyncCmdTaskExecutor.execute(() -> doExecute(id));
            } catch (Exception ex) {
                log.error("asyncCmd submit failed. id={}", id, ex);
            }
        });
    }

    private void doExecute(Long id) {
        AsyncCmdVO entity = this.asyncCmdService.getDetail(id);
        if (entity == null) {
            log.info("asyncCmd not exist, skip. id={}", id);
            return;
        }

        // 1 抢占执行权
        boolean locked = this.asyncCmdStateSupport.lockById(entity);
        if (!locked) {
            log.info("asyncCmd already handled. id={}", entity.getId());
            return;
        }

        try {
            final long subTaskCount = Optional.of(entity.getSubTaskCount()).orElse(0);
            this.getExecuteHandler(subTaskCount).execute(entity);
        } catch (Exception ex) {
            // 兜底主任务卡在执行中只能等重置状态
            this.asyncCmdStateSupport.taskFail(entity, null, ex);
        }
    }

    /**
     * 选择执行策略.
     *
     * @param subTaskCount 子任务总数
     * @return AsyncCmdExecuteHandler
     */
    private AsyncCmdExecuteHandler getExecuteHandler(long subTaskCount) {
        return subTaskCount <= 0 ? asyncCmdExecuteMainHandler : asyncCmdExecuteGroupHandler;
    }
}

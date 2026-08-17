/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.support;

import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdStatusEnum;
import com.iwindplus.base.async.cmd.domain.property.AsyncCmdProperty;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdVO;
import com.iwindplus.base.async.cmd.service.AsyncCmdService;
import com.iwindplus.base.async.cmd.service.AsyncCmdSubService;
import com.iwindplus.base.util.TransactionUtil;
import java.util.Optional;
import java.util.concurrent.RejectedExecutionException;
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
    AsyncCmdExecuteHandler asyncCmdExecuteHandlerMain,
    AsyncCmdExecuteHandler asyncCmdExecuteHandlerGroup,
    DtpExecutor asyncCmdTaskExecutor) {

    /**
     * 执行任务.
     *
     * @param entity 对象
     * @return boolean
     */
    public boolean execute(AsyncCmdVO entity) {
        if (entity == null || entity.getId() == null) {
            return false;
        }

        final Long id = entity.getId();

        TransactionUtil.registerAfterCommit(
            () -> submitExecutor(id)
        );

        return true;
    }

    private boolean submitExecutor(Long id) {
        try {
            asyncCmdTaskExecutor.execute(() -> doExecute(id));

            return true;
        } catch (RejectedExecutionException ex) {
            log.warn("asyncCmd executor rejected. id={}", id, ex);

            return false;
        } catch (Exception ex) {
            log.error("asyncCmd executor failed. id={}", id, ex);

            return false;
        }
    }

    private void doExecute(Long id) {
        AsyncCmdVO entity = this.asyncCmdService.getDetail(id);
        if (entity == null) {
            log.info("asyncCmd not exist, skip. id={}", id);
            return;
        }

        // 只有待执行/异步等待状态才处理
        if (!needProcessStatus(entity)) {
            return;
        }

        // 待执行状态
        if (AsyncCmdStatusEnum.TO_BE_EXECUTE.equals(entity.getStatus())) {
            boolean locked = asyncCmdStateSupport.taskToBeExecuteToExecute(entity);
            if (!locked) {
                log.info("asyncCmd already handled. id={}", entity.getId());
                return;
            }
        }

        final long start = System.currentTimeMillis();

        try {
            final long subTaskCount = Optional.of(entity.getSubTaskCount()).orElse(0);
            this.getExecuteHandler(subTaskCount).execute(entity);
        } catch (Exception ex) {
            log.error("asyncCmd execute failed. id={}", entity.getId(), ex);

            // 兜底主任务卡在执行中只能等重置状态
            if (AsyncCmdStatusEnum.EXECUTE.equals(entity.getStatus())) {
                this.asyncCmdStateSupport.taskFail(entity, null,
                    System.currentTimeMillis() - start, ex, false);
                return;
            }

            // 兜底主任务卡在异步等待中只能等重置状态
            if (AsyncCmdStatusEnum.WAITING.equals(entity.getStatus())) {
                this.asyncCmdStateSupport.taskAsyncWaitFail(entity, null,
                    System.currentTimeMillis() - start, ex);
                return;
            }
        }
    }

    private boolean needProcessStatus(AsyncCmdVO entity) {
        AsyncCmdStatusEnum status = entity.getStatus();
        return AsyncCmdStatusEnum.TO_BE_EXECUTE.equals(status)
            || AsyncCmdStatusEnum.WAITING.equals(status);
    }

    /**
     * 选择执行策略.
     *
     * @param subTaskCount 子任务总数
     * @return AsyncCmdExecuteHandler
     */
    private AsyncCmdExecuteHandler getExecuteHandler(long subTaskCount) {
        return subTaskCount <= 0 ? asyncCmdExecuteHandlerMain : asyncCmdExecuteHandlerGroup;
    }
}

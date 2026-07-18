/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.support;

import cn.hutool.core.bean.BeanUtil;
import com.iwindplus.base.async.cmd.domain.bo.AsyncCmdBO;
import com.iwindplus.base.async.cmd.domain.bo.AsyncCmdExecutorBO;
import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdStatusEnum;
import com.iwindplus.base.async.cmd.domain.property.AsyncCmdProperty;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdVO;
import com.iwindplus.base.async.cmd.factory.AsyncCmdTaskHandlerStrategyFactory;
import com.iwindplus.base.async.cmd.service.AsyncCmdService;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

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
    AsyncCmdTaskHandlerStrategyFactory asyncCmdTaskHandlerStrategyFactory,
    DtpExecutor asyncCmdTaskExecutor,
    TransactionTemplate transactionTemplate) {

    /**
     * 异步执行任务
     */
    public void execute(AsyncCmdBO entity) {
        if (entity == null || entity.getId() == null) {
            return;
        }

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {

                    @Override
                    public void afterCommit() {
                        submit(entity.getId());
                    }
                }
            );

            return;
        }

        submit(entity.getId());
    }

    private void submit(Long id) {
        try {
            asyncCmdTaskExecutor.execute(() -> doExecute(id));
        } catch (Exception ex) {
            log.error("asyncCmd submit failed. id={}", id, ex);
        }
    }

    private void doExecute(Long id) {
        AsyncCmdVO cmd = asyncCmdService.getDetail(id);
        if (cmd == null) {
            return;
        }
        final AsyncCmdBO entity = BeanUtil.copyProperties(cmd, AsyncCmdBO.class);
        // 1 抢占执行权
        boolean locked = lock(entity);
        if (!locked) {
            log.info("asyncCmd already handled. id={}", entity.getId());
            return;
        }

        try {
            // 2 执行业务逻辑（无事务）
            this.executeBusinessLogic(entity);

            // 3 成功
            this.executeSuccess(entity);
        } catch (Exception ex) {
            log.error("asyncCmd execute failed. id={}", entity.getId(), ex);
            // 4 失败
            this.executeFail(entity, ex);
        }
    }

    /**
     * 抢占执行权
     *
     * @param entity 命令对象
     * @return boolean
     */
    private boolean lock(AsyncCmdBO entity) {
        return Boolean.TRUE.equals(
            this.transactionTemplate.execute(status ->
                asyncCmdService.editStatusById(
                    entity.getId(),
                    AsyncCmdStatusEnum.TO_BE_EXECUTE,
                    AsyncCmdStatusEnum.EXECUTE
                )
            )
        );
    }

    /**
     * 实际业务执行
     *
     * @param entity 对象
     */
    private void executeBusinessLogic(AsyncCmdBO entity) {
        Assert.notNull(entity.getExecuteName(), "executeName cannot be null");
        AsyncCmdTaskHandler handler =
            asyncCmdTaskHandlerStrategyFactory.getTaskHandler(entity.getExecuteName());

        AsyncCmdExecutorBO executorBO = BeanUtil.copyProperties(entity, AsyncCmdExecutorBO.class);
        handler.execute(executorBO);
    }

    /**
     * 执行成功
     *
     * @param entity 对象
     */
    private void executeSuccess(AsyncCmdBO entity) {
        this.transactionTemplate.executeWithoutResult(status -> {
            boolean result = asyncCmdService.editStatusById(
                entity.getId(),
                AsyncCmdStatusEnum.EXECUTE,
                AsyncCmdStatusEnum.SUCCESS
            );
            if (result) {
                log.info("asyncCmd execute success. id={}", entity.getId());
                // 删除记录（可选）
                if (Boolean.TRUE.equals(this.property.getEnabledSuccessDelete())) {
                    asyncCmdService.removeById(entity.getId(), this.property.getEnabledSuccessRealDelete());
                }
            }
        });
    }

    /**
     * 执行失败
     *
     * @param entity 对象
     * @param ex     异常
     */
    private void executeFail(AsyncCmdBO entity, Exception ex) {
        this.transactionTemplate.executeWithoutResult(status -> {
            int retryCount = Optional.ofNullable(entity.getRetryCount()).orElse(0) + 1;

            this.asyncCmdService.editStatusById(
                entity.getId(),
                AsyncCmdStatusEnum.EXECUTE,
                AsyncCmdStatusEnum.FAILED,
                this.getStack(ex),
                retryCount,
                this.getNextRetryTime(entity, retryCount)
            );
        });
    }

    /**
     * 获取下次重试时间
     *
     * @param entity     命令对象
     * @param retryCount 重试次数
     * @return LocalDateTime
     */
    private LocalDateTime getNextRetryTime(AsyncCmdBO entity, int retryCount) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next = asyncCmdService.getNextRetryTime(entity.getNextRetryTime(), retryCount);
        return next.isBefore(now) ? now.plusSeconds(5) : next;
    }

    /**
     * 获取异常堆栈
     *
     * @param ex 异常
     * @return String
     */
    private String getStack(Exception ex) {
        String stack = ExceptionUtils.getStackTrace(ex);
        return Boolean.TRUE.equals(property.getEnabledExceptionCapture())
            ? StringUtils.abbreviate(stack, property.getExceptionCaptureLength())
            : stack;
    }
}

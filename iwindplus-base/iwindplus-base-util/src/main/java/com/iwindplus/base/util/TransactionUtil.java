/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util;

import com.iwindplus.base.domain.constant.CommonConstant;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 事务工具类.
 *
 * @author zengdegui
 * @since 2026/08/02 16:17
 */
@Slf4j
public class TransactionUtil {

    private TransactionUtil() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 注册事务提交后执行任务.
     *
     * @param runnable 任务
     */
    public static void registerAfterCommit(Runnable runnable) {
        if (isTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        runnable.run();
                    }
                }
            );
            return;
        }

        runnable.run();
    }

    /**
     * 判断当前是否存在活跃的事务.
     *
     * @return true-存在活跃事务，false-不存在
     */
    public static boolean isTransactionActive() {
        return TransactionSynchronizationManager.isSynchronizationActive()
            && TransactionSynchronizationManager.isActualTransactionActive();
    }

    /**
     * 在事务中执行操作并返回结果.
     *
     * @param transactionTemplate 事务模板
     * @param supplier 操作
     * @param <T> 返回类型
     * @return 结果
     */
    public static <T> T executeInTransaction(TransactionTemplate transactionTemplate, Supplier<T> supplier) {
        return transactionTemplate.execute(status -> supplier.get());
    }
}

/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.dtx.domain.enums;

import java.util.List;
import lombok.Getter;

/**
 * 分布式事务操作枚举.
 *
 * @author zengdegui
 * @since 2026/03/01 20:00
 */
@Getter
public enum TxActionEnum {

    /**
     * 确认操作.
     */
    CONFIRM(
        GlobalTxStatusEnum.CONFIRM_SUCCESS,
        GlobalTxStatusEnum.CONFIRMING,
        GlobalTxStatusEnum.CONFIRM_FAIL,
        GlobalTxStatusEnum.CANCEL_SUCCESS,
        List.of(GlobalTxStatusEnum.TRYING, GlobalTxStatusEnum.CONFIRM_FAIL),
        List.of(BranchTxStatusEnum.TRY_SUCCESS, BranchTxStatusEnum.CONFIRM_FAIL, BranchTxStatusEnum.CONFIRMING),
        List.of(BranchTxStatusEnum.TRY_SUCCESS, BranchTxStatusEnum.CONFIRM_FAIL),
        BranchTxStatusEnum.CONFIRMING,
        BranchTxStatusEnum.CONFIRM_SUCCESS,
        BranchTxStatusEnum.CONFIRM_FAIL
    ),

    /**
     * 取消操作.
     */
    CANCEL(
        GlobalTxStatusEnum.CANCEL_SUCCESS,
        GlobalTxStatusEnum.CANCELING,
        GlobalTxStatusEnum.CANCEL_FAIL,
        GlobalTxStatusEnum.CONFIRM_SUCCESS,
        List.of(GlobalTxStatusEnum.TRYING, GlobalTxStatusEnum.CANCEL_FAIL),
        List.of(BranchTxStatusEnum.TRY_SUCCESS, BranchTxStatusEnum.TRY_FAIL, BranchTxStatusEnum.CANCEL_FAIL, BranchTxStatusEnum.CANCELING),
        List.of(BranchTxStatusEnum.TRY_SUCCESS, BranchTxStatusEnum.CANCEL_FAIL),
        BranchTxStatusEnum.CANCELING,
        BranchTxStatusEnum.CANCEL_SUCCESS,
        BranchTxStatusEnum.CANCEL_FAIL
    );

    /**
     * 目标状态.
     */
    private final GlobalTxStatusEnum targetStatus;

    /**
     * 处理中状态.
     */
    private final GlobalTxStatusEnum processingStatus;

    /**
     * 失败状态.
     */
    private final GlobalTxStatusEnum failStatus;

    /**
     * 对方状态.
     */
    private final GlobalTxStatusEnum oppositeStatus;

    /**
     * 可重试状态.
     */
    private final List<GlobalTxStatusEnum> retryableStatusList;

    /**
     * 分支事务源状态.
     */
    private final List<BranchTxStatusEnum> branchSourceStatusList;

    /**
     * 分支事务锁状态.
     */
    private final List<BranchTxStatusEnum> branchLockStatusList;

    /**
     * 分支事务处理中状态.
     */
    private final BranchTxStatusEnum branchProcessingStatus;

    /**
     * 分支事务成功状态.
     */
    private final BranchTxStatusEnum branchSuccessStatus;

    /**
     * 分支事务失败状态.
     */
    private final BranchTxStatusEnum branchFailStatus;

    /**
     * 构造函数.
     *
     * @param targetStatus           目标状态
     * @param processingStatus       处理中状态
     * @param failStatus             失败状态
     * @param oppositeStatus         对方状态
     * @param retryableStatusList    可重试状态
     * @param branchSourceStatusList 分支事务源状态
     * @param branchLockStatusList   分支事务锁状态
     * @param branchProcessingStatus 分支事务处理中状态
     * @param branchSuccessStatus    分支事务成功状态
     * @param branchFailStatus       分支事务失败状态
     */
    TxActionEnum(GlobalTxStatusEnum targetStatus,
        GlobalTxStatusEnum processingStatus,
        GlobalTxStatusEnum failStatus,
        GlobalTxStatusEnum oppositeStatus,
        List<GlobalTxStatusEnum> retryableStatusList,
        List<BranchTxStatusEnum> branchSourceStatusList,
        List<BranchTxStatusEnum> branchLockStatusList,
        BranchTxStatusEnum branchProcessingStatus,
        BranchTxStatusEnum branchSuccessStatus,
        BranchTxStatusEnum branchFailStatus) {
        this.targetStatus = targetStatus;
        this.processingStatus = processingStatus;
        this.failStatus = failStatus;
        this.oppositeStatus = oppositeStatus;
        this.retryableStatusList = retryableStatusList;
        this.branchSourceStatusList = branchSourceStatusList;
        this.branchLockStatusList = branchLockStatusList;
        this.branchProcessingStatus = branchProcessingStatus;
        this.branchSuccessStatus = branchSuccessStatus;
        this.branchFailStatus = branchFailStatus;
    }

    public boolean isRetryable(GlobalTxStatusEnum status) {
        return retryableStatusList.contains(status);
    }
}

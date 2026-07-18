/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.dtx.server.support.impl;

import cn.hutool.core.collection.CollUtil;
import com.iwindplus.dtx.domain.dto.TccGlobalTxSearchDTO;
import com.iwindplus.dtx.domain.enums.BranchTxStatusEnum;
import com.iwindplus.dtx.domain.enums.DtxJobEnum;
import com.iwindplus.dtx.domain.enums.GlobalTxStatusEnum;
import com.iwindplus.dtx.domain.vo.TccGlobalTxVO;
import com.iwindplus.dtx.server.dal.model.TccBranchTxDO;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 分布式事务超时job操作策略实现类.
 *
 * @author zengdegui
 * @since 2025/11/29 23:12
 */
@Component
@Slf4j
public class DtxJobHandlerTimeoutImpl extends AbstractDtxJobHandlerImpl {

    @Override
    public DtxJobEnum support() {
        return DtxJobEnum.TIMEOUT_JOB;
    }

    @Override
    protected void doExecute(List<TccGlobalTxVO> entityList) {
        log.info("超时任务开始处理，size={}", entityList.size());
        if (CollUtil.isEmpty(entityList)) {
            return;
        }

        int successCount = 0;
        int failCount = 0;

        for (TccGlobalTxVO tx : entityList) {
            try {
                boolean result = processTimeoutTx(tx);
                if (result) {
                    successCount++;
                } else {
                    failCount++;
                }
            } catch (Exception e) {
                failCount++;
                log.error("处理超时事务异常，xid={}, error={}", tx.getXid(), e.getMessage(), e);
            }
        }

        log.info("超时任务处理完成，总数={}, 成功={}, 失败={}", entityList.size(), successCount, failCount);
    }

    /**
     * 处理超时事务.
     *
     * @param tx 全局事务
     * @return 是否处理成功
     */
    private boolean processTimeoutTx(TccGlobalTxVO tx) {
        String xid = tx.getXid();
        log.info("开始处理超时事务，xid={}, bizType={}, expireTime={}",
            xid, tx.getBizType(), tx.getExpireTime());

        // 查询分支事务状态
        List<TccBranchTxDO> branches = getBranches(xid);

        if (CollUtil.isEmpty(branches)) {
            log.warn("超时事务无分支事务，直接回滚，xid={}", xid);
            return this.tccCoordinator.cancel(xid);
        }

        // 分析分支事务状态
        BranchTxAnalysis analysis = analyzeBranches(branches);
        log.info("超时事务分支状态分析，xid={}, 总数={}, Try成功={}, Try失败={}, 其他={}",
            xid, branches.size(), analysis.trySuccessCount, analysis.tryFailCount, analysis.otherCount);

        // 根据分支状态决定处理策略
        if (analysis.tryFailCount > 0) {
            log.warn("超时事务存在Try失败分支，执行回滚，xid={}, Try失败数={}", xid, analysis.tryFailCount);
        }

        if (analysis.trySuccessCount > 0) {
            log.warn("超时事务存在Try成功分支，需要执行Cancel回滚资源，xid={}, Try成功数={}",
                xid, analysis.trySuccessCount);
        }

        // 执行Cancel操作
        boolean cancelResult = this.tccCoordinator.cancel(xid);

        if (cancelResult) {
            log.info("超时事务Cancel成功，xid={}", xid);
        } else {
            log.error("超时事务Cancel失败，xid={}，将在重试任务中继续处理", xid);
            // 记录失败详情，便于后续排查
            logFailedTx(tx, analysis);
        }

        return cancelResult;
    }

    /**
     * 获取分支事务列表.
     */
    private List<TccBranchTxDO> getBranches(String xid) {
        try {
            // 查询所有状态的分支事务
            return this.branchTxService.listByXid(xid,
                List.of(BranchTxStatusEnum.TRYING, BranchTxStatusEnum.TRY_SUCCESS,
                    BranchTxStatusEnum.TRY_FAIL, BranchTxStatusEnum.CONFIRMING,
                    BranchTxStatusEnum.CONFIRM_SUCCESS, BranchTxStatusEnum.CONFIRM_FAIL,
                    BranchTxStatusEnum.CANCELING, BranchTxStatusEnum.CANCEL_SUCCESS,
                    BranchTxStatusEnum.CANCEL_FAIL));
        } catch (Exception e) {
            log.error("查询分支事务失败，xid={}, error={}", xid, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 分析分支事务状态.
     */
    private BranchTxAnalysis analyzeBranches(List<TccBranchTxDO> branches) {
        BranchTxAnalysis analysis = new BranchTxAnalysis();

        for (TccBranchTxDO branch : branches) {
            if (branch.getStatus() == null) {
                analysis.otherCount++;
                continue;
            }

            switch (branch.getStatus()) {
                case TRY_SUCCESS:
                    analysis.trySuccessCount++;
                    break;
                case TRY_FAIL:
                    analysis.tryFailCount++;
                    break;
                default:
                    analysis.otherCount++;
                    break;
            }
        }

        return analysis;
    }

    /**
     * 记录失败事务详情.
     */
    private void logFailedTx(TccGlobalTxVO tx, BranchTxAnalysis analysis) {
        log.error("超时事务处理失败详情 - xid={}, bizType={}, timeoutSeconds={}, retryCount={}, " +
                "expireTime={}, nextRetryTime={}, 分支总数={}, Try成功={}, Try失败={}, 其他={}",
            tx.getXid(), tx.getBizType(), tx.getTimeoutSeconds(), tx.getRetryCount(),
            tx.getExpireTime(), tx.getNextRetryTime(),
            analysis.trySuccessCount + analysis.tryFailCount + analysis.otherCount,
            analysis.trySuccessCount, analysis.tryFailCount, analysis.otherCount);
    }

    /**
     * 分支事务状态分析结果.
     */
    private static class BranchTxAnalysis {
        int trySuccessCount = 0;
        int tryFailCount = 0;
        int otherCount = 0;
    }

    @Override
    protected TccGlobalTxSearchDTO buildDtxJobSearchDTO() {
        return TccGlobalTxSearchDTO.builder()
            .status(GlobalTxStatusEnum.TRYING)
            .expireTime(LocalDateTime.now())
            .build();
    }
}

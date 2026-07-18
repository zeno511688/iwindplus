/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.dtx.server.coordinator.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.http.client.domain.enums.HttpClientTypeEnum;
import com.iwindplus.base.http.client.factory.HttpClientExecutorStrategyFactory;
import com.iwindplus.base.util.DatesUtil;
import com.iwindplus.dtx.domain.dto.TccBranchTxDTO;
import com.iwindplus.dtx.domain.dto.TccGlobalTxDTO;
import com.iwindplus.dtx.domain.enums.BranchTxStatusEnum;
import com.iwindplus.dtx.domain.enums.GlobalTxStatusEnum;
import com.iwindplus.dtx.domain.enums.TxActionEnum;
import com.iwindplus.dtx.domain.vo.TccBranchResultVO;
import com.iwindplus.dtx.domain.vo.TccBranchTxVO;
import com.iwindplus.dtx.domain.vo.TccGlobalTxVO;
import com.iwindplus.dtx.server.config.property.DtxProperty;
import com.iwindplus.dtx.server.coordinator.TccCoordinator;
import com.iwindplus.dtx.server.dal.model.TccBranchTxDO;
import com.iwindplus.dtx.server.service.TccBranchTxService;
import com.iwindplus.dtx.server.service.TccGlobalTxService;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * tcc协调器实现类 .
 *
 * @author zengdegui
 * @since 2026/02/04 22:26
 */
@Component
@Slf4j
public class TccCoordinatorImpl implements TccCoordinator {

    @Resource
    private DtxProperty property;

    @Resource
    private TccGlobalTxService globalTxService;

    @Resource
    private TccBranchTxService branchTxService;

    @Resource
    private DtpExecutor tccTaskExecutor;

    @Resource
    private HttpClientExecutorStrategyFactory factory;

    @Override
    public String begin(String bizType, Long timeoutSeconds) {
        final String xid = IdUtil.getSnowflakeNextIdStr();

        TccGlobalTxDTO entity = TccGlobalTxDTO.builder()
            .xid(xid)
            .bizType(bizType)
            .timeoutSeconds(timeoutSeconds)
            .status(GlobalTxStatusEnum.TRYING)
            .retryCount(0)
            .expireTime(LocalDateTime.now().plusSeconds(timeoutSeconds))
            .nextRetryTime(LocalDateTime.now())
            .build();

        globalTxService.save(entity);
        return xid;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean confirm(String xid) {
        return executeGlobalTx(xid, TxActionEnum.CONFIRM);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean cancel(String xid) {
        return executeGlobalTx(xid, TxActionEnum.CANCEL);
    }

    @Override
    public Long register(TccBranchTxDTO entity) {
        entity.setStatus(BranchTxStatusEnum.TRYING);
        branchTxService.save(entity);
        return entity.getId();
    }

    @Override
    public boolean trySuccess(Long id) {
        return branchTxService.editStatusById(
            id,
            BranchTxStatusEnum.TRYING,
            BranchTxStatusEnum.TRY_SUCCESS
        );
    }

    @Override
    public boolean tryFail(Long id) {
        return branchTxService.editStatusById(
            id,
            BranchTxStatusEnum.TRYING,
            BranchTxStatusEnum.TRY_FAIL
        );
    }

    @Override
    public Integer getSize() {
        int active = tccTaskExecutor.getActiveCount();
        int max = tccTaskExecutor.getMaximumPoolSize();
        return Math.max(1, Math.min(property.getMaxPageSize(), max - active));
    }

    @Override
    public LocalDateTime getNextRetryTime(LocalDateTime base, Integer retryCount) {
        List<LocalDateTime> times =
            DatesUtil.convertFrequencyToLocalDateTime(
                Optional.ofNullable(base).orElse(LocalDateTime.now()),
                property.getRetry().getFrequency()
            );

        if (CollUtil.isEmpty(times)) {
            return LocalDateTime.now().plusSeconds(5);
        }

        int index = Math.max(0, retryCount - 1);
        return times.get(Math.min(index, times.size() - 1));
    }

    private boolean executeGlobalTx(String xid, TxActionEnum action) {
        TccGlobalTxVO data = globalTxService.getDetailByXid(xid);
        if (data == null || data.getStatus() == null) {
            return false;
        }

        GlobalTxStatusEnum current = data.getStatus();

        if (current == action.getTargetStatus()) {
            return true;
        }
        if (current == action.getOppositeStatus()) {
            return false;
        }

        // 使用乐观锁更新状态，并返回最新的事务信息
        TccGlobalTxVO latest = globalTxService.editStatusByMultiFromWithResult(
            xid,
            action.getRetryableStatusList(),
            action.getProcessingStatus()
        );

        if (latest != null && latest.getStatus() == action.getTargetStatus()) {
            return true;
        }

        if (latest == null) {
            return false;
        }

        boolean allSuccess = executeBranches(xid, data.getTimeoutSeconds(), action);

        if (!allSuccess) {
            scheduleRetry(data, action);
            return false;
        }

        return finalizeGlobalTx(xid, action);
    }

    private boolean executeBranches(String xid, Long timeoutSeconds, TxActionEnum action) {
        List<TccBranchTxDO> branches =
            branchTxService.listByXid(xid, action.getBranchSourceStatusList());

        if (CollUtil.isEmpty(branches)) {
            return true;
        }

        List<CompletableFuture<TccBranchResultVO>> futures =
            branches.stream()
                .map(b -> CompletableFuture.supplyAsync(
                    () -> executeBranch(b, action),
                    tccTaskExecutor
                ))
                .toList();

        List<TccBranchResultVO> results =
            awaitBranchResults(timeoutSeconds, futures, xid, branches, action);

        if (results == null) {
            return false;
        }

        return results.stream()
            .filter(Objects::nonNull)
            .allMatch(TccBranchResultVO::getSuccess);
    }

    private TccBranchResultVO executeBranch(
        TccBranchTxDO branch,
        TxActionEnum action) {

        long start = System.currentTimeMillis();

        // 原子操作：检查最终状态并尝试获取锁，避免竞态条件
        TccBranchResultVO cached = checkAndLockBranch(branch, action, start);
        if (cached != null) {
            return cached;
        }

        return invokeRemote(branch, action, start);
    }

    /**
     * 原子检查分支状态并尝试获取锁. 避免 checkFinalState 和 editStatusByMultiFrom 之间的竞态条件.
     *
     * @return 非空表示已确定结果（成功/失败），null 表示需要执行远程调用
     */
    private TccBranchResultVO checkAndLockBranch(
        TccBranchTxDO branch,
        TxActionEnum action,
        long start) {

        TccBranchTxVO latest = branchTxService.getDetail(branch.getId());

        if (latest == null) {
            return TccBranchResultVO.failure(branch.getId(), action.getBranchFailStatus(), "branch not found");
        }

        // 检查是否已经是成功状态
        if (latest.getStatus() == action.getBranchSuccessStatus()) {
            return TccBranchResultVO.success(
                branch.getId(),
                latest.getStatus(),
                System.currentTimeMillis() - start
            );
        }

        // Cancel操作时，TRY_FAIL状态的分支不需要执行Cancel，直接返回成功
        if (action == TxActionEnum.CANCEL && latest.getStatus() == BranchTxStatusEnum.TRY_FAIL) {
            return TccBranchResultVO.success(
                branch.getId(),
                latest.getStatus(),
                System.currentTimeMillis() - start
            );
        }

        // 尝试获取锁（原子更新状态）
        boolean locked = branchTxService.editStatusByMultiFrom(
            branch.getId(),
            action.getBranchLockStatusList(),
            action.getBranchProcessingStatus()
        );

        if (!locked) {
            return TccBranchResultVO.failure(branch.getId(), action.getBranchFailStatus(), "lock failed");
        }

        // null 表示未完成，需要执行远程调用
        return null;
    }

    /**
     * 同步调用远程分支. 使用 tccTaskExecutor 控制并行度.
     */
    private TccBranchResultVO invokeRemote(
        TccBranchTxDO branch,
        TxActionEnum action,
        long start) {

        String url = buildBranchUrl(branch, action);

        try {
            // 使用同步 POST 方法，由 tccTaskExecutor 控制并行
            ResultVO<Boolean> res = factory.getHttpClientExecutor(HttpClientTypeEnum.REST_CLIENT)
                .post(
                    url,
                    branch.getPayload(),
                    null,
                    new TypeReference<>() {
                    }
                );

            // 空指针判断：HTTP 客户端可能返回 null
            if (res == null) {
                String errorMsg = "http response is null for branch " + branch.getId();
                log.warn("{} url={}", errorMsg, url);
                return TccBranchResultVO.failure(branch.getId(), action.getBranchFailStatus(), errorMsg);
            }

            return handleSuccess(branch, res, action, start);
        } catch (Exception e) {
            log.error("invoke remote failed for branch {}, url={}", branch.getId(), url, e);
            return handleFail(branch, action, e);
        }
    }

    private TccBranchResultVO handleSuccess(
        TccBranchTxDO branch,
        ResultVO<Boolean> result,
        TxActionEnum action,
        long start) {

        result.errorThrow();

        TccBranchTxVO current = branchTxService.getDetail(branch.getId());

        if (current != null &&
            current.getStatus() == action.getBranchSuccessStatus()) {

            return TccBranchResultVO.success(
                branch.getId(),
                current.getStatus(),
                System.currentTimeMillis() - start
            );
        }

        branchTxService.editStatusById(
            branch.getId(),
            action.getBranchProcessingStatus(),
            action.getBranchSuccessStatus()
        );

        return TccBranchResultVO.success(
            branch.getId(),
            action.getBranchSuccessStatus(),
            System.currentTimeMillis() - start
        );
    }

    private TccBranchResultVO handleFail(
        TccBranchTxDO branch,
        TxActionEnum action,
        Throwable ex) {

        branchTxService.editStatusById(
            branch.getId(),
            action.getBranchProcessingStatus(),
            action.getBranchFailStatus(),
            getStack(ex)
        );

        return TccBranchResultVO.failure(
            branch.getId(),
            action.getBranchFailStatus(),
            ex.getMessage()
        );
    }

    private List<TccBranchResultVO> awaitBranchResults(
        Long timeout,
        List<CompletableFuture<TccBranchResultVO>> futures,
        String xid,
        List<TccBranchTxDO> branches,
        TxActionEnum action) {

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .get(timeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.warn("branch wait failed, mark incomplete branches as failed, xid={}", xid, e);
            // 超时或其他异常，标记未完成的分支为失败状态
            markTimeoutBranchesAsFailed(xid, branches, futures, action);
        }

        return futures.stream()
            .map(f -> f.getNow(null))
            .filter(Objects::nonNull)
            .map(this::validateDbState)
            .filter(Objects::nonNull)
            .toList();
    }

    /**
     * 标记超时的分支为失败状态. 避免依赖 cancel 中断远程调用，确保数据库状态一致性.
     *
     * @param xid      全局事务ID
     * @param branches 分支列表（与 futures 顺序一致）
     * @param futures  分支执行 Future 列表
     * @param action   当前动作类型
     */
    private void markTimeoutBranchesAsFailed(
        String xid,
        List<TccBranchTxDO> branches,
        List<CompletableFuture<TccBranchResultVO>> futures,
        TxActionEnum action) {

        for (int i = 0; i < branches.size(); i++) {
            TccBranchTxDO branch = branches.get(i);
            CompletableFuture<TccBranchResultVO> future = futures.get(i);

            if (!future.isDone()) {
                // 超时分支更新为失败状态
                try {
                    branchTxService.editStatusById(
                        branch.getId(),
                        action.getBranchProcessingStatus(),
                        action.getBranchFailStatus(),
                        "branch execution timeout"
                    );
                    log.warn("branch timeout marked as failed, branchId={}, xid={}, action={}",
                        branch.getId(), xid, action);
                } catch (Exception e) {
                    log.error("failed to mark timeout branch as failed, branchId={}, xid={}",
                        branch.getId(), xid, e);
                }
            }
        }
    }

    private TccBranchResultVO validateDbState(TccBranchResultVO result) {
        TccBranchTxVO latest =
            branchTxService.getDetail(result.getBranchId());

        if (latest == null) {
            return null;
        }

        BranchTxStatusEnum status = latest.getStatus();

        // 检查是否已经是最终成功状态
        if (status == BranchTxStatusEnum.CONFIRM_SUCCESS
            || status == BranchTxStatusEnum.CANCEL_SUCCESS
            || status == BranchTxStatusEnum.TRY_SUCCESS) {
            return TccBranchResultVO.success(
                result.getBranchId(),
                status,
                result.getElapsedMs()
            );
        }

        // 检查是否是最终失败状态
        if (status == BranchTxStatusEnum.CONFIRM_FAIL
            || status == BranchTxStatusEnum.CANCEL_FAIL
            || status == BranchTxStatusEnum.TRY_FAIL) {
            return TccBranchResultVO.failure(
                result.getBranchId(),
                status,
                result.getErrorMsg()
            );
        }

        // 中间状态（TRYING、CONFIRMING、CANCELING）返回原始结果
        return result;
    }

    private void scheduleRetry(TccGlobalTxVO data, TxActionEnum action) {
        TccGlobalTxVO latest = globalTxService.getDetailByXid(data.getXid());

        final boolean flag = latest != null
            && (latest.getStatus() == action.getTargetStatus()
            || latest.getStatus() == action.getFailStatus());
        if (flag) {
            return;
        }

        int count = Optional.ofNullable(data.getRetryCount()).orElse(0) + 1;

        LocalDateTime next = getNextRetryTime(data.getNextRetryTime(), count);

        globalTxService.editStatusById(
            data.getXid(),
            action.getProcessingStatus(),
            action.getFailStatus(),
            count,
            next
        );
    }

    private boolean finalizeGlobalTx(String xid, TxActionEnum action) {
        boolean result = globalTxService.editStatusByXid(
            xid,
            action.getProcessingStatus(),
            action.getTargetStatus()
        );
        if (result) {
            log.info("Global tx {} success, xid={}", action.name().toLowerCase(), xid);
            // 删除记录（可选）
            if (Boolean.TRUE.equals(this.property.getEnabledSuccessDelete())) {
                this.globalTxService.removeByXid(xid, this.property.getEnabledSuccessRealDelete());
            }
        } else {
            log.warn("Failed to update global tx to {}, xid={}",
                action.getTargetStatus(), xid);
        }
        return result;
    }

    private String getStack(Throwable ex) {
        String stack = ExceptionUtils.getStackTrace(ex);
        return Boolean.TRUE.equals(property.getEnabledExceptionCapture())
            ? StringUtils.abbreviate(stack, property.getExceptionCaptureLength())
            : stack;
    }

    private String buildBranchUrl(TccBranchTxDO branch, TxActionEnum action) {
        String path = action == TxActionEnum.CONFIRM
            ? branch.getConfirmUrl()
            : branch.getCancelUrl();

        return String.format("%s%s", branch.getContextPath(), path);
    }
}
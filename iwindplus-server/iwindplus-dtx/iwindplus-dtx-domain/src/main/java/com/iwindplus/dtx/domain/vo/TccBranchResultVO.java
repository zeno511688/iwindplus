package com.iwindplus.dtx.domain.vo;

import cn.hutool.core.util.StrUtil;
import com.iwindplus.dtx.domain.enums.BranchTxStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * tcc分支事务执行结果视图对象.
 *
 * @author zengdegui
 * @since 2026/02/04 22:26
 */
@Schema(description = "tcc分支事务执行结果视图对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TccBranchResultVO implements Serializable {

    /**
     * 分支ID.
     */
    @Schema(description = "分支ID")
    private Long branchId;

    /**
     * 是否成功.
     */
    @Schema(description = "是否成功")
    private Boolean success;

    /**
     * 执行后的状态.
     */
    @Schema(description = "执行后的状态")
    private BranchTxStatusEnum finalStatus;

    /**
     * 错误信息（成功时为null）.
     */
    @Schema(description = "错误信息")
    private String errorMsg;

    /**
     * 执行耗时（毫秒）.
     */
    @Schema(description = "执行耗时")
    private Long elapsedMs;

    /**
     * 执行时间戳.
     */
    @Schema(description = "执行时间戳")
    private LocalDateTime executeTime;

    /**
     * 构造成功结果.
     *
     * @param branchId    分支ID
     * @param finalStatus 最终状态
     * @param elapsedMs   耗时毫秒
     * @return TccBranchResultVO
     */
    public static TccBranchResultVO success(Long branchId, BranchTxStatusEnum finalStatus, Long elapsedMs) {
        return TccBranchResultVO.builder()
            .branchId(branchId)
            .success(Boolean.TRUE)
            .finalStatus(finalStatus)
            .elapsedMs(elapsedMs)
            .executeTime(LocalDateTime.now())
            .build();
    }

    /**
     * 构造成功结果（无耗时）.
     *
     * @param branchId    分支ID
     * @param finalStatus 最终状态
     * @return TccBranchResultVO
     */
    public static TccBranchResultVO success(Long branchId, BranchTxStatusEnum finalStatus) {
        return success(branchId, finalStatus, 0L);
    }

    /**
     * 构造失败结果.
     *
     * @param branchId    分支ID
     * @param finalStatus 最终状态
     * @param errorMsg    错误信息
     * @return TccBranchResultVO
     */
    public static TccBranchResultVO failure(Long branchId, BranchTxStatusEnum finalStatus, String errorMsg) {
        return TccBranchResultVO.builder()
            .branchId(branchId)
            .success(Boolean.FALSE)
            .finalStatus(finalStatus)
            .errorMsg(errorMsg)
            .executeTime(LocalDateTime.now())
            .build();
    }

    /**
     * 是否已最终完成（成功或明确失败）.
     *
     * @return boolean
     */
    public boolean getFinished() {
        return Boolean.TRUE.equals(success) || finalStatus != null;
    }

    /**
     * 是否需要重试.
     *
     * @return boolean
     */
    public boolean needRetry() {
        return Boolean.FALSE.equals(success) &&
            (finalStatus == BranchTxStatusEnum.CONFIRM_FAIL ||
                finalStatus == BranchTxStatusEnum.CANCEL_FAIL);
    }

    /**
     * 获取简要描述.
     *
     * @return String
     */
    public String getSummary() {
        if (Boolean.TRUE.equals(success)) {
            return String.format("Branch[%d] %s success in %dms",
                branchId, finalStatus, elapsedMs);
        } else {
            return String.format("Branch[%d] %s failed: %s",
                branchId, finalStatus, StrUtil.subSufByLength(errorMsg, 100));
        }
    }
}
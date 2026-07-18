package com.iwindplus.dtx.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * tcc分支事务批量执行结果视图对象.
 *
 * @author zengdegui
 * @since 2026/03/01 18:06
 */
@Schema(description = "tcc分支事务批量执行结果视图对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TccBatchBranchResultVO implements Serializable {

    /**
     * 全局事务ID.
     */
    @Schema(description = "全局事务ID")
    private String xid;

    /**
     * 分支结果列表.
     */
    @Schema(description = "分支结果列表")
    private List<TccBranchResultVO> results;

    /**
     * 开始时间.
     */
    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    /**
     * 结束时间.
     */
    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    /**
     * 获取成功数量.
     *
     * @return long
     */
    public long getSuccessCount() {
        return results.stream().filter(TccBranchResultVO::getSuccess).count();
    }

    /**
     * 获取失败数量.
     *
     * @return long
     */
    public long getFailureCount() {
        return results.size() - getSuccessCount();
    }

    /**
     * 获取总耗时（毫秒）.
     *
     * @return long
     */
    public long getTotalElapsedMs() {
        return Duration.between(startTime, endTime).toMillis();
    }

    /**
     * 获取执行报告.
     *
     * @return String
     */
    public String getReport() {
        long total = results.size();
        long success = getSuccessCount();
        long failed = getFailureCount();

        return String.format(
            "Batch[%s] completed: total=%d, success=%d, failed=%d, elapsed=%dms%nDetails:%n%s",
            xid, total, success, failed, getTotalElapsedMs(),
            results.stream().map(TccBranchResultVO::getSummary).collect(Collectors.joining(System.lineSeparator()))
        );
    }
}
/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.document.domain.dto;

import com.iwindplus.base.document.domain.enums.ExportTaskStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 导出任务状态流转数据传输对象.
 *
 * @author zengdegui
 * @since 2026/08/12
 */
@Schema(description = "导出任务状态流转数据传输对象")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportTaskStatusEditDTO {

    /**
     * 主键（必填）.
     */
    @Schema(description = "主键")
    private Long id;

    /**
     * 从状态（可选，CAS条件，为空则不校验）.
     */
    @Schema(description = "从状态（CAS条件）")
    private ExportTaskStatusEnum from;

    /**
     * 到状态（可选，为空则不更新状态）.
     */
    @Schema(description = "到状态")
    private ExportTaskStatusEnum to;

    /**
     * 耗时（可选）.
     */
    @Schema(description = "耗时")
    private Long costTime;

    /**
     * 错误信息（可选）.
     */
    @Schema(description = "错误信息")
    private String errorMsg;

    /**
     * 重试次数（可选）.
     */
    @Schema(description = "重试次数")
    private Integer retryCount;

    /**
     * 下次重试时间（可选，主任务使用）.
     */
    @Schema(description = "下次重试时间")
    private Long nextRetryTime;

    /**
     * 租约/回调等待截止时间（可选）.
     */
    @Schema(description = "截止时间")
    private Long expireTime;

    /**
     * 导出数据总数.
     */
    @Schema(description = "导出数据总数")
    private Long totalCount;

    /**
     * 已导出数量.
     */
    @Schema(description = "已导出数量")
    private Long exportedCount;

    /**
     * 进度比例（可选，0-100）.
     */
    @Schema(description = "进度比例（0-100）")
    private Integer progress;

    /**
     * 扩展对象.
     */
    @Schema(description = "扩展对象")
    private ExportTaskExtDTO ext;
}

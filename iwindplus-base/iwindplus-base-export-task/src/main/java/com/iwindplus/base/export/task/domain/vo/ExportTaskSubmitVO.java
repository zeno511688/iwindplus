/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.export.task.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 导出任务提交返回VO.
 *
 * @author zengdegui
 * @since 2026/08/29
 */
@Schema(description = "导出任务提交返回VO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportTaskSubmitVO implements Serializable {

    /**
     * 主键.
     */
    @Schema(description = "主键")
    private Long id;

    /**
     * 业务流水号.
     */
    @Schema(description = "业务流水号")
    private String bizNumber;
}

/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.export.task.domain.dto;

import com.iwindplus.base.domain.dto.DbPageDTO;
import com.iwindplus.base.export.task.domain.enums.ExportTaskStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 导出任务查询对象.
 *
 * @author zengdegui
 * @since 2026/08/27
 */
@Schema(description = "导出任务查询对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ExportTaskSearchDTO extends DbPageDTO {

    /**
     * 状态（PENDING：待执行，EXECUTING：执行中，SUCCESS：成功，FAILED：失败，DISCARD：丢弃）.
     */
    @Schema(description = "状态（PENDING：待执行，EXECUTING：执行中，SUCCESS：成功，FAILED：失败，DISCARD：丢弃）")
    private ExportTaskStatusEnum status;

    /**
     * 业务流水号.
     */
    @Schema(description = "业务流水号")
    private String bizNumber;

    /**
     * 文件名.
     */
    @Schema(description = "文件名")
    private String fileName;

    /**
     * 文件路径.
     */
    @Schema(description = "文件路径")
    private String filePath;

    /**
     * 执行器名称.
     */
    @Schema(description = "执行器名称")
    private String executeName;
}

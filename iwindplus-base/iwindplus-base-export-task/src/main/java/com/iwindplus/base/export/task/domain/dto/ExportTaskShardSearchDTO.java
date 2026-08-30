/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.export.task.domain.dto;

import com.iwindplus.base.export.task.domain.enums.ExportTaskStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 导出任务分片查询DTO.
 *
 * @author zengdegui
 * @since 2026/08/27
 */
@Schema(description = "导出任务分片查询DTO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportTaskShardSearchDTO {

    /**
     * 状态列表.
     */
    @Schema(description = "状态列表")
    private List<ExportTaskStatusEnum> statusList;

    /**
     * 分片索引.
     */
    @Schema(description = "分片索引")
    private Integer shardIndex;

    /**
     * 分片总数.
     */
    @Schema(description = "分片总数")
    private Integer shardTotal;

    /**
     * 每页条数.
     */
    @Schema(description = "每页条数")
    private Integer size;

    /**
     * 上一次查询的最后主键.
     */
    @Schema(description = "上一次查询的最后主键")
    private Long lastId;
}

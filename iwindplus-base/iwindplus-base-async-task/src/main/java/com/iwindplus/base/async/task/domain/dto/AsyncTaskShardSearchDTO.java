/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.task.domain.dto;

import com.iwindplus.base.async.task.domain.enums.AsyncTaskStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 异步任务分片搜索数据传输对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "异步任务分片搜索数据传输对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AsyncTaskShardSearchDTO implements Serializable {

    /**
     * 每页显示条数.
     */
    @Schema(description = "每页显示条数")
    private Integer size;

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
     * 最后一条记录的ID.
     */
    @Schema(description = "最后一条记录的ID")
    private Long lastId;

    /**
     * 状态（PENDING：待执行，EXECUTING：执行中，WAITING：等待中，SUCCESS：成功，FAILED：失败，DISCARD：丢弃）.
     */
    @Schema(description = "状态（PENDING：待执行，EXECUTING：执行中，WAITING：等待中，SUCCESS：成功，FAILED：失败，DISCARD：丢弃）")
    private AsyncTaskStatusEnum status;

    /**
     * 状态列表.
     */
    @Schema(description = "状态列表")
    private List<AsyncTaskStatusEnum> statusList;
}

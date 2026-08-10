/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.domain.dto;

import com.iwindplus.flow.domain.enums.FlowInstanceCallbackStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 流程实例回调分片搜索数据传输对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "流程实例回调分片搜索数据传输对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FlowInstanceCallbackShardSearchDTO implements Serializable {

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
     * 状态.
     */
    @Schema(description = "状态")
    private FlowInstanceCallbackStatusEnum status;

    /**
     * 状态列表.
     */
    @Schema(description = "状态列表")
    private List<FlowInstanceCallbackStatusEnum> statusList;
}

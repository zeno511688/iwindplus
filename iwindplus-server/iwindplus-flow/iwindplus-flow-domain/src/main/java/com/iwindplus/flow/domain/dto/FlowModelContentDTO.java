/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 流程模型内容数据传输对象.
 *
 * @author zengdegui
 * @since 2024/12/30 23:30
 */
@Schema(description = "流程模型内容数据传输对象")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlowModelContentDTO implements Serializable {

    /**
     * 节点.
     */
    @Schema(description = "节点")
    private FlowNodeDTO node;

}

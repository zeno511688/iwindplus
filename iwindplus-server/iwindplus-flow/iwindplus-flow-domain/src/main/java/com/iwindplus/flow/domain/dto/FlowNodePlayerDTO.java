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
 * 流程节点参与人数据传输对象.
 *
 * @author zengdegui
 * @since 2024/12/30 23:30
 */
@Schema(description = "流程节点参与人数据传输对象")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlowNodePlayerDTO implements Serializable {

    /**
     * 主键.
     */
    @Schema(description = "主键")
    private Long id;

    /**
     * 名称.
     */
    @Schema(description = "名称")
    private String name;
}

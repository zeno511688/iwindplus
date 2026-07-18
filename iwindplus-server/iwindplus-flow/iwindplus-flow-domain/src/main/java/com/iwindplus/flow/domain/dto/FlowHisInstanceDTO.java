/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.domain.dto;

import com.iwindplus.flow.domain.enums.FlowInstanceStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 历史流程实例数据传输对象.
 *
 * @author zengdegui
 * @since 2026/01/11 20:14
 */
@Schema(description = "历史流程实例数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FlowHisInstanceDTO extends FlowInstanceDTO {

    /**
     * 实例状态.
     */
    @Schema(description = "实例状态")
    private FlowInstanceStatusEnum status;

    /**
     * 耗时.
     */
    @Schema(description = "耗时")
    private Long takeTime;
}

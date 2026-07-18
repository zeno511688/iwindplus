/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 流程审批结果视图对象.
 *
 * @author zengdegui
 * @since 2026/05/22 22:06
 */
@Schema(description = "流程审批结果视图对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FlowApprovalResultVO implements Serializable {

    /**
     * 是否审批完成.
     */
    @Schema(description = "是否审批完成")
    private Boolean approved;

    /**
     * 需要删除的参与人.
     */
    @Schema(description = "需要删除的参与人")
    private List<Long> removePlayerIds;

    /**
     * 是否记录中间审批.
     */
    @Schema(description = "是否记录中间审批")
    private Boolean recordIntermediate;
}

/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.domain.dto;

import com.iwindplus.base.domain.vo.UserBaseVO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 驳回任务数据传输对象.
 *
 * @author zengdegui
 * @since 2026/05/20 23:23
 */
@Schema(description = "驳回任务数据传输对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FlowRejectTaskDTO implements Serializable {

    /**
     * 任务主键.
     */
    @Schema(description = "任务主键")
    @NotNull(message = "{taskId.notEmpty}")
    private Long taskId;

    /**
     * 退回目标节点编码（为空则终止整个流程，非空则退回到指定节点）.
     */
    @Schema(description = "退回目标节点编码，为空表示直接驳回终止流程")
    private String targetNodeCode;

    /**
     * 审批意见.
     */
    @Schema(description = "审批意见")
    private String comment;

    /**
     * 当前用户
     */
    @Schema(description = "当前用户")
    private UserBaseVO currentUser;
}

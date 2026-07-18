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
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 审批通过任务数据传输对象.
 *
 * @author zengdegui
 * @since 2026/05/20 23:15
 */
@Schema(description = "审批通过任务数据传输对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FlowApproveTaskDTO implements Serializable {

    /**
     * 任务主键
     */
    @Schema(description = "任务主键")
    @NotNull(message = "{taskId.notEmpty}")
    private Long taskId;

    /**
     * 流程变量
     */
    @Schema(description = "流程变量")
    private Map<String, Object> variables;

    /**
     * 审批意见
     */
    @Schema(description = "审批意见")
    @Size(max = 100, message = "{comment.length}")
    private String comment;

    /**
     * 当前用户
     */
    @Schema(description = "当前用户")
    private UserBaseVO currentUser;
}

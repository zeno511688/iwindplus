/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.domain.dto;

import com.iwindplus.base.domain.vo.UserBaseVO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 流程跳转任务数据传输对象.
 *
 * @author zengdegui
 * @since 2026/05/20 23:24
 */
@Schema(description = "流程跳转任务数据传输对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FlowJumpTaskDTO implements Serializable {

    /**
     * 当前任务主键.
     */
    @Schema(description = "当前任务主键")
    @NotNull(message = "{taskId.notEmpty}")
    private Long taskId;

    /**
     * 目标节点编码.
     */
    @Schema(description = "目标节点编码")
    @NotBlank(message = "{targetNodeCode.notEmpty}")
    private String targetNodeCode;

    /**
     * 当前用户
     */
    @Schema(description = "当前用户")
    private UserBaseVO currentUser;
}

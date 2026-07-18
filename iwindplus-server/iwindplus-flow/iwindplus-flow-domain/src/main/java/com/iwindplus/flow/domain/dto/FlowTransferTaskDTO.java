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
 * 转交任务数据传输对象.
 *
 * @author zengdegui
 * @since 2026/05/20 23:31
 */
@Schema(description = "转交任务数据传输对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FlowTransferTaskDTO implements Serializable {

    /**
     * 任务主键.
     */
    @Schema(description = "任务主键")
    @NotNull(message = "{taskId.notEmpty}")
    private Long taskId;

    /**
     * 转交目标用户主键.
     */
    @Schema(description = "转交目标用户主键")
    @NotNull(message = "{toUserId.notEmpty}")
    private Long toUserId;

    /**
     * 转交目标用户姓名.
     */
    @Schema(description = "转交目标用户姓名")
    @NotBlank(message = "{toUserName.notEmpty}")
    private String toUserName;

    /**
     * 当前用户
     */
    @Schema(description = "当前用户")
    private UserBaseVO currentUser;
}
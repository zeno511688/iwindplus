/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.domain.dto;

import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdStatusEnum;
import com.iwindplus.base.async.cmd.domain.enums.DispatchModeEnum;
import com.iwindplus.base.domain.dto.DbVersionBaseDTO;
import com.iwindplus.base.domain.validation.EditGroup;
import com.iwindplus.base.domain.validation.SaveGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;

/**
 * 异步命令数据传输对象.
 *
 * @author zengdegui
 * @since 2025/9/14
 */
@Schema(description = "异步命令数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AsyncCmdDTO extends DbVersionBaseDTO {

    /**
     * 状态（TO_BE_EXECUTE：待执行，EXECUTE：执行，SUCCESS：成功，FAILED：失败，DISCARD：丢弃）.
     */
    @Schema(description = "状态（TO_BE_EXECUTE：待执行，EXECUTE：执行，SUCCESS：成功，FAILED：失败，DISCARD：丢弃）")
    private AsyncCmdStatusEnum status;

    /**
     * 环境.
     */
    @Schema(description = "环境")
    private String env;

    /**
     * 业务类型，例如 ORDER、USER.
     */
    @Schema(description = "业务类型，例如 ORDER、USER")
    @NotBlank(message = "{bizType.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 100, message = "{bizType.length}", groups = {SaveGroup.class, EditGroup.class})
    private String bizType;

    /**
     * 事件类型，例如 ORDER_CREATED.
     */
    @Schema(description = "事件类型，例如 ORDER_CREATED")
    @NotBlank(message = "{eventType.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 100, message = "{eventType.length}", groups = {SaveGroup.class, EditGroup.class})
    private String eventType;

    /**
     * 业务流水号.
     */
    @Schema(description = "业务流水号")
    @NotBlank(message = "{bizNumber.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 100, message = "{bizNumber.length}", groups = {SaveGroup.class, EditGroup.class})
    private String bizNumber;

    /**
     * 调度模式（DISPATCH：调度中心，EXECUTE：异步，UNKNOWN：未知）.
     */
    @Schema(description = "调度模式（DISPATCH：调度中心，EXECUTE：异步，UNKNOWN：未知）")
    private DispatchModeEnum dispatchMode;

    /**
     * 执行器名称.
     */
    @Schema(description = "执行器名称")
    @NotBlank(message = "{executeName.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 100, message = "{executeName.length}", groups = {SaveGroup.class, EditGroup.class})
    private String executeName;

    /**
     * 内容.
     */
    @Schema(description = "内容")
    @NotEmpty(message = "{content.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    private Map<String, Object> content;

    /**
     * 过期时间.
     */
    @Schema(description = "过期时间")
    private LocalDateTime expireTime;

    /**
     * 下一次重试时间.
     */
    @Schema(description = "下一次重试时间")
    private LocalDateTime nextRetryTime;

    /**
     * 重试次数.
     */
    @Schema(description = "重试次数")
    private Integer retryCount;

    /**
     * 错误信息.
     */
    @Schema(description = "错误信息")
    private String errorMsg;
}

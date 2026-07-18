/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.domain.dto;

import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdStatusEnum;
import com.iwindplus.base.domain.dto.DbPageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 异步命令搜索数据传输对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "异步命令搜索数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AsyncCmdSearchDTO extends DbPageDTO {

    /**
     * 任务名称.
     */
    @Schema(description = "任务名称")
    private String taskName;

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
    private String bizType;

    /**
     * 事件类型，例如 ORDER_CREATED.
     */
    @Schema(description = "事件类型，例如 ORDER_CREATED")
    private String eventType;

    /**
     * 业务流水号.
     */
    @Schema(description = "业务流水号")
    private String bizNumber;

    /**
     * 状态列表.
     */
    @Schema(description = "状态列表")
    private List<AsyncCmdStatusEnum> statusList;

    /**
     * 执行器名称.
     */
    @Schema(description = "执行器名称")
    private String executeName;

    /**
     * 过期时间.
     */
    @Schema(description = "过期时间")
    private LocalDateTime expireTime;

    /**
     * 重试时间.
     */
    @Schema(description = "重试时间")
    private LocalDateTime retryTime;

    /**
     * 是否显示内容.
     */
    @Schema(description = "是否显示内容")
    private Boolean showContent;
}

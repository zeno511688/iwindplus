/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.domain.vo;

import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdStatusEnum;
import com.iwindplus.base.domain.dto.DbVersionBaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 异步命令子任务基本字段视图对象.
 *
 * @author zengdegui
 * @since 2025/9/14
 */
@Schema(description = "异步命令子任务基本字段视图对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AsyncCmdSubBaseVO extends DbVersionBaseDTO {

    /**
     * 状态（PENDING：待执行，EXECUTE：执行，WAITING：等待中，SUCCESS：成功，FAILED：失败）.
     */
    @Schema(description = "状态（PENDING：待执行，EXECUTE：执行，WAITING：等待中，SUCCESS：成功，FAILED：失败）")
    private AsyncCmdStatusEnum status;

    /**
     * 业务名称.
     */
    @Schema(description = "业务名称")
    private String bizName;

    /**
     * 业务key，例如 ORDER.
     */
    @Schema(description = "业务key，例如 ORDER")
    private String bizKey;

    /**
     * 业务类型，例如 ORDER_CREATE.
     */
    @Schema(description = "业务类型，例如 ORDER_CREATE")
    private String bizType;

    /**
     * 业务流水号.
     */
    @Schema(description = "业务流水号")
    private String bizNumber;

    /**
     * 阶段（同阶段子任务并发）.
     */
    @Schema(description = "阶段")
    private Integer stage;

    /**
     * 排序号
     */
    @Schema(description = "排序号")
    private Integer seq;

    /**
     * 耗时.
     */
    @Schema(description = "耗时")
    private Long costTime;

    /**
     * 回调等待截止时间（异步等待超时兜底）.
     */
    @Schema(description = "回调等待截止时间")
    private Long expireTime;

    /**
     * 进度比例（0-100）.
     */
    @Schema(description = "进度比例（0-100）")
    private Integer progress;

    /**
     * 异步命令主键.
     */
    @Schema(description = "异步命令主键")
    private Long asyncCmdId;
}

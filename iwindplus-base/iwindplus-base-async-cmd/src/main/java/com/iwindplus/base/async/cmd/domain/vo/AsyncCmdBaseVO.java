/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.domain.vo;

import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdStatusEnum;
import com.iwindplus.base.domain.vo.DbVersionBaseVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 异步命令基本字段视图对象.
 *
 * @author zengdegui
 * @since 2025/9/14
 */
@Schema(description = "异步命令基本字段视图对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AsyncCmdBaseVO extends DbVersionBaseVO {

    /**
     * 状态（PENDING：待执行，EXECUTE：执行，WAITING：等待中，SUCCESS：成功，FAILED：失败，DISCARD：丢弃）.
     */
    @Schema(description = "状态（PENDING：待执行，EXECUTE：执行，WAITING：等待中，SUCCESS：成功，FAILED：失败，DISCARD：丢弃）")
    private AsyncCmdStatusEnum status;

    /**
     * 环境.
     */
    @Schema(description = "环境")
    private String env;

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
     * 子任务总数.
     */
    @Schema(description = "子任务总数")
    private Integer subTaskCount;

    /**
     * 累计耗时.
     */
    @Schema(description = "耗时")
    private Long costTime;

    /**
     * 进度比例（0-100）.
     */
    @Schema(description = "进度比例（0-100）")
    private Integer progress;
}

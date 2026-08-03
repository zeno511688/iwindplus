/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 异步命令提交结果视图对象.
 *
 * @author zengdegui
 * @since 2025/9/14
 */
@Schema(description = "异步命令提交结果视图对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AsyncCmdSubmitVO implements Serializable {

    /**
     * 主键.
     */
    @Schema(description = "主键")
    private Long id;

    /**
     * 业务key（必填），例如 ORDER.
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
}

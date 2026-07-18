/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 异步命令执行基础数据传输对象.
 *
 * @author zengdegui
 * @since 2025/12/28 00:22
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AsyncCmdExecutorBaseBO implements Serializable {

    /**
     * 业务类型（必填），例如 ORDER、USER.
     */
    @Schema(description = "业务类型，例如 ORDER、USER")
    private String bizType;

    /**
     * 事件类型（必填），例如 ORDER_CREATED.
     */
    @Schema(description = "事件类型，例如 ORDER_CREATED")
    private String eventType;

    /**
     * 业务流水号（必填）.
     */
    @Schema(description = "业务流水号")
    private String bizNumber;
}

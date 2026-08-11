/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.domain.dto;

import com.iwindplus.base.async.cmd.support.AsyncCmdTaskHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 异步命令组任务提交数据传输对象.
 *
 * @author zengdegui
 * @since 2025/12/28 00:22
 */
@Schema(description = "异步命令组任务提交数据传输对象")
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class AsyncCmdGroupSubmitDTO extends AsyncCmdSubmitBaseDTO {

    /**
     * 执行器类（必填）.
     */
    @Schema(description = "执行器类")
    private Class<? extends AsyncCmdTaskHandler> executorClass;

    /**
     * 备注（可选）.
     */
    @Schema(description = "备注")
    private String remark;

    /**
     * 是否需要回调（可选）.
     */
    @Schema(description = "是否需要回调")
    private Boolean needCallback;

    /**
     * 是否先回调（可选，先主执行回调成功再分发子任务）.
     */
    @Schema(description = "是否先回调（先主执行回调成功再分发子任务）")
    private Boolean callbackFirst;

    /**
     * 是否需要显示（可选，查进度时用）.
     */
    @Schema(description = "是否需要显示")
    private Boolean needDisplay;

    /**
     * 子任务列表（必填）.
     */
    @Schema(description = "子任务列表")
    private List<AsyncCmdSubSubmitDTO> subTasks;
}

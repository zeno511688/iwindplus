/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 异步命令组任务视图对象.
 *
 * @author zengdegui
 * @since 2026/08/02 13:59
 */
@Schema(description = "异步命令组任务视图对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AsyncCmdGroupVO extends AsyncCmdVO {

    /**
     * 子任务集合.
     */
    @Schema(description = "子任务集合")
    private List<AsyncCmdSubVO> subTasks;
}

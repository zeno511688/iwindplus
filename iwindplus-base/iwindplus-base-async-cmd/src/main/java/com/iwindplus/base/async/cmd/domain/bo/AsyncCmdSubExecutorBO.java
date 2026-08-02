/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.domain.bo;

import com.iwindplus.base.async.cmd.support.AsyncCmdTaskHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 异步命令子任务业务数据传输对象.
 *
 * @author zengdegui
 * @since 2025/12/29
 */
@Schema(description = "异步命令子任务业务数据传输对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AsyncCmdSubExecutorBO implements Serializable {

    /**
     * 执行顺序（必填）.
     */
    @Schema(description = "执行顺序（必填）")
    private Integer taskOrder;

    /**
     * 内容（必填）.
     */
    @Schema(description = "内容（必填）")
    private Map<String, Object> content;

    /**
     * 执行器类（必填）.
     */
    @Schema(description = "执行器类（必填）")
    private Class<? extends AsyncCmdTaskHandler> executorClass;

    /**
     * 备注.
     */
    @Schema(description = "备注")
    private String remark;
}

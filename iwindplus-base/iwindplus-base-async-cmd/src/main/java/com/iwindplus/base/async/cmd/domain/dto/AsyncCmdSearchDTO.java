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
     * 状态（TO_BE_EXECUTE：待执行，EXECUTE：执行，ASYNC_WAIT：异步等待，SUCCESS：成功，FAILED：失败，DISCARD：丢弃）.
     */
    @Schema(description = "状态（TO_BE_EXECUTE：待执行，EXECUTE：执行，ASYNC_WAIT：异步等待，SUCCESS：成功，FAILED：失败，DISCARD：丢弃）")
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
     * 状态列表.
     */
    @Schema(description = "状态列表")
    private List<AsyncCmdStatusEnum> statusList;

    /**
     * 执行器名称.
     */
    @Schema(description = "执行器名称")
    private String executeName;
}

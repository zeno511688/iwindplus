/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.domain.dto;

import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdStatusEnum;
import com.iwindplus.base.domain.dto.DbVersionBaseDTO;
import com.iwindplus.base.domain.validation.EditGroup;
import com.iwindplus.base.domain.validation.SaveGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;

/**
 * 异步命令子任务数据传输对象.
 *
 * @author zengdegui
 * @since 2025/9/14
 */
@Schema(description = "异步命令子任务数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AsyncCmdSubDTO extends DbVersionBaseDTO {

    /**
     * 状态（TO_BE_EXECUTE：待执行，EXECUTE：执行，ASYNC_WAIT：异步等待，SUCCESS：成功，FAILED：失败）.
     */
    @Schema(description = "状态（TO_BE_EXECUTE：待执行，EXECUTE：执行，ASYNC_WAIT：异步等待，SUCCESS：成功，FAILED：失败）")
    private AsyncCmdStatusEnum status;

    /**
     * 业务名称.
     */
    @Schema(description = "业务名称")
    @Length(max = 100, message = "{bizName.length}", groups = {SaveGroup.class, EditGroup.class})
    private String bizName;

    /**
     * 业务key，例如 ORDER.
     */
    @Schema(description = "业务key，例如 ORDER")
    @Length(max = 100, message = "{bizKey.length}", groups = {SaveGroup.class, EditGroup.class})
    private String bizKey;

    /**
     * 业务类型，例如 ORDER_CREATE.
     */
    @Schema(description = "业务类型，例如 ORDER_CREATE")
    @NotBlank(message = "{bizType.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 100, message = "{bizType.length}", groups = {SaveGroup.class, EditGroup.class})
    private String bizType;

    /**
     * 业务流水号.
     */
    @Schema(description = "业务流水号")
    @Length(max = 100, message = "{bizNumber.length}", groups = {SaveGroup.class, EditGroup.class})
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
    @NotNull(message = "{seq.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    private Integer seq;

    /**
     * 执行器名称.
     */
    @Schema(description = "执行器名称")
    @NotBlank(message = "{executeName.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 100, message = "{executeName.length}", groups = {SaveGroup.class, EditGroup.class})
    private String executeName;

    /**
     * 参数.
     */
    @Schema(description = "参数")
    @NotEmpty(message = "{param.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    private Map<String, Object> param;

    /**
     * 结果（供后续任务读取，同一批互相不可见，由于是并发）.
     */
    @Schema(description = "结果")
    private Map<String, Object> result;

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

    /**
     * 耗时.
     */
    @Schema(description = "耗时")
    private Long costTime;

    /**
     * 是否需要回调.
     */
    @Schema(description = "是否需要回调")
    private Boolean needCallback;

    /**
     * 是否需要显示（查进度时用）.
     */
    @Schema(description = "是否需要显示")
    private Boolean needDisplay;

    /**
     * 扩展对象.
     */
    @Schema(description = "扩展对象")
    private AsyncCmdSubExtDTO ext;

    /**
     * 异步命令主键.
     */
    @Schema(description = "异步命令主键")
    private Long asyncCmdId;
}

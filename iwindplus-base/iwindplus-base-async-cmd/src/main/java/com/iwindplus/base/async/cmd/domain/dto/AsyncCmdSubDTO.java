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
     * 状态（TO_BE_EXECUTE：待执行，EXECUTE：执行，SUCCESS：成功，FAILED：失败）.
     */
    @Schema(description = "状态（TO_BE_EXECUTE：待执行，EXECUTE：执行，SUCCESS：成功，FAILED：失败）")
    private AsyncCmdStatusEnum status;

    /**
     * 业务类型，例如 ORDER、USER.
     */
    @Schema(description = "业务类型，例如 ORDER、USER")
    @NotBlank(message = "{bizType.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 100, message = "{bizType.length}", groups = {SaveGroup.class, EditGroup.class})
    private String bizType;

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
     * 内容.
     */
    @Schema(description = "内容")
    @NotEmpty(message = "{content.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    private Map<String, Object> content;

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
     * 重试次数.
     */
    @Schema(description = "异步命令主键")
    private Long asyncCmdId;
}

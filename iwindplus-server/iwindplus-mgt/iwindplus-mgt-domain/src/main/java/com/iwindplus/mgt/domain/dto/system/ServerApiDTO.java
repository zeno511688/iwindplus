/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.dto.system;

import com.iwindplus.base.domain.dto.DbVersionBaseDTO;
import com.iwindplus.base.domain.validation.EditGroup;
import com.iwindplus.base.domain.validation.SaveGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;

/**
 * 服务API数据传输对象.
 *
 * @author zengdegui
 * @since 2020/4/14
 */
@Schema(description = "服务API数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ServerApiDTO extends DbVersionBaseDTO {

    /**
     * 应用名称.
     */
    @Schema(description = "应用名称")
    @NotBlank(message = "{appName.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 100, message = "{appName.length}", groups = {SaveGroup.class, EditGroup.class})
    private String appName;

    /**
     * 控制器名称.
     */
    @Schema(description = "控制器名称")
    @NotBlank(message = "{controllerName.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 100, message = "{controllerName.length}", groups = {SaveGroup.class, EditGroup.class})
    private String controllerName;

    /**
     * 请求方式.
     */
    @Schema(description = "请求方式")
    @NotBlank(message = "{requestMethod.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 100, message = "{requestMethod.length}", groups = {SaveGroup.class, EditGroup.class})
    private String requestMethod;

    /**
     * API名称.
     */
    @Schema(description = "API名称")
    @NotBlank(message = "{apiName.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 100, message = "{apiName.length}", groups = {SaveGroup.class, EditGroup.class})
    private String apiName;

    /**
     * API路径.
     */
    @Schema(description = "API路径")
    @NotBlank(message = "{apiUrl.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 255, message = "{apiUrl.length}", groups = {SaveGroup.class, EditGroup.class})
    private String apiUrl;

    /**
     * 限流次数，每个时间窗口允许请求数量.
     */
    @Schema(description = "限流次数，每个时间窗口允许请求数量")
    private Long rate;

    /**
     * 排序号.
     */
    @Schema(description = "排序号")
    private Integer seq;

    /**
     * 是否隐藏.
     */
    @Schema(description = "是否隐藏")
    private Boolean hideFlag;
}

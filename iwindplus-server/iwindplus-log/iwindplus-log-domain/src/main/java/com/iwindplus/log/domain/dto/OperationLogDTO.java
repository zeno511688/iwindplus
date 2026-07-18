/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.log.domain.dto;

import com.iwindplus.base.domain.dto.DbBaseTwoDTO;
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
 * 操作日志.
 *
 * @author zengdegui
 * @since 2024/4/10
 */
@Schema(description = "操作日志对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class OperationLogDTO extends DbBaseTwoDTO {

    /**
     * 请求唯一标识.
     */
    @Schema(description = "请求唯一标识")
    @Length(max = 100, message = "{requestId.length}", groups = {SaveGroup.class, EditGroup.class})
    private String requestId;

    /**
     * 访问实例.
     */
    @Schema(description = "访问实例")
    @NotBlank(message = "{targetServer.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 100, message = "{targetServer.length}", groups = {SaveGroup.class, EditGroup.class})
    private String targetServer;

    /**
     * 业务流水号.
     */
    @Schema(description = "业务流水号")
    @Length(max = 100, message = "{bizNumber.length}", groups = {SaveGroup.class, EditGroup.class})
    private String bizNumber;

    /**
     * 业务类型.
     */
    @Schema(description = "业务类型")
    @NotBlank(message = "{bizType.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 100, message = "{bizType.length}", groups = {SaveGroup.class, EditGroup.class})
    private String bizType;

    /**
     * 操作类型.
     */
    @Schema(description = "操作类型")
    @NotBlank(message = "{operateType.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 100, message = "{operateType.length}", groups = {SaveGroup.class, EditGroup.class})
    private String operateType;

    /**
     * 操作名称.
     */
    @Schema(description = "操作名称")
    @NotBlank(message = "{operateName.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 100, message = "{operateName.length}", groups = {SaveGroup.class, EditGroup.class})
    private String operateName;

    /**
     * 操作描述.
     */
    @Schema(description = "操作描述")
    @NotBlank(message = "{operateDesc.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 255, message = "{operateDesc.length}", groups = {SaveGroup.class, EditGroup.class})
    private String operateDesc;

    /**
     * 请求参数.
     */
    @Schema(description = "请求参数")
    private String requestParam;

    /**
     * 请求体.
     */
    @Schema(description = "请求体")
    private String requestBody;

    /**
     * 响应体.
     */
    @Schema(description = "响应体")
    private String responseBody;

    /**
     * 请求时间.
     */
    @Schema(description = "请求时间")
    @Length(max = 100, message = "{requestTime.length}", groups = {SaveGroup.class, EditGroup.class})
    private String requestTime;

    /**
     * 响应时间.
     */
    @Schema(description = "响应时间")
    @Length(max = 100, message = "{responseTime.length}", groups = {SaveGroup.class, EditGroup.class})
    private String responseTime;

    /**
     * 执行时间（ms）.
     */
    @Schema(description = "执行时间（ms）")
    private Long executeTime;

    /**
     * 平台名称.
     */
    @Schema(description = "平台名称")
    @Length(max = 100, message = "{platformName.length}", groups = {SaveGroup.class, EditGroup.class})
    private String platformName;

    /**
     * 系统名称.
     */
    @Schema(description = "系统名称")
    @Length(max = 100, message = "{osName.length}", groups = {SaveGroup.class, EditGroup.class})
    private String osName;

    /**
     * 浏览器名称.
     */
    @Schema(description = "浏览器名称")
    @Length(max = 100, message = "{browserName.length}", groups = {SaveGroup.class, EditGroup.class})
    private String browserName;

    /**
     * 跟踪唯一标识.
     */
    @Schema(description = "跟踪唯一标识")
    @Length(max = 100, message = "{bizTraceId.length}", groups = {SaveGroup.class, EditGroup.class})
    private String bizTraceId;

    /**
     * 请求ip.
     */
    @Schema(description = "请求ip")
    @Length(max = 100, message = "{ip.length}", groups = {SaveGroup.class, EditGroup.class})
    private String ip;

    /**
     * 用户主键.
     */
    @Schema(description = "用户主键")
    private Long userId;

    /**
     * 组织主键.
     */
    @Schema(description = "组织主键")
    private Long orgId;
}

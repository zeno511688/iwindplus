/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.log.domain.vo;

import com.iwindplus.base.domain.vo.DbBaseTwoVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 操作日志详情视图对象.
 *
 * @author zengdegui
 * @since 2019/7/16
 */
@Schema(description = "操作日志详情视图对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class OperationLogVO extends DbBaseTwoVO {

    /**
     * 请求唯一标识.
     */
    @Schema(description = "请求唯一标识")
    private String requestId;

    /**
     * 访问实例.
     */
    @Schema(description = "访问实例")
    private String targetServer;

    /**
     * 业务流水号.
     */
    @Schema(description = "业务流水号")
    private String bizNumber;

    /**
     * 业务类型.
     */
    @Schema(description = "业务类型")
    private String bizType;

    /**
     * 操作类型.
     */
    @Schema(description = "操作类型")
    private String operateType;

    /**
     * 操作名称.
     */
    @Schema(description = "操作名称")
    private String operateName;

    /**
     * 操作描述.
     */
    @Schema(description = "操作描述")
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
    private String requestTime;

    /**
     * 响应时间.
     */
    @Schema(description = "响应时间")
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
    private String platformName;

    /**
     * 系统名称.
     */
    @Schema(description = "系统名称")
    private String osName;

    /**
     * 浏览器名称.
     */
    @Schema(description = "浏览器名称")
    private String browserName;

    /**
     * 跟踪唯一标识.
     */
    @Schema(description = "跟踪唯一标识")
    private String bizTraceId;

    /**
     * 请求ip.
     */
    @Schema(description = "请求ip")
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

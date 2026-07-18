/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.log.domain.dto;

import com.iwindplus.base.domain.dto.DbPageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;

/**
 * 网关日志搜索数据传输对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "网关日志搜索数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class GatewayLogSearchDTO extends DbPageDTO {

    /**
     * 请求唯一标识.
     */
    @Schema(description = "请求唯一标识")
    private String requestId;

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

    /**
     * 访问实例.
     */
    @Schema(description = "访问实例")
    @Length(max = 100, message = "{targetServer.length}")
    private String targetServer;

    /**
     * 请求路径.
     */
    @Schema(description = "请求路径")
    @Length(max = 255, message = "{requestPath.length}")
    private String requestPath;

    /**
     * 工号
     */
    @Schema(description = "工号")
    @Length(max = 100, message = "{jobNumber.length}")
    private String jobNumber;

    /**
     * 用户手机.
     */
    @Schema(description = "用户手机")
    @Length(max = 100, message = "{mobile.length}")
    private String mobile;

    /**
     * 跟踪唯一标识.
     */
    @Schema(description = "跟踪唯一标识")
    @Length(max = 100, message = "{bizTraceId.length}")
    private String bizTraceId;

    /**
     * ip.
     */
    @Schema(description = "ip")
    @Length(max = 100, message = "{ip.length}")
    private String ip;

    /**
     * 响应状态码.
     */
    @Schema(description = "响应状态码")
    private Integer responseStatus;

    /**
     * 响应错误编码.
     */
    @Schema(description = "响应错误编码")
    private String responseErrorCode;
}

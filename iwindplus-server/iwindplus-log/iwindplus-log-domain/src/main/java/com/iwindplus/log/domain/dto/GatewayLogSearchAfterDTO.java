/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.log.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;

/**
 * 网关日志搜索数据传输对象(search_after深分页).
 *
 * @author zengdegui
 * @since 2024/4/10
 */
@Schema(description = "网关日志搜索数据传输对象(search_after深分页)")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class GatewayLogSearchAfterDTO implements Serializable {

    /**
     * 每页显示条数.
     */
    @Schema(description = "每页显示条数")
    private Integer size;

    /**
     * 下一页游标.
     */
    @Schema(description = "下一页游标")
    private List<Object> searchAfter;

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
     * 工号.
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

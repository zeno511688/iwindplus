/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.log.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 操作日志最新数据传输对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "操作日志最新数据传输对象")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperationLogNewestDTO implements Serializable {

    /**
     * 请求唯一标识.
     */
    @Schema(description = "请求唯一标识")
    private String requestId;

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
     * 当前登录用户主键.
     */
    @Schema(description = "当前登录用户主键")
    private Long currentUserId;

    /**
     * 组织主键.
     */
    @Schema(description = "组织主键")
    private Long orgId;
}

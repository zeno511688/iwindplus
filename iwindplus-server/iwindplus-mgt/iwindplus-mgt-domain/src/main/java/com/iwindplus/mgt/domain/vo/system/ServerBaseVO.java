/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.vo.system;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 服务基础字段视图对象.
 *
 * @author zengdegui
 * @since 2020/4/14
 */
@Schema(description = "服务基础字段视图对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ServerBaseVO implements Serializable {

    /**
     * 主键.
     */
    @Schema(description = "主键")
    private Long id;

    /**
     * 路由ID.
     */
    @Schema(description = "路由ID")
    private String routeId;

    /**
     * 名称.
     */
    @Schema(description = "名称")
    private String name;
}

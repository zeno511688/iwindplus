/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.shiro.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 访问权限视图对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "访问权限视图对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AccessPermsVO implements Serializable {

    /**
     * API路径.
     */
    @Schema(description = "API路径")
    private String apiUrl;

    /**
     * 权限（过滤器名称，anon：免认证）.
     */
    @Schema(description = "权限（过滤器名称，anon：免认证）")
    private String permission;
}

/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.shiro.domain.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwindplus.base.shiro.domain.constant.ShiroConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 访问token视图对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "访问token视图对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AccessTokenVO implements Serializable {
    /**
     * 访问token.
     */
    @Schema(description = "访问token")
    @JsonProperty(ShiroConstant.ACCESS_TOKEN)
    private String accessToken;

    /**
     * 刷新token.
     */
    @Schema(description = "刷新token")
    @JsonProperty(ShiroConstant.REFRESH_TOKEN)
    private String refreshToken;

    /**
     * 过期时间.
     */
    @Schema(description = "过期时间")
    @JsonProperty(ShiroConstant.EXPIRES_IN)
    private Integer expiresIn;
}

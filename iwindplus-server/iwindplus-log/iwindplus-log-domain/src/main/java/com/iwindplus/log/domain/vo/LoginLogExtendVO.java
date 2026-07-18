/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.log.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 登录日志扩展视图对象.
 *
 * @author zengdegui
 * @since 2019/7/16
 */
@Schema(description = "登录日志扩展视图对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class LoginLogExtendVO extends LoginLogVO {

    /**
     * 省份.
     */
    @Schema(description = "省份")
    private String province;

    /**
     * 城市.
     */
    @Schema(description = "城市")
    private String city;

    /**
     * 工号
     */
    @Schema(description = "工号")
    private String jobNumber;

    /**
     * 手机.
     */
    @Schema(description = "手机")
    private String mobile;
}

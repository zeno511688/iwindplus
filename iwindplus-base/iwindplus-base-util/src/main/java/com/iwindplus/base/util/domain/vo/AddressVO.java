/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * ip获取所在省市区信息对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "ip获取所在省市区信息对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AddressVO implements Serializable {

    /**
     * IP地址.
     */
    @Schema(description = "IP地址")
    private String ip;

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
}

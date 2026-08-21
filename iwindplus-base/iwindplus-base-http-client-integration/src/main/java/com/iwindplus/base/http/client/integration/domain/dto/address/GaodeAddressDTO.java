/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.integration.domain.dto.address;

import lombok.Data;

/**
 * 高德云图 IP 查询响应.
 *
 * @author zengdegui
 * @since 2026/08/20
 */
@Data
public class GaodeAddressDTO {

    /**
     * 省份.
     */
    private String province;

    /**
     * 城市.
     */
    private String city;
}

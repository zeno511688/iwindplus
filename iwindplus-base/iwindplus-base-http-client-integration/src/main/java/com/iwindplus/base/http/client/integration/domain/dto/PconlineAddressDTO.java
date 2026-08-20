/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.integration.domain.dto;

import lombok.Data;

/**
 * 太平洋网络 IP 查询响应.
 *
 * @author zengdegui
 * @since 2026/08/20
 */
@Data
public class PconlineAddressDTO {

    /**
     * 省份.
     */
    private String pro;

    /**
     * 城市.
     */
    private String city;
}

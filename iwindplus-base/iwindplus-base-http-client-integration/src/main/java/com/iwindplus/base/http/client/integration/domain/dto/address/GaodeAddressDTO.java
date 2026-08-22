/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.integration.domain.dto.address;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwindplus.base.http.client.integration.domain.constant.AddressConstant;
import lombok.Data;

/**
 * 高德 IP 查询响应.
 *
 * @author zengdegui
 * @since 2026/08/20
 */
@Data
public class GaodeAddressDTO {

    /**
     * 返回结果状态值.
     * 返回值为 0 或 1，0 表示请求失败；1 表示请求成功。
     */
    private Integer status;

    /**
     * 返回状态说明.
     * 当 status 为 0 时，info 会返回具体错误原因。
     */
    private String info;

    /**
     * 省份.
     */
    private String province;

    /**
     * 城市.
     */
    private String city;

    /**
     * 城市编码.
     */
    @JsonProperty(AddressConstant.FIELD_AD_CODE)
    private String adCode;

    /**
     * 矩形区域边界（格式："左下经度,左下纬度;右上经度,右上纬度"）.
     */
    private String rectangle;
}

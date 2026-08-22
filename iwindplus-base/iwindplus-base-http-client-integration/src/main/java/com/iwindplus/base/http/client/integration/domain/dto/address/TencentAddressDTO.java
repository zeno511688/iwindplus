/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.integration.domain.dto.address;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwindplus.base.http.client.integration.domain.constant.AddressConstant;
import java.math.BigDecimal;
import lombok.Data;

/**
 * 腾讯地图 IP 查询响应.
 *
 * @author zengdegui
 * @since 2026/08/20
 */
@Data
public class TencentAddressDTO {

    /**
     * 状态码.
     * 返回值为 0 或 1，0 表示请求成功；1 表示请求失败。
     */
    private Integer status;

    /**
     * 提示信息.
     */
    private String message;

    /**
     * 查询结果.
     */
    private Result result;

    /**
     * 查询结果对象.
     */
    @Data
    public static class Result {

        /**
         * IP地址.
         */
        private String ip;

        /**
         * 行政区信息.
         */
        @JsonProperty(AddressConstant.FIELD_AD_INFO)
        private AdInfo adInfo;

        /**
         * 经纬度坐标.
         */
        private Location location;
    }

    /**
     * 行政区信息对象.
     */
    @Data
    public static class AdInfo {

        /**
         * 国家.
         */
        private String nation;

        /**
         * 省份.
         */
        private String province;

        /**
         * 城市.
         */
        private String city;

        /**
         * 区域.
         */
        private String district;

        /**
         * 国家编码.
         */
        @JsonProperty(AddressConstant.FIELD_NATION_CODE)
        private String nationCode;
    }

    /**
     * 经纬度坐标对象.
     */
    @Data
    public static class Location {

        /**
         * 经度.
         */
        @JsonProperty(AddressConstant.FIELD_LNG)
        private BigDecimal lng;

        /**
         * 纬度.
         */
        @JsonProperty(AddressConstant.FIELD_LAT)
        private BigDecimal lat;
    }
}

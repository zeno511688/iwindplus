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
 * 百度地图 IP 查询响应.
 *
 * @author zengdegui
 * @since 2026/08/20
 */
@Data
public class BaiduAddressDTO {

    /**
     * 状态码.
     * 返回值为 0 或 1，0 表示请求成功；1 表示请求失败。
     */
    private Integer status;

    /**
     * 地址信息（简要格式）.
     * 格式："国家|省份|城市|区县|街道|运营商|邮编|区号"
     */
    private String address;

    /**
     * 内容.
     */
    private Content content;

    /**
     * 百度地址详情.
     */
    @Data
    public static class Content {

        /**
         * 简要地址信息
         */
        private String address;

        /**
         * 地址详情.
         */
        @JsonProperty(AddressConstant.FIELD_ADDRESS_DETAIL)
        private AddressDetail addressDetail;

        /**
         * 经纬度坐标.
         */
        @JsonProperty(AddressConstant.FIELD_POINT)
        private Point point;
    }

    /**
     * 地址详情对象.
     */
    @Data
    public static class AddressDetail {

        /**
         * 省份.
         */
        private String province;

        /**
         * 城市.
         */
        private String city;

        /**
         * 城市代码.
         */
        @JsonProperty(AddressConstant.FIELD_CITY_CODE)
        private String cityCode;

        /**
         * 区域.
         */
        private String district;

        /**
         * 街道.
         */
        private String street;

        /**
         * 门牌号.
         */
        @JsonProperty(AddressConstant.FIELD_STREET_NUMBER)
        private String streetNumber;

        /**
         * 详细地址.
         */
        private String address;
    }

    /**
     * 经纬度坐标对象.
     */
    @Data
    public static class Point {

        /**
         * 经度.
         */
        @JsonProperty(AddressConstant.FIELD_X)
        private BigDecimal x;

        /**
         * 纬度.
         */
        @JsonProperty(AddressConstant.FIELD_Y)
        private BigDecimal y;
    }
}

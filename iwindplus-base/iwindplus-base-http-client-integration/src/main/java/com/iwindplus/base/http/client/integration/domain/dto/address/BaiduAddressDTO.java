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
 * 百度地图 IP 查询响应.
 *
 * @author zengdegui
 * @since 2026/08/20
 */
@Data
public class BaiduAddressDTO {

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
         * 地址详情.
         */
        @JsonProperty(AddressConstant.FIELD_ADDRESS_DETAIL)
        private AddressDetail addressDetail;
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
    }
}

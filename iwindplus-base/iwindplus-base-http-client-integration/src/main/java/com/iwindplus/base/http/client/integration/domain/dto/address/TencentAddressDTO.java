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
 * 腾讯地图 IP 查询响应.
 *
 * @author zengdegui
 * @since 2026/08/20
 */
@Data
public class TencentAddressDTO {

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
         * 行政区信息.
         */
        @JsonProperty(AddressConstant.FIELD_AD_INFO)
        private AdInfo adInfo;
    }

    /**
     * 行政区信息对象.
     */
    @Data
    public static class AdInfo {

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

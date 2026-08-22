/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.integration.domain.constant;

import com.iwindplus.base.domain.constant.CommonConstant;

/**
 * 地址相关常数.
 *
 * @author zengdegui
 * @since 2026/08/21 00:05
 */
public final class AddressConstant {

    private AddressConstant() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 请求参数-json.
     */
    public static final String PARAM_JSON = "json";

    /**
     * 请求参数-ip.
     */
    public static final String PARAM_IP = "ip";

    /**
     * 请求参数-ak.
     */
    public static final String PARAM_AK = "ak";

    /**
     * 请求参数-sk.
     */
    public static final String PARAM_SK = "sk";

    /**
     * 请求参数-sn（百度）.
     */
    public static final String PARAM_SN = "sn";

    /**
     * 请求参数-sig（腾讯）.
     */
    public static final String PARAM_SIG = "sig";

    /**
     * 请求参数-key.
     */
    public static final String PARAM_KEY = "key";

    /**
     * 请求参数-token.
     */
    public static final String PARAM_TOKEN = "token";

    /**
     * 请求参数值-true.
     */
    public static final String VALUE_TRUE = "true";

    /**
     * JSON字段-address_detail（百度地址详情）.
     */
    public static final String FIELD_ADDRESS_DETAIL = "address_detail";

    /**
     * JSON字段-city_code（百度地址详情）.
     */
    public static final String FIELD_CITY_CODE = "city_code";

    /**
     * JSON字段-street_number（百度地址详情）.
     */
    public static final String FIELD_STREET_NUMBER = "street_number";

    /**
     * JSON字段-ad_info（腾讯区域信息）.
     */
    public static final String FIELD_AD_INFO = "ad_info";

    /**
     * JSON字段-ad_info（腾讯国家编码）.
     */
    public static final String FIELD_NATION_CODE = "nation_code";

    /**
     * JSON字段-adcode（高德城市编码）.
     */
    public static final String FIELD_AD_CODE = "adcode";

    /**
     * JSON字段-point（百度经纬度坐标）.
     */
    public static final String FIELD_POINT = "point";

    /**
     * JSON字段-x（经度）.
     */
    public static final String FIELD_X = "x";

    /**
     * JSON字段-y（纬度）.
     */
    public static final String FIELD_Y = "y";

    /**
     * JSON字段-lng（经度）.
     */
    public static final String FIELD_LNG = "lng";

    /**
     * JSON字段-lat（纬度）.
     */
    public static final String FIELD_LAT = "lat";

    /**
     * IP138数据数组-国家索引.
     */
    public static final int IP138_NATION_INDEX = 0;

    /**
     * IP138数据数组-省份索引.
     */
    public static final int IP138_PROVINCE_INDEX = 1;

    /**
     * IP138数据数组-城市索引.
     */
    public static final int IP138_CITY_INDEX = 2;

    /**
     * IP138数据数组-最小长度.
     */
    public static final int IP138_MIN_DATA_SIZE = 3;

    /**
     * URL常量.
     */
    public static final class Url {

        /**
         * 太平洋网络的接口.
         */
        public static final String PCONLINE_URL_STR = "https://whois.pconline.com.cn/ipJson.jsp";

        /**
         * 高德地图的接口.
         */
        public static final String GAODE_URL_STR = "https://restapi.amap.com/v3/ip";

        /**
         * ip138的接口（准确）.
         */
        public static final String IP138_URL_STR = "https://api.ip138.com/ip";

        /**
         * 百度地图IP定位接口.
         */
        public static final String BAIDU_URL_STR = "https://api.map.baidu.com/location/ip";

        /**
         * 腾讯地图IP定位接口.
         */
        public static final String TENCENT_URL_STR = "https://apis.map.qq.com/ws/location/v1/ip";
    }
}

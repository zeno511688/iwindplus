/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.integration.domain.dto.sumsub;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * SumSub 申请人请求.
 *
 * @author zengdegui
 * @since 2026/08/20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SumSubApplicantDTO implements Serializable {

    /**
     * 外部用户ID（必填）.
     */
    private String externalUserId;

    /**
     * 电子邮箱.
     */
    private String email;

    /**
     * 手机号.
     */
    private String phone;

    /**
     * 固定电话.
     */
    private String fixedInfo;

    /**
     * 名.
     */
    private String firstName;

    /**
     * 姓.
     */
    private String lastName;

    /**
     * 中间名.
     */
    private String middleName;

    /**
     * 出生日期（格式：YYYY-MM-DD）.
     */
    private String dob;

    /**
     * 国籍（ISO 3166-1 alpha-3）.
     */
    private String country;

    /**
     * 居住国家（ISO 3166-1 alpha-3）.
     */
    private String residenceCountry;

    /**
     * 性别（male/female）.
     */
    private String gender;

    /**
     * 地址信息.
     */
    private AddressInfo addressInfo;

    /**
     * 审核级别.
     */
    private String review;

    /**
     * 地址信息.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressInfo implements Serializable {

        /**
         * 国家（ISO 3166-1 alpha-3）.
         */
        private String country;

        /**
         * 省份/州.
         */
        private String region;

        /**
         * 城市.
         */
        private String town;

        /**
         * 街道.
         */
        private String street;

        /**
         * 门牌号.
         */
        private String house;

        /**
         * 邮编.
         */
        private String postCode;

        /**
         * 完整地址.
         */
        private String formattedAddress;
    }
}

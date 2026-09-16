/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.sumsub.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * SumSub 申请人请求.
 *
 * <p>对应官方接口 {@code POST /resources/applicants?levelName=xxx} 的请求体结构：
 * <ul>
 *     <li>{@code externalUserId}、{@code email}、{@code phone}、{@code lang}、{@code type} 为顶层字段；</li>
 *     <li>个人信息（姓名、出生日期、国籍、地址等）位于 {@code fixedInfo} 嵌套对象中。</li>
 * </ul>
 *
 * @author zengdegui
 * @since 2026/9/7
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SumSubApplicantDTO implements Serializable {

    /**
     * 外部用户ID（必填，最多512字符）.
     */
    private String externalUserId;

    /**
     * 电子邮箱（若开启邮箱验证则必填）.
     */
    private String email;

    /**
     * 手机号（若开启手机验证则必填，E.164格式）.
     */
    private String phone;

    /**
     * 语言（ISO 639-1 两字母代码，如 en、zh）.
     */
    private String lang;

    /**
     * 申请人类型（individual / company）.
     */
    private String type;

    /**
     * 固定信息（申请人提交的信息，用于与文档识别数据交叉验证）.
     */
    private FixedInfo fixedInfo;

    /**
     * 固定信息对象.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FixedInfo implements Serializable {

        /**
         * 名.
         */
        private String firstName;

        /**
         * 中间名.
         */
        private String middleName;

        /**
         * 姓.
         */
        private String lastName;

        /**
         * 别名.
         */
        private String aliasName;

        /**
         * 性别（M / F / X）.
         */
        private String gender;

        /**
         * 出生日期（格式：YYYY-MM-DD）.
         */
        private String dob;

        /**
         * 出生地.
         */
        private String placeOfBirth;

        /**
         * 出生国家（ISO 3166-1 alpha-3）.
         */
        private String countryOfBirth;

        /**
         * 出生州/省.
         */
        private String stateOfBirth;

        /**
         * 国家（ISO 3166-1 alpha-3）.
         */
        private String country;

        /**
         * 国籍（ISO 3166-1 alpha-3）.
         */
        private String nationality;

        /**
         * 地址列表.
         */
        private List<AddressInfo> addresses;
    }

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
         * 邮编.
         */
        private String postCode;

        /**
         * 州/省.
         */
        private String state;

        /**
         * 城市.
         */
        private String town;

        /**
         * 街道.
         */
        private String street;

        /**
         * 门牌号等附加信息.
         */
        private String subStreet;

        /**
         * 完整地址.
         */
        private String formattedAddress;
    }
}

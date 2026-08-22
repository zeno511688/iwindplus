/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.integration.domain.vo.kyc.sumsub;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * SumSub 文档信息.
 *
 * @author zengdegui
 * @since 2026/08/20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SumSubDocumentVO implements Serializable {

    /**
     * 文档ID.
     */
    private String id;

    /**
     * 文档类型（PASSPORT,ID_CARD,DRIVERS_LICENSE等）.
     */
    private String docType;

    /**
     * 国家（ISO 3166-1 alpha-3）.
     */
    private String country;

    /**
     * 文档状态.
     */
    private String status;

    /**
     * 拒绝原因.
     */
    private String rejectReason;

    /**
     * 审核结果.
     */
    private String reviewResult;

    /**
     * 审核状态.
     */
    private String reviewStatus;

    /**
     * 文档元数据.
     */
    private DocumentMetadata metadata;

    /**
     * 文档元数据.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocumentMetadata implements Serializable {

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
         * 出生日期.
         */
        private String dob;

        /**
         * 性别.
         */
        private String gender;

        /**
         * 签发日期.
         */
        private String validFrom;

        /**
         * 有效期至.
         */
        private String validUntil;

        /**
         * 文档编号.
         */
        private String documentNumber;

        /**
         * 签发国家.
         */
        private String issuingCountry;

        /**
         * 地址.
         */
        private String address;
    }
}

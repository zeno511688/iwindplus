/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.integration.domain.constant;

import com.iwindplus.base.domain.constant.CommonConstant;

/**
 * SumSub相关常数.
 *
 * @author zengdegui
 * @since 2026/08/21 00:06
 */
public final class SumSubConstant {

    private SumSubConstant() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 请求头-X-App-Token.
     */
    public static final String HEADER_X_APP_TOKEN = "X-App-Token";

    /**
     * 请求头-X-App-Secret.
     */
    public static final String HEADER_X_APP_SECRET = "X-App-Secret";

    /**
     * 请求头-X-Access-Token.
     */
    public static final String HEADER_X_ACCESS_TOKEN = "X-Access-Token";

    /**
     * 请求头-X-App-Access-TS（时间戳）.
     */
    public static final String HEADER_X_APP_ACCESS_TS = "X-App-Access-TS";

    /**
     * 请求头-X-App-Access-Sign（签名）.
     */
    public static final String HEADER_X_APP_ACCESS_SIGN = "X-App-Access-Sign";

    /**
     * 请求参数-userId.
     */
    public static final String PARAM_USER_ID = "userId";

    /**
     * 请求参数-externalUserId.
     */
    public static final String PARAM_EXTERNAL_USER_ID = "externalUserId";

    /**
     * 请求参数-ttlInSecs.
     */
    public static final String PARAM_TTL_IN_SECS = "ttlInSecs";

    /**
     * 请求参数-levelName.
     */
    public static final String PARAM_LEVEL_NAME = "levelName";

    /**
     * 签名算法-HmacSHA256.
     */
    public static final String ALGORITHM_HMAC_SHA256 = "HmacSHA256";

    /**
     * Webhook类型-申请人审核完成.
     */
    public static final String WEBHOOK_TYPE_APPLICANT_REVIEWED = "applicantReviewed";

    /**
     * Webhook类型-申请人待审核.
     */
    public static final String WEBHOOK_TYPE_APPLICANT_PENDING = "applicantPending";

    /**
     * Webhook类型-申请人个人信息变更.
     */
    public static final String WEBHOOK_TYPE_APPLICANT_PERSONAL_INFO_CHANGED = "applicantPersonalInfoChanged";

    /**
     * Webhook类型-申请人文档上传.
     */
    public static final String WEBHOOK_TYPE_APPLICANT_DOCUMENT_UPLOADED = "applicantDocumentUploaded";

    /**
     * Webhook类型-申请人文档状态变更.
     */
    public static final String WEBHOOK_TYPE_APPLICANT_DOCUMENT_STATUS_CHANGED = "applicantDocumentStatusChanged";

    /**
     * Webhook类型-申请人已创建.
     */
    public static final String WEBHOOK_TYPE_APPLICANT_CREATED = "applicantCreated";

    /**
     * Webhook类型-申请人已删除.
     */
    public static final String WEBHOOK_TYPE_APPLICANT_DELETED = "applicantDeleted";

    /**
     * Webhook类型-申请人已重置.
     */
    public static final String WEBHOOK_TYPE_APPLICANT_RESET = "applicantReset";

    /**
     * Webhook类型-申请人已暂停.
     */
    public static final String WEBHOOK_TYPE_APPLICANT_SUSPENDED = "applicantSuspended";

    /**
     * Webhook类型-申请人已恢复.
     */
    public static final String WEBHOOK_TYPE_APPLICANT_RESUMED = "applicantResumed";

    /**
     * URL常量.
     */
    public static final class Url {

        private Url() {
            throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
        }

        /**
         * SumSub API基础URL.
         */
        public static final String BASE_URL = "https://api.sumsub.com";

        /**
         * 访问令牌接口.
         */
        public static final String ACCESS_TOKEN = BASE_URL + "/resources/accessTokens";

        /**
         * 申请人接口.
         */
        public static final String APPLICANT = BASE_URL + "/resources/applicants";

        /**
         * 申请人详情接口（需要拼接applicantId）.
         */
        public static final String APPLICANT_DETAIL = APPLICANT + "/%s";

        /**
         * 重置申请人接口（需要拼接applicantId）.
         */
        public static final String APPLICANT_RESET = APPLICANT + "/%s/reset";

        /**
         * 申请人文档接口（需要拼接applicantId）.
         */
        public static final String APPLICANT_DOCUMENTS = APPLICANT + "/%s/documents";

        /**
         * 文档接口.
         */
        public static final String DOCUMENT = BASE_URL + "/resources/documents";

        /**
         * 文档详情接口（需要拼接documentId）.
         */
        public static final String DOCUMENT_DETAIL = DOCUMENT + "/%s";

        /**
         * 文档校验接口（需要拼接documentId）.
         */
        public static final String DOCUMENT_CHECKS = DOCUMENT + "/%s/checks";
    }
}

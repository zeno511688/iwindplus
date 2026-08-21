/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.integration.domain.vo.sumsub;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * SumSub 申请人响应.
 *
 * @author zengdegui
 * @since 2026/08/20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SumSubApplicantVO implements Serializable {

    /**
     * 申请人ID.
     */
    private String id;

    /**
     * 外部用户ID.
     */
    private String externalUserId;

    /**
     * 审核状态.
     */
    private ReviewResult review;

    /**
     * 创建时间.
     */
    private Long createdAt;

    /**
     * 修改时间.
     */
    private Long modifiedAt;

    /**
     * 审核结果.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewResult implements Serializable {

        /**
         * 审核状态（init/pending/queued,completed,resumed,retry）.
         */
        private String reviewStatus;

        /**
         * 审核结果状态（GREEN/RED）.
         */
        private String reviewResult;

        /**
         * 审核结果码.
         */
        private String reviewCode;

        /**
         * 审核结果标签.
         */
        private List<String> reviewLabels;

        /**
         * 拒绝原因.
         */
        private List<RejectReason> rejectReasons;

        /**
         * 审核评论.
         */
        private String comment;

        /**
         * 拒绝原因.
         */
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class RejectReason implements Serializable {

            /**
             * 拒绝原因码.
             */
            private String rejectReasonCode;

            /**
             * 拒绝原因描述.
             */
            private String rejectReasonDescription;

            /**
             * 文档ID.
             */
            private String documentId;

            /**
             * 审核状态.
             */
            private String reviewStatus;
        }
    }
}

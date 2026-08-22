/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.integration.domain.dto.kyc.sumsub;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import com.iwindplus.base.http.client.integration.domain.vo.kyc.sumsub.SumSubApplicantVO;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * SumSub Webhook 回调数据.
 *
 * @author zengdegui
 * @since 2026/08/20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SumSubWebhookDTO implements Serializable {

    /**
     * Webhook类型.
     */
    private String type;

    /**
     * 申请人ID.
     */
    private String applicantId;

    /**
     * 外部用户ID.
     */
    private String externalUserId;

    /**
     * 审核结果.
     */
    private SumSubApplicantVO.ReviewResult reviewResult;

    /**
     * 审查状态.
     */
    private String reviewStatus;

    /**
     * 审查结果.
     */
    private String reviewResultType;

    /**
     * 审查评论.
     */
    private String reviewComment;

    /**
     * 创建时间.
     */
    private Long createdAt;

    /**
     * 文档ID（文档相关事件）.
     */
    private String documentId;

    /**
     * 文档类型.
     */
    private String docType;

    /**
     * 文档状态.
     */
    private String docStatus;
}

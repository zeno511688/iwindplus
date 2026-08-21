/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.integration.example;

import com.iwindplus.base.http.client.integration.domain.dto.sumsub.SumSubAccessTokenDTO;
import com.iwindplus.base.http.client.integration.domain.vo.sumsub.SumSubAccessTokenVO;
import com.iwindplus.base.http.client.integration.domain.dto.sumsub.SumSubApplicantDTO;
import com.iwindplus.base.http.client.integration.domain.vo.sumsub.SumSubApplicantVO;
import com.iwindplus.base.http.client.integration.domain.vo.sumsub.SumSubDocumentCheckVO;
import com.iwindplus.base.http.client.integration.domain.vo.sumsub.SumSubDocumentVO;
import com.iwindplus.base.http.client.integration.service.SumSubService;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * SumSub服务使用示例.
 *
 * @author zengdegui
 * @since 2026/08/20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SumSubServiceExample {

    private final SumSubService sumSubService;

    /**
     * 示例1：获取访问令牌.
     *
     * @param externalUserId 外部用户ID
     * @return 访问令牌响应
     */
    public Optional<SumSubAccessTokenVO> getAccessTokenExample(String externalUserId) {
        // 构建请求参数
        SumSubAccessTokenDTO request = SumSubAccessTokenDTO.builder()
            .externalUserId(externalUserId)
            .ttlInSecs(2592000) // 30天
            .levelName("basic-kyc-level")
            .build();

        // 调用服务获取访问令牌
        Optional<SumSubAccessTokenVO> tokenOpt = sumSubService.getAccessToken(request);

        // 处理结果
        tokenOpt.ifPresent(token -> {
            log.info("Access token: {}", token.getToken());
            log.info("Expired at: {}", token.getExpiredAt());
        });

        return tokenOpt;
    }

    /**
     * 示例2：创建申请人.
     *
     * @param externalUserId 外部用户ID
     * @param email          邮箱
     * @param phone          手机号
     * @return 申请人响应
     */
    public Optional<SumSubApplicantVO> createApplicantExample(
        String externalUserId, String email, String phone) {
        // 构建申请人信息
        SumSubApplicantDTO request = SumSubApplicantDTO.builder()
            .externalUserId(externalUserId)
            .email(email)
            .phone(phone)
            .firstName("John")
            .lastName("Doe")
            .dob("1990-01-01")
            .country("USA")
            .build();

        // 创建申请人
        Optional<SumSubApplicantVO> applicantOpt = sumSubService.createApplicant(request);

        // 处理结果
        applicantOpt.ifPresent(applicant -> {
            log.info("Applicant created: id={}", applicant.getId());
            log.info("Review status: {}",
                applicant.getReview() != null ? applicant.getReview().getReviewStatus() : null);
        });

        return applicantOpt;
    }

    /**
     * 示例3：获取申请人信息.
     *
     * @param applicantId 申请人ID
     * @return 申请人响应
     */
    public Optional<SumSubApplicantVO> getApplicantExample(String applicantId) {
        // 获取申请人信息
        Optional<SumSubApplicantVO> applicantOpt = sumSubService.getApplicant(applicantId);

        // 处理结果
        applicantOpt.ifPresent(applicant -> {
            log.info("Applicant ID: {}", applicant.getId());
            log.info("External User ID: {}", applicant.getExternalUserId());

            if (applicant.getReview() != null) {
                log.info("Review status: {}", applicant.getReview().getReviewStatus());
                log.info("Review result: {}", applicant.getReview().getReviewResult());

                // 检查是否被拒绝
                if ("RED".equals(applicant.getReview().getReviewResult())) {
                    log.warn("Applicant was rejected");
                    if (applicant.getReview().getRejectReasons() != null) {
                        applicant.getReview().getRejectReasons().forEach(reason -> {
                            log.warn("Reject reason: {} - {}",
                                reason.getRejectReasonCode(),
                                reason.getRejectReasonDescription());
                        });
                    }
                }
            }
        });

        return applicantOpt;
    }

    /**
     * 示例4：根据外部用户ID获取申请人信息.
     *
     * @param externalUserId 外部用户ID
     * @return 申请人响应
     */
    public Optional<SumSubApplicantVO> getApplicantByExternalUserIdExample(String externalUserId) {
        return sumSubService.getApplicantByExternalUserId(externalUserId);
    }

    /**
     * 示例5：更新申请人信息.
     *
     * @param applicantId   申请人ID
     * @param externalUserId 外部用户ID
     * @param email         邮箱
     * @return 申请人响应
     */
    public Optional<SumSubApplicantVO> updateApplicantExample(
        String applicantId, String externalUserId, String email) {
        // 构建更新信息
        SumSubApplicantDTO request = SumSubApplicantDTO.builder()
            .externalUserId(externalUserId)
            .email(email)
            .build();

        // 更新申请人
        return sumSubService.updateApplicant(applicantId, request);
    }

    /**
     * 示例6：重置申请人审核状态.
     *
     * @param applicantId 申请人ID
     * @return 申请人响应
     */
    public Optional<SumSubApplicantVO> resetApplicantExample(String applicantId) {
        return sumSubService.resetApplicant(applicantId);
    }

    /**
     * 示例7：获取申请人文档列表.
     *
     * @param applicantId 申请人ID
     * @return 文档列表
     */
    public Optional<List<SumSubDocumentVO>> getDocumentsExample(String applicantId) {
        // 获取文档列表
        Optional<List<SumSubDocumentVO>> documentsOpt = sumSubService.getDocuments(applicantId);

        // 处理结果
        documentsOpt.ifPresent(documents -> {
            log.info("Total documents: {}", documents.size());
            documents.forEach(doc -> {
                log.info("Document: id={}, type={}, status={}",
                    doc.getId(), doc.getDocType(), doc.getStatus());
            });
        });

        return documentsOpt;
    }

    /**
     * 示例8：获取文档信息.
     *
     * @param documentId 文档ID
     * @return 文档信息
     */
    public Optional<SumSubDocumentVO> getDocumentExample(String documentId) {
        // 获取文档信息
        Optional<SumSubDocumentVO> docOpt = sumSubService.getDocument(documentId);

        // 处理结果
        docOpt.ifPresent(doc -> {
            log.info("Document ID: {}", doc.getId());
            log.info("Document type: {}", doc.getDocType());
            log.info("Document status: {}", doc.getStatus());

            if (doc.getMetadata() != null) {
                log.info("Document metadata: firstName={}, lastName={}",
                    doc.getMetadata().getFirstName(),
                    doc.getMetadata().getLastName());
            }
        });

        return docOpt;
    }

    /**
     * 示例9：获取文档检查结果.
     *
     * @param documentId 文档ID
     * @return 检查结果列表
     */
    public Optional<List<SumSubDocumentCheckVO>> getDocumentChecksExample(String documentId) {
        // 获取检查结果
        Optional<List<SumSubDocumentCheckVO>> checksOpt = sumSubService.getDocumentChecks(documentId);

        // 处理结果
        checksOpt.ifPresent(checks -> {
            log.info("Total checks: {}", checks.size());
            checks.forEach(check -> {
                log.info("Check: id={}, type={}, status={}, result={}",
                    check.getId(), check.getCheckType(), check.getStatus(), check.getResult());

                if (check.getDetails() != null) {
                    check.getDetails().forEach(detail -> {
                        log.info("  Detail: field={}, value={}, status={}",
                            detail.getField(), detail.getValue(), detail.getStatus());
                    });
                }
            });
        });

        return checksOpt;
    }
}

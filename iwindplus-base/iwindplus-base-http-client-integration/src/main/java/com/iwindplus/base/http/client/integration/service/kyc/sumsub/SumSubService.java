/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.integration.service.kyc.sumsub;

import com.iwindplus.base.http.client.integration.domain.dto.kyc.sumsub.SumSubAccessTokenDTO;
import com.iwindplus.base.http.client.integration.domain.dto.kyc.sumsub.SumSubApplicantDTO;
import com.iwindplus.base.http.client.integration.domain.vo.kyc.sumsub.SumSubAccessTokenVO;
import com.iwindplus.base.http.client.integration.domain.vo.kyc.sumsub.SumSubApplicantVO;
import com.iwindplus.base.http.client.integration.domain.vo.kyc.sumsub.SumSubDocumentCheckVO;
import com.iwindplus.base.http.client.integration.domain.vo.kyc.sumsub.SumSubDocumentVO;
import java.util.List;
import java.util.Optional;

/**
 * SumSub服务业务层接口.
 *
 * @author zengdegui
 * @since 2026/08/20
 */
public interface SumSubService extends SumSubBaseService {

    /**
     * 获取访问令牌.
     *
     * @param request 请求参数（必填）
     * @return Optional<SumSubAccessTokenRespVO>
     */
    Optional<SumSubAccessTokenVO> getAccessToken(SumSubAccessTokenDTO request);

    /**
     * 创建申请人.
     *
     * @param request 申请人信息（必填）
     * @return Optional<SumSubApplicantRespVO>
     */
    Optional<SumSubApplicantVO> createApplicant(SumSubApplicantDTO request);

    /**
     * 获取申请人信息.
     *
     * @param applicantId 申请人ID（必填）
     * @return Optional<SumSubApplicantRespVO>
     */
    Optional<SumSubApplicantVO> getApplicant(String applicantId);

    /**
     * 根据外部用户ID获取申请人信息.
     *
     * @param externalUserId 外部用户ID（必填）
     * @return Optional<SumSubApplicantRespVO>
     */
    Optional<SumSubApplicantVO> getApplicantByExternalUserId(String externalUserId);

    /**
     * 更新申请人信息.
     *
     * @param applicantId 申请人ID（必填）
     * @param request     申请人信息（必填）
     * @return Optional<SumSubApplicantRespVO>
     */
    Optional<SumSubApplicantVO> updateApplicant(String applicantId, SumSubApplicantDTO request);

    /**
     * 重置申请人审核状态.
     *
     * @param applicantId 申请人ID（必填）
     * @return Optional<SumSubApplicantRespVO>
     */
    Optional<SumSubApplicantVO> resetApplicant(String applicantId);

    /**
     * 获取申请人文档列表.
     *
     * @param applicantId 申请人ID（必填）
     * @return Optional<List < SumSubDocumentVO>>
     */
    Optional<List<SumSubDocumentVO>> getDocuments(String applicantId);

    /**
     * 获取文档信息.
     *
     * @param documentId 文档ID（必填）
     * @return Optional<SumSubDocumentVO>
     */
    Optional<SumSubDocumentVO> getDocument(String documentId);

    /**
     * 获取文档检查结果.
     *
     * @param documentId 文档ID（必填）
     * @return Optional<List < SumSubDocumentCheckVO>>
     */
    Optional<List<SumSubDocumentCheckVO>> getDocumentChecks(String documentId);
}

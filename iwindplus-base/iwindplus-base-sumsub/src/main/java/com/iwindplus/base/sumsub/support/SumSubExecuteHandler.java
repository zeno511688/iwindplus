/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.sumsub.support;

import com.iwindplus.base.sumsub.domain.dto.SumSubAccessTokenDTO;
import com.iwindplus.base.sumsub.domain.dto.SumSubApplicantDTO;
import com.iwindplus.base.sumsub.domain.dto.SumSubWebhookDTO;
import com.iwindplus.base.sumsub.domain.property.SumSubProperty.SumSubConfig;
import com.iwindplus.base.sumsub.domain.vo.SumSubAccessTokenVO;
import com.iwindplus.base.sumsub.domain.vo.SumSubApplicantVO;
import com.iwindplus.base.sumsub.domain.vo.SumSubDocumentCheckVO;
import com.iwindplus.base.sumsub.domain.vo.SumSubDocumentVO;
import com.iwindplus.base.sumsub.service.BaseConfigService;
import com.iwindplus.base.sumsub.service.BaseService;
import java.util.List;
import java.util.Optional;

/**
 * SumSub策略标识接口.
 *
 * <p>策略模式：每个配置对应一个运行时策略实例。</p>
 *
 * @author zengdegui
 * @since 2026/9/7
 */
public interface SumSubExecuteHandler extends BaseService, BaseConfigService<SumSubConfig> {

    /**
     * 处理Webhook回调.
     *
     * @param webhookData 回调数据（必填）
     */
    void handleWebhook(SumSubWebhookDTO webhookData);

    /**
     * 验证Webhook签名.
     *
     * @param timestamp 时间戳（从请求头获取）
     * @param body      请求体（原始JSON字符串）
     * @param signature 签名（从请求头获取）
     * @return 验证结果
     */
    boolean verifyWebhookSignature(String timestamp, String body, String signature);

    /**
     * 获取访问令牌.
     *
     * @param request 请求参数（必填）
     * @return Optional<SumSubAccessTokenVO>
     */
    Optional<SumSubAccessTokenVO> getAccessToken(SumSubAccessTokenDTO request);

    /**
     * 创建申请人.
     *
     * @param request 申请人信息（必填）
     * @return Optional<SumSubApplicantVO>
     */
    Optional<SumSubApplicantVO> createApplicant(SumSubApplicantDTO request);

    /**
     * 获取申请人信息.
     *
     * @param applicantId 申请人ID（必填）
     * @return Optional<SumSubApplicantVO>
     */
    Optional<SumSubApplicantVO> getApplicant(String applicantId);

    /**
     * 根据外部用户ID获取申请人信息.
     *
     * @param externalUserId 外部用户ID（必填）
     * @return Optional<SumSubApplicantVO>
     */
    Optional<SumSubApplicantVO> getApplicantByExternalUserId(String externalUserId);

    /**
     * 更新申请人信息.
     *
     * @param applicantId 申请人ID（必填）
     * @param request     申请人信息（必填）
     * @return Optional<SumSubApplicantVO>
     */
    Optional<SumSubApplicantVO> updateApplicant(String applicantId, SumSubApplicantDTO request);

    /**
     * 重置申请人审核状态.
     *
     * @param applicantId 申请人ID（必填）
     * @return Optional<SumSubApplicantVO>
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

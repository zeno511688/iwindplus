/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.integration.support.kyc;

import com.iwindplus.base.http.client.integration.domain.enums.KycProviderEnum;
import java.util.Optional;

/**
 * KYC服务执行策略接口.
 *
 * @author zengdegui
 * @since 2026/08/21
 */
public interface KycExecuteHandler {

    /**
     * 获取提供商类型.
     *
     * @return 提供商枚举
     */
    KycProviderEnum getProvider();

    /**
     * 创建验证流程.
     * 不同服务商有不同的实体概念：
     * - SumSub: Applicant（申请人）
     * - Veriff: Session（会话）
     * - Jumio: Transaction（交易）
     *
     * @param request 验证流程请求参数（JSON格式，由具体实现解析）
     * @return 验证流程响应（JSON格式，由具体实现返回）
     */
    Optional<String> createVerification(Object request);

    /**
     * 查询验证状态.
     *
     * @param verificationId 验证流程ID（ApplicantId/SessionId/TransactionId）
     * @return 验证状态响应（JSON格式，由具体实现返回）
     */
    Optional<String> getVerificationStatus(String verificationId);

    /**
     * 处理Webhook回调.
     *
     * @param webhookData Webhook数据（JSON格式，由具体实现解析）
     */
    void handleWebhook(String webhookData);

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
     * 健康检查.
     *
     * @return 是否健康
     */
    default boolean isHealthy() {
        return true;
    }

    /**
     * 获取优先级.
     *
     * @return 优先级（数字越小优先级越高）
     */
    default int getPriority() {
        return getProvider().getPriority();
    }
}

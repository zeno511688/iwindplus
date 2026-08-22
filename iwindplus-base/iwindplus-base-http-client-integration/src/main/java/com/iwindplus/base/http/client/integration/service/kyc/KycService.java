/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.integration.service.kyc;

import com.iwindplus.base.http.client.integration.domain.enums.KycProviderEnum;
import java.util.List;
import java.util.Optional;

/**
 * KYC服务统一接口.
 * 提供跨服务商的统一调用入口，屏蔽底层服务商差异.
 *
 * @author zengdegui
 * @since 2026/08/21
 */
public interface KycService {

    /**
     * 创建验证流程（自动路由，支持故障转移）.
     * 按配置的优先级依次尝试各个提供商，直到成功为止.
     *
     * @param request 验证流程请求参数（JSON格式，由具体实现解析）
     * @return 验证流程响应（JSON格式，由具体实现返回）
     */
    Optional<String> createVerification(Object request);

    /**
     * 创建验证流程（指定提供商）.
     *
     * @param request  验证流程请求参数（JSON格式，由具体实现解析）
     * @param provider KYC服务提供商（必填）
     * @return 验证流程响应（JSON格式，由具体实现返回）
     */
    Optional<String> createVerification(Object request, KycProviderEnum provider);

    /**
     * 查询验证状态.
     * 统一接口，内部根据provider调用对应服务商的实现.
     *
     * @param provider       KYC服务提供商（必填）
     * @param verificationId 验证流程ID（ApplicantId/SessionId/TransactionId）
     * @return 验证状态响应（JSON格式，由具体实现返回）
     */
    Optional<String> getVerificationStatus(KycProviderEnum provider, String verificationId);

    /**
     * 处理Webhook回调.
     * 统一接口，内部根据provider调用对应服务商的实现.
     *
     * @param provider    KYC服务提供商（必填）
     * @param webhookData Webhook数据（JSON格式，由具体实现解析）
     */
    void handleWebhook(KycProviderEnum provider, String webhookData);

    /**
     * 验证Webhook签名.
     * 统一接口，内部根据provider调用对应服务商的实现.
     *
     * @param provider  KYC服务提供商（必填）
     * @param timestamp 时间戳（从请求头获取）
     * @param body      请求体（原始JSON字符串）
     * @param signature 签名（从请求头获取）
     * @return 验证结果
     */
    boolean verifyWebhookSignature(KycProviderEnum provider, String timestamp, String body, String signature);

    /**
     * 获取所有可用的KYC服务提供商.
     *
     * @return 提供商列表
     */
    List<KycProviderEnum> getAvailableProviders();
}

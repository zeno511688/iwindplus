/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.integration.service.kyc.sumsub;

import com.iwindplus.base.http.client.integration.domain.dto.kyc.sumsub.SumSubWebhookDTO;
import com.iwindplus.base.http.client.integration.domain.property.KycProperty.ProviderConfig;

/**
 * SumSub服务业务层基础接口类.
 *
 * @author zengdegui
 *  @since 2026/08/20
 */
public interface SumSubBaseService {

    /**
     * 获取SumSub配置.
     *
     * @return ProviderConfig
     */
    ProviderConfig getConfig();

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
}

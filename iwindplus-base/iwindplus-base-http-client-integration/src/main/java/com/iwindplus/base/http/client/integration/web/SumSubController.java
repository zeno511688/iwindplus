/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.integration.web;

import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.http.client.integration.domain.constant.SumSubConstant;
import com.iwindplus.base.http.client.integration.domain.dto.sumsub.SumSubWebhookDTO;
import com.iwindplus.base.http.client.integration.service.SumSubService;
import com.iwindplus.base.util.JacksonUtil;
import com.iwindplus.base.web.controller.BaseController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * SumSub Webhook相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2026/08/20
 */
@Tag(name = "SumSub Webhook接口")
@Slf4j
@RestController
@ConditionalOnProperty(
    prefix = "sum-sub.web",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true)
@RequestMapping("${sum-sub.web.path:sumsub}")
@Validated
@RequiredArgsConstructor
public class SumSubController extends BaseController {

    private final SumSubService sumSubService;

    /**
     * 处理SumSub Webhook回调（带签名验证）.
     *
     * @param timestamp 时间戳（从请求头X-App-Access-TS获取）
     * @param signature 签名（从请求头X-App-Access-Sign获取）
     * @param rawBody   原始请求体（JSON字符串）
     */
    @Operation(summary = "处理SumSub Webhook回调（带签名验证）")
    @PostMapping("handleWebhook")
    public void handleWebhook(
        @RequestHeader(value = SumSubConstant.HEADER_X_APP_ACCESS_TS, required = false) String timestamp,
        @RequestHeader(value = SumSubConstant.HEADER_X_APP_ACCESS_SIGN, required = false) String signature,
        @RequestBody String rawBody) {

        log.info("Received SumSub webhook callback");

        // 签名验证（如果启用）
        if (CharSequenceUtil.isNotBlank(sumSubService.getProperty().getWebhookSecretKey())) {
            sumSubService.verifyWebhookSignature(timestamp, rawBody, signature);
            log.info("Webhook signature verification passed");
        }

        SumSubWebhookDTO webhookData = JacksonUtil.parseObject(rawBody, SumSubWebhookDTO.class);

        log.info("Parsed webhook data: type={}, applicantId={}, externalUserId={}",
            webhookData.getType(), webhookData.getApplicantId(), webhookData.getExternalUserId());

        // 处理Webhook回调
        sumSubService.handleWebhook(webhookData);
    }
}

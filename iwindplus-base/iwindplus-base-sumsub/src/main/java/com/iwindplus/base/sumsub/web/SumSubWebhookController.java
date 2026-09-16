/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.sumsub.web;

import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.sumsub.domain.constant.SumSubConstant;
import com.iwindplus.base.sumsub.domain.dto.SumSubWebhookDTO;
import com.iwindplus.base.sumsub.factory.SumSubExecuteHandlerFactory;
import com.iwindplus.base.sumsub.support.SumSubExecuteHandler;
import com.iwindplus.base.util.JacksonUtil;
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
 * SumSub Webhook 控制器.
 *
 * @author zengdegui
 * @since 2026/9/7
 */
@Tag(name = "SumSub Webhook接口")
@Slf4j
@RestController
@ConditionalOnProperty(
    prefix = "sumsub",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true)
@RequestMapping("${sumsub.web.path:sumsub}")
@Validated
@RequiredArgsConstructor
public class SumSubWebhookController {

    private final SumSubExecuteHandlerFactory sumSubExecuteHandlerFactory;

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

        SumSubExecuteHandler handler = sumSubExecuteHandlerFactory.getDefaultHandler();

        // 签名验证（如果启用）
        if (CharSequenceUtil.isNotBlank(handler.getConfig().getWebhookSecretKey())) {
            handler.verifyWebhookSignature(timestamp, rawBody, signature);
            log.info("Webhook signature verification passed");
        }

        SumSubWebhookDTO webhookData = JacksonUtil.parseObject(rawBody, SumSubWebhookDTO.class);

        log.info("Parsed webhook data: type={}, applicantId={}, externalUserId={}",
            webhookData.getType(), webhookData.getApplicantId(), webhookData.getExternalUserId());

        // 处理Webhook回调
        handler.handleWebhook(webhookData);
    }
}

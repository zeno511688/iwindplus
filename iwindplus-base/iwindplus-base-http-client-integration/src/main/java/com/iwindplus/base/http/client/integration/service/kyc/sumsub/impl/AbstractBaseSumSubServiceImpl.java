/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.integration.service.kyc.sumsub.impl;

import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.domain.constant.CommonConstant.NumberConstant;
import com.iwindplus.base.domain.constant.CommonConstant.SymbolConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.http.client.executor.HttpClientExecutor;
import com.iwindplus.base.http.client.factory.HttpClientExecutorStrategyFactory;
import com.iwindplus.base.http.client.integration.domain.constant.SumSubConstant;
import com.iwindplus.base.http.client.integration.domain.dto.kyc.sumsub.SumSubWebhookDTO;
import com.iwindplus.base.http.client.integration.domain.property.KycProperty.ProviderConfig;
import com.iwindplus.base.http.client.integration.factory.SumSubWebhookHandlerStrategyFactory;
import com.iwindplus.base.http.client.integration.service.kyc.sumsub.SumSubBaseService;
import com.iwindplus.base.util.JacksonUtil;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/**
 * SumSub服务抽象基类.
 * 提供 SumSub 特有的签名、认证等通用功能.
 *
 * @author zengdegui
 * @since 2026/08/20
 */
@Slf4j
@Getter
public abstract class AbstractBaseSumSubServiceImpl implements SumSubBaseService {

    private final HttpClientExecutorStrategyFactory httpClientExecutorStrategyFactory;
    private final ProviderConfig config;
    private final SumSubWebhookHandlerStrategyFactory webhookHandlerFactory;

    public AbstractBaseSumSubServiceImpl(
        HttpClientExecutorStrategyFactory httpClientExecutorStrategyFactory,
        ProviderConfig config,
        SumSubWebhookHandlerStrategyFactory webhookHandlerFactory) {
        this.httpClientExecutorStrategyFactory = httpClientExecutorStrategyFactory;
        this.config = config;
        this.webhookHandlerFactory = webhookHandlerFactory;
    }

    @Override
    public void handleWebhook(SumSubWebhookDTO webhookData) {
        log.info("Received SumSub webhook: type={}, applicantId={}, externalUserId={}",
            webhookData.getType(), webhookData.getApplicantId(), webhookData.getExternalUserId());

        String webhookType = webhookData.getType();
        if (webhookType == null) {
            log.warn("Webhook type is null, skipping processing");
            return;
        }

        try {
            webhookHandlerFactory.getHandler(webhookType).accept(webhookData);
        } catch (Exception e) {
            log.error("Failed to handle webhook: type={}, error={}", webhookType, e.getMessage(), e);
        }
    }

    @Override
    public boolean verifyWebhookSignature(String timestamp, String body, String signature) {
        String secretKey = config.getWebhookSecretKey();
        if (timestamp == null || body == null || signature == null || secretKey == null) {
            log.warn("Invalid signature verification parameters: timestamp={}, body={}, signature={}, secretKey={}",
                timestamp != null, body != null, signature != null, secretKey != null);
            return false;
        }

        try {
            String data = timestamp + body;
            Mac mac = Mac.getInstance(SumSubConstant.ALGORITHM_HMAC_SHA256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                secretKey.getBytes(StandardCharsets.UTF_8),
                SumSubConstant.ALGORITHM_HMAC_SHA256
            );
            mac.init(secretKeySpec);
            byte[] expectedHmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            byte[] actualHmacBytes = Hex.decodeHex(signature);

            // 使用常量时间比较，防止时间侧信道攻击
            boolean isValid = MessageDigest.isEqual(expectedHmacBytes, actualHmacBytes);

            if (!isValid) {
                // 日志中不输出完整签名，仅输出前后几位用于排查
                String expectedSignature = Hex.encodeHexString(expectedHmacBytes);
                log.warn("Signature verification failed: expected={}...{}, actual={}...{}",
                    expectedSignature.substring(0, Math.min(8, expectedSignature.length())),
                    expectedSignature.substring(Math.max(0, expectedSignature.length() - 8)),
                    signature.substring(0, Math.min(8, signature.length())),
                    signature.substring(Math.max(0, signature.length() - 8)));
            }

            return isValid;
        } catch (Exception e) {
            log.error("Webhook signature verification failed", e);
            throw new BizException(BizCodeEnum.INVALID_SIGN);
        }
    }

    /**
     * 构建认证请求头.
     *
     * @param method HTTP方法
     * @param url    请求URL
     * @param body   请求体（可为空）
     * @return 请求头
     */
    protected Map<String, String> buildAuthHeaders(String method, String url, Object body) {
        long timestamp = System.currentTimeMillis() / NumberConstant.NUMBER_ONE_THOUSAND;
        String bodyStr = body != null ? serializeBody(body) : SymbolConstant.EMPTY_STR;
        String signature = generateSignature(timestamp, method, url, bodyStr);

        Map<String, String> headers = new HashMap<>(16);
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.put(SumSubConstant.HEADER_X_APP_TOKEN, config.getApiKey());
        headers.put(SumSubConstant.HEADER_X_APP_ACCESS_TS, String.valueOf(timestamp));
        headers.put(SumSubConstant.HEADER_X_APP_ACCESS_SIGN, signature);
        return headers;
    }

    /**
     * 序列化请求体.
     *
     * @param body 请求体对象
     * @return JSON字符串
     */
    protected String serializeBody(Object body) {
        if (body == null) {
            return SymbolConstant.EMPTY_STR;
        }
        if (body instanceof String str) {
            return str;
        }
        return JacksonUtil.toJsonStr(body);
    }

    /**
     * 生成SumSub签名.
     *
     * @param timestamp 时间戳
     * @param method    HTTP方法
     * @param url       请求URL
     * @param body      请求体（可为空）
     * @return 签名
     */
    protected String generateSignature(long timestamp, String method, String url, String body) {
        try {
            String data = timestamp + method + url + (body != null ? body : SymbolConstant.EMPTY_STR);
            Mac mac = Mac.getInstance(SumSubConstant.ALGORITHM_HMAC_SHA256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                config.getApiSecret().getBytes(StandardCharsets.UTF_8),
                SumSubConstant.ALGORITHM_HMAC_SHA256
            );
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Hex.encodeHexString(hash);
        } catch (Exception e) {
            log.error(ExceptionConstant.EXCEPTION, e);
            throw new BizException(BizCodeEnum.INVALID_SIGN);
        }
    }

    /**
     * 获取默认HTTP客户端执行器.
     *
     * @return HttpClientExecutor
     */
    protected HttpClientExecutor getHttpClientExecutor() {
        return httpClientExecutorStrategyFactory.getDefaultHttpClientExecutor();
    }
}

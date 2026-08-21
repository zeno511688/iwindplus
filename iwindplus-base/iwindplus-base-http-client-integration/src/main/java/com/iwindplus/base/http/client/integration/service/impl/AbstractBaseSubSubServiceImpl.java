/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.integration.service.impl;

import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.domain.constant.CommonConstant.NumberConstant;
import com.iwindplus.base.domain.constant.CommonConstant.SymbolConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.http.client.factory.HttpClientExecutorStrategyFactory;
import com.iwindplus.base.http.client.integration.domain.constant.SumSubConstant;
import com.iwindplus.base.http.client.integration.domain.property.SumSubProperty;
import com.iwindplus.base.http.client.integration.service.SumSubService;
import com.iwindplus.base.util.JacksonUtil;
import java.nio.charset.StandardCharsets;
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
 * 抽象SubSub实现类.
 *
 * @author zengdegui
 * @since 2025/11/29 23:12
 */
@Slf4j
@Getter
public abstract class AbstractBaseSubSubServiceImpl extends AbstractBaseServiceImpl implements SumSubService {

    private final SumSubProperty property;

    public AbstractBaseSubSubServiceImpl(
        HttpClientExecutorStrategyFactory httpClientExecutorStrategyFactory,
        SumSubProperty property) {
        super(httpClientExecutorStrategyFactory);
        this.property = property;
    }

    @Override
    public SumSubProperty getProperty(){
        return property;
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
        String bodyStr = body != null ? this.serializeBody(body) : SymbolConstant.EMPTY_STR;
        String signature = this.generateSignature(timestamp, method, url, bodyStr);

        Map<String, String> headers = new HashMap<>(16);
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.put(SumSubConstant.HEADER_X_APP_TOKEN, this.getProperty().getApiKey());
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
    private String serializeBody(Object body) {
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
                this.getProperty().getApiSecret().getBytes(StandardCharsets.UTF_8),
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
     * 验证Webhook签名.
     * <p>
     * SumSub签名算法：signature = HMAC-SHA256(timestamp + requestBody, secretKey)
     * </p>
     *
     * @param timestamp 时间戳（从请求头X-App-Access-TS获取）
     * @param body      请求体（原始JSON字符串）
     * @param signature 签名（从请求头X-App-Access-Sign获取）
     * @return 验证结果
     */
    @Override
    public boolean verifyWebhookSignature(String timestamp, String body, String signature) {
        String secretKey = this.getProperty().getWebhookSecretKey();
        if (timestamp == null || body == null || signature == null || secretKey == null) {
            log.warn("Invalid signature verification parameters: timestamp={}, body={}, signature={}, secretKey={}",
                timestamp != null, body != null, signature != null, secretKey != null);
            return false;
        }

        try {
            // 拼接时间戳和请求体
            String data = timestamp + body;

            // 计算HMAC-SHA256签名
            Mac mac = Mac.getInstance(SumSubConstant.ALGORITHM_HMAC_SHA256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                secretKey.getBytes(StandardCharsets.UTF_8),
                SumSubConstant.ALGORITHM_HMAC_SHA256
            );
            mac.init(secretKeySpec);
            byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            // 转换为十六进制字符串
            String expectedSignature = Hex.encodeHexString(hmacBytes);

            // 比较签名（忽略大小写）
            boolean isValid = expectedSignature.equalsIgnoreCase(signature);

            if (!isValid) {
                log.warn("Signature verification failed: expected={}, actual={}", expectedSignature, signature);
            }

            return isValid;
        } catch (Exception e) {
            log.error("Webhook signature verification failed", e);

            throw new BizException(BizCodeEnum.INVALID_SIGN);
        }
    }
}

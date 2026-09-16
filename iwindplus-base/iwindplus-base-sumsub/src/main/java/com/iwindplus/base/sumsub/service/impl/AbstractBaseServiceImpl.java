/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.sumsub.service.impl;

import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.domain.constant.CommonConstant.NumberConstant;
import com.iwindplus.base.domain.constant.CommonConstant.SymbolConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.sumsub.domain.constant.SumSubConstant;
import com.iwindplus.base.sumsub.domain.property.SumSubProperty;
import com.iwindplus.base.sumsub.service.BaseService;
import com.iwindplus.base.util.JacksonUtil;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/**
 * SumSub业务基础抽象类.
 *
 * @param <T> 配置类型
 * @author zengdegui
 * @since 2026/9/7
 */
@Slf4j
public abstract class AbstractBaseServiceImpl<T extends SumSubProperty.BaseConfig>
    extends AbstractBaseConfigServiceImpl<T> implements BaseService {

    /**
     * 健康检查.
     *
     * @return 是否健康
     */
    @Override
    public boolean isHealthy() {
        return this.config != null && Boolean.TRUE.equals(this.config.getEnabled());
    }

    /**
     * 获取优先级.
     *
     * @return 优先级（数字越小优先级越高）
     */
    @Override
    public int getPriority() {
        return Optional.ofNullable(this.getConfig().getPriority()).orElse(Integer.MAX_VALUE);
    }

    /**
     * 获取配置编码.
     *
     * @return 配置编码
     */
    @Override
    public String getCode() {
        return this.getConfig().getCode();
    }

    /**
     * 构建认证请求头.
     *
     * @param method HTTP方法
     * @param url    请求URL（含query参数，需与实际请求URL一致）
     * @param body   请求体（可为空）
     * @return 请求头
     */
    protected Map<String, String> buildAuthHeaders(String method, String url, Object body) {
        long timestamp = System.currentTimeMillis() / NumberConstant.NUMBER_ONE_THOUSAND;
        String bodyStr = body != null ? this.serializeBody(body) : SymbolConstant.EMPTY_STR;
        String signature = this.generateSignature(timestamp, method, url, bodyStr);

        Map<String, String> headers = new HashMap<>(16);
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.put(SumSubConstant.HEADER_X_APP_TOKEN, this.getConfig().getAccessKey());
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
     * @param url       请求URL（含query参数，需与实际请求URL一致）
     * @param body      请求体（可为空）
     * @return 签名
     */
    protected String generateSignature(long timestamp, String method, String url, String body) {
        String data = timestamp + method + url + (body != null ? body : SymbolConstant.EMPTY_STR);
        byte[] hash = this.hmacSha256(data, this.getConfig().getSecretKey());
        return Hex.encodeHexString(hash);
    }

    /**
     * 计算HmacSHA256摘要.
     *
     * @param data      待计算的数据
     * @param secretKey 密钥
     * @return 摘要字节数组
     */
    protected byte[] hmacSha256(String data, String secretKey) {
        try {
            Mac mac = Mac.getInstance(SumSubConstant.ALGORITHM_HMAC_SHA256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                secretKey.getBytes(StandardCharsets.UTF_8),
                SumSubConstant.ALGORITHM_HMAC_SHA256
            );
            mac.init(secretKeySpec);
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error(ExceptionConstant.EXCEPTION, e);
            throw new BizException(BizCodeEnum.INVALID_SIGN);
        }
    }
}

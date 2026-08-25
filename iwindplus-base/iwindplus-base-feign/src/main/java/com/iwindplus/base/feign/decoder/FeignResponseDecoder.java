/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.feign.decoder;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.iwindplus.base.domain.constant.CommonConstant.ResponseConstant;
import com.iwindplus.base.util.CryptoUtil;
import com.iwindplus.base.web.domain.property.ResponseBodyProperty;
import feign.FeignException;
import feign.Response;
import feign.codec.Decoder;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ConcurrentReferenceHashMap;

/**
 * Feign统一响应解码器.
 * <p>
 * 支持响应体解密功能，当配置启用时会自动解密bizData字段。
 *
 * @author zengdegui
 * @since 2024/06/18 20:15
 */
@Slf4j
public class FeignResponseDecoder implements Decoder {

    /**
     * JavaType缓存，使用弱引用避免内存泄漏.
     */
    private static final Map<Type, JavaType> TYPE_CACHE =
        new ConcurrentReferenceHashMap<>(64, ConcurrentReferenceHashMap.ReferenceType.WEAK);

    /**
     * ObjectReader缓存，避免重复创建Reader对象.
     */
    private static final Map<JavaType, ObjectReader> READER_CACHE =
        new ConcurrentReferenceHashMap<>(64, ConcurrentReferenceHashMap.ReferenceType.WEAK);

    /**
     * 默认解码器.
     */
    private final Decoder decoder;

    /**
     * JSON对象映射器.
     */
    private final ObjectMapper objectMapper;

    /**
     * 响应体配置属性.
     */
    private final ResponseBodyProperty cfg;

    /**
     * 解密功能是否启用（缓存配置检查结果）.
     */
    private final boolean cryptoEnabled;

    /**
     * 构造函数.
     *
     * @param decoder      默认解码器
     * @param objectMapper JSON对象映射器
     * @param cfg          响应体配置属性
     */
    public FeignResponseDecoder(Decoder decoder, ObjectMapper objectMapper, ResponseBodyProperty cfg) {
        this.decoder = decoder;
        this.objectMapper = objectMapper;
        this.cfg = cfg;
        this.cryptoEnabled = isCryptoEnabledInternal();
    }

    @Override
    public Object decode(Response response, Type type) throws IOException, FeignException {
        // 快速路径：解密未启用，直接透传
        if (!cryptoEnabled) {
            return decoder.decode(response, type);
        }

        // 快速路径：响应体为空
        if (null == response.body()) {
            return decoder.decode(response, type);
        }

        // 读取响应体（一次性读取，避免多次IO）
        byte[] bodyBytes;
        try (InputStream in = response.body().asInputStream()) {
            bodyBytes = in.readAllBytes();
        }

        // 快速路径：尝试解析JSON，失败则透传
        JsonNode root;
        try {
            root = objectMapper.readTree(bodyBytes);
        } catch (IOException e) {
            // JSON解析失败，使用原始字节流透传
            return decoder.decode(response.toBuilder().body(bodyBytes).build(), type);
        }

        // 快速路径：非对象类型，直接反序列化
        if (null == root || !root.isObject()) {
            return decoder.decode(response.toBuilder().body(bodyBytes).build(), type);
        }

        // 解密业务数据（仅当bizData存在且为文本时）
        final JsonNode biz = root.get(ResponseConstant.BIZ_DATA);
        if (null != biz && biz.isTextual()) {
            try {
                String encryptedText = biz.asText();
                String decryptedText = CryptoUtil.decrypt(encryptedText, cfg.getCrypto());
                JsonNode decryptedNode = objectMapper.readTree(decryptedText);
                ((ObjectNode) root).set(ResponseConstant.BIZ_DATA, decryptedNode);
            } catch (Exception e) {
                log.error("Failed to decrypt bizData: {}", e.getMessage());
                throw new IllegalStateException("Failed to decrypt bizData", e);
            }
        }

        // 使用缓存的ObjectReader进行反序列化
        return getCachedReader(type).readValue(root);
    }

    /**
     * 检查解密功能是否启用（内部方法，用于构造时初始化）.
     *
     * @return true-已启用，false-未启用
     */
    private boolean isCryptoEnabledInternal() {
        return null != cfg
            && null != cfg.getCrypto()
            && Boolean.TRUE.equals(cfg.getCrypto().getEnabled());
    }

    /**
     * 获取缓存的ObjectReader.
     * <p>
     * 使用双重缓存策略：先缓存JavaType，再缓存ObjectReader.
     *
     * @param type 目标类型
     * @return ObjectReader对象
     */
    private ObjectReader getCachedReader(Type type) {
        JavaType javaType = TYPE_CACHE.computeIfAbsent(type, objectMapper::constructType);
        return READER_CACHE.computeIfAbsent(javaType, objectMapper::readerFor);
    }
}

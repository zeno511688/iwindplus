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
 * feign统一响应解码器.
 *
 * @author zengdegui
 * @since 2024/06/18 20:15
 */
@Slf4j
public class FeignResponseDecoder implements Decoder {

    private static final Map<Type, JavaType> TYPE_CACHE =
        new ConcurrentReferenceHashMap<>(16, ConcurrentReferenceHashMap.ReferenceType.WEAK);

    private Decoder decoder;

    private ObjectMapper objectMapper;

    private ResponseBodyProperty cfg;

    public FeignResponseDecoder(Decoder decoder, ObjectMapper objectMapper, ResponseBodyProperty cfg) {
        this.decoder = decoder;
        this.objectMapper = objectMapper;
        this.cfg = cfg;
    }

    @Override
    public Object decode(Response response, Type type) throws IOException, FeignException {
        // 1. 配置未开启 → 直接透传
        if (null == cfg || null == cfg.getCrypto() || Boolean.FALSE.equals(cfg.getCrypto().getEnabled())) {
            return decoder.decode(response, type);
        }

        // 2. body 为空 → 直接透传
        if (null == response.body()) {
            return decoder.decode(response, type);
        }

        // 3. 零拷贝读树
        byte[] bodyBytes;
        try (InputStream in = response.body().asInputStream()) {
            bodyBytes = in.readAllBytes();
        }

        JsonNode root = objectMapper.readTree(bodyBytes);
        if (null == root || !root.isObject()) {
            final Response rebuildResponse = response.toBuilder()
                .body(bodyBytes)
                .build();
            return decoder.decode(rebuildResponse, type);
        }

        // 4. 解密 & 原地替换
        final JsonNode biz = root.get(ResponseConstant.BIZ_DATA);
        if (biz != null && biz.isTextual()) {
            String plain = CryptoUtil.decrypt(biz.asText(), cfg.getCrypto());
            ((ObjectNode) root).set(ResponseConstant.BIZ_DATA, objectMapper.readTree(plain));
        }

        return objectMapper.readerFor(javaType(type)).readValue(root);
    }

    private JavaType javaType(Type type) {
        // computeIfAbsent 线程安全，且只在第一次计算
        return TYPE_CACHE.computeIfAbsent(type, objectMapper::constructType);
    }
}

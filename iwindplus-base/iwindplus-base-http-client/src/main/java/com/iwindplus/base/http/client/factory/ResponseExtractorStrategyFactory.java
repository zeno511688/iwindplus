/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.factory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.http.client.domain.dto.HttpExecuteResultDTO;
import com.iwindplus.base.http.client.extractor.ResponseExtractor;
import com.iwindplus.base.http.client.extractor.ResponseTypeDescriptor;
import com.iwindplus.base.http.client.extractor.impl.ClassJsonResponseExtractor;
import com.iwindplus.base.http.client.extractor.impl.StringResponseExtractor;
import com.iwindplus.base.http.client.extractor.impl.TypeReferenceJsonResponseExtractor;
import com.iwindplus.base.http.client.extractor.impl.VoidResponseExtractor;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * 响应类型描述符策略工厂.
 *
 * @author zengdegui
 * @since 2026/01/21 00:54
 */
@Slf4j
public class ResponseExtractorStrategyFactory {

    private final List<ResponseExtractor<?>> extractors = new ArrayList<>(10);

    /**
     * 构造函数.
     */
    public ResponseExtractorStrategyFactory() {
        extractors.add(new VoidResponseExtractor());
        extractors.add(new StringResponseExtractor());
        extractors.add(new TypeReferenceJsonResponseExtractor<>());
        extractors.add(new ClassJsonResponseExtractor());
    }

    /**
     * 响应提取.
     *
     * @param result 执行结果
     * @param clazz  响应类型
     * @param <T>    响应类型
     * @return 响应结果
     */
    public <T> T extract(HttpExecuteResultDTO result, Class<T> clazz) {
        ResponseTypeDescriptor descriptor = ResponseTypeDescriptor.forClass(clazz);
        return doExtract(result, descriptor);
    }

    /**
     * 响应提取.
     *
     * @param result        执行结果
     * @param typeReference 响应类型
     * @param <T>           响应类型
     * @return 响应结果
     */
    public <T> T extract(HttpExecuteResultDTO result, TypeReference<?> typeReference) {
        return doExtract(result, ResponseTypeDescriptor.forTypeReference(typeReference));
    }

    private <T> T doExtract(HttpExecuteResultDTO result, ResponseTypeDescriptor descriptor) {
        for (ResponseExtractor<?> extractor : extractors) {
            if (extractor.supports(descriptor)) {
                return (T) extractor.extract(result, descriptor);
            }
        }
        log.error("ResponseExtractorRegistry Invalid strategy={}", descriptor);
        throw new BizException(BizCodeEnum.INVALID_STRATEGY);
    }
}

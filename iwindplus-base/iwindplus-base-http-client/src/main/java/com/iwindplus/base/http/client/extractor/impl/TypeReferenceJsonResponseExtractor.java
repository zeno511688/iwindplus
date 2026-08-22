/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.extractor.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.http.client.domain.dto.HttpExecuteResultDTO;
import com.iwindplus.base.http.client.extractor.ResponseTypeDescriptor;
import com.iwindplus.base.util.JacksonUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * TypeReference响应类型描述符.
 *
 * @author zengdegui
 * @since 2026/01/21 00:53
 */
@Slf4j
public class TypeReferenceJsonResponseExtractor<T> extends AbstractResponseExtractor<T> {

    @Override
    public boolean supports(ResponseTypeDescriptor descriptor) {
        return descriptor.isTypeReference();
    }

    @Override
    public T extract(HttpExecuteResultDTO result, ResponseTypeDescriptor descriptor) {
        final String body = getBody(result);
        if (CharSequenceUtil.isBlank(body)) {
            return null;
        }
        try {
            final TypeReference<?> typeReference = descriptor.typeReference();
            return (T) JacksonUtil.parseObject(body, typeReference);
        } catch (Exception e) {
            log.error("Failed to parse JSON response to type: {}, body: {}", 
                descriptor.typeReference().getType().getTypeName(), body, e);
            throw new BizException(BizCodeEnum.RESPONSE_NOT_JSON, 
                String.format("Failed to parse response to %s", descriptor.typeReference().getType().getTypeName()));
        }
    }
}

/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.extractor.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.iwindplus.base.http.client.domain.dto.HttpExecuteResultDTO;
import com.iwindplus.base.http.client.extractor.ResponseTypeDescriptor;
import com.iwindplus.base.util.JacksonUtil;

/**
 * TypeReference响应类型描述符.
 *
 * @author zengdegui
 * @since 2026/01/21 00:53
 */
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
        final TypeReference<?> typeReference = descriptor.typeReference();
        return (T) JacksonUtil.parseObject(body, typeReference);
    }
}

/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.extractor.impl;

import com.iwindplus.base.http.client.domain.dto.HttpExecuteResultDTO;
import com.iwindplus.base.http.client.extractor.ResponseTypeDescriptor;

/**
 * 字符串响应类型描述符.
 *
 * @author zengdegui
 * @since 2026/01/21 00:49
 */
public class StringResponseExtractor extends AbstractResponseExtractor<String> {

    @Override
    public boolean supports(ResponseTypeDescriptor descriptor) {
        return descriptor.isClass() && descriptor.rawClass() == String.class;
    }

    @Override
    public String extract(HttpExecuteResultDTO result, ResponseTypeDescriptor descriptor) {
        checkError(result);
        return result.body();
    }
}

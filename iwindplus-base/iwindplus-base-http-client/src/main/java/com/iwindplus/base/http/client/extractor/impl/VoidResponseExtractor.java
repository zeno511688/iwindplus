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
 * 无返回响应类型描述符.
 *
 * @author zengdegui
 * @since 2026/01/21 00:59
 */
public class VoidResponseExtractor extends AbstractResponseExtractor<Void> {

    @Override
    public boolean supports(ResponseTypeDescriptor descriptor) {
        return descriptor.isClass()
            && (descriptor.rawClass() == Void.class || descriptor.rawClass() == void.class);
    }

    @Override
    public Void extract(HttpExecuteResultDTO result, ResponseTypeDescriptor descriptor) {
        checkError(result);
        return null;
    }
}
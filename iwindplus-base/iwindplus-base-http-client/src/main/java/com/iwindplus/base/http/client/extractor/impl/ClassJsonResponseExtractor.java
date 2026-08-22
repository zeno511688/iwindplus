/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.extractor.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.http.client.domain.dto.HttpExecuteResultDTO;
import com.iwindplus.base.http.client.extractor.ResponseTypeDescriptor;
import com.iwindplus.base.util.JacksonUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * class json响应类型描述符.
 *
 * @author zengdegui
 * @since 2026/01/21 00:51
 */
@Slf4j
public class ClassJsonResponseExtractor extends AbstractResponseExtractor<Object> {

    @Override
    public boolean supports(ResponseTypeDescriptor descriptor) {
        return descriptor.isClass()
            && descriptor.rawClass() != String.class
            && descriptor.rawClass() != Void.class
            && descriptor.rawClass() != void.class;
    }

    @Override
    public Object extract(HttpExecuteResultDTO result, ResponseTypeDescriptor descriptor) {
        final String body = getBody(result);
        if (CharSequenceUtil.isBlank(body)) {
            return null;
        }

        try {
            return JacksonUtil.parseObject(body, descriptor.rawClass());
        } catch (Exception e) {
            log.error("Failed to parse JSON response to type: {}, body: {}", 
                descriptor.rawClass().getName(), body, e);
            throw new BizException(BizCodeEnum.RESPONSE_NOT_JSON, 
                String.format("Failed to parse response to %s", descriptor.rawClass().getSimpleName()));
        }
    }
}

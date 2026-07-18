/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.extractor;

import com.iwindplus.base.http.client.domain.dto.HttpExecuteResultDTO;

/**
 * 响应提取器.
 *
 * @author zengdegui
 * @since 2026/01/21 00:43
 */
public interface ResponseExtractor<T> {

    /**
     * 是否支持该响应类型
     *
     * @param descriptor 响应类型
     * @return boolean
     */
    boolean supports(ResponseTypeDescriptor descriptor);

    /**
     * 从 HttpExecuteResultDTO 提取业务结果
     *
     * @param result     结果
     * @param descriptor 响应类型
     * @return T
     */
    T extract(HttpExecuteResultDTO result, ResponseTypeDescriptor descriptor);
}
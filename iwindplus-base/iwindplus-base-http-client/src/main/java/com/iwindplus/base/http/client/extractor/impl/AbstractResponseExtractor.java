/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.extractor.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.http.client.domain.dto.HttpExecuteResultDTO;
import com.iwindplus.base.http.client.extractor.ResponseExtractor;
import lombok.extern.slf4j.Slf4j;

/**
 * 抽象响应类型描述符.
 *
 * @author zengdegui
 * @since 2026/01/21 00:47
 */
@Slf4j
public abstract class AbstractResponseExtractor<T> implements ResponseExtractor<T> {

    /**
     * 检查执行结果是否有错误.
     *
     * @param result 执行结果
     */
    protected void checkError(HttpExecuteResultDTO result) {
        if (result == null) {
            throw new BizException(BizCodeEnum.EMPTY_RESPONSE);
        }
    }

    /**
     * 检查执行结果是否有错误和响应体.
     *
     * @param result 执行结果
     */
    protected String getBody(HttpExecuteResultDTO result) {
        checkError(result);

        if (CharSequenceUtil.isBlank(result.body())) {
            return null;
        }
        if (!JSONUtil.isTypeJSON(result.body())) {
            return null;
        }
        return result.body();
    }
}

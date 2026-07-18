/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.web.support;

import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.domain.vo.UserBaseVO;
import com.iwindplus.base.util.CryptoUtil;
import com.iwindplus.base.util.HttpsUtil;
import com.iwindplus.base.util.JacksonUtil;
import com.iwindplus.base.web.domain.property.FilterProperty;
import com.iwindplus.base.web.domain.property.FilterProperty.FilterCryptoConfig;
import com.iwindplus.base.web.domain.property.ResponseBodyProperty;
import com.iwindplus.base.web.domain.property.ResponseBodyProperty.ResponseBodyCryptoConfig;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;

/**
 * web管理器.
 *
 * @author zengdegui
 * @since 2025/02/15 21:45
 */
@Slf4j
public record WebManager(
    FilterProperty filterProperty,
    ResponseBodyProperty responseBodyProperty) {

    /**
     * 解析用户信息.
     *
     * @param userInfoStr 请求头用户信息字符串
     * @return UserBaseVO
     */
    public UserBaseVO getUserInfo(String userInfoStr) {
        final FilterCryptoConfig crypto = this.filterProperty.getCrypto();
        final String data = CryptoUtil.decrypt(userInfoStr, crypto);
        return JacksonUtil.parseObject(data, UserBaseVO.class);
    }


    /**
     * 响应数据.
     *
     * @param response       响应
     * @param httpStatusCode http状态码
     * @param result         响应实体
     */
    public void responseData(HttpServletResponse response, HttpStatusCode httpStatusCode, ResultVO<Object> result) {
        this.encryptResult(result);
        HttpsUtil.responseData(response, httpStatusCode, result);
    }

    /**
     * 加密返回结果.
     *
     * @param result 结果
     */
    public void encryptResult(ResultVO<Object> result) {
        final ResponseBodyCryptoConfig crypto = responseBodyProperty.getCrypto();
        if (Boolean.FALSE.equals(crypto.getEnabled())) {
            return;
        }

        final String encrypt = CryptoUtil.encrypt(JacksonUtil.toJsonStr(result.getBizData()), crypto);
        result.setBizData(encrypt);
    }
}
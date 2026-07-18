/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.ocr.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.http.client.domain.enums.HttpClientTypeEnum;
import com.iwindplus.base.http.client.executor.HttpClientExecutor;
import com.iwindplus.base.http.client.factory.HttpClientExecutorStrategyFactory;
import com.iwindplus.base.ocr.domain.constant.OcrConstant;
import com.iwindplus.base.ocr.domain.enums.OcrPrintIdTypeEnum;
import com.iwindplus.base.ocr.service.OcrPrintWordService;
import com.iwindplus.base.util.JacksonUtil;
import jakarta.annotation.Resource;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.multipart.MultipartFile;

/**
 * 印刷文字ocr业务层接口实现类.
 *
 * @author zengdegui
 * @since 2019/8/13
 */
@Slf4j
public class OcrPrintWordServiceImpl extends AbstractOcrBaseServiceImpl implements OcrPrintWordService {

    @Resource
    private HttpClientExecutorStrategyFactory httpClientExecutorStrategyFactory;

    @Override
    public JsonNode parseIdCardImage(MultipartFile file, OcrPrintIdTypeEnum idType) {
        Map<String, Object> bodyMap = new LinkedHashMap<>(16);
        Map<String, Object> childrenMap = new LinkedHashMap<>(16);
        childrenMap.put("side", idType.getValue());
        bodyMap.put("configure", childrenMap);
        return getJsonNode(OcrConstant.PrintWordConstant.ID_CARD_URL, file, bodyMap);
    }

    @Override
    public JsonNode parseBusinessLicenseImage(MultipartFile file) {
        Map<String, Object> bodyMap = new LinkedHashMap<>(16);
        return getJsonNode(OcrConstant.PrintWordConstant.BUSINESS_LICENSE_URL, file, bodyMap);
    }

    private JsonNode getJsonNode(String url, MultipartFile file, Map<String, Object> bodyMap) {
        super.checkFile(file);
        String authorization = new StringBuilder("APPCODE ").append(this.getConfig().getPrintWord().getAppCode()).toString();
        Map<String, String> header = new HashMap<>(16);
        header.put(HttpHeaders.AUTHORIZATION, authorization);
        try {
            bodyMap.put("image", Base64.getEncoder().encodeToString(file.getBytes()));
            final String result = this.getHttpClientExecutor().post(url, bodyMap, header, String.class);
            if (CharSequenceUtil.isNotBlank(result)) {
                JsonNode data = JacksonUtil.parseTree(result);
                if (data != null) {
                    final JsonNode successNode = data.get("success");
                    boolean status = successNode != null && successNode.asBoolean();
                    if (status) {
                        return data;
                    }
                }
            }
        } catch (IOException ex) {
            log.error(ExceptionConstant.IO_EXCEPTION, ex);
        }
        throw new BizException(BizCodeEnum.SCAN_ERROR);
    }

    private HttpClientExecutor getHttpClientExecutor() {
        return this.httpClientExecutorStrategyFactory.getHttpClientExecutor(HttpClientTypeEnum.OK_HTTP);
    }

}

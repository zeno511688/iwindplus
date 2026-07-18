/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.ocr.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.http.client.domain.enums.HttpClientTypeEnum;
import com.iwindplus.base.http.client.executor.HttpClientExecutor;
import com.iwindplus.base.http.client.factory.HttpClientExecutorStrategyFactory;
import com.iwindplus.base.ocr.domain.constant.OcrConstant;
import com.iwindplus.base.ocr.domain.enums.OcrXiangyunIdTypeEnum;
import com.iwindplus.base.ocr.service.OcrXiangyunService;
import com.iwindplus.base.util.JacksonUtil;
import jakarta.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

/**
 * 翔云ocr业务层接口实现类.
 *
 * @author zengdegui
 * @since 2019/8/13
 */
@Slf4j
public class OcrXiangyunServiceImpl extends AbstractOcrBaseServiceImpl implements OcrXiangyunService {

    @Resource
    private HttpClientExecutorStrategyFactory httpClientExecutorStrategyFactory;

    @Override
    public JsonNode parseIdCardImage(MultipartFile file, OcrXiangyunIdTypeEnum idType) {
        Map<String, Object> bodyMap = new LinkedHashMap<>(16);
        bodyMap.put("key", this.getConfig().getXiangyun().getAccessKey());
        bodyMap.put("secret", this.getConfig().getXiangyun().getSecretKey());
        bodyMap.put("format", "json");
        bodyMap.put("typeId", idType.getValue());
        return getJsonNode(OcrConstant.XiangyunConstant.ID_CARD_URL, file, bodyMap);
    }

    @Override
    public JsonNode parseBusinessLicenseImage(MultipartFile file) {
        Map<String, Object> bodyMap = new LinkedHashMap<>(16);
        bodyMap.put("key", this.getConfig().getXiangyun().getAccessKey());
        bodyMap.put("secret", this.getConfig().getXiangyun().getSecretKey());
        bodyMap.put("format", "json");
        bodyMap.put("typeId", OcrConstant.XiangyunConstant.BUSINESS_LICENSE_CODE);
        bodyMap.put("outvalue", 0);
        return getJsonNode(OcrConstant.XiangyunConstant.BUSINESS_LICENSE_URL, file, bodyMap);
    }

    private JsonNode getJsonNode(String url, MultipartFile file, Map<String, Object> bodyMap) {
        super.checkFile(file);
        final String result = this.getHttpClientExecutor().post(url, bodyMap, List.of(file), null, String.class);
        if (CharSequenceUtil.isNotBlank(result)) {
            JsonNode data = JacksonUtil.parseTree(result);
            if (data != null) {
                final JsonNode messageNode = data.get("message");
                final JsonNode statusNode = messageNode != null ? messageNode.get("status") : null;
                int status = statusNode != null ? statusNode.asInt() : -1;
                if (status >= 0) {
                    return data;
                }
            }
        }
        throw new BizException(BizCodeEnum.SCAN_ERROR);
    }

    private HttpClientExecutor getHttpClientExecutor() {
        return this.httpClientExecutorStrategyFactory.getHttpClientExecutor(HttpClientTypeEnum.OK_HTTP);
    }
}

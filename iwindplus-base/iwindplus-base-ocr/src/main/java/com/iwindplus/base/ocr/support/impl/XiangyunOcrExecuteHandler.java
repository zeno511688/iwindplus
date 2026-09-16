/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.ocr.support.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.http.client.domain.enums.HttpClientTypeEnum;
import com.iwindplus.base.http.client.support.HttpClientExecuteHandler;
import com.iwindplus.base.http.client.factory.HttpClientExecuteHandlerFactory;
import com.iwindplus.base.ocr.domain.constant.OcrConstant.XiangyunConstant;
import com.iwindplus.base.ocr.domain.enums.OcrTypeEnum;
import com.iwindplus.base.ocr.domain.enums.OcrXiangyunIdTypeEnum;
import com.iwindplus.base.ocr.domain.property.OcrProperty.XiangyunConfig;
import com.iwindplus.base.ocr.domain.vo.OcrBusinessLicenseVO;
import com.iwindplus.base.ocr.domain.vo.OcrIdCardVO;
import com.iwindplus.base.ocr.service.impl.AbstractBaseServiceImpl;
import com.iwindplus.base.ocr.support.OcrExecuteHandler;
import com.iwindplus.base.util.JacksonUtil;
import jakarta.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.web.multipart.MultipartFile;

/**
 * 翔云OCR业务层实现类.
 *
 * @author zengdegui
 * @since 2019/8/13
 */
@Slf4j
public class XiangyunOcrExecuteHandler extends AbstractBaseServiceImpl<XiangyunConfig> implements OcrExecuteHandler {

    @Resource
    private HttpClientExecuteHandlerFactory httpClientExecuteHandlerFactory;

    /**
     * 构造函数.
     *
     * @param multipartProperties 文件上传配置
     * @param config              翔云配置
     */
    public XiangyunOcrExecuteHandler(
        MultipartProperties multipartProperties,
        XiangyunConfig config) {
        super(multipartProperties);
        super.setConfig(config);
    }

    @Override
    public OcrTypeEnum getType() {
        return OcrTypeEnum.XIANGYUN;
    }

    @Override
    public OcrIdCardVO parseIdCardImage(MultipartFile file, Object idType) {
        if (!(idType instanceof OcrXiangyunIdTypeEnum)) {
            throw new IllegalArgumentException("idType must be instance of OcrXiangyunIdTypeEnum");
        }
        Map<String, Object> bodyMap = new LinkedHashMap<>(16);
        bodyMap.put("key", this.getConfig().getAccessKey());
        bodyMap.put("secret", this.getConfig().getSecretKey());
        bodyMap.put("format", "json");
        bodyMap.put("typeId", ((OcrXiangyunIdTypeEnum) idType).getValue());
        return getIdCardResult(XiangyunConstant.ID_CARD_URL, file, bodyMap);
    }

    @Override
    public OcrBusinessLicenseVO parseBusinessLicenseImage(MultipartFile file) {
        Map<String, Object> bodyMap = new LinkedHashMap<>(16);
        bodyMap.put("key", this.getConfig().getAccessKey());
        bodyMap.put("secret", this.getConfig().getSecretKey());
        bodyMap.put("format", "json");
        bodyMap.put("typeId", XiangyunConstant.BUSINESS_LICENSE_CODE);
        bodyMap.put("outvalue", 0);
        return getBusinessLicenseResult(XiangyunConstant.BUSINESS_LICENSE_URL, file, bodyMap);
    }

    private OcrIdCardVO getIdCardResult(String url, MultipartFile file, Map<String, Object> bodyMap) {
        super.checkFile(file);
        final String result = this.getHandler().post(url, bodyMap, List.of(file), null, String.class);
        if (CharSequenceUtil.isNotBlank(result)) {
            JsonNode data = JacksonUtil.parseTree(result);
            if (data != null && isSuccess(data)) {
                Map<String, String> fieldMap = extractFields(data);
                return OcrIdCardVO.builder()
                    .name(fieldMap.get("姓名"))
                    .sex(fieldMap.get("性别"))
                    .nation(fieldMap.get("民族"))
                    .birth(fieldMap.get("出生"))
                    .address(fieldMap.get("住址"))
                    .idNumber(fieldMap.get("公民身份号码"))
                    .authority(fieldMap.get("签发机关"))
                    .validPeriod(fieldMap.get("有效期限"))
                    .build();
            }
        }
        throw new BizException(BizCodeEnum.SCAN_ERROR);
    }

    private OcrBusinessLicenseVO getBusinessLicenseResult(String url, MultipartFile file,
        Map<String, Object> bodyMap) {
        super.checkFile(file);
        final String result = this.getHandler().post(url, bodyMap, List.of(file), null, String.class);
        if (CharSequenceUtil.isNotBlank(result)) {
            JsonNode data = JacksonUtil.parseTree(result);
            if (data != null && isSuccess(data)) {
                Map<String, String> fieldMap = extractFields(data);
                return OcrBusinessLicenseVO.builder()
                    .companyName(fieldMap.get("公司名称"))
                    .creditCode(fieldMap.get("统一社会信用代码"))
                    .legalPerson(fieldMap.get("法定代表人"))
                    .address(fieldMap.get("地址"))
                    .businessScope(fieldMap.get("经营范围"))
                    .establishDate(fieldMap.get("成立日期"))
                    .businessTerm(fieldMap.get("营业期限"))
                    .registrationAuthority(fieldMap.get("登记机关"))
                    .build();
            }
        }
        throw new BizException(BizCodeEnum.SCAN_ERROR);
    }

    private boolean isSuccess(JsonNode data) {
        final JsonNode messageNode = data.get("message");
        final JsonNode statusNode = messageNode != null ? messageNode.get("status") : null;
        int status = statusNode != null ? statusNode.asInt() : -1;
        return status >= 0;
    }

    private Map<String, String> extractFields(JsonNode data) {
        Map<String, String> fieldMap = new LinkedHashMap<>(16);
        final JsonNode cardsInfoNode = data.get("cardsinfo");
        if (cardsInfoNode != null && cardsInfoNode.isArray()) {
            for (JsonNode cardInfo : cardsInfoNode) {
                final JsonNode cardsNode = cardInfo.get("cards");
                if (cardsNode != null && cardsNode.isArray()) {
                    for (JsonNode card : cardsNode) {
                        final JsonNode nameNode = card.get("name");
                        final JsonNode valueNode = card.get("value");
                        if (nameNode != null && valueNode != null) {
                            fieldMap.put(nameNode.asText(), valueNode.asText());
                        }
                    }
                }
            }
        }
        return fieldMap;
    }

    private HttpClientExecuteHandler getHandler() {
        return this.httpClientExecuteHandlerFactory.getHandler(HttpClientTypeEnum.OK_HTTP);
    }
}

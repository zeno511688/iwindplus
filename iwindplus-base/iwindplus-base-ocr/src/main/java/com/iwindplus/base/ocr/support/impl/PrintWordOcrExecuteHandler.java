/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.ocr.support.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.http.client.domain.enums.HttpClientTypeEnum;
import com.iwindplus.base.http.client.factory.HttpClientExecuteHandlerFactory;
import com.iwindplus.base.http.client.support.HttpClientExecuteHandler;
import com.iwindplus.base.ocr.domain.constant.OcrConstant.PrintWordConstant;
import com.iwindplus.base.ocr.domain.enums.OcrPrintIdTypeEnum;
import com.iwindplus.base.ocr.domain.enums.OcrTypeEnum;
import com.iwindplus.base.ocr.domain.property.OcrProperty.PrintWordConfig;
import com.iwindplus.base.ocr.domain.vo.OcrBusinessLicenseVO;
import com.iwindplus.base.ocr.domain.vo.OcrIdCardVO;
import com.iwindplus.base.ocr.service.impl.AbstractBaseServiceImpl;
import com.iwindplus.base.ocr.support.OcrExecuteHandler;
import com.iwindplus.base.util.JacksonUtil;
import jakarta.annotation.Resource;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.web.multipart.MultipartFile;

/**
 * 印刷文字OCR业务层实现类.
 *
 * @author zengdegui
 * @since 2019/8/13
 */
@Slf4j
public class PrintWordOcrExecuteHandler extends AbstractBaseServiceImpl<PrintWordConfig> implements OcrExecuteHandler {

    @Resource
    private HttpClientExecuteHandlerFactory httpClientExecuteHandlerFactory;

    /**
     * 构造函数.
     *
     * @param multipartProperties 文件上传配置
     * @param config              印刷文字OCR配置
     */
    public PrintWordOcrExecuteHandler(
        MultipartProperties multipartProperties,
        PrintWordConfig config) {
        super(multipartProperties);
        super.setConfig(config);
    }

    @Override
    public OcrTypeEnum getType() {
        return OcrTypeEnum.PRINT_WORD;
    }

    @Override
    public OcrIdCardVO parseIdCardImage(MultipartFile file, Object idType) {
        if (!(idType instanceof OcrPrintIdTypeEnum)) {
            throw new IllegalArgumentException("idType must be instance of OcrPrintIdTypeEnum");
        }
        Map<String, Object> bodyMap = new LinkedHashMap<>(16);
        Map<String, Object> childrenMap = new LinkedHashMap<>(16);
        childrenMap.put("side", ((OcrPrintIdTypeEnum) idType).getValue());
        bodyMap.put("configure", childrenMap);
        return getIdCardResult(PrintWordConstant.ID_CARD_URL, file, bodyMap);
    }

    @Override
    public OcrBusinessLicenseVO parseBusinessLicenseImage(MultipartFile file) {
        Map<String, Object> bodyMap = new LinkedHashMap<>(16);
        return getBusinessLicenseResult(PrintWordConstant.BUSINESS_LICENSE_URL, file, bodyMap);
    }

    private OcrIdCardVO getIdCardResult(String url, MultipartFile file, Map<String, Object> bodyMap) {
        JsonNode bodyNode = request(url, file, bodyMap);
        return OcrIdCardVO.builder()
            .name(getFieldValue(bodyNode, "name", "姓名"))
            .sex(getFieldValue(bodyNode, "sex", "性别"))
            .nation(getFieldValue(bodyNode, "nation", "民族"))
            .birth(getFieldValue(bodyNode, "birth", "出生"))
            .address(getFieldValue(bodyNode, "address", "住址"))
            .idNumber(getFieldValue(bodyNode, "id_number", "idNumber", "公民身份号码"))
            .authority(getFieldValue(bodyNode, "authority", "签发机关"))
            .validPeriod(getFieldValue(bodyNode, "valid_period", "validPeriod", "有效期限"))
            .build();
    }

    private OcrBusinessLicenseVO getBusinessLicenseResult(String url, MultipartFile file,
        Map<String, Object> bodyMap) {
        JsonNode bodyNode = request(url, file, bodyMap);
        return OcrBusinessLicenseVO.builder()
            .companyName(getFieldValue(bodyNode, "company_name", "companyName", "公司名称"))
            .creditCode(getFieldValue(bodyNode, "credit_code", "creditCode", "统一社会信用代码"))
            .legalPerson(getFieldValue(bodyNode, "legal_person", "legalPerson", "法定代表人"))
            .address(getFieldValue(bodyNode, "address", "地址"))
            .businessScope(getFieldValue(bodyNode, "business_scope", "businessScope", "经营范围"))
            .establishDate(getFieldValue(bodyNode, "establish_date", "establishDate", "成立日期"))
            .businessTerm(getFieldValue(bodyNode, "business_term", "businessTerm", "营业期限"))
            .registrationAuthority(getFieldValue(bodyNode, "registration_authority", "registrationAuthority",
                "登记机关"))
            .build();
    }

    private JsonNode request(String url, MultipartFile file, Map<String, Object> bodyMap) {
        super.checkFile(file);
        String authorization = new StringBuilder("APPCODE ").append(this.getConfig().getAppCode()).toString();
        Map<String, String> header = new HashMap<>(16);
        header.put(HttpHeaders.AUTHORIZATION, authorization);
        try {
            bodyMap.put("image", Base64.getEncoder().encodeToString(file.getBytes()));
            final String result = this.getHandler().post(url, bodyMap, header, String.class);
            if (CharSequenceUtil.isNotBlank(result)) {
                JsonNode data = JacksonUtil.parseTree(result);
                if (data != null) {
                    final JsonNode successNode = data.get("success");
                    boolean success = successNode != null && successNode.asBoolean();
                    if (success) {
                        final JsonNode bodyNode = data.get("showapi_res_body");
                        if (bodyNode != null && bodyNode.isObject()) {
                            return bodyNode;
                        }
                    }
                }
            }
        } catch (IOException ex) {
            log.error(ExceptionConstant.IO_EXCEPTION, ex);
        }
        throw new BizException(BizCodeEnum.SCAN_ERROR);
    }

    private String getFieldValue(JsonNode bodyNode, String... keys) {
        if (bodyNode == null) {
            return null;
        }
        for (String key : keys) {
            final JsonNode valueNode = bodyNode.get(key);
            if (valueNode != null && !valueNode.isNull()) {
                return valueNode.asText();
            }
        }
        return null;
    }

    private HttpClientExecuteHandler getHandler() {
        return this.httpClientExecuteHandlerFactory.getHandler(HttpClientTypeEnum.OK_HTTP);
    }

}

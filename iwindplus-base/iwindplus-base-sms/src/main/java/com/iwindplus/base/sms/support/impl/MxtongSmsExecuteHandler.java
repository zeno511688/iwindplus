/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.sms.support.impl;

import cn.hutool.core.net.url.UrlBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.domain.constant.CommonConstant.SymbolConstant;
import com.iwindplus.base.domain.enums.SmsTypeEnum;
import com.iwindplus.base.sms.domain.constant.SmsConstant.MxtongConstant;
import com.iwindplus.base.sms.domain.dto.SmsSendRequestDTO;
import com.iwindplus.base.sms.domain.property.SmsProperty.MxtongConfig;
import com.iwindplus.base.sms.service.impl.AbstractBaseServiceImpl;
import com.iwindplus.base.sms.support.SmsExecuteHandler;
import com.iwindplus.base.util.JacksonUtil;
import com.iwindplus.base.util.TemplateUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * 麦讯通短信业务层接口实现类.
 *
 * @author zengdegui
 * @since 2019/8/13
 */
@Slf4j
public class MxtongSmsExecuteHandler extends AbstractBaseServiceImpl<MxtongConfig> implements SmsExecuteHandler {

    /**
     * 构造函数.
     *
     * @param config 麦讯通短信配置
     */
    public MxtongSmsExecuteHandler(MxtongConfig config) {
        this.setConfig(config);
    }

    @Override
    public SmsTypeEnum getProvider() {
        return SmsTypeEnum.MXTONG;
    }

    @Override
    protected String sendSms(SmsSendRequestDTO entity) throws IOException {
        final List<String> phoneNumbers = entity.getPhoneNumbers();
        MxtongConfig mxtong = this.getConfig();
        String result = null;
        String path = UrlBuilder.ofHttp(MxtongConstant.MX_TONG_SMS_URL)
            .addQuery(MxtongConstant.ACCESS_KEY, mxtong.getAccessKey())
            .addQuery(MxtongConstant.SECRET_KEY, mxtong.getSecretKey())
            .addQuery(MxtongConstant.MOBILE, phoneNumbers.stream().collect(Collectors.joining(SymbolConstant.COMMA)))
            .addQuery(MxtongConstant.CONTENT, TemplateUtil.getTemplateContent(entity.getTemplateContent(), entity.getTemplateParams()))
            .addQuery(MxtongConstant.NEED_STATUS, "true")
            .addQuery(MxtongConstant.RESPONSE_TYPE, "json")
            .build();
        try (InputStreamReader inputStreamReader = new InputStreamReader(new URI(path).toURL().openStream(), Charset.defaultCharset());
            BufferedReader br = new BufferedReader(inputStreamReader)) {
            String data = br.readLine();
            JsonNode jsonNode = JacksonUtil.parseTree(data);
            if (jsonNode != null) {
                final JsonNode resultNode = jsonNode.get(MxtongConstant.RESPONSE_CODE);
                int vo = resultNode != null ? resultNode.asInt() : -1;
                if (vo == 0) {
                    final JsonNode bizNumberNode = jsonNode.get(MxtongConstant.BIZ_NUMBER);
                    result = bizNumberNode != null ? bizNumberNode.asText() : null;
                }
            }
        } catch (URISyntaxException ex) {
            log.error(ExceptionConstant.URI_SYNTAX_EXCEPTION, ex);
        }
        return result;
    }
}

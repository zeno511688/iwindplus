/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.sms.support.impl;

import cn.hutool.core.net.url.UrlBuilder;
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.domain.constant.CommonConstant.SymbolConstant;
import com.iwindplus.base.domain.enums.SmsTypeEnum;
import com.iwindplus.base.sms.domain.constant.SmsConstant.LingKaiConstant;
import com.iwindplus.base.sms.domain.dto.SmsSendRequestDTO;
import com.iwindplus.base.sms.domain.property.SmsProperty.LingkaiConfig;
import com.iwindplus.base.sms.service.impl.AbstractBaseServiceImpl;
import com.iwindplus.base.sms.support.SmsExecuteHandler;
import com.iwindplus.base.util.CryptoUtil;
import com.iwindplus.base.util.TemplateUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * 凌凯短信业务层接口实现类.
 *
 * @author zengdegui
 * @since 2019/8/13
 */
@Slf4j
public class LingkaiSmsExecuteHandler extends AbstractBaseServiceImpl<LingkaiConfig> implements SmsExecuteHandler {

    /**
     * 构造函数.
     *
     * @param config 凌凯短信配置
     */
    public LingkaiSmsExecuteHandler(LingkaiConfig config) {
        this.setConfig(config);
    }

    @Override
    public SmsTypeEnum getProvider() {
        return SmsTypeEnum.LINGKAI;
    }

    @Override
    protected String sendSms(SmsSendRequestDTO entity) throws IOException {
        final List<String> phoneNumbers = entity.getPhoneNumbers();
        LingkaiConfig lingkai = this.getConfig();
        String path = UrlBuilder.ofHttp(LingKaiConstant.LING_KAI_SMS_URL)
            .addQuery(LingKaiConstant.ACCESS_KEY, lingkai.getAccessKey())
            .addQuery(LingKaiConstant.SECRET_KEY, lingkai.getSecretKey())
            .addQuery(LingKaiConstant.MOBILE, phoneNumbers.stream().collect(Collectors.joining(SymbolConstant.COMMA)))
            .addQuery(LingKaiConstant.CONTENT, TemplateUtil.getTemplateContent(entity.getTemplateContent(), entity.getTemplateParams()))
            .build();
        try (InputStreamReader inputStreamReader = new InputStreamReader(new URI(path).toURL().openStream(), Charset.defaultCharset());
            BufferedReader br = new BufferedReader(inputStreamReader)) {
            String data = br.readLine();
            boolean success = Optional.ofNullable(data).map(p -> Integer.parseInt(p) > 0).orElse(Boolean.FALSE);
            if (success) {
                return CryptoUtil.encryptBySm3(phoneNumbers.stream().collect(Collectors.joining(SymbolConstant.UNDERLINE)));
            }
        } catch (URISyntaxException ex) {
            log.error(ExceptionConstant.URI_SYNTAX_EXCEPTION, ex);
        }
        return null;
    }
}
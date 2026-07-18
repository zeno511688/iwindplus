/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.sms.service.impl;

import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.util.RandomUtil;
import com.google.common.collect.Lists;
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.domain.constant.CommonConstant.SymbolConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.sms.domain.constant.SmsConstant;
import com.iwindplus.base.sms.domain.constant.SmsConstant.LingKaiConstant;
import com.iwindplus.base.sms.domain.property.SmsProperty.LingkaiConfig;
import com.iwindplus.base.sms.domain.vo.SmsBatchVO;
import com.iwindplus.base.sms.domain.vo.SmsLogVO;
import com.iwindplus.base.sms.service.SmsLingkaiService;
import com.iwindplus.base.util.CryptoUtil;
import com.iwindplus.base.util.TemplateUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
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
public class SmsLingkaiServiceImpl extends AbstractSmsBaseServiceImpl implements SmsLingkaiService {

    @Override
    public SmsLogVO smsSendCaptcha(String phoneNumber, Integer captchaLength, Integer captchaTimeout) {
        String captcha = RandomUtil.randomNumbers(Optional.ofNullable(captchaLength).orElse(SmsConstant.CAPTCHA_LENGTH));
        List<SmsBatchVO> result = this.smsSend(Arrays.asList(phoneNumber), Arrays.asList(captcha), null);
        return super.getSmsLogVO(captchaTimeout, result);
    }

    @Override
    public List<SmsBatchVO> smsSend(List<String> phoneNumbers, List<String> templateParams, Integer phoneNumberGroupSize) {
        List<SmsBatchVO> list = new ArrayList<>(10);
        try {
            this.smsBatch(phoneNumbers, templateParams, phoneNumberGroupSize, list);
        } catch (IOException ex) {
            log.error(ExceptionConstant.IO_EXCEPTION, ex);

            throw new BizException(BizCodeEnum.SEND_ERROR);
        }
        return list;
    }

    private void smsBatch(List<String> phoneNumbers, List<String> templateParams,
        Integer phoneNumberGroupSize, List<SmsBatchVO> list) throws IOException {
        int batchSize = Optional.ofNullable(phoneNumberGroupSize).orElse(SmsConstant.PHONE_NUMBER_GROUP_SIZE);
        List<List<String>> batches = Lists.partition(phoneNumbers, batchSize);
        for (List<String> subPhoneNumbers : batches) {
            boolean result = this.getSmsResponse(subPhoneNumbers, templateParams);
            if (result) {
                String bizNumber = CryptoUtil.encryptBySm3(subPhoneNumbers.stream().collect(Collectors.joining(SymbolConstant.UNDERLINE)));
                SmsBatchVO build = SmsBatchVO.builder()
                    .bizNumber(bizNumber)
                    .phoneNumbers(subPhoneNumbers)
                    .templateParams(templateParams)
                    .phoneNumberGroupSize(batchSize)
                    .build();
                list.add(build);
            }
        }
    }

    private boolean getSmsResponse(List<String> phoneNumbers, List<String> templateParams) throws IOException {
        LingkaiConfig lingkai = super.getConfig().getLingkai();
        String path = UrlBuilder.ofHttp(LingKaiConstant.LING_KAI_SMS_URL)
            .addQuery(LingKaiConstant.ACCESS_KEY, lingkai.getAccessKey())
            .addQuery(LingKaiConstant.SECRET_KEY, lingkai.getSecretKey())
            .addQuery(LingKaiConstant.MOBILE, phoneNumbers.stream().collect(Collectors.joining(SymbolConstant.COMMA)))
            .addQuery(LingKaiConstant.CONTENT, TemplateUtil.getTemplateContent(lingkai.getTemplateContent(), templateParams))
            .build();
        try (InputStreamReader inputStreamReader = new InputStreamReader(new URI(path).toURL().openStream(), Charset.defaultCharset());
            BufferedReader br = new BufferedReader(inputStreamReader)) {
            String data = br.readLine();
            return Optional.ofNullable(data).map(p -> Integer.parseInt(p) > 0).orElse(Boolean.FALSE);
        } catch (URISyntaxException ex) {
            log.error(ExceptionConstant.URI_SYNTAX_EXCEPTION, ex);
        }
        return false;
    }
}
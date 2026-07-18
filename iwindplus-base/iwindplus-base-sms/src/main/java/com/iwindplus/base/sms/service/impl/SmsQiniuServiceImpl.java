/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.sms.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.RandomUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.sms.domain.constant.SmsConstant;
import com.iwindplus.base.sms.domain.constant.SmsConstant.QiniuConstant;
import com.iwindplus.base.sms.domain.vo.SmsBatchVO;
import com.iwindplus.base.sms.domain.vo.SmsLogVO;
import com.iwindplus.base.sms.service.SmsQiniuService;
import com.iwindplus.base.util.JacksonUtil;
import com.iwindplus.base.util.TemplateUtil;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.sms.SmsManager;
import com.qiniu.sms.model.TemplateInfo;
import com.qiniu.util.Auth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

/**
 * 七牛云短信业务层接口实现类.
 *
 * @author zengdegui
 * @since 2019/8/13
 */
@Slf4j
public class SmsQiniuServiceImpl extends AbstractSmsBaseServiceImpl implements SmsQiniuService {

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
        } catch (QiniuException | JsonProcessingException ex) {
            log.error(ExceptionConstant.EXCEPTION, ex);

            throw new BizException(BizCodeEnum.SEND_ERROR);
        }
        return list;
    }

    private void smsBatch(List<String> phoneNumbers, List<String> templateParams, Integer phoneNumberGroupSize, List<SmsBatchVO> list)
        throws QiniuException, JsonProcessingException {
        int batchSize = Optional.ofNullable(phoneNumberGroupSize).orElse(SmsConstant.PHONE_NUMBER_GROUP_SIZE);
        List<List<String>> batches = Lists.partition(phoneNumbers, batchSize);
        for (List<String> subPhoneNumbers : batches) {
            // 发送短信
            String bizNumber = this.getSmsResponse(subPhoneNumbers, templateParams);
            if (CharSequenceUtil.isNotBlank(bizNumber)) {
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

    private String getSmsResponse(List<String> phoneNumbers, List<String> templateParams) throws QiniuException {
        Auth auth = Auth.create(this.getConfig().getQiniu().getAccessKey(), this.getConfig().getQiniu().getSecretKey());
        SmsManager smsManager = new SmsManager(auth);
        String[] mobiles = phoneNumbers.stream().toArray(String[]::new);
        Response response = null;
        try {
            TemplateInfo.Item describeTemplateItem = smsManager.describeTemplateItem(this.getConfig().getQiniu().getTemplateContent());
            if (Objects.nonNull(describeTemplateItem)) {
                Map<String, String> templateParam = TemplateUtil.getTemplateParam(describeTemplateItem.getTemplate(), templateParams);
                if (MapUtil.isNotEmpty(templateParam)) {
                    response = smsManager.sendMessage(this.getConfig().getQiniu().getTemplateContent(), mobiles, templateParam);
                } else {
                    response = smsManager.sendFulltextMessage(mobiles, describeTemplateItem.getTemplate());
                }
            }
            if (Objects.nonNull(response) && response.isOK()) {
                JsonNode data = JacksonUtil.parseTree(response.bodyString());
                return Optional.ofNullable(data)
                    .map(p -> p.get(QiniuConstant.BIZ_NUMBER))
                    .map(JsonNode::asText)
                    .orElse(null);
            }
        } finally {
            this.closeResponse(response);
        }
        return null;
    }

    private void closeResponse(Response response) {
        if (Objects.nonNull(response)) {
            response.close();
        }
    }
}
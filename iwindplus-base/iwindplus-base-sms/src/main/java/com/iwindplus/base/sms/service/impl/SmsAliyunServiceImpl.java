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
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.auth.sts.AssumeRoleRequest;
import com.aliyuncs.auth.sts.AssumeRoleResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.iwindplus.base.domain.constant.CommonConstant;
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.domain.constant.CommonConstant.SymbolConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.sms.domain.constant.SmsConstant;
import com.iwindplus.base.sms.domain.constant.SmsConstant.AliyunConstant;
import com.iwindplus.base.sms.domain.property.SmsProperty.AliyunConfig;
import com.iwindplus.base.sms.domain.property.SmsProperty.AliyunConfig.StsConfig;
import com.iwindplus.base.sms.domain.vo.SmsBatchVO;
import com.iwindplus.base.sms.domain.vo.SmsLogVO;
import com.iwindplus.base.sms.service.SmsAliyunService;
import com.iwindplus.base.util.DatesUtil;
import com.iwindplus.base.util.JacksonUtil;
import com.iwindplus.base.util.TemplateUtil;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

/**
 * 阿里云短信业务层接口实现类.
 *
 * @author zengdegui
 * @since 2019/8/13
 */
@Slf4j
public class SmsAliyunServiceImpl extends AbstractSmsBaseServiceImpl implements SmsAliyunService {

    @Override
    public SmsLogVO smsSendCaptcha(String phoneNumber, Integer captchaLength, Integer captchaTimeout) {
        String captcha = RandomUtil.randomNumbers(Optional.ofNullable(captchaLength).orElse(SmsConstant.CAPTCHA_LENGTH));
        List<SmsBatchVO> result = this.smsSend(Arrays.asList(phoneNumber), Arrays.asList(captcha), null);
        return super.getSmsLogVO(captchaTimeout, result);
    }

    @Override
    public List<SmsBatchVO> smsSend(List<String> phoneNumbers, List<String> templateParams, Integer phoneNumberGroupSize) {
        return this.smsSend(phoneNumbers, templateParams, null, phoneNumberGroupSize);
    }

    @Override
    public List<SmsBatchVO> smsSend(List<String> phoneNumbers, List<String> templateParams, String smsUpExtendCode, Integer phoneNumberGroupSize) {
        List<SmsBatchVO> list = new ArrayList<>(10);
        try {
            this.smsBatch(phoneNumbers, templateParams, smsUpExtendCode, phoneNumberGroupSize, list);
        } catch (ClientException ex) {
            log.error(ExceptionConstant.EXCEPTION, ex);

            throw new BizException(BizCodeEnum.SEND_ERROR);
        }
        return list;
    }

    private void smsBatch(List<String> phoneNumbers, List<String> templateParams, String smsUpExtendCode, Integer phoneNumberGroupSize,
        List<SmsBatchVO> list) throws ClientException {
        int batchSize = Optional.ofNullable(phoneNumberGroupSize).orElse(SmsConstant.PHONE_NUMBER_GROUP_SIZE);
        List<List<String>> batches = Lists.partition(phoneNumbers, batchSize);
        for (List<String> subPhoneNumbers : batches) {
            // 发送短信
            String bizNumber = this.getSmsResponse(subPhoneNumbers, templateParams, smsUpExtendCode);
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

    private String getSmsResponse(List<String> phoneNumbers, List<String> templateParams, String smsUpExtendCode)
        throws ClientException {
        CommonRequest commonRequest = new CommonRequest();
        commonRequest.setSysAction("SendSms");
        commonRequest.putQueryParameter("SignName", this.getConfig().getAliyun().getSignName());
        commonRequest.putQueryParameter("TemplateCode", this.getConfig().getAliyun().getTemplateContent());
        String templateParamJson = this.getTemplateParamJson(templateParams);
        if (CharSequenceUtil.isNotBlank(templateParamJson)) {
            commonRequest.putQueryParameter("TemplateParam", templateParamJson);
        }
        if (CharSequenceUtil.isNotBlank(smsUpExtendCode)) {
            commonRequest.putQueryParameter("SmsUpExtendCode", smsUpExtendCode);
        }
        commonRequest.putQueryParameter("PhoneNumbers", phoneNumbers.stream().collect(Collectors.joining(",")));
        CommonResponse response = this.getCommonResponse(commonRequest);
        if (Objects.nonNull(response) && response.getHttpStatus() == CommonConstant.NumberConstant.NUMBER_TWO_HUNDRED) {
            JsonNode data = JacksonUtil.parseTree(response.getData());
            if (data == null) {
                throw new BizException(BizCodeEnum.SEND_ERROR);
            }
            final JsonNode codeNode = data.get(AliyunConstant.RESPONSE_CODE);
            String code = codeNode != null ? codeNode.asText() : null;
            if (CharSequenceUtil.equals(HttpStatus.OK.name(), code)) {
                final JsonNode bizNumberNode = data.get(AliyunConstant.BIZ_NUMBER);
                return bizNumberNode != null ? bizNumberNode.asText() : null;
            } else if (CharSequenceUtil.equals(AliyunConstant.MOBILE_NUMBER_ILLEGAL, code)) {
                throw new BizException(BizCodeEnum.MOBILE_FORMAT_ERROR);
            } else if (CharSequenceUtil.equals(AliyunConstant.BUSINESS_LIMIT_CONTROL, code)) {
                throw new BizException(BizCodeEnum.FREQUENCY_LIMIT);
            } else if (CharSequenceUtil.equals(AliyunConstant.PARAM_NOT_SUPPORT_URL, code)) {
                throw new BizException(BizCodeEnum.TEMPLATE_PARAM_NOT_SUPPORT_URL);
            } else if (CharSequenceUtil.equals(AliyunConstant.AMOUNT_NOT_ENOUGH, code)) {
                throw new BizException(BizCodeEnum.AMOUNT_NOT_ENOUGH);
            } else {
                throw new BizException(BizCodeEnum.SEND_ERROR);
            }
        }
        return null;
    }

    private String getTemplateParamJson(List<String> templateParams) throws ClientException {
        CommonRequest commonRequest = new CommonRequest();
        commonRequest.setSysAction("QuerySmsTemplate");
        commonRequest.putQueryParameter("TemplateCode", this.getConfig().getAliyun().getTemplateContent());
        CommonResponse response = this.getCommonResponse(commonRequest);
        if (Objects.nonNull(response) && response.getHttpStatus() == CommonConstant.NumberConstant.NUMBER_TWO_HUNDRED) {
            JsonNode data = JacksonUtil.parseTree(response.getData());
            String templateContent = data.get("TemplateContent").asText();
            Map<String, String> templateParam = TemplateUtil.getTemplateParam(templateContent, templateParams);
            if (MapUtil.isNotEmpty(templateParam)) {
                return JacksonUtil.toJsonStr(templateParam);
            }
        }
        return null;
    }

    private CommonResponse getCommonResponse(CommonRequest commonRequest) throws ClientException {
        String regionId = "default";
        DefaultProfile profile;
        final AliyunConfig aliyun = super.getConfig().getAliyun();
        final StsConfig sts = aliyun.getSts();
        if (Objects.nonNull(sts)) {
            final LocalDateTime securityTokenExpiration = sts.getExpiration();
            if (Objects.isNull(securityTokenExpiration) || LocalDateTime.now().isAfter(securityTokenExpiration)) {
                AssumeRoleResponse response = this.getAssumeRoleResponse(aliyun, sts);
                final LocalDateTime expiration = DatesUtil.parseUtcDate(response.getCredentials().getExpiration());
                sts.setAccessKey(response.getCredentials().getAccessKeyId());
                sts.setSecretKey(response.getCredentials().getAccessKeySecret());
                sts.setSecurityToken(response.getCredentials().getSecurityToken());
                sts.setExpiration(expiration);
                aliyun.setSts(sts);
                super.getConfig().setAliyun(aliyun);
            }
            profile = DefaultProfile.getProfile(regionId, sts.getAccessKey(), sts.getSecretKey(), sts.getSecurityToken());
        } else {
            profile = DefaultProfile.getProfile(regionId, aliyun.getAccessKey(), aliyun.getSecretKey());
        }
        DefaultAcsClient client = new DefaultAcsClient(profile);
        commonRequest.setSysMethod(MethodType.POST);
        commonRequest.setSysDomain("dysmsapi.aliyuncs.com");
        commonRequest.setSysVersion("2017-05-25");
        try {
            return client.getCommonResponse(commonRequest);
        } finally {
            this.closeAcsClient(client);
        }
    }

    private AssumeRoleResponse getAssumeRoleResponse(AliyunConfig aliyun, StsConfig sts) {
        DefaultProfile.addEndpoint(SymbolConstant.EMPTY_STR, "Sts", sts.getEndpoint());
        IClientProfile clientProfile = DefaultProfile.getProfile(SymbolConstant.EMPTY_STR, aliyun.getAccessKey(), aliyun.getSecretKey());
        DefaultAcsClient client = new DefaultAcsClient(clientProfile);
        final AssumeRoleRequest request = new AssumeRoleRequest();
        request.setSysMethod(MethodType.POST);
        request.setRoleArn(sts.getRoleArn());
        request.setRoleSessionName("aliyun-java-sdk-core-" + System.currentTimeMillis());
        if (CharSequenceUtil.isNotBlank(sts.getPolicy())) {
            request.setPolicy(sts.getPolicy());
        }
        request.setDurationSeconds(AliyunConstant.SECURITY_TOKEN_EXPIRE_TIME);
        AssumeRoleResponse response;
        try {
            response = client.getAcsResponse(request);
        } catch (Exception ex) {
            log.error(ExceptionConstant.EXCEPTION, ex);

            throw new BizException(BizCodeEnum.GET_ACCESS_CREDENTIALS_ERROR);
        } finally {
            this.closeAcsClient(client);
        }
        return response;
    }

    private void closeAcsClient(DefaultAcsClient acsClient) {
        if (Objects.nonNull(acsClient)) {
            acsClient.shutdown();
        }
    }
}

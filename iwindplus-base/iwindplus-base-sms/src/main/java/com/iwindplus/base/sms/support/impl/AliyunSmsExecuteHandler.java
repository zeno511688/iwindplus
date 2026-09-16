/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.sms.support.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
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
import com.iwindplus.base.domain.constant.CommonConstant;
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.domain.constant.CommonConstant.SymbolConstant;
import com.iwindplus.base.domain.dto.StsTokenDTO;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.enums.SmsTypeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.sms.domain.constant.SmsConstant.AliyunConstant;
import com.iwindplus.base.sms.domain.dto.SmsSendRequestDTO;
import com.iwindplus.base.sms.domain.property.SmsProperty.AliyunConfig;
import com.iwindplus.base.sms.service.impl.AbstractBaseServiceImpl;
import com.iwindplus.base.sms.support.SmsExecuteHandler;
import com.iwindplus.base.util.JacksonUtil;
import com.iwindplus.base.util.TemplateUtil;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;

/**
 * 阿里云短信业务服务策略.
 *
 * @author zengdegui
 * @since 2019/8/13
 */
@Slf4j
public class AliyunSmsExecuteHandler extends AbstractBaseServiceImpl<AliyunConfig> implements SmsExecuteHandler {

    /**
     * 构造函数.
     *
     * @param config 阿里云短信配置
     */
    public AliyunSmsExecuteHandler(AliyunConfig config) {
        this.setConfig(config);
    }

    @Override
    public SmsTypeEnum getProvider() {
        return SmsTypeEnum.ALIYUN;
    }

    @Override
    protected String sendSms(SmsSendRequestDTO entity) throws ClientException {
        final List<String> phoneNumbers = entity.getPhoneNumbers();
        Assert.hasText(entity.getSignName(), "signName must not be blank");

        CommonRequest commonRequest = new CommonRequest();
        commonRequest.setSysAction("SendBatchSms");
        // 批量接口：手机号数组、签名数组、模板参数数组（每个手机号对应一份参数）
        commonRequest.putQueryParameter("PhoneNumberJson", JacksonUtil.toJsonStr(phoneNumbers));
        commonRequest.putQueryParameter("SignNameJson", JacksonUtil.toJsonStr(phoneNumbers.stream().map(m -> entity.getSignName()).collect(Collectors.toList())));
        commonRequest.putQueryParameter("TemplateCode", entity.getTemplateContent());
        String templateParamJson = this.getTemplateParamJson(entity.getTemplateContent(), entity.getTemplateParams());
        if (CharSequenceUtil.isNotBlank(templateParamJson)) {
            commonRequest.putQueryParameter("TemplateParamJson", JacksonUtil.toJsonStr(
                phoneNumbers.stream().map(m -> templateParamJson).collect(Collectors.toList())));
        }
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

    private String getTemplateParamJson(String templateContent, List<String> templateParams) throws ClientException {
        CommonRequest commonRequest = new CommonRequest();
        commonRequest.setSysAction("QuerySmsTemplate");
        commonRequest.putQueryParameter("TemplateCode", templateContent);
        CommonResponse response = this.getCommonResponse(commonRequest);
        if (Objects.nonNull(response) && response.getHttpStatus() == CommonConstant.NumberConstant.NUMBER_TWO_HUNDRED) {
            JsonNode data = JacksonUtil.parseTree(response.getData());
            if (data == null) {
                return null;
            }
            final JsonNode templateContentNode = data.get("TemplateContent");
            if (templateContentNode == null) {
                return null;
            }
            String templateContentStr = templateContentNode.asText();
            Map<String, String> templateParam = TemplateUtil.getTemplateParam(templateContentStr, templateParams);
            if (MapUtil.isNotEmpty(templateParam)) {
                return JacksonUtil.toJsonStr(templateParam);
            }
        }
        return null;
    }

    private CommonResponse getCommonResponse(CommonRequest commonRequest) throws ClientException {
        String regionId = "default";
        DefaultProfile profile;
        final AliyunConfig aliyun = this.getConfig();
        final StsTokenDTO sts = aliyun.getSts();
        if (Objects.nonNull(sts) && Boolean.TRUE.equals(sts.getEnabled())) {
            refreshStsTokenIfNeeded(aliyun, sts);
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

    private synchronized void refreshStsTokenIfNeeded(AliyunConfig aliyun, StsTokenDTO sts) {
        final Long securityTokenExpiration = sts.getExpiration();
        if (Objects.isNull(securityTokenExpiration) || System.currentTimeMillis() > securityTokenExpiration) {
            AssumeRoleResponse response = this.getAssumeRoleResponse(aliyun, sts);
            final long expiration = Instant.parse(response.getCredentials().getExpiration()).toEpochMilli();
            sts.setAccessKey(response.getCredentials().getAccessKeyId());
            sts.setSecretKey(response.getCredentials().getAccessKeySecret());
            sts.setSecurityToken(response.getCredentials().getSecurityToken());
            sts.setExpiration(expiration);
            aliyun.setSts(sts);
            this.setConfig(aliyun);
        }
    }

    private AssumeRoleResponse getAssumeRoleResponse(AliyunConfig aliyun, StsTokenDTO sts) {
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

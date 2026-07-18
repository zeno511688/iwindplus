/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.setup.server.service.impl;

import com.iwindplus.base.domain.enums.SmsTypeEnum;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.sms.domain.property.SmsProperty;
import com.iwindplus.base.sms.domain.vo.SmsLogVO;
import com.iwindplus.base.sms.service.SmsAliyunService;
import com.iwindplus.base.sms.service.SmsLingkaiService;
import com.iwindplus.base.sms.service.SmsMxtongService;
import com.iwindplus.base.sms.service.SmsQiniuService;
import com.iwindplus.log.client.SmsCaptchaLogClient;
import com.iwindplus.log.domain.dto.SmsCaptchaLogDTO;
import com.iwindplus.log.domain.dto.SmsSendValidDTO;
import com.iwindplus.mgt.client.power.UserClient;
import com.iwindplus.mgt.domain.dto.power.UserBaseQueryDTO;
import com.iwindplus.mgt.domain.vo.power.UserInfoVO;
import com.iwindplus.mgt.domain.vo.power.UserVO;
import com.iwindplus.setup.domain.dto.SmsSendDTO;
import com.iwindplus.setup.domain.vo.SmsConfigVO;
import com.iwindplus.setup.domain.vo.SmsTplVO;
import com.iwindplus.setup.server.service.SmsConfigService;
import com.iwindplus.setup.server.service.SmsService;
import com.iwindplus.setup.server.service.SmsTplService;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 短信业务层接口实现类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class SmsServiceImpl implements SmsService {

    private final SmsConfigService smsConfigService;
    private final SmsTplService smsTplService;
    private final SmsAliyunService smsAliyunService;
    private final SmsQiniuService smsQiniuService;
    private final SmsLingkaiService smsLingkaiService;
    private final SmsMxtongService smsMxtongService;
    private final SmsCaptchaLogClient smsCaptchaLogClient;
    private final UserClient userClient;
    private final DtpExecutor smsTaskExecutor;

    @Override
    public void sendCaptcha(String requestId, String tplCode, String mobile) {
        final UserBaseQueryDTO entity = UserBaseQueryDTO.builder().mobile(mobile).build();
        final ResultVO<UserInfoVO> userResponse = this.userClient.getLoginInfoByCondition(entity);
        userResponse.errorThrow();
        final UserInfoVO user = userResponse.getBizData();
        final Long userId = user.getUserId();
        final Long orgId = user.getOrgId();

        this.sendSms(requestId, tplCode, mobile, userId, orgId);
    }

    @Override
    public void sendCaptchaByUserId(String requestId, String tplCode, Long userId, Long orgId) {
        final ResultVO<UserVO> userResponse = this.userClient.getDetail(userId);
        userResponse.errorThrow();
        final UserVO user = userResponse.getBizData();
        final String mobile = user.getMobile();

        this.sendSms(requestId, tplCode, mobile, userId, orgId);
    }

    @Override
    public void send(SmsSendDTO entity) {
        final SmsTplVO smsTpl = this.smsTplService.getByCode(entity.getTplCode());
        final SmsConfigVO smsConfig = this.smsConfigService.getDetail(smsTpl.getConfigId());
        if (SmsTypeEnum.ALIYUN.equals(smsConfig.getType())) {
            this.buildSmsConfigAliyun(smsConfig, smsTpl);
            CompletableFuture.runAsync(() ->
                    this.smsAliyunService.smsSend(entity.getPhoneNumbers(), entity.getTemplateParamValue(), null)
                , this.smsTaskExecutor);
        } else if (SmsTypeEnum.QINIU.equals(smsConfig.getType())) {
            this.buildSmsConfigQiniu(smsConfig, smsTpl);
            CompletableFuture.runAsync(() ->
                    this.smsQiniuService.smsSend(entity.getPhoneNumbers(), entity.getTemplateParamValue(), null)
                , this.smsTaskExecutor);
        } else if (SmsTypeEnum.LINGKAI.equals(smsConfig.getType())) {
            this.buildSmsConfigLingkai(smsConfig, smsTpl);
            CompletableFuture.runAsync(() ->
                    this.smsLingkaiService.smsSend(entity.getPhoneNumbers(), entity.getTemplateParamValue(), null)
                , this.smsTaskExecutor);
        } else if (SmsTypeEnum.MXTONG.equals(smsConfig.getType())) {
            this.buildSmsConfigMxtong(smsConfig, smsTpl);
            CompletableFuture.runAsync(() ->
                    this.smsMxtongService.smsSend(entity.getPhoneNumbers(), entity.getTemplateParamValue(), null)
                , this.smsTaskExecutor);
        }
    }

    private void saveSmsLog(SmsLogVO data, SmsTplVO smsTpl, String requestId, Long userId, Long orgId) {
        if (Objects.nonNull(data)) {
            SmsCaptchaLogDTO param = SmsCaptchaLogDTO.builder()
                .requestId(requestId)
                .userId(userId)
                .orgId(orgId)
                .tplCode(smsTpl.getCode())
                .bizNumber(data.getBizNumber())
                .mobile(data.getPhoneNumber())
                .captcha(data.getCaptcha())
                .expireTime(data.getExpireTime())
                .build();
            this.smsCaptchaLogClient.save(param);
        }
    }

    private void buildSmsConfigAliyun(SmsConfigVO smsConfig, SmsTplVO smsTpl) {
        final SmsProperty.AliyunConfig.StsConfig stsConfig = SmsProperty.AliyunConfig.StsConfig.builder()
            .endpoint(smsConfig.getStsEndpoint())
            .roleArn(smsConfig.getRoleArn())
            .policy(smsConfig.getPolicy())
            .build();
        final SmsProperty.AliyunConfig aliyunConfig = SmsProperty.AliyunConfig.builder()
            .accessKey(smsConfig.getAccessKey())
            .secretKey(smsConfig.getSecretKey())
            .signName(smsTpl.getSignName())
            .templateContent(smsTpl.getTemplateContent())
            .sts(stsConfig)
            .build();
        SmsProperty config = SmsProperty.builder()
            .aliyun(aliyunConfig)
            .build();
        this.smsAliyunService.setConfig(config);
    }

    private void buildSmsConfigQiniu(SmsConfigVO smsConfig, SmsTplVO smsTpl) {
        final SmsProperty.QiniuConfig qiniuConfig = SmsProperty.QiniuConfig.builder()
            .accessKey(smsConfig.getAccessKey())
            .secretKey(smsConfig.getSecretKey())
            .templateContent(smsTpl.getTemplateContent())
            .build();
        SmsProperty config = SmsProperty.builder()
            .qiniu(qiniuConfig)
            .build();
        this.smsQiniuService.setConfig(config);
    }

    private void buildSmsConfigLingkai(SmsConfigVO smsConfig, SmsTplVO smsTpl) {
        final SmsProperty.LingkaiConfig lingkaiConfig = SmsProperty.LingkaiConfig.builder()
            .accessKey(smsConfig.getAccessKey())
            .secretKey(smsConfig.getSecretKey())
            .templateContent(smsTpl.getTemplateContent())
            .build();
        SmsProperty config = SmsProperty.builder()
            .lingkai(lingkaiConfig)
            .build();
        this.smsLingkaiService.setConfig(config);
    }

    private void buildSmsConfigMxtong(SmsConfigVO smsConfig, SmsTplVO smsTpl) {
        final SmsProperty.MxtongConfig mxtongConfig = SmsProperty.MxtongConfig.builder()
            .accessKey(smsConfig.getAccessKey())
            .secretKey(smsConfig.getSecretKey())
            .templateContent(smsTpl.getTemplateContent())
            .build();
        SmsProperty config = SmsProperty.builder()
            .mxtong(mxtongConfig)
            .build();
        this.smsMxtongService.setConfig(config);
    }

    private void sendSms(String requestId, String tplCode, String mobile, Long userId, Long orgId) {
        final SmsTplVO smsTpl = this.smsTplService.getByCode(tplCode);
        final SmsConfigVO smsConfig = this.smsConfigService.getDetail(smsTpl.getConfigId());
        final SmsSendValidDTO entity = SmsSendValidDTO.builder()
            .userId(userId)
            .orgId(orgId)
            .tplCode(tplCode)
            .limitCountDay(smsTpl.getLimitCountDay())
            .limitCountHour(smsTpl.getLimitCountHour())
            .limitCountMinute(smsTpl.getLimitCountMinute())
            .build();
        this.smsCaptchaLogClient.checkCanSend(entity);
        if (SmsTypeEnum.ALIYUN.equals(smsConfig.getType())) {
            this.buildSmsConfigAliyun(smsConfig, smsTpl);
            CompletableFuture.runAsync(() -> {
                SmsLogVO data = this.smsAliyunService.smsSendCaptcha(mobile, smsTpl.getCaptchaLength(), smsTpl.getCaptchaTimeout());
                this.saveSmsLog(data, smsTpl, requestId, userId, orgId);
            }, this.smsTaskExecutor);
        } else if (SmsTypeEnum.QINIU.equals(smsConfig.getType())) {
            this.buildSmsConfigQiniu(smsConfig, smsTpl);
            CompletableFuture.runAsync(() -> {
                SmsLogVO data = this.smsQiniuService.smsSendCaptcha(mobile, smsTpl.getCaptchaLength(), smsTpl.getCaptchaTimeout());
                this.saveSmsLog(data, smsTpl, requestId, userId, orgId);
            }, this.smsTaskExecutor);
        } else if (SmsTypeEnum.LINGKAI.equals(smsConfig.getType())) {
            this.buildSmsConfigLingkai(smsConfig, smsTpl);
            CompletableFuture.runAsync(() -> {
                SmsLogVO data = this.smsLingkaiService.smsSendCaptcha(mobile, smsTpl.getCaptchaLength(), smsTpl.getCaptchaTimeout());
                this.saveSmsLog(data, smsTpl, requestId, userId, orgId);
            }, this.smsTaskExecutor);
        } else if (SmsTypeEnum.MXTONG.equals(smsConfig.getType())) {
            this.buildSmsConfigMxtong(smsConfig, smsTpl);
            CompletableFuture.runAsync(() -> {
                SmsLogVO data = this.smsMxtongService.smsSendCaptcha(mobile, smsTpl.getCaptchaLength(), smsTpl.getCaptchaTimeout());
                this.saveSmsLog(data, smsTpl, requestId, userId, orgId);
            }, this.smsTaskExecutor);
        }
    }
}

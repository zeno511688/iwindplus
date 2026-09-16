/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.integr.application.service;

import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.sms.domain.dto.SmsSendCaptchaDTO;
import com.iwindplus.base.sms.domain.dto.SmsSendRequestDTO;
import com.iwindplus.base.sms.domain.vo.SmsSendResultVO;
import com.iwindplus.base.sms.factory.SmsExecuteHandlerFactory;
import com.iwindplus.integr.application.query.SmsTplQueryService;
import com.iwindplus.integr.common.constant.IntegrConstant;
import com.iwindplus.integr.application.service.dto.SmsSendDTO;
import com.iwindplus.integr.application.query.vo.SmsTplVO;
import com.iwindplus.log.api.dto.SmsCaptchaLogDTO;
import com.iwindplus.log.api.dto.SmsSendValidDTO;
import com.iwindplus.log.client.SmsCaptchaLogClient;
import com.iwindplus.mgt.api.upms.dto.UserBaseQueryDTO;
import com.iwindplus.mgt.api.upms.vo.UserInfoVO;
import com.iwindplus.mgt.api.upms.vo.UserVO;
import com.iwindplus.mgt.client.upms.UserClient;
import jakarta.annotation.Resource;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 短信业务层接口类.
 *
 * @author zengdegui
 * @since 2021/7/20
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class SmsApplicationService {

    @Resource
    private SmsExecuteHandlerFactory smsExecuteHandlerFactory;

    @Resource
    private SmsTplQueryService smsTplQueryService;

    @Resource
    private SmsCaptchaLogClient smsCaptchaLogClient;

    @Resource
    private UserClient userClient;

    @Resource(name = IntegrConstant.THREAD_POOL_BEAN_NAME_SMS)
    private DtpExecutor threadPoolExecutor;

    /**
     * 发送手机验证码（手机）.
     *
     * @param tplCode 模板编码（必填）
     * @param mobile  手机（必填）
     */
    public void sendCaptcha(String tplCode, String mobile) {
        final UserBaseQueryDTO entity = UserBaseQueryDTO.builder().mobile(mobile).build();
        final ResultVO<UserInfoVO> userResponse = this.userClient.getLoginInfoByCondition(entity);
        userResponse.errorThrow();
        final UserInfoVO user = userResponse.getBizData();
        final Long userId = user.getUserId();
        final Long orgId = user.getOrgId();

        this.sendSms(tplCode, mobile, userId, orgId);
    }

    /**
     * 发送手机验证码（用户主键）.
     *
     * @param tplCode 模板编码（必填）
     * @param userId  用户主键（必填）
     * @param orgId   组织主键（必填）
     */
    public void sendCaptchaByUserId(String tplCode, Long userId, Long orgId) {
        final ResultVO<UserVO> userResponse = this.userClient.getDetail(userId);
        userResponse.errorThrow();
        final UserVO user = userResponse.getBizData();
        final String mobile = user.getMobile();

        this.sendSms(tplCode, mobile, userId, orgId);
    }

    /**
     * 发送短信.
     *
     * @param entity 对象
     * @return
     */
    public void send(SmsSendDTO entity) {
        final SmsTplVO smsTpl = this.smsTplQueryService.getByCode(entity.getTplCode());
        final SmsSendRequestDTO request = SmsSendRequestDTO.builder()
            .phoneNumbers(entity.getPhoneNumbers())
            .templateContent(smsTpl.getTemplateContent())
            .signName(smsTpl.getSignName())
            .templateParams(entity.getTemplateParams())
            .build();
        CompletableFuture.runAsync(() -> this.smsExecuteHandlerFactory.smsSend(request), this.threadPoolExecutor);
    }

    private void saveSmsLog(SmsSendResultVO data, SmsTplVO smsTpl, Long userId, Long orgId) {
        if (Objects.nonNull(data)) {
            SmsCaptchaLogDTO param = SmsCaptchaLogDTO.builder()
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

    private void sendSms(String tplCode,
        String mobile, Long userId, Long orgId) {
        final SmsTplVO smsTpl = this.smsTplQueryService.getByCode(tplCode);
        final SmsSendValidDTO entity = SmsSendValidDTO.builder()
            .userId(userId)
            .orgId(orgId)
            .tplCode(tplCode)
            .limitCountDay(smsTpl.getLimitCountDay())
            .limitCountHour(smsTpl.getLimitCountHour())
            .limitCountMinute(smsTpl.getLimitCountMinute())
            .build();
        this.smsCaptchaLogClient.checkCanSend(entity);
        final SmsSendCaptchaDTO request = SmsSendCaptchaDTO.builder()
            .phoneNumber(mobile)
            .templateContent(smsTpl.getTemplateContent())
            .signName(smsTpl.getSignName())
            .captchaLength(smsTpl.getCaptchaLength())
            .captchaTimeout(smsTpl.getCaptchaTimeout())
            .build();
        CompletableFuture.runAsync(() -> {
            Optional<SmsSendResultVO> result = this.smsExecuteHandlerFactory.smsSendCaptcha(request);
            result.ifPresent(data -> this.saveSmsLog(data, smsTpl, userId, orgId));
        }, this.threadPoolExecutor);
    }

}

/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.sms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.google.common.collect.Lists;
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.sms.domain.constant.SmsConstant;
import com.iwindplus.base.sms.domain.dto.SmsSendCaptchaDTO;
import com.iwindplus.base.sms.domain.dto.SmsSendRequestDTO;
import com.iwindplus.base.sms.domain.property.SmsProperty;
import com.iwindplus.base.sms.domain.vo.SmsSendBatchResultVO;
import com.iwindplus.base.sms.domain.vo.SmsSendResultVO;
import com.iwindplus.base.sms.service.BaseService;
import com.iwindplus.base.util.SecureRandomUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

/**
 * 短信业务基础抽象类.
 *
 * @param <T> 配置类型
 * @author zengdegui
 * @since 2020/3/13
 */
@Slf4j
public abstract class AbstractBaseServiceImpl<T extends SmsProperty.BaseConfig>
    extends AbstractBaseConfigServiceImpl<T> implements BaseService {

    /**
     * 健康检查.
     *
     * @return 是否健康
     */
    @Override
    public boolean isHealthy() {
        return this.config != null && Boolean.TRUE.equals(this.config.getEnabled());
    }

    /**
     * 获取优先级.
     *
     * @return 优先级（数字越小优先级越高）
     */
    @Override
    public int getPriority() {
        return Optional.ofNullable(this.getConfig().getPriority()).orElse(Integer.MAX_VALUE);
    }

    /**
     * 获取配置编码.
     *
     * @return 配置编码
     */
    @Override
    public String getCode() {
        return this.getConfig().getCode();
    }

    /**
     * 发送短信验证码.
     *
     * @param entity 短信验证码发送参数
     * @return 短信日志，发送失败时返回 {@link Optional#empty()}
     */
    public Optional<SmsSendResultVO> smsSendCaptcha(SmsSendCaptchaDTO entity) {
        String captcha = SecureRandomUtil.randomNumbers(Optional.ofNullable(entity.getCaptchaLength()).orElse(SmsConstant.CAPTCHA_LENGTH));
        final Integer timeout = Optional.ofNullable(entity.getCaptchaTimeout()).orElse(SmsConstant.CAPTCHA_TIMEOUT);

        SmsSendRequestDTO sendDTO = SmsSendRequestDTO.builder()
            .phoneNumbers(List.of(entity.getPhoneNumber()))
            .templateContent(entity.getTemplateContent())
            .signName(entity.getSignName())
            .templateParams(List.of(captcha, String.valueOf(timeout)))
            .build();
        Optional<List<SmsSendBatchResultVO>> result = this.smsSend(sendDTO);
        return this.getSmsCaptchaResultVO(timeout, result);
    }

    /**
     * 发送短信，成功返回流水号.
     *
     * @param entity 短信发送参数
     * @return 批量发送结果，发送失败时返回 {@link Optional#empty()}
     */
    public Optional<List<SmsSendBatchResultVO>> smsSend(SmsSendRequestDTO entity) {
        List<SmsSendBatchResultVO> list = new ArrayList<>(10);
        int batchSize = Optional.ofNullable(entity.getPhoneNumberGroupSize()).orElse(SmsConstant.PHONE_NUMBER_GROUP_SIZE);
        List<List<String>> batches = Lists.partition(entity.getPhoneNumbers(), batchSize);
        for (List<String> subPhoneNumbers : batches) {
            try {
                entity.setPhoneNumbers(subPhoneNumbers);
                String bizNumber = this.sendSms(entity);
                if (CharSequenceUtil.isNotBlank(bizNumber)) {
                    SmsSendBatchResultVO build = SmsSendBatchResultVO.builder()
                        .bizNumber(bizNumber)
                        .phoneNumbers(entity.getPhoneNumbers())
                        .templateParams(entity.getTemplateParams())
                        .build();
                    list.add(build);
                }
            } catch (Exception ex) {
                log.error(ExceptionConstant.EXCEPTION, ex);
                return Optional.empty();
            }
        }
        return list.isEmpty() ? Optional.empty() : Optional.of(list);
    }

    /**
     * 发送短信（由各提供商实现，调用各自的SDK/HTTP接口）.
     *
     * @param entity 短信发送参数
     * @return 短信流水号，发送失败返回 {@code null}
     * @throws Exception 发送异常
     */
    protected abstract String sendSms(SmsSendRequestDTO entity) throws Exception;

    /**
     * 获取短信日志.
     *
     * @param timeout 验证码有效期
     * @param result  批量发送结果
     * @return 短信日志，发送失败时返回 {@link Optional#empty()}
     */
    protected Optional<SmsSendResultVO> getSmsCaptchaResultVO(Integer timeout, Optional<List<SmsSendBatchResultVO>> result) {
        if (result.isEmpty()) {
            return Optional.empty();
        }

        final List<SmsSendBatchResultVO> batchResultList = result.get();
        if (CollUtil.isEmpty(batchResultList)) {
            return Optional.empty();
        }

        final SmsSendBatchResultVO batchVO = batchResultList.get(0);
        if (batchVO == null) {
            return Optional.empty();
        }

        final String phoneNumber = batchVO.getPhoneNumbers().get(0);
        final String captcha = batchVO.getTemplateParams().get(0);
        long expireTime = System.currentTimeMillis() + timeout * 60 * 1000;

        final SmsSendResultVO data = SmsSendResultVO.builder()
            .bizNumber(batchVO.getBizNumber())
            .phoneNumber(phoneNumber)
            .captcha(captcha)
            .expireTime(expireTime)
            .build();
        return Optional.of(data);
    }
}

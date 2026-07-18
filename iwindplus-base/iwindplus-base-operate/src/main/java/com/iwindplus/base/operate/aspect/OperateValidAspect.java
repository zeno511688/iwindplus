/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.operate.aspect;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.iwindplus.base.domain.constant.CommonConstant.HeaderConstant;
import com.iwindplus.base.domain.context.UserContextHolder;
import com.iwindplus.base.domain.dto.UserExtendFunctionValidDTO;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.domain.vo.UserBaseVO;
import com.iwindplus.base.domain.vo.UserExtendFunctionValidVO;
import com.iwindplus.base.http.client.domain.enums.HttpClientTypeEnum;
import com.iwindplus.base.http.client.factory.HttpClientExecutorStrategyFactory;
import com.iwindplus.base.operate.domain.annotation.OperateValid;
import com.iwindplus.base.operate.domain.property.OperateProperty;
import com.iwindplus.base.operate.domain.property.OperateProperty.OperateValidConfig;
import com.iwindplus.base.util.DatesUtil;
import com.iwindplus.base.util.HttpsUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;

/**
 * 操作校验切面.
 *
 * @author zengdegui
 * @since 2024/07/06 12:18
 */
@Slf4j
@Aspect
@Order(20)
public class OperateValidAspect {

    @Resource
    private OperateProperty property;

    @Resource
    private HttpClientExecutorStrategyFactory httpClientExecutorStrategyFactory;

    /**
     * 切点.
     */
    @Pointcut("@annotation(com.iwindplus.base.operate.domain.annotation.OperateValid)")
    public void pointCutMethod() {
    }

    /**
     * Before 切面
     *
     * @param joinPoint joinPoint
     */
    @Before("pointCutMethod()")
    public void beforePointCut(JoinPoint joinPoint) {
        if (!isEnabled()) {
            return;
        }

        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        OperateValid annotation = method.getAnnotation(OperateValid.class);
        if (annotation == null || allValidationDisabled(annotation)) {
            return;
        }

        UserBaseVO context = UserContextHolder.getContext();
        HttpServletRequest request = HttpsUtil.getHttpServletRequest();
        if (ObjectUtil.isEmpty(context) || ObjectUtil.isEmpty(request)) {
            return;
        }

        final long beginMillis = System.currentTimeMillis();
        log.info("操作校验开始, time={}", DatesUtil.parseDate(beginMillis, DatePattern.NORM_DATETIME_MS_PATTERN));

        try {
            UserExtendFunctionValidVO data = getUserExtendFunctionValid(annotation, context, request);
            log.info("操作校验结果={}", data);
            validateResult(annotation, data);
        } finally {
            final long endMillis = System.currentTimeMillis();
            log.info("操作校验结束, time={}, duration={}ms",
                DatesUtil.parseDate(endMillis, DatePattern.NORM_DATETIME_MS_PATTERN),
                endMillis - beginMillis);
        }
    }

    private boolean isEnabled() {
        return Boolean.TRUE.equals(property.getEnabled())
            && Boolean.TRUE.equals(property.getValid().getEnabled());
    }

    private boolean allValidationDisabled(OperateValid annotation) {
        return Boolean.FALSE.equals(annotation.enabledGa())
            && Boolean.FALSE.equals(annotation.enabledMail())
            && Boolean.FALSE.equals(annotation.enabledSms())
            && Boolean.FALSE.equals(annotation.enabledYubikey());
    }

    private UserExtendFunctionValidDTO collectCaptchaData(OperateValid annotation, UserBaseVO context, HttpServletRequest request) {
        String gaCaptcha = annotation.enabledGa() ? request.getHeader(HeaderConstant.X_GA_CAPTCHA) : null;
        String mailCaptcha = annotation.enabledMail() ? request.getHeader(HeaderConstant.X_MAIL_CAPTCHA) : null;
        String smsCaptcha = annotation.enabledSms() ? request.getHeader(HeaderConstant.X_SMS_CAPTCHA) : null;
        String yubikeySource = annotation.enabledYubikey() ? request.getHeader(HeaderConstant.X_YUBIKEY_SOURCE) : null;
        String yubikeySign = annotation.enabledYubikey() ? request.getHeader(HeaderConstant.X_YUBIKEY_SIGN) : null;

        if (annotation.enabledGa()) {
            if (ObjectUtil.isEmpty(gaCaptcha)) {
                throw new BizException(BizCodeEnum.GA_CAPTCHA_NOT_EMPTY);
            }
            if (!CharSequenceUtil.isNumeric(gaCaptcha)) {
                throw new BizException(BizCodeEnum.ONLY_SUPPORT_NUMBER);
            }
        }
        if (annotation.enabledMail() && ObjectUtil.isEmpty(mailCaptcha)) {
            throw new BizException(BizCodeEnum.MAIL_CAPTCHA_NOT_EMPTY);
        }
        if (annotation.enabledSms() && ObjectUtil.isEmpty(smsCaptcha)) {
            throw new BizException(BizCodeEnum.SMS_CAPTCHA_NOT_EMPTY);
        }
        if (annotation.enabledYubikey()) {
            if (ObjectUtil.isEmpty(yubikeySource)) {
                throw new BizException(BizCodeEnum.YUBIKEY_SOURCE_NOT_EMPTY);
            }
            if (ObjectUtil.isEmpty(yubikeySign)) {
                throw new BizException(BizCodeEnum.YUBIKEY_SIGN_NOT_EMPTY);
            }
        }

        return UserExtendFunctionValidDTO.builder()
            .userId(context.getUserId())
            .orgId(context.getOrgId())
            .gaCaptcha(gaCaptcha)
            .mailCaptcha(mailCaptcha)
            .smsCaptcha(smsCaptcha)
            .yubikeySource(yubikeySource)
            .yubikeySign(yubikeySign)
            .build();
    }

    private UserExtendFunctionValidVO getUserExtendFunctionValid(OperateValid annotation, UserBaseVO context, HttpServletRequest request) {
        final OperateValidConfig cfg = property.getValid();
        UserExtendFunctionValidDTO dto = collectCaptchaData(annotation, context, request);
        final Map<String, Object> query = BeanUtil.beanToMap(dto);
        final ResultVO<UserExtendFunctionValidVO> result = httpClientExecutorStrategyFactory
            .getHttpClientExecutor(HttpClientTypeEnum.REST_CLIENT)
            .post(
                cfg.getUrl(),
                query,
                null,
                new TypeReference<>() {
                }
            );
        result.errorThrow();
        UserExtendFunctionValidVO data = result.getBizData();
        return data;
    }

    private void validateResult(OperateValid annotation, UserExtendFunctionValidVO result) {
        if (result == null) {
            return;
        }

        if (annotation.enabledGa()) {
            if (Boolean.FALSE.equals(result.getGaBindFlag())) {
                throw new BizException(BizCodeEnum.GA_UNBOUND);
            }
            if (Boolean.FALSE.equals(result.getGaCheckFlag())) {
                throw new BizException(BizCodeEnum.GA_CAPTCHA_ERROR);
            }
        }
        if (annotation.enabledMail()) {
            if (Boolean.FALSE.equals(result.getMailBindFlag())) {
                throw new BizException(BizCodeEnum.MAIL_UNBOUND);
            }
            if (Boolean.FALSE.equals(result.getMailCheckFlag())) {
                throw new BizException(BizCodeEnum.MAIL_CAPTCHA_ERROR);
            }
        }
        if (annotation.enabledSms()) {
            if (Boolean.FALSE.equals(result.getMobileBindFlag())) {
                throw new BizException(BizCodeEnum.MOBILE_UNBOUND);
            }
            if (Boolean.FALSE.equals(result.getSmsCheckFlag())) {
                throw new BizException(BizCodeEnum.SMS_CAPTCHA_ERROR);
            }
        }
        if (annotation.enabledYubikey()) {
            if (Boolean.FALSE.equals(result.getYubikeyBindFlag())) {
                throw new BizException(BizCodeEnum.YUBIKEY_UNBOUND);
            }
            if (Boolean.FALSE.equals(result.getYubikeyCheckFlag())) {
                throw new BizException(BizCodeEnum.YUBIKEY_VERIFY_ERROR);
            }
        }
    }
}

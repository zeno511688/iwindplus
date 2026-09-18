/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.auth.infrastructure.client;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.iwindplus.auth.infrastructure.model.dto.OauthUserDTO;
import com.iwindplus.auth.infrastructure.model.vo.UserDetailVO;
import com.iwindplus.auth.infrastructure.configuration.AuthProperty;
import com.iwindplus.auth.infrastructure.configuration.AuthProperty.MailConfig;
import com.iwindplus.auth.infrastructure.configuration.AuthProperty.SmsConfig;
import com.iwindplus.auth.infrastructure.configuration.ServerApiProperty;
import com.iwindplus.auth.infrastructure.configuration.ServerApiProperty.LogApiConfig;
import com.iwindplus.auth.infrastructure.configuration.ServerApiProperty.MgtApiConfig;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.http.client.domain.enums.HttpClientTypeEnum;
import com.iwindplus.base.http.client.factory.HttpClientExecuteHandlerFactory;
import com.iwindplus.base.http.client.support.HttpClientExecuteHandler;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * 登陆认证业务层客户端.
 *
 * @author zengdegui
 * @since 2024/06/11 20:38
 */
@Slf4j
@Component
public class LoginAuthClient {

    private final AuthProperty property;
    private final ServerApiProperty serverApiProperty;
    private final HttpClientExecuteHandler httpClientExecuteHandler;

    /**
     * 构造函数.
     *
     * @param property          配置
     * @param serverApiProperty 服务配置
     * @param factory           factory
     */
    public LoginAuthClient(
        AuthProperty property,
        ServerApiProperty serverApiProperty,
        HttpClientExecuteHandlerFactory factory) {
        this.property = property;
        this.serverApiProperty = serverApiProperty;
        this.httpClientExecuteHandler = factory.getHandler(HttpClientTypeEnum.REST_CLIENT);
    }

    /**
     * 用户名认证方式.
     *
     * @param username 用户名
     * @return UserDetails
     */
    public UserDetails loadUserByUsername(String username) {
        return this.getUserDetailsByParam(username);
    }

    /**
     * 绑定授权认证方式.
     *
     * @param code 编码
     * @return UserDetails
     */
    public UserDetails loadUserByCode(String code) {
        final MgtApiConfig mgtApiConfig = this.serverApiProperty.getMgt();
        final String url = serverApiProperty.resolveUrl(mgtApiConfig.getUserLoginByCodeUrl());
        final Map<String, String> query = Map.of(
            "code", code
        );
        final ResultVO<UserDetailVO> response = httpClientExecuteHandler
            .get(
                url,
                query,
                null,
                new TypeReference<>() {
                }
            );
        response.errorThrow();
        final UserDetailVO data = response.getBizData();
        return this.getUserDetails(data);
    }

    /**
     * 邮箱认证方式.
     *
     * @param mail 邮箱
     * @return UserDetails
     */
    public UserDetails loadUserByMail(String mail) {
        return this.getUserDetailsByParam(mail);
    }

    /**
     * 手机号码认证方式.
     *
     * @param mobile 手机号
     * @return UserDetails
     */
    public UserDetails loadUserByMobile(String mobile) {
        return this.getUserDetailsByParam(mobile);
    }

    /**
     * 验证邮箱验证码是否正确.
     *
     * @param mail    邮箱
     * @param captcha 验证码
     * @return boolean
     */
    public boolean validateCaptchaByMail(String mail, String captcha) {
        final LogApiConfig logApiConfig = this.serverApiProperty.getLog();
        final MailConfig mailConfig = this.property.getMail();
        final String tplCode = mailConfig.getTplCode();
        final String url = serverApiProperty.resolveUrl(logApiConfig.getMailCaptchaLogValidateUrl());
        final Map<String, String> query = Map.of(
            "tplCode", tplCode,
            "mail", mail,
            "captcha", captcha
        );
        final ResultVO<Boolean> response = httpClientExecuteHandler
            .get(
                url,
                query,
                null,
                new TypeReference<>() {
                }
            );
        response.errorThrow();
        return response.getBizData();
    }

    /**
     * 验证手机验证码是否正确.
     *
     * @param mobile  手机
     * @param captcha 验证码
     * @return boolean
     */
    public boolean validateCaptchaByMobile(String mobile, String captcha) {
        final LogApiConfig logApiConfig = this.serverApiProperty.getLog();
        final SmsConfig smsConfig = this.property.getSms();
        final String tplCode = smsConfig.getTplCode();
        final String url = serverApiProperty.resolveUrl(logApiConfig.getSmsCaptchaLogValidateUrl());
        final Map<String, String> query = Map.of(
            "tplCode", tplCode,
            "mobile", mobile,
            "captcha", captcha
        );
        final ResultVO<Boolean> response = httpClientExecuteHandler
            .get(
                url,
                query,
                null,
                new TypeReference<>() {
                }
            );
        response.errorThrow();
        return response.getBizData();
    }

    /**
     * UserDetails.
     *
     * @param param param
     * @return UserDetails
     */
    private UserDetails getUserDetailsByParam(String param) {
        final MgtApiConfig mgtApiConfig = this.serverApiProperty.getMgt();
        final String url = serverApiProperty.resolveUrl(mgtApiConfig.getUserLoginByParamUrl());
        final Map<String, String> query = Map.of(
            "param", param
        );
        final ResultVO<UserDetailVO> response = httpClientExecuteHandler
            .get(
                url,
                query,
                null,
                new TypeReference<>() {
                }
            );
        response.errorThrow();
        final UserDetailVO data = response.getBizData();
        return this.getUserDetails(data);
    }


    /**
     * UserDetails.
     *
     * @param data data
     * @return UserDetails
     */
    private UserDetails getUserDetails(UserDetailVO data) {
        final OauthUserDTO result = BeanUtil.copyProperties(data, OauthUserDTO.class);
        result.setEnabled(data.getEnabled());
        final Set<String> permissions = data.getPermissions();
        if (CollUtil.isNotEmpty(permissions)) {
            result.addGrantedAuthority(permissions);
        }
        return result;
    }
}

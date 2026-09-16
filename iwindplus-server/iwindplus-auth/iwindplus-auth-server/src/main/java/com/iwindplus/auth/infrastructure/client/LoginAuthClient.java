/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.auth.infrastructure.client;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.iwindplus.auth.infrastructure.client.dto.OauthUserDTO;
import com.iwindplus.auth.infrastructure.configuration.AuthProperty;
import com.iwindplus.auth.infrastructure.configuration.AuthProperty.MailConfig;
import com.iwindplus.auth.infrastructure.configuration.AuthProperty.SmsConfig;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.log.client.MailCaptchaLogClient;
import com.iwindplus.mgt.api.upms.vo.UserDetailVO;
import com.iwindplus.mgt.client.upms.UserClient;
import java.util.Set;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class LoginAuthClient {

    private final UserClient userClient;
    private final MailCaptchaLogClient mailCaptchaLogClient;
    private final AuthProperty property;

    /**
     * 用户名认证方式.
     *
     * @param username 用户名
     * @return UserDetails
     */
    public UserDetails loadUserByUsername(String username) {
        return this.getUserDetails(userClient, username);
    }

    /**
     * 绑定授权认证方式.
     *
     * @param code 编码
     * @return UserDetails
     */
    public UserDetails loadUserByCode(String code) {
        ResultVO<UserDetailVO> result = this.userClient.getLoginByCode(code);
        result.errorThrow();
        final UserDetailVO data = result.getBizData();
        return this.getUserDetails(data);
    }

    /**
     * 邮箱认证方式.
     *
     * @param mail 邮箱
     * @return UserDetails
     */
    public UserDetails loadUserByMail(String mail) {
        return this.getUserDetails(userClient, mail);
    }

    /**
     * 验证邮箱验证码是否正确.
     *
     * @param mail    邮箱
     * @param captcha 验证码
     * @return boolean
     */
    public boolean validateCaptchaByMail(String mail, String captcha) {
        final MailConfig mailConfig = this.property.getMail();
        final String tplCode = mailConfig.getTplCode();
        ResultVO<Boolean> result = this.mailCaptchaLogClient.validate(tplCode, mail, captcha);
        result.errorThrow();
        return result.getBizData();
    }

    /**
     * 手机号码认证方式.
     *
     * @param mobile 手机号
     * @return UserDetails
     */
    public UserDetails loadUserByMobile(String mobile) {
        return this.getUserDetails(userClient, mobile);
    }

    /**
     * 验证手机验证码是否正确.
     *
     * @param mobile  手机
     * @param captcha 验证码
     * @return boolean
     */
    public boolean validateCaptchaByMobile(String mobile, String captcha) {
        final SmsConfig smsConfig = this.property.getSms();
        final String tplCode = smsConfig.getTplCode();
        ResultVO<Boolean> result = this.mailCaptchaLogClient.validate(tplCode, mobile, captcha);
        result.errorThrow();
        return result.getBizData();
    }

    /**
     * UserDetails.
     *
     * @param userClient userClient
     * @param param      param
     * @return UserDetails
     */
    private UserDetails getUserDetails(UserClient userClient, String param) {
        ResultVO<UserDetailVO> result = userClient.getLoginByParam(param);
        result.errorThrow();
        final UserDetailVO data = result.getBizData();
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

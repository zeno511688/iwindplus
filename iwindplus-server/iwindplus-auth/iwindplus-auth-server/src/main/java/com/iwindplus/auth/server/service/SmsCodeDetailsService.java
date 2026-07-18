/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.auth.server.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * 短信认证业务层接口.
 *
 * @author zengdegui
 * @since 2024/06/11 20:38
 */
@Service
public interface SmsCodeDetailsService {
    /**
     * 手机号码认证方式.
     *
     * @param mobile 手机号
     * @return UserDetails
     */
    UserDetails loadUserByMobile(String mobile);

    /**
     * 验证手机验证码是否正确.
     *
     * @param code    配置编码
     * @param mobile  手机
     * @param captcha 验证码
     * @return boolean
     */
    boolean validate(String code, String mobile, String captcha);
}

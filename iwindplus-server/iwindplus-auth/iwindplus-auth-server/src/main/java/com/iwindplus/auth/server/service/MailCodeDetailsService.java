/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.auth.server.service;

import org.springframework.security.core.userdetails.UserDetails;

/**
 * 邮箱认证业务层接口.
 *
 * @author zengdegui
 * @since 2024/06/11 20:38
 */
public interface MailCodeDetailsService {

    /**
     * 邮箱认证方式.
     *
     * @param mail 邮箱
     * @return UserDetails
     */
    UserDetails loadUserByMail(String mail);

    /**
     * 验证邮箱验证码是否正确.
     *
     * @param code    配置编码
     * @param mail    邮箱
     * @param captcha 验证码
     * @return boolean
     */
    boolean validate(String code, String mail, String captcha);
}

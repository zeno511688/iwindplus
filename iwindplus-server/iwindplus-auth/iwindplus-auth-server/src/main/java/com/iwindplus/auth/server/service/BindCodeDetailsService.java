/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.auth.server.service;

import org.springframework.security.core.userdetails.UserDetails;

/**
 * 第三方绑定授权认证业务层接口.
 *
 * @author zengdegui
 * @since 2024/06/11 20:38
 */
public interface BindCodeDetailsService {

    /**
     * 第三方绑定授权认证方式.
     *
     * @param code 编码
     * @return UserDetails
     */
    UserDetails loadUserByCode(String code);
}

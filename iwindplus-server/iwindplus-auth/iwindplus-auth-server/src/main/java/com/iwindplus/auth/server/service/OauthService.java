/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.auth.server.service;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 认证业务层接口.
 *
 * @author zengdegui 2024/12/4 14:18
 */
public interface OauthService {

    /**
     * 退出.
     *
     * @param request 请求
     */
    void logout(HttpServletRequest request);
}

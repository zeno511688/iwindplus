/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.auth.client;

import com.iwindplus.auth.api.AuthorizationApi;
import com.iwindplus.auth.domain.constant.AuthConstant;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * 认证客户端.
 *
 * @author zengdegui
 * @since 2025/04/04 01:16
 */
@FeignClient(
    value = AuthConstant.AUTH_SERVER_NAME,
    contextId = "authorizationClient"
)
public interface AuthorizationClient extends AuthorizationApi {

}

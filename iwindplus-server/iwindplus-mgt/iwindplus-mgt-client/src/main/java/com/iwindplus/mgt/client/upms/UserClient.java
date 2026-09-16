/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.client.upms;

import com.iwindplus.mgt.api.upms.UserApi;
import com.iwindplus.mgt.common.constant.MgtConstant;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * 用户客户端.
 *
 * @author zengdegui
 * @since 2020年4月1日
 */
@FeignClient(
    value = MgtConstant.MGT_SERVER_NAME,
    contextId = "userClient"
)
public interface UserClient extends UserApi {
}

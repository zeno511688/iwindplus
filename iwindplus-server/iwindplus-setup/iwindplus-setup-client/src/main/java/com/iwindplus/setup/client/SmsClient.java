/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.setup.client;

import com.iwindplus.setup.api.SmsApi;
import com.iwindplus.setup.domain.constant.SetupConstant;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * 短信客户端.
 *
 * @author zengdegui
 * @since 2020年4月1日
 */
@FeignClient(
    value = SetupConstant.SETUP_SERVER_NAME,
    contextId = "smsClient"
)
public interface SmsClient extends SmsApi {
}

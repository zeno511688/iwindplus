/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.integr.client;

import com.iwindplus.integr.api.SmsApi;
import com.iwindplus.integr.common.constant.IntegrConstant;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * 短信客户端.
 *
 * @author zengdegui
 * @since 2020年4月1日
 */
@FeignClient(
    value = IntegrConstant.INTEGR_SERVER_NAME,
    contextId = "smsClient"
)
public interface SmsClient extends SmsApi {
}

/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.log.client;

import com.iwindplus.log.api.SmsCaptchaLogApi;
import com.iwindplus.log.domain.constant.LogConstant;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * 短信验证码日志客户端.
 *
 * @author zengdegui
 * @since 2020年4月1日
 */
@FeignClient(
    value = LogConstant.LOG_SERVER_NAME,
    contextId = "smsCaptchaLogClient"
)
public interface SmsCaptchaLogClient extends SmsCaptchaLogApi {

}

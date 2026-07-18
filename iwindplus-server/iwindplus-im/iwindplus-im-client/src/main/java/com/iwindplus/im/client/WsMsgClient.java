/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.im.client;

import com.iwindplus.im.api.WsMsgApi;
import com.iwindplus.im.domain.constant.ImConstant;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * 消息推送客户端.
 *
 * @author zengdegui
 * @since 2020年4月1日
 */
@FeignClient(
    value = ImConstant.IM_SERVER_NAME,
    contextId = "wsMsgClient"
)
public interface WsMsgClient extends WsMsgApi {
}

/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.client;

import com.iwindplus.flow.api.FlowInstanceApi;
import com.iwindplus.flow.domain.constant.FlowConstant;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * 流程实例客户端.
 *
 * @author zengdegui
 * @since 2026/05/21 23:23
 */
@FeignClient(
    value = FlowConstant.FLOW_SERVER_NAME,
    contextId = "flowInstanceClient"
)
public interface FlowInstanceClient extends FlowInstanceApi {

}
